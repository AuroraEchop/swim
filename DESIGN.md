# Frontend Design System

## Design Positioning

本系统是学校课程设计中的航运公司后台管理系统。前端视觉应保持专业、正式、清晰，重点是让老师和同学能快速看懂业务结构和操作路径。界面不追求潮流感，不使用暗色驾驶舱、大面积渐变、玻璃拟态或复杂动效。

场景判断：用户在明亮教室或普通办公环境中，通过笔记本或台式机演示后台系统，目标是在几分钟内完成登录、查看首页、维护业务数据和展示运输结算闭环。因此采用浅色办公主题。

## Visual Direction

| 维度 | 规范 |
| --- | --- |
| 风格 | 正式管理后台，清晰、稳重、低装饰 |
| 主题 | 浅色主题 |
| 色彩策略 | Restrained，冷灰中性色为主，深海青作为主色 |
| 组件基础 | Element Plus，适度覆盖主题变量 |
| 信息密度 | 中等偏高，适合表格和表单操作 |
| 动效 | 仅用于状态反馈和抽屉展开，150 到 220ms |

## Color Tokens

颜色使用 OKLCH。主色控制在页面面积的 10% 以内，主要用于主按钮、当前菜单、链接、焦点态和关键状态提示。

```css
:root {
  --color-bg-page: oklch(97.5% 0.008 210);
  --color-bg-surface: oklch(99% 0.006 210);
  --color-bg-subtle: oklch(95.5% 0.01 210);
  --color-bg-sidebar: oklch(94% 0.018 214);

  --color-text-primary: oklch(25% 0.025 220);
  --color-text-regular: oklch(38% 0.02 220);
  --color-text-secondary: oklch(52% 0.018 220);
  --color-text-placeholder: oklch(66% 0.014 220);

  --color-border-light: oklch(90% 0.012 214);
  --color-border-base: oklch(84% 0.014 214);

  --color-primary: oklch(44% 0.095 205);
  --color-primary-hover: oklch(50% 0.105 205);
  --color-primary-active: oklch(38% 0.09 205);
  --color-primary-soft: oklch(93% 0.035 205);

  --color-success: oklch(54% 0.12 150);
  --color-success-soft: oklch(94% 0.04 150);
  --color-warning: oklch(68% 0.13 75);
  --color-warning-soft: oklch(95% 0.05 75);
  --color-danger: oklch(56% 0.15 28);
  --color-danger-soft: oklch(94% 0.045 28);
  --color-info: oklch(55% 0.07 235);
  --color-info-soft: oklch(94% 0.03 235);
}
```

## Semantic Color Mapping

| 场景 | 颜色 |
| --- | --- |
| 主操作、当前菜单、链接 | `--color-primary` |
| 启用、空闲、已结算 | success |
| 待出发、待结算、待处理 | warning |
| 运输中、部分结算、信息提示 | info 或 primary |
| 停用、取消、离职、删除 | danger 或 neutral disabled |
| 普通边框、表格分割线 | border tokens |

## Typography

采用系统字体，不引入花哨字体。后台界面的可读性和稳定性优先。

```css
font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Microsoft YaHei", system-ui, sans-serif;
```

| 用途 | 字号 | 行高 | 字重 |
| --- | --- | --- | --- |
| 页面标题 | 22px | 30px | 600 |
| 分区标题 | 18px | 26px | 600 |
| 表格标题、表单组标题 | 15px | 22px | 600 |
| 正文、表单、表格 | 14px | 22px | 400 |
| 辅助说明 | 13px | 20px | 400 |
| 数据和金额 | 14px | 22px | 500 |

规则：

1. 不使用展示字体。
2. 不使用流式字号。
3. 表格、表单、菜单保持 14px 基准。
4. 金额字段使用等宽数字特性：`font-variant-numeric: tabular-nums;`。

## Layout

后台采用标准三段式布局。

| 区域 | 规范 |
| --- | --- |
| 侧边栏 | 宽 224px，折叠宽 64px |
| 顶栏 | 高 56px，展示页面标题、刷新、用户菜单 |
| 主内容区 | 背景冷灰，内边距 20px 到 24px |
| 页面最大宽度 | 不强制居中，管理后台应充分利用横向空间 |
| 表单抽屉 | 桌面端 520px 到 640px，窄屏 100% |

页面结构建议：

```text
PageHeader
SearchPanel
Toolbar
DataTable
Pagination
Drawer/Form
```

不要把所有内容都放进卡片。列表页可以使用一个白色内容面承载筛选和表格，统计页可以使用少量指标块，但避免同样尺寸的卡片无限重复。

## Component Rules

### Buttons

| 类型 | 用途 |
| --- | --- |
| Primary | 新增、保存、登录、确认提交 |
| Default | 取消、返回、次要操作 |
| Text | 表格行内查看、编辑 |
| Danger | 删除、取消任务等风险操作 |

规则：

1. 主按钮每个操作区最多一个。
2. 表格行操作顺序固定：查看、编辑、状态、删除。
3. 删除必须二次确认。

### Tables

表格是本系统核心组件。

1. 表头背景使用 `--color-bg-subtle`。
2. 行高建议 48px。
3. 操作列固定在右侧。
4. 长文本使用 tooltip 或详情抽屉，不撑开表格。
5. 金额右对齐，状态居中或左对齐均可，但同一页面保持一致。
6. 空状态提供下一步动作，例如“新增船舶”“新建运输任务”。

### Forms

1. 新增和编辑优先使用右侧抽屉。
2. 简短状态更新可使用弹窗。
3. 表单标签宽度统一 96px 或 112px。
4. 必填项使用 Element Plus 默认星号。
5. 金额、重量、日期时间使用明确单位和格式提示。

### Status Tags

状态标签必须语义一致，避免同一个状态在不同页面颜色不同。

| 状态 | 中文 | 类型 |
| --- | --- | --- |
| `ENABLED` | 启用 | success |
| `DISABLED` | 停用 | info 或 danger，按业务语境 |
| `IDLE` | 空闲 | success |
| `SAILING` | 运输中 | primary |
| `MAINTENANCE` | 维修中 | warning |
| `ON_DUTY` | 在岗 | success |
| `ON_LEAVE` | 休假 | warning |
| `UNASSIGNED` | 待分配 | info |
| `RESIGNED` | 离职 | info |
| `PENDING` | 待出发 | warning |
| `IN_TRANSIT` | 运输中 | primary |
| `ARRIVED` | 已到达 | success |
| `CANCELLED` | 已取消 | info |
| `UNSETTLED` | 未结算 | warning |
| `PARTIAL` | 部分结算 | primary |
| `SETTLED` | 已结算 | success |

## Page Style Guidance

### Login Page

登录页要正式，不做营销页。

布局建议：

1. 左侧为系统名称、简短说明和三项业务范围。
2. 右侧为登录表单。
3. 背景使用冷灰浅色，可加入非常克制的航线线条或港口网格纹理。
4. 登录按钮使用主色。

文案：

```text
航运公司管理系统
统一管理船舶、船员、运输任务与结算流程
```

不要写“演示环境”“测试系统”“仅供展示”。

### Dashboard

首页用于答辩快速展示系统价值。

1. 顶部展示 5 个关键指标，但不要做大屏样式。
2. 金额概览使用横向信息条或简洁统计块。
3. 近期运输任务使用表格，不用时间线花样组件。
4. 快捷入口使用朴素按钮或图标按钮。

### Business Lists

船舶、船员、运输任务、结算、用户这些页面应保持统一结构。

1. 筛选区在上，表格在下。
2. 新增按钮放在页面右上或表格工具栏右侧。
3. 高风险操作使用确认框。
4. 详情用抽屉，不默认用大弹窗。

### Dictionary And Role Pages

这类页面信息量小，保持紧凑。

1. 字典类型使用 tabs。
2. 角色页面不做复杂权限树。
3. `permissions` 字段不落库，前端可作为预留说明，不做主要交互。

## Motion

只使用轻量动效。

| 动作 | 时长 | 说明 |
| --- | --- | --- |
| 菜单折叠 | 180ms | 宽度和透明度变化 |
| 抽屉出现 | 200ms | Element Plus 默认即可 |
| hover | 150ms | 背景色、边框色 |
| loading skeleton | 低调闪烁 | 用于表格和统计区 |

不做页面入场动画，不做弹跳，不做无意义装饰动效。

## Accessibility And Usability

1. 文本和背景对比度至少满足 WCAG AA。
2. 表单错误提示必须紧跟字段。
3. 按钮禁用态要明显。
4. 焦点态使用主色轮廓，不移除 outline。
5. 所有图标按钮必须有 tooltip 或 aria-label。
6. 表格横向滚动时操作列固定。

## Implementation Notes

Element Plus 主题覆盖建议放在：

```text
frontend/src/styles/theme.css
frontend/src/styles/global.css
```

建议使用 CSS 变量映射 Element Plus：

```css
:root {
  --el-color-primary: var(--color-primary);
  --el-border-radius-base: 6px;
  --el-font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Microsoft YaHei", system-ui, sans-serif;
}
```

## Design Acceptance Checklist

- 页面看起来像正式后台系统，不像营销页或大屏。
- 左侧菜单、顶部栏、表格、表单风格一致。
- 主色使用克制，不超过主要界面面积的 10%。
- 所有业务状态颜色语义稳定。
- 表格在 1366px 宽度下可用。
- 768px 到 1024px 下菜单可折叠，表格可横向滚动。
- 登录页不出现弱化系统完成度的文案。
- 密码明文方案只作为课程设计说明出现，不显示为安全警告。
