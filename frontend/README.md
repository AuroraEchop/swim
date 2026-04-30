# 简易航运公司管理系统前端

本目录是课程设计项目前端，技术栈为 Vue 3、Vite、TypeScript、Pinia、Vue Router、Axios、Element Plus。

## 已搭建内容

- 登录页、后台主布局、顶部栏、侧边导航
- 路由守卫和登录状态持久化
- Axios 请求封装和 `/api` 代理
- 首页统计页面骨架
- 各业务模块占位页
- 基于 `DESIGN.md` 的正式浅色后台管理风格

## 常用命令

```powershell
pnpm install
pnpm dev
pnpm build
```

开发时后端默认运行在 `http://localhost:8080/api`，前端通过 Vite 代理访问 `/api`。
