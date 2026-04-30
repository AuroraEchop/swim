# Agent Handoff Notes

## Project Facts

- Workspace: `D:\project\swim`
- Current project name: 航运公司管理系统
- Backend path: `D:\project\swim\backend`
- Frontend path: `D:\project\swim\frontend`
- Database script: `D:\project\swim\sql\schema.sql`
- Full database dump: `D:\project\swim\sql\shipping_management_full.sql`
- Database import script: `D:\project\swim\scripts\database-transfer.ps1`
- Database import double-click launcher: `D:\project\swim\scripts\run-database-transfer.cmd`
- Frontend design doc: `D:\project\swim\前端页面设计文档.md`
- Frontend style guide: `D:\project\swim\DESIGN.md`
- Product context: `D:\project\swim\PRODUCT.md`
- Backend base URL: `http://localhost:8080/api`
- Frontend dev URL: `http://localhost:5173`
- JDK: Java 17
- Maven path: `C:\Users\ganmaojun\scoop\apps\maven\current\bin\mvn.cmd`
- MySQL database: `shipping_management`
- MySQL user/password: `gavin` / `456123`

## Course Design Constraint

This is a course design project. Authentication is intentionally simplified:

- `sys_user.password` stores plaintext passwords.
- Login compares the submitted password directly with the database value.
- Password changes update plaintext values directly.
- `loginToken` uses a simple demo format: `demo-token-{username}-{id}`.
- Do not treat this as a bug or spend effort adding production-grade security unless the user explicitly changes scope.

## Role Permission Rule

- `ADMIN`: full read/write access, including create, update, delete, status changes, payment registration, users, roles, and dictionaries.
- `BUSINESS` and `VIEWER`: read-only access to business data. They can view ships, crew members, transport orders, settlements, and change their own password, but cannot mutate business data.

## Implemented Backend Modules

- `auth`: login, current user, password change, logout
- `ships`: ship CRUD and status update
- `crew-members`: crew CRUD and status update
- `transport-orders`: transport order CRUD and status transition
- `settlements`: settlement CRUD and payment update
- `dashboard`: summary and recent transport orders
- `dictionaries`: dictionary item CRUD
- `users`: user management
- `roles`: role management

## Commands

Run backend tests:

```powershell
cd D:\project\swim\backend
& 'C:\Users\ganmaojun\scoop\apps\maven\current\bin\mvn.cmd' test
```

Package backend:

```powershell
cd D:\project\swim\backend
& 'C:\Users\ganmaojun\scoop\apps\maven\current\bin\mvn.cmd' -DskipTests package
```

Build frontend:

```powershell
cd D:\project\swim\frontend
pnpm build
```

Run frontend dev server:

```powershell
cd D:\project\swim\frontend
pnpm dev
```

Start MySQL as the current user:

```powershell
Start-Process -FilePath 'C:\Users\ganmaojun\scoop\apps\mysql\current\bin\mysqld.exe' -ArgumentList '--defaults-file=C:\Users\ganmaojun\scoop\persist\mysql\my.ini' -WindowStyle Hidden
```

Initialize database:

```powershell
Get-Content -Raw -LiteralPath 'D:\project\swim\sql\schema.sql' | mysql -ugavin -p456123
```

Import the exported database dump:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\database-transfer.ps1
```

For double-click usage on Windows, use `scripts\run-database-transfer.cmd`; it invokes the PowerShell script with `ExecutionPolicy Bypass`.

The script imports `sql\shipping_management_full.sql`. It prompts for MySQL credentials, checks whether the target database exists, and lets the user either drop/recreate it or enter a new database name.

## Git Practice

The user asked to commit after each stage. Keep commits small and stage-specific.

Completed backend stages include core APIs, dashboard, dictionary, users/roles, and auth helper APIs. The active branch is `front-redesign`; frontend implementation, integration cleanup, database import tooling, and the full database dump have been committed on this branch.

## Current Recommended Work

Main backend and frontend pages are implemented and smoke-tested through real APIs. Current work should focus on running the app for manual presentation walkthrough and final polish rather than new module scaffolding:

1. Manual browser walkthrough for presentation flow.
2. Responsive and empty-state polish where needed.
3. Final packaging or deployment notes if the course submission requires them.

The current frontend includes Vite + Vue 3 + TypeScript, Pinia, Vue Router, Axios, Element Plus, login, route guard, backend proxy, dashboard shell, password change dialog, dictionary select, ship select, transport order select, role select, status tag, ships CRUD page, crew members CRUD page, transport orders CRUD/status page, settlements CRUD/payment page, dictionaries CRUD page, users CRUD page, and roles CRUD page. Use `DESIGN.md` as the visual source of truth. `前端页面设计文档.md` defines pages, routes, and interactions; `DESIGN.md` defines colors, typography, layout, component states, and frontend style constraints.
