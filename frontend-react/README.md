# 航运公司管理系统 - React 前端

基于 React 19 + TypeScript + Ant Design 5 的前端实现，与 Vue 前端共用同一个后端 API。

## 技术栈

| 类型 | 方案 |
|---|---|
| 构建 | Vite |
| 框架 | React 19 + TypeScript |
| 路由 | React Router v7 |
| 状态管理 | Zustand |
| HTTP | Axios |
| UI 组件库 | Ant Design 5 |
| 包管理 | pnpm |

## 运行

```powershell
cd D:\project\swim\frontend-react
pnpm install
pnpm dev
```

默认地址：http://localhost:5174

## 构建

```powershell
pnpm build
```

## 已实现页面

- 登录页
- 业务工作台（KPI 统计 + 近期运输任务）
- 船舶管理（CRUD + 状态变更）
- 船员管理（CRUD + 状态变更）
- 运输任务（CRUD + 状态流转）
- 财务结算（CRUD + 收款登记）
- 基础字典（Tab 切换 + CRUD）
- 用户管理（CRUD）
- 角色管理（CRUD）
- 修改密码
