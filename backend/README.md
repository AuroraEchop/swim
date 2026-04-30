# Shipping Management Backend

Spring Boot backend for the shipping management course design project.

## Stack

- Java 17
- Spring Boot 3.3.5
- MyBatis
- MySQL
- JUnit 5

## Database

```text
jdbc:mysql://localhost:3306/shipping_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
username: gavin
password: 456123
```

Initialize the database from the project root:

```powershell
Get-Content -Raw -LiteralPath 'D:\project\swim\sql\schema.sql' | mysql -ugavin -p456123
```

## Course Design Authentication

This project intentionally stores user passwords as plaintext. Login compares the submitted password with the database value directly, and password changes write the new plaintext password back to `sys_user.password`.

This is a course design simplification and should not be treated as a defect inside this project scope.

## Implemented APIs

- `POST /api/auth/login`
- `GET /api/auth/me`
- `PUT /api/auth/password`
- `POST /api/auth/logout`
- `/api/ships`
- `/api/crew-members`
- `/api/transport-orders`
- `/api/settlements`
- `/api/dashboard/summary`
- `/api/dashboard/recent-transport-orders`
- `/api/dictionaries`
- `/api/users`
- `/api/roles`

See `D:\project\swim\API接口文档.md` for request and response details.

## Tests

Run from `backend/`:

```powershell
& 'C:\Users\ganmaojun\scoop\apps\maven\current\bin\mvn.cmd' test
```

As of 2026-04-30, the backend has 123 passing tests.
