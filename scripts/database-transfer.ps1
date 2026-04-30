Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$DefaultDatabase = 'shipping_management'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$DefaultSqlFile = Join-Path $ProjectRoot 'sql\shipping_management_full.sql'

function Pause-And-Exit {
  param([int] $Code = 0)
  Write-Host ''
  Read-Host 'Press Enter to exit'
  exit $Code
}

function Find-Tool {
  param([string] $Name)

  $command = Get-Command $Name -ErrorAction SilentlyContinue
  if ($command) {
    return $command.Source
  }

  $candidates = @(
    (Join-Path $env:USERPROFILE "scoop\apps\mysql\current\bin\$Name.exe"),
    (Join-Path $env:ProgramFiles "MySQL\MySQL Server 8.0\bin\$Name.exe"),
    (Join-Path ${env:ProgramFiles(x86)} "MySQL\MySQL Server 8.0\bin\$Name.exe")
  )

  foreach ($candidate in $candidates) {
    if ($candidate -and (Test-Path -LiteralPath $candidate)) {
      return $candidate
    }
  }

  throw "Cannot find $Name. Add MySQL bin directory to PATH or install MySQL with Scoop."
}

function Read-WithDefault {
  param(
    [string] $Prompt,
    [string] $DefaultValue
  )

  $value = Read-Host "$Prompt [$DefaultValue]"
  if ([string]::IsNullOrWhiteSpace($value)) {
    return $DefaultValue
  }
  return $value.Trim().Trim('"')
}

function Read-Required {
  param([string] $Prompt)

  while ($true) {
    $value = Read-Host $Prompt
    if (-not [string]::IsNullOrWhiteSpace($value)) {
      return $value.Trim()
    }
    Write-Host 'Value cannot be empty.' -ForegroundColor Yellow
  }
}

function ConvertTo-PlainText {
  param([securestring] $SecureValue)

  $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecureValue)
  try {
    return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
  } finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
  }
}

function Assert-DatabaseName {
  param([string] $DatabaseName)

  if ($DatabaseName -notmatch '^[A-Za-z0-9_]+$') {
    throw 'Database name can only contain letters, numbers, and underscore.'
  }
}

function Escape-SqlString {
  param([string] $Value)
  return $Value.Replace("'", "''")
}

function Quote-DatabaseName {
  param([string] $DatabaseName)
  Assert-DatabaseName $DatabaseName
  return "``$DatabaseName``"
}

function Get-MysqlArgs {
  param(
    [string] $HostName,
    [string] $Port,
    [string] $User
  )

  return @("-h$HostName", "-P$Port", "-u$User", '--default-character-set=utf8mb4')
}

function Invoke-MysqlQuery {
  param(
    [string] $MysqlPath,
    [string[]] $MysqlArgs,
    [string] $Sql
  )

  $output = & $MysqlPath @MysqlArgs '-N' '-B' '-e' $Sql 2>&1
  if ($LASTEXITCODE -ne 0) {
    throw ($output | Out-String)
  }
  return ($output | Out-String).Trim()
}

function Test-DatabaseExists {
  param(
    [string] $MysqlPath,
    [string[]] $MysqlArgs,
    [string] $DatabaseName
  )

  $escaped = Escape-SqlString $DatabaseName
  $sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '$escaped';"
  $count = Invoke-MysqlQuery -MysqlPath $MysqlPath -MysqlArgs $MysqlArgs -Sql $sql
  return $count -eq '1'
}

function Read-Connection {
  $hostName = Read-WithDefault 'MySQL host' 'localhost'
  $port = Read-WithDefault 'MySQL port' '3306'
  $user = Read-Required 'MySQL username'
  $securePassword = Read-Host 'MySQL password' -AsSecureString
  $password = ConvertTo-PlainText $securePassword

  return @{
    HostName = $hostName
    Port = $port
    User = $user
    Password = $password
  }
}

function Import-Database {
  param([string] $MysqlPath)

  if (-not (Test-Path -LiteralPath $DefaultSqlFile)) {
    throw "SQL file does not exist: $DefaultSqlFile"
  }

  Write-Host 'Shipping Management Database Import' -ForegroundColor Cyan
  Write-Host "SQL file: $DefaultSqlFile"
  Write-Host ''

  $connection = Read-Connection
  $env:MYSQL_PWD = $connection.Password

  try {
    $mysqlArgs = Get-MysqlArgs -HostName $connection.HostName -Port $connection.Port -User $connection.User
    $databaseName = Read-WithDefault 'Target database name' $DefaultDatabase
    Assert-DatabaseName $databaseName

    while (Test-DatabaseExists -MysqlPath $MysqlPath -MysqlArgs $mysqlArgs -DatabaseName $databaseName) {
      Write-Host ''
      Write-Host "Database '$databaseName' already exists." -ForegroundColor Yellow
      Write-Host '[D] Drop old database and recreate'
      Write-Host '[R] Rename target database'
      Write-Host '[C] Cancel import'
      $choice = (Read-Host 'Choose D/R/C').Trim().ToUpperInvariant()

      if ($choice -eq 'D') {
        $quotedDatabase = Quote-DatabaseName $databaseName
        Invoke-MysqlQuery -MysqlPath $MysqlPath -MysqlArgs $mysqlArgs -Sql "DROP DATABASE $quotedDatabase;"
        Write-Host "Dropped database '$databaseName'." -ForegroundColor Yellow
        break
      }

      if ($choice -eq 'R') {
        $databaseName = Read-Required 'New target database name'
        Assert-DatabaseName $databaseName
        continue
      }

      if ($choice -eq 'C') {
        Write-Host 'Import cancelled.'
        return
      }

      Write-Host 'Invalid choice.' -ForegroundColor Yellow
    }

    $quotedTarget = Quote-DatabaseName $databaseName
    Invoke-MysqlQuery -MysqlPath $MysqlPath -MysqlArgs $mysqlArgs -Sql "CREATE DATABASE IF NOT EXISTS $quotedTarget DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

    Write-Host ''
    Write-Host "Importing into '$databaseName'..."
    Get-Content -Raw -Encoding UTF8 -LiteralPath $DefaultSqlFile | & $MysqlPath @mysqlArgs $databaseName

    if ($LASTEXITCODE -ne 0) {
      throw 'mysql import failed.'
    }

    Write-Host ''
    Write-Host "Imported into database: $databaseName" -ForegroundColor Green
    if ($databaseName -ne $DefaultDatabase) {
      Write-Host "Note: backend application.yml still uses '$DefaultDatabase'. Update it if this project should use '$databaseName'." -ForegroundColor Yellow
    }
  } finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
  }
}

try {
  $mysqlPath = Find-Tool 'mysql'
  Import-Database -MysqlPath $mysqlPath
  Pause-And-Exit 0
} catch {
  Write-Host ''
  Write-Host 'Failed:' -ForegroundColor Red
  Write-Host $_.Exception.Message -ForegroundColor Red
  Pause-And-Exit 1
}
