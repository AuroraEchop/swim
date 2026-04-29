# 简易航运公司管理系统 DTO 设计文档

## 1. 文档说明

本文档用于规范后端接口中的 DTO 设计。后续 Spring Boot 后端开发应优先参考本文档，保证前后端字段命名、请求参数、响应结构和 API 接口文档保持一致。

DTO 只用于接口层数据传输，不直接等同于数据库实体表。数据库实体可以包含数据库字段、创建时间、更新时间等内部信息；DTO 则只暴露前端需要提交或展示的数据。

## 2. 通用 DTO 约定

### 2.1 命名规范

DTO 类按照模块和用途命名：

| 类型 | 命名示例 | 说明 |
| --- | --- | --- |
| 新增请求 | `CreateShipRequest` | 用于 `POST` 新增资源 |
| 修改请求 | `UpdateShipRequest` | 用于 `PUT` 修改资源 |
| 状态修改请求 | `UpdateShipStatusRequest` | 用于 `PATCH` 修改状态 |
| 查询响应 | `ShipResponse` | 用于列表和详情展示 |
| 创建响应 | `ShipCreateResponse` | 用于新增成功后返回主键或编号 |
| 登录请求 | `LoginRequest` | 用于登录 |
| 登录响应 | `LoginResponse` | 用于登录成功返回 |

### 2.2 字段命名

1. Java DTO 字段使用小驼峰命名，例如 `shipNo`、`shipName`、`createdAt`。
2. JSON 字段默认使用小驼峰命名，与 Java 字段保持一致。
3. 数据库字段使用下划线命名，例如 `ship_no`、`created_at`。
4. 金额和重量字段使用 `BigDecimal`。
5. 日期时间字段使用 `LocalDateTime`，响应格式统一为 `yyyy-MM-dd HH:mm:ss`。
6. 枚举字段直接使用枚举值字符串，例如 `IDLE`、`PENDING`、`SETTLED`。

### 2.3 校验规范

请求 DTO 中应使用 Bean Validation 注解进行基础校验。

常用注解：

| 注解 | 说明 |
| --- | --- |
| `@NotBlank` | 字符串必填且不能为空白 |
| `@NotNull` | 对象、枚举、数字必填 |
| `@Size` | 字符串长度限制 |
| `@DecimalMin` | 数字最小值限制 |
| `@Email` | 邮箱格式校验 |

业务规则不放在 DTO 中，例如“船舶载重量不能小于货物重量”“已结算运输任务不能修改”等，应放在 Service 层。

### 2.4 统一响应 DTO

所有非 `204 No Content` 接口统一返回：

```java
ApiResponse<T>
```

字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 业务状态码 |
| message | String | 响应信息 |
| data | T | 响应数据 |

分页数据统一使用：

```java
PageResult<T>
```

字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| records | List<T> | 当前页数据 |
| page | int | 当前页码 |
| pageSize | int | 每页数量 |
| total | long | 总记录数 |
| totalPages | long | 总页数 |

## 3. 认证模块 DTO

### 3.1 LoginRequest

用于用户登录。

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| username | String | 是 | 3-30 位 | 用户名 |
| password | String | 是 | 6-30 位 | 明文密码 |

说明：本项目是课程设计，密码直接按明文传输给后端并与数据库明文密码比较。这是项目范围内的明确简化设计。

### 3.2 LoginResponse

用于登录成功响应。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| loginToken | String | 简单登录标识 |
| tokenType | String | 固定为 `Bearer` |
| user | LoginUser | 当前登录用户信息 |

### 3.3 LoginUser

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 用户 ID |
| username | String | 用户名 |
| realName | String | 真实姓名 |
| roleCode | String | 角色编码 |

## 4. 船舶模块 DTO

### 4.1 CreateShipRequest

用于新增船舶。

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| shipNo | String | 是 | 3-30 位，唯一 | 船舶编号 |
| shipName | String | 是 | 2-50 位 | 船名 |
| shipType | String | 是 | 非空 | 船舶类型 |
| loadCapacity | BigDecimal | 是 | 大于 0 | 载重量，单位吨 |
| homePort | String | 否 | 最长 50 位 | 所属港口 |
| status | ShipStatus | 是 | 枚举值 | 船舶状态 |
| remark | String | 否 | 最长 255 位 | 备注 |

### 4.2 UpdateShipRequest

用于修改船舶。船舶编号 `shipNo` 不在修改 DTO 中，避免修改唯一业务编号。

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| shipName | String | 是 | 2-50 位 | 船名 |
| shipType | String | 是 | 非空 | 船舶类型 |
| loadCapacity | BigDecimal | 是 | 大于 0 | 载重量，单位吨 |
| homePort | String | 否 | 最长 50 位 | 所属港口 |
| status | ShipStatus | 是 | 枚举值 | 船舶状态 |
| remark | String | 否 | 最长 255 位 | 备注 |

### 4.3 UpdateShipStatusRequest

用于单独修改船舶状态。

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| status | ShipStatus | 是 | 枚举值 | 船舶状态 |

### 4.4 ShipResponse

用于船舶列表和详情响应。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 船舶 ID |
| shipNo | String | 船舶编号 |
| shipName | String | 船名 |
| shipType | String | 船舶类型 |
| loadCapacity | BigDecimal | 载重量，单位吨 |
| homePort | String | 所属港口 |
| status | ShipStatus | 船舶状态 |
| remark | String | 备注 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### 4.5 ShipCreateResponse

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 新增船舶 ID |

## 5. 船员模块 DTO

### 5.1 CreateCrewMemberRequest

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| crewNo | String | 是 | 3-30 位，唯一 | 船员编号 |
| name | String | 是 | 2-30 位 | 姓名 |
| gender | String | 否 | 最长 10 位 | 性别 |
| phone | String | 否 | 最长 30 位 | 联系电话 |
| certificateNo | String | 是 | 最长 50 位，唯一 | 证件编号 |
| position | String | 是 | 非空，最长 50 位 | 岗位 |
| shipId | Long | 否 | 已存在船舶 ID | 所属船舶 |
| status | CrewStatus | 是 | 枚举值 | 船员状态 |
| remark | String | 否 | 最长 255 位 | 备注 |

### 5.2 UpdateCrewMemberRequest

船员编号 `crewNo` 不允许通过普通修改接口修改。

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| name | String | 是 | 2-30 位 | 姓名 |
| gender | String | 否 | 最长 10 位 | 性别 |
| phone | String | 否 | 最长 30 位 | 联系电话 |
| certificateNo | String | 是 | 最长 50 位，唯一 | 证件编号 |
| position | String | 是 | 非空，最长 50 位 | 岗位 |
| shipId | Long | 否 | 已存在船舶 ID | 所属船舶 |
| status | CrewStatus | 是 | 枚举值 | 船员状态 |
| remark | String | 否 | 最长 255 位 | 备注 |

### 5.3 UpdateCrewMemberStatusRequest

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| status | CrewStatus | 是 | 船员状态 |

### 5.4 CrewMemberResponse

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 船员 ID |
| crewNo | String | 船员编号 |
| name | String | 姓名 |
| gender | String | 性别 |
| phone | String | 联系电话 |
| certificateNo | String | 证件编号 |
| position | String | 岗位 |
| shipId | Long | 所属船舶 ID |
| shipName | String | 所属船舶名称 |
| status | CrewStatus | 船员状态 |
| remark | String | 备注 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### 5.5 CrewMemberCreateResponse

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 新增船员 ID |

## 6. 运输任务模块 DTO

### 6.1 CreateTransportOrderRequest

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| cargoName | String | 是 | 2-100 位 | 货物名称 |
| cargoType | String | 是 | 非空 | 货物类型 |
| cargoWeight | BigDecimal | 是 | 大于 0 | 货物重量，单位吨 |
| originPort | String | 是 | 非空，最长 50 位 | 起运港 |
| destinationPort | String | 是 | 非空，最长 50 位 | 目的港 |
| shipId | Long | 是 | 已存在船舶 ID | 运输船舶 |
| customerName | String | 是 | 2-100 位 | 客户名称 |
| customerPhone | String | 否 | 最长 30 位 | 客户电话 |
| plannedDepartureTime | LocalDateTime | 是 | 日期时间 | 预计出发时间 |
| plannedArrivalTime | LocalDateTime | 是 | 日期时间 | 预计到达时间 |
| remark | String | 否 | 最长 255 位 | 备注 |

### 6.2 UpdateTransportOrderRequest

运输任务编号 `orderNo` 由系统生成，不通过修改 DTO 传入。

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| cargoName | String | 是 | 2-100 位 | 货物名称 |
| cargoType | String | 是 | 非空 | 货物类型 |
| cargoWeight | BigDecimal | 是 | 大于 0 | 货物重量，单位吨 |
| originPort | String | 是 | 非空，最长 50 位 | 起运港 |
| destinationPort | String | 是 | 非空，最长 50 位 | 目的港 |
| shipId | Long | 是 | 已存在船舶 ID | 运输船舶 |
| customerName | String | 是 | 2-100 位 | 客户名称 |
| customerPhone | String | 否 | 最长 30 位 | 客户电话 |
| plannedDepartureTime | LocalDateTime | 是 | 日期时间 | 预计出发时间 |
| plannedArrivalTime | LocalDateTime | 是 | 日期时间 | 预计到达时间 |
| remark | String | 否 | 最长 255 位 | 备注 |

### 6.3 UpdateTransportStatusRequest

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| status | TransportStatus | 是 | 新运输状态 |
| actualDepartureTime | LocalDateTime | 否 | 实际出发时间 |
| actualArrivalTime | LocalDateTime | 否 | 实际到达时间 |

### 6.4 TransportOrderResponse

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 运输任务 ID |
| orderNo | String | 运输任务编号 |
| cargoName | String | 货物名称 |
| cargoType | String | 货物类型 |
| cargoWeight | BigDecimal | 货物重量，单位吨 |
| originPort | String | 起运港 |
| destinationPort | String | 目的港 |
| shipId | Long | 船舶 ID |
| shipName | String | 船名 |
| customerName | String | 客户名称 |
| customerPhone | String | 客户电话 |
| plannedDepartureTime | LocalDateTime | 预计出发时间 |
| plannedArrivalTime | LocalDateTime | 预计到达时间 |
| actualDepartureTime | LocalDateTime | 实际出发时间 |
| actualArrivalTime | LocalDateTime | 实际到达时间 |
| status | TransportStatus | 运输状态 |
| settlementId | Long | 结算记录 ID |
| settlementStatus | SettlementStatus | 结算状态 |
| remark | String | 备注 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### 6.5 TransportOrderCreateResponse

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 新增运输任务 ID |
| orderNo | String | 系统生成的运输任务编号 |

## 7. 财务结算模块 DTO

### 7.1 CreateSettlementRequest

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| transportOrderId | Long | 是 | 已存在运输任务 ID | 运输任务 |
| freightAmount | BigDecimal | 是 | 大于等于 0 | 运费金额 |
| additionalFee | BigDecimal | 否 | 大于等于 0 | 附加费用，默认 0 |
| discountAmount | BigDecimal | 否 | 大于等于 0 | 优惠金额，默认 0 |
| receivedAmount | BigDecimal | 否 | 大于等于 0 | 实收金额，默认 0 |
| remark | String | 否 | 最长 255 位 | 备注 |

### 7.2 UpdateSettlementRequest

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| freightAmount | BigDecimal | 是 | 大于等于 0 | 运费金额 |
| additionalFee | BigDecimal | 否 | 大于等于 0 | 附加费用 |
| discountAmount | BigDecimal | 否 | 大于等于 0 | 优惠金额 |
| receivedAmount | BigDecimal | 否 | 大于等于 0 | 实收金额 |
| remark | String | 否 | 最长 255 位 | 备注 |

### 7.3 UpdateSettlementPaymentRequest

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| receivedAmount | BigDecimal | 是 | 大于等于 0 | 实收金额 |
| paymentTime | LocalDateTime | 否 | 日期时间 | 付款时间 |

### 7.4 SettlementResponse

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 结算 ID |
| settlementNo | String | 结算编号 |
| transportOrderId | Long | 运输任务 ID |
| transportOrderNo | String | 运输任务编号 |
| customerName | String | 客户名称 |
| freightAmount | BigDecimal | 运费金额 |
| additionalFee | BigDecimal | 附加费用 |
| discountAmount | BigDecimal | 优惠金额 |
| receivableAmount | BigDecimal | 应收金额 |
| receivedAmount | BigDecimal | 实收金额 |
| status | SettlementStatus | 结算状态 |
| settledAt | LocalDateTime | 结算完成时间 |
| remark | String | 备注 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### 7.5 SettlementCreateResponse

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 新增结算 ID |
| settlementNo | String | 系统生成的结算编号 |

### 7.6 SettlementPaymentResponse

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| status | SettlementStatus | 付款后结算状态 |
| settledAt | LocalDateTime | 结算完成时间，未结清时为空 |

## 8. 字典模块 DTO

### 8.1 CreateDictionaryItemRequest

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| dictType | String | 是 | 非空，最长 50 位 | 字典类型 |
| label | String | 是 | 非空，最长 50 位 | 显示名称 |
| value | String | 是 | 非空，最长 50 位 | 字典值 |
| sort | Integer | 否 | 整数 | 排序号，默认 0 |
| enabled | Boolean | 否 | 布尔值 | 是否启用，默认 true |
| remark | String | 否 | 最长 255 位 | 备注 |

### 8.2 UpdateDictionaryItemRequest

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| label | String | 是 | 非空，最长 50 位 | 显示名称 |
| value | String | 是 | 非空，最长 50 位 | 字典值 |
| sort | Integer | 否 | 整数 | 排序号 |
| enabled | Boolean | 否 | 布尔值 | 是否启用 |
| remark | String | 否 | 最长 255 位 | 备注 |

### 8.3 DictionaryItemResponse

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 字典项 ID |
| dictType | String | 字典类型 |
| label | String | 显示名称 |
| value | String | 字典值 |
| sort | Integer | 排序号 |
| enabled | Boolean | 是否启用 |
| remark | String | 备注 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### 8.4 DictionaryItemCreateResponse

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 新增字典项 ID |

## 9. 用户与角色模块 DTO

### 9.1 CreateUserRequest

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| username | String | 是 | 3-30 位，唯一 | 用户名 |
| password | String | 是 | 6-30 位 | 明文密码，课程设计简化使用 |
| realName | String | 是 | 2-30 位 | 真实姓名 |
| phone | String | 否 | 最长 30 位 | 联系电话 |
| email | String | 否 | 邮箱格式，最长 100 位 | 邮箱 |
| roleId | Long | 是 | 已存在角色 ID | 角色 |
| status | UserStatus | 是 | 枚举值 | 用户状态 |

### 9.2 UpdateUserRequest

普通修改用户时不修改 `username` 和 `password`。密码修改使用单独 DTO。

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| realName | String | 是 | 2-30 位 | 真实姓名 |
| phone | String | 否 | 最长 30 位 | 联系电话 |
| email | String | 否 | 邮箱格式，最长 100 位 | 邮箱 |
| roleId | Long | 是 | 已存在角色 ID | 角色 |
| status | UserStatus | 是 | 枚举值 | 用户状态 |

### 9.3 UpdatePasswordRequest

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| oldPassword | String | 是 | 6-30 位 | 旧明文密码 |
| newPassword | String | 是 | 6-30 位 | 新明文密码 |

### 9.4 UserResponse

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 用户 ID |
| username | String | 用户名 |
| realName | String | 真实姓名 |
| phone | String | 联系电话 |
| email | String | 邮箱 |
| roleId | Long | 角色 ID |
| roleName | String | 角色名称 |
| roleCode | String | 角色编码 |
| status | UserStatus | 用户状态 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### 9.5 UserCreateResponse

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 新增用户 ID |

### 9.6 RoleResponse

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 角色 ID |
| roleName | String | 角色名称 |
| roleCode | String | 角色编码 |
| description | String | 角色说明 |
| builtIn | Boolean | 是否内置角色 |

## 10. 首页统计模块 DTO

### 10.1 DashboardSummaryResponse

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| shipCount | Long | 船舶数量 |
| crewCount | Long | 船员数量 |
| pendingTransportCount | Long | 待出发运输任务数量 |
| inTransitCount | Long | 运输中任务数量 |
| unsettledCount | Long | 未结清记录数量 |
| totalReceivableAmount | BigDecimal | 应收总金额 |
| totalReceivedAmount | BigDecimal | 实收总金额 |

### 10.2 RecentTransportOrderResponse

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 运输任务 ID |
| orderNo | String | 运输任务编号 |
| cargoName | String | 货物名称 |
| shipName | String | 船名 |
| originPort | String | 起运港 |
| destinationPort | String | 目的港 |
| status | TransportStatus | 运输状态 |
| plannedDepartureTime | LocalDateTime | 预计出发时间 |

## 11. 枚举类型

### 11.1 UserStatus

| 值 | 说明 |
| --- | --- |
| ENABLED | 启用 |
| DISABLED | 禁用 |

### 11.2 ShipStatus

| 值 | 说明 |
| --- | --- |
| IDLE | 空闲 |
| SAILING | 运输中 |
| MAINTENANCE | 维修中 |
| DISABLED | 停用 |

### 11.3 CrewStatus

| 值 | 说明 |
| --- | --- |
| ON_DUTY | 在岗 |
| ON_LEAVE | 休假 |
| UNASSIGNED | 待分配 |
| RESIGNED | 离职 |

### 11.4 TransportStatus

| 值 | 说明 |
| --- | --- |
| PENDING | 待出发 |
| IN_TRANSIT | 运输中 |
| ARRIVED | 已到达 |
| CANCELLED | 已取消 |

### 11.5 SettlementStatus

| 值 | 说明 |
| --- | --- |
| UNSETTLED | 未结算 |
| PARTIAL | 部分结算 |
| SETTLED | 已结算 |

## 12. 后续开发执行规则

1. 新增接口前，先确认本文档是否已有对应 DTO。
2. 若实际开发发现字段不足，先更新本文档，再修改代码。
3. Request DTO 只接收前端允许提交的字段，不暴露数据库内部字段。
4. Response DTO 只返回前端需要展示的字段。
5. 新增和修改请求应使用不同 DTO，避免前端误传不可修改字段。
6. 业务编号如 `shipNo`、`crewNo`、`orderNo`、`settlementNo` 应谨慎修改；系统生成的编号不应由前端传入。
7. 课程设计中的密码字段使用明文，这是明确取舍，DTO 中直接命名为 `password`。
