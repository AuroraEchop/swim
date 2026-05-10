# 航运公司管理系统

这是一个航运业务课程设计项目，采用前后端分离架构。当前已完成 Spring Boot 后端接口、MySQL 表结构、接口文档、DTO 文档、本地开发环境配置、前端页面设计，以及 Vue 3 和 React 两套前端实现。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 前端（Vue） | Vue 3，Vite，TypeScript，Pinia，Vue Router，Axios，Element Plus |
| 前端（React） | React 19，Vite，TypeScript，Zustand，React Router，Axios，Ant Design 5 |
| 后端 | Java 17，Spring Boot 3.3.5，MyBatis |
| 数据库 | MySQL |
| 构建工具 | Maven |
| 接口风格 | RESTful JSON API |

## 项目结构

```text
D:\project\swim
├─ backend/                 Spring Boot 后端
├─ frontend/                Vue 3 前端
├─ frontend-react/          React 19 前端
├─ sql/schema.sql           数据库建表与演示数据脚本
├─ sql/shipping_management_full.sql
│                           当前数据库完整导出文件
├─ scripts/database-transfer.ps1
│                           数据库导入脚本
├─ scripts/run-database-transfer.cmd
│                           双击运行数据库导入脚本的启动器
├─ DESIGN.md                前端视觉风格规范
├─ PRODUCT.md               产品定位与约束
├─ 需求文档.md
├─ API接口文档.md
├─ DTO设计文档.md
├─ 前端页面设计文档.md
├─ 数据库表结构设计.md
└─ 开发环境配置.md
```

两套前端均已实现登录页、后台主布局、路由守卫、API 客户端、修改密码弹窗、首页统计、船舶管理、船员管理、运输任务、财务结算、基础字典、用户管理和角色管理页面。页面风格以 `DESIGN.md` 为准：浅色后台管理界面，深海青作为主色，整体保持正式、清晰、适合学校课程设计演示。

## 运行前准备

1. 启动 MySQL。

```powershell
Start-Process -FilePath 'C:\Users\ganmaojun\scoop\apps\mysql\current\bin\mysqld.exe' -ArgumentList '--defaults-file=C:\Users\ganmaojun\scoop\persist\mysql\my.ini' -WindowStyle Hidden
```

2. 初始化数据库。

```powershell
Get-Content -Raw -LiteralPath 'D:\project\swim\sql\schema.sql' | mysql -ugavin -p456123
```

3. 启动后端。

```powershell
cd D:\project\swim\backend
& 'C:\Users\ganmaojun\scoop\apps\maven\current\bin\mvn.cmd' spring-boot:run
```

后端基础地址：

```text
http://localhost:8080/api
```

4. 启动前端（二选一）。

Vue 前端：

```powershell
cd D:\project\swim\frontend
pnpm dev
```

React 前端：

```powershell
cd D:\project\swim\frontend-react
pnpm dev
```

前端默认地址：

| 前端 | 地址 |
| --- | --- |
| Vue | http://localhost:5173 |
| React | http://localhost:5174 |

两套前端可同时运行，共用同一个后端 API。

## 数据库导入

当前数据库已导出到：

```text
sql/shipping_management_full.sql
```

需要在另一台电脑导入数据时，推荐双击：

```text
scripts/run-database-transfer.cmd
```

也可以在命令行运行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\database-transfer.ps1
```

脚本会直接导入 `sql/shipping_management_full.sql`。运行后输入 MySQL 用户名和密码；如果目标库已存在，可以选择删除原库并重建，或者输入一个新的数据库名。

如果目标电脑已经允许直接运行 `.ps1`，也可以双击 `scripts/database-transfer.ps1`。导入到非默认库名时，需要同步修改后端 `backend/src/main/resources/application.yml` 里的数据库名。

## 测试

```powershell
cd D:\project\swim\backend
& 'C:\Users\ganmaojun\scoop\apps\maven\current\bin\mvn.cmd' test
```

截至 2026-04-30，后端测试为 123 个，全部通过。

前端构建检查：

```powershell
cd D:\project\swim\frontend
pnpm build

cd D:\project\swim\frontend-react
pnpm build
```

## 演示账号

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `123456` | 管理员 |
| `business01` | `123456` | 业务用户，只读查看 |

权限控制采用基础角色控制：管理员可执行新增、编辑、删除、状态变更和收款登记等写操作；业务用户可查看业务数据，但不能修改数据。

本项目是课程设计，登录只做用户名与密码校验。用户密码以明文存储在 `sys_user.password`，后端直接明文比较和明文更新；这是有意简化，不作为本项目漏洞处理。

## 已实现后端模块

- 认证：登录、当前用户、修改密码、退出登录
- 船舶管理
- 船员管理
- 运输任务管理
- 财务结算管理
- 首页统计
- 基础字典
- 用户管理
- 角色管理

## 关键文档

- [需求文档.md](需求文档.md)
- [API接口文档.md](API接口文档.md)
- [DTO设计文档.md](DTO设计文档.md)
- [前端页面设计文档.md](前端页面设计文档.md)
- [DESIGN.md](DESIGN.md)
- [PRODUCT.md](PRODUCT.md)
- [数据库表结构设计.md](数据库表结构设计.md)
- [开发环境配置.md](开发环境配置.md)
- [backend/README.md](backend/README.md)
- [frontend/README.md](frontend/README.md)
- [frontend-react/README.md](frontend-react/README.md)

## 当前联调状态

截至 2026-04-30，已完成前后端联调冒烟：登录、首页数据、字典、角色、用户、船舶、船员、运输任务、结算收款均可通过真实接口完成基础增删改查或状态操作。联调测试数据已在验证脚本中清理。
