# 简易航运公司管理系统

这是一个航运业务课程设计项目，采用前后端分离架构。当前已完成 Spring Boot 后端接口、MySQL 表结构、接口文档、DTO 文档和本地开发环境配置；下一阶段适合开始 Vue 3 前端开发。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 前端 | Vue 3，计划使用 Vite、Pinia、Vue Router、Axios、Element Plus |
| 后端 | Java 17，Spring Boot 3.3.5，MyBatis |
| 数据库 | MySQL |
| 构建工具 | Maven |
| 接口风格 | RESTful JSON API |

## 项目结构

```text
D:\project\swim
├─ backend/                 Spring Boot 后端
├─ sql/schema.sql           数据库建表与演示数据脚本
├─ 需求文档.md
├─ API接口文档.md
├─ DTO设计文档.md
├─ 数据库表结构设计.md
└─ 开发环境配置.md
```

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

## 测试

```powershell
cd D:\project\swim\backend
& 'C:\Users\ganmaojun\scoop\apps\maven\current\bin\mvn.cmd' test
```

截至 2026-04-30，后端测试为 123 个，全部通过。

## 演示账号

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `123456` | 管理员 |
| `business01` | `123456` | 业务用户 |

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
- [数据库表结构设计.md](数据库表结构设计.md)
- [开发环境配置.md](开发环境配置.md)
- [backend/README.md](backend/README.md)

## 下一阶段建议

从前端开始：先搭建 Vue 3 项目骨架，再完成登录页、后台主布局、路由守卫、Axios 封装，然后按首页、船舶、船员、运输任务、结算、字典、用户角色的顺序接入接口。
