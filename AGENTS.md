# Agent Handoff Notes

## Project Facts

- Workspace: `D:\project\swim`
- Current project name: 简易航运公司管理系统
- Backend path: `D:\project\swim\backend`
- Frontend path: `D:\project\swim\frontend`
- Database script: `D:\project\swim\sql\schema.sql`
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

## Git Practice

The user asked to commit after each stage. Keep commits small and stage-specific.

Completed backend stages include core APIs, dashboard, dictionary, users/roles, and auth helper APIs. Frontend waste branches were removed; continue from `front-redesign` based on `11a5888 docs: tidy project handoff documentation`.

## Next Recommended Work

Continue frontend implementation from `前端页面设计文档.md` with Vue 3:

1. Finish dictionaries and shared select data loading.
2. Implement ships and crew pages with shared table/form patterns.
3. Implement transport orders and settlements.
4. Implement users and roles.

The current frontend already includes Vite + Vue 3 + TypeScript, Pinia, Vue Router, Axios, Element Plus, login, route guard, backend proxy, dashboard shell, dictionary select, status tag, ships CRUD page, and module placeholders. Use `DESIGN.md` as the visual source of truth. `前端页面设计文档.md` defines pages, routes, and interactions; `DESIGN.md` defines colors, typography, layout, component states, and frontend style constraints.
