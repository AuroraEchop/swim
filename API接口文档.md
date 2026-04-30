# 简易航运公司管理系统 API 接口文档

## 1. 文档说明

本文档用于定义简易航运公司管理系统前后端分离开发所需的 RESTful API 接口。接口主要覆盖登录认证、用户管理、角色管理、船舶管理、船员管理、货物运输管理、财务结算管理、基础字典管理和首页统计等功能。

## 2. 接口基本约定

### 2.1 基础地址

开发环境基础地址：

```text
http://localhost:8080/api
```

生产环境基础地址根据部署环境调整。

### 2.2 数据格式

请求体和响应体统一使用 JSON 格式。

请求头：

```http
Content-Type: application/json
Accept: application/json
```

需要基础登录状态的接口可携带：

```http
Authorization: Bearer <loginToken>
```

说明：本课程设计中的 `loginToken` 只作为前后端分离调用时的简单登录标识。当前后端登录成功后返回 `demo-token-{username}-{id}` 格式的普通字符串，并在 `/auth/me` 和 `/auth/password` 中做简单解析。它不要求使用 JWT，不要求实现刷新 Token、复杂会话管理或生产级鉴权机制。

### 2.3 RESTful 命名规范

1. 资源统一使用名词复数形式，例如 `/ships`、`/crew-members`、`/transport-orders`。
2. 查询资源使用 `GET`。
3. 新增资源使用 `POST`。
4. 全量或主要字段更新使用 `PUT`。
5. 局部状态更新使用 `PATCH`。
6. 删除资源使用 `DELETE`。
7. 资源 ID 放在路径中，例如 `/ships/{id}`。
8. 查询条件通过 query string 传递，例如 `/ships?status=IDLE&page=1&pageSize=10`。

### 2.4 统一响应格式

除 `204 No Content` 外，接口响应体统一使用以下格式。`204 No Content` 表示操作成功但不返回响应体。

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

失败响应：

```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| code | number | 是 | 业务状态码，通常与 HTTP 状态码保持一致 |
| message | string | 是 | 响应提示信息 |
| data | any | 是 | 响应数据，失败时可为 `null` |

### 2.5 分页响应格式

分页查询统一返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "page": 1,
    "pageSize": 10,
    "total": 100,
    "totalPages": 10
  }
}
```

分页参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| page | number | 否 | 1 | 当前页码，从 1 开始 |
| pageSize | number | 否 | 10 | 每页数量，建议最大不超过 100 |

### 2.6 HTTP 状态码约定

| 状态码 | 说明 |
| --- | --- |
| 200 | 请求成功 |
| 201 | 创建成功 |
| 204 | 删除成功或无响应体 |
| 400 | 请求参数错误 |
| 401 | 未登录或登录已过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 409 | 数据冲突，例如编号重复 |
| 500 | 服务器内部错误 |

### 2.7 通用错误码

| code | message | 说明 |
| --- | --- | --- |
| 400 | 请求参数错误 | 参数格式不正确或必填项缺失 |
| 401 | 未登录或登录已过期 | Token 缺失、无效或过期 |
| 403 | 无权限访问 | 当前用户无对应操作权限 |
| 404 | 资源不存在 | 查询、修改或删除的数据不存在 |
| 409 | 数据已存在 | 唯一字段冲突，例如用户名、船舶编号重复 |
| 500 | 服务器内部错误 | 后端未知异常 |

### 2.8 课程设计安全简化约定

本项目是课程设计项目，接口设计以业务流程完整、前后端调用清晰、便于编码实现和答辩演示为目标。登录与密码处理采用明确的简化方案：

1. 用户登录只校验用户名和密码是否与数据库记录一致。
2. 用户表中的密码字段直接保存明文密码，不进行哈希、加盐、加密或密文比对。
3. 登录成功后返回的 `loginToken` 只用于前端保存登录状态和后续请求携带，不要求实现 JWT 标准。
4. 权限控制可按角色做基础菜单和按钮控制，不要求实现复杂权限中间件。
5. 以上设计是用户基于课程设计成本和收益权衡后的有意取舍，不视为本项目漏洞，也无需在本项目范围内优化。

后续数据库表结构设计中，用户表的 `password` 字段应明确标注为“明文密码，课程设计简化使用”。

## 3. 枚举定义

### 3.1 用户状态 UserStatus

| 值 | 说明 |
| --- | --- |
| ENABLED | 启用 |
| DISABLED | 禁用 |

### 3.2 船舶状态 ShipStatus

| 值 | 说明 |
| --- | --- |
| IDLE | 空闲 |
| SAILING | 运输中 |
| MAINTENANCE | 维修中 |
| DISABLED | 停用 |

### 3.3 船员状态 CrewStatus

| 值 | 说明 |
| --- | --- |
| ON_DUTY | 在岗 |
| ON_LEAVE | 休假 |
| UNASSIGNED | 待分配 |
| RESIGNED | 离职 |

### 3.4 运输状态 TransportStatus

| 值 | 说明 |
| --- | --- |
| PENDING | 待出发 |
| IN_TRANSIT | 运输中 |
| ARRIVED | 已到达 |
| CANCELLED | 已取消 |

### 3.5 结算状态 SettlementStatus

| 值 | 说明 |
| --- | --- |
| UNSETTLED | 未结算 |
| PARTIAL | 部分结算 |
| SETTLED | 已结算 |

## 4. 认证接口

### 4.1 用户登录

```http
POST /auth/login
```

接口说明：该接口采用课程设计简化登录逻辑。后端根据 `username` 查询用户记录，然后将请求中的 `password` 与数据库中保存的明文 `password` 直接比较。比较一致则登录成功，不一致则登录失败。该实现是本项目的明确设计取舍，不需要改造为密文密码校验。

请求体：

```json
{
  "username": "admin",
  "password": "123456"
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | string | 是 | 用户名，长度 3-30 |
| password | string | 是 | 明文密码，长度 6-30 |

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "loginToken": "demo-token-admin-1",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "roleCode": "ADMIN"
    }
  }
}
```

失败响应：

| 状态码 | 场景 |
| --- | --- |
| 400 | 用户名或密码为空 |
| 400 | 用户名或密码错误 |
| 400 | 用户已被禁用 |

### 4.2 获取当前登录用户

```http
GET /auth/me
```

接口说明：该接口用于前端刷新页面后获取当前用户信息。当前实现从 `Authorization: Bearer demo-token-{username}-{id}` 中解析用户名，再查询数据库中的用户记录。Token 缺失、格式错误或用户不存在时返回 `401`。

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "roleCode": "ADMIN",
    "permissions": [
      "user:read",
      "ship:read",
      "transport:update",
      "dictionary:manage"
    ]
  }
}
```

### 4.3 修改当前用户密码

```http
PUT /auth/password
```

请求体：

```json
{
  "oldPassword": "123456",
  "newPassword": "newPassword123"
}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

校验规则：

1. 旧密码与数据库中保存的明文密码一致。
2. 新密码长度为 6-30。
3. 新密码不能与旧密码相同。
4. 新密码直接以明文形式更新到数据库，不进行加密处理。
5. Token 缺失、格式错误或用户不存在时返回 `401`。

### 4.4 退出登录

```http
POST /auth/logout
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

## 5. 用户管理接口

### 5.1 分页查询用户列表

```http
GET /users
```

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | string | 否 | 按用户名模糊查询 |
| realName | string | 否 | 按真实姓名模糊查询 |
| roleId | number | 否 | 按角色 ID 查询 |
| status | string | 否 | 用户状态，见 UserStatus |
| page | number | 否 | 页码 |
| pageSize | number | 否 | 每页数量 |

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "phone": "13800000000",
        "roleId": 1,
        "roleName": "管理员",
        "status": "ENABLED",
        "createdAt": "2026-04-29 10:00:00"
      }
    ],
    "page": 1,
    "pageSize": 10,
    "total": 1,
    "totalPages": 1
  }
}
```

### 5.2 获取用户详情

```http
GET /users/{id}
```

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | number | 是 | 用户 ID |

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "phone": "13800000000",
    "email": "admin@example.com",
    "roleId": 1,
    "status": "ENABLED",
    "createdAt": "2026-04-29 10:00:00",
    "updatedAt": "2026-04-29 10:00:00"
  }
}
```

### 5.3 新增用户

```http
POST /users
```

请求体：

```json
{
  "username": "business01",
  "password": "123456",
  "realName": "业务员一号",
  "phone": "13800000001",
  "email": "business01@example.com",
  "roleId": 2,
  "status": "ENABLED"
}
```

成功响应：`201 Created`

```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "id": 2
  }
}
```

校验规则：

1. `username` 必填，长度 3-30，系统内唯一。
2. `password` 必填，长度 6-30，按课程设计简化方案直接明文保存到数据库。
3. `realName` 必填，长度 2-30。
4. `roleId` 必填，必须对应已存在角色。
5. `status` 必须为 UserStatus 枚举值。

### 5.4 修改用户

```http
PUT /users/{id}
```

请求体：

```json
{
  "realName": "业务管理员",
  "phone": "13800000001",
  "email": "business01@example.com",
  "roleId": 2,
  "status": "ENABLED"
}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "修改成功",
  "data": null
}
```

### 5.5 删除用户

```http
DELETE /users/{id}
```

成功响应：`204 No Content`

删除约束：

1. 不允许删除系统内置管理员账号。
2. 当前实现不做复杂会话识别，因此不额外判断“当前登录用户是否正在删除自己”。前端可根据当前用户 ID 隐藏自身删除按钮。

## 6. 角色管理接口

### 6.1 查询角色列表

```http
GET /roles
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "roleName": "管理员",
      "roleCode": "ADMIN",
      "description": "拥有系统全部权限"
    },
    {
      "id": 2,
      "roleName": "业务用户",
      "roleCode": "BUSINESS",
      "description": "负责业务数据维护"
    }
  ]
}
```

### 6.2 新增角色

```http
POST /roles
```

请求体：

```json
{
  "roleName": "查看用户",
  "roleCode": "VIEWER",
  "description": "仅允许查看业务数据",
  "permissions": [
    "ship:read",
    "crew:read",
    "transport:read",
    "settlement:read"
  ]
}
```

成功响应：`201 Created`

```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "id": 3
  }
}
```

### 6.3 修改角色

```http
PUT /roles/{id}
```

请求体：

```json
{
  "roleName": "业务管理员",
  "description": "负责业务数据维护",
  "permissions": [
    "ship:read",
    "ship:create",
    "ship:update",
    "crew:read"
  ]
}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "修改成功",
  "data": null
}
```

### 6.4 删除角色

```http
DELETE /roles/{id}
```

成功响应：`204 No Content`

删除约束：

1. 已被用户使用的角色不能删除。
2. 系统内置角色不能删除。

## 7. 船舶管理接口

### 7.1 分页查询船舶列表

```http
GET /ships
```

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| keyword | string | 否 | 按船名或船舶编号模糊查询 |
| type | string | 否 | 船舶类型 |
| homePort | string | 否 | 所属港口 |
| status | string | 否 | 船舶状态，见 ShipStatus |
| page | number | 否 | 页码 |
| pageSize | number | 否 | 每页数量 |

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "shipNo": "SHIP-001",
        "shipName": "远航一号",
        "shipType": "集装箱船",
        "loadCapacity": 50000.00,
        "homePort": "上海港",
        "status": "IDLE",
        "createdAt": "2026-04-29 10:00:00"
      }
    ],
    "page": 1,
    "pageSize": 10,
    "total": 1,
    "totalPages": 1
  }
}
```

### 7.2 获取船舶详情

```http
GET /ships/{id}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "shipNo": "SHIP-001",
    "shipName": "远航一号",
    "shipType": "集装箱船",
    "loadCapacity": 50000.00,
    "homePort": "上海港",
    "status": "IDLE",
    "remark": "主力运输船舶",
    "createdAt": "2026-04-29 10:00:00",
    "updatedAt": "2026-04-29 10:00:00"
  }
}
```

### 7.3 新增船舶

```http
POST /ships
```

请求体：

```json
{
  "shipNo": "SHIP-001",
  "shipName": "远航一号",
  "shipType": "集装箱船",
  "loadCapacity": 50000.00,
  "homePort": "上海港",
  "status": "IDLE",
  "remark": "主力运输船舶"
}
```

成功响应：`201 Created`

```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "id": 1
  }
}
```

校验规则：

1. `shipNo` 必填，长度 3-30，系统内唯一。
2. `shipName` 必填，长度 2-50。
3. `shipType` 必填。
4. `loadCapacity` 必填，必须大于 0，单位为吨。
5. `status` 必须为 ShipStatus 枚举值。

### 7.4 修改船舶

```http
PUT /ships/{id}
```

请求体：

```json
{
  "shipName": "远航一号",
  "shipType": "集装箱船",
  "loadCapacity": 52000.00,
  "homePort": "上海港",
  "status": "MAINTENANCE",
  "remark": "例行检修"
}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "修改成功",
  "data": null
}
```

### 7.5 修改船舶状态

```http
PATCH /ships/{id}/status
```

请求体：

```json
{
  "status": "IDLE"
}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "状态修改成功",
  "data": null
}
```

### 7.6 删除船舶

```http
DELETE /ships/{id}
```

成功响应：`204 No Content`

删除约束：

1. 正在执行运输任务的船舶不能删除。
2. 已关联船员或历史运输记录的船舶建议改为停用，不建议物理删除。

## 8. 船员管理接口

### 8.1 分页查询船员列表

```http
GET /crew-members
```

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| keyword | string | 否 | 按姓名或证件编号模糊查询 |
| position | string | 否 | 岗位 |
| shipId | number | 否 | 所属船舶 ID |
| status | string | 否 | 船员状态，见 CrewStatus |
| page | number | 否 | 页码 |
| pageSize | number | 否 | 每页数量 |

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "crewNo": "CREW-001",
        "name": "张三",
        "gender": "男",
        "phone": "13800000002",
        "certificateNo": "CERT-001",
        "position": "船长",
        "shipId": 1,
        "shipName": "远航一号",
        "status": "ON_DUTY"
      }
    ],
    "page": 1,
    "pageSize": 10,
    "total": 1,
    "totalPages": 1
  }
}
```

### 8.2 获取船员详情

```http
GET /crew-members/{id}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "crewNo": "CREW-001",
    "name": "张三",
    "gender": "男",
    "phone": "13800000002",
    "certificateNo": "CERT-001",
    "position": "船长",
    "shipId": 1,
    "shipName": "远航一号",
    "status": "ON_DUTY",
    "remark": "经验丰富",
    "createdAt": "2026-04-29 10:00:00",
    "updatedAt": "2026-04-29 10:00:00"
  }
}
```

### 8.3 新增船员

```http
POST /crew-members
```

请求体：

```json
{
  "crewNo": "CREW-001",
  "name": "张三",
  "gender": "男",
  "phone": "13800000002",
  "certificateNo": "CERT-001",
  "position": "船长",
  "shipId": 1,
  "status": "ON_DUTY",
  "remark": "经验丰富"
}
```

成功响应：`201 Created`

```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "id": 1
  }
}
```

校验规则：

1. `crewNo` 必填，系统内唯一。
2. `name` 必填，长度 2-30。
3. `certificateNo` 必填，系统内唯一。
4. `position` 必填。
5. `shipId` 非必填；填写时必须对应已存在船舶。
6. `status` 必须为 CrewStatus 枚举值。

### 8.4 修改船员

```http
PUT /crew-members/{id}
```

请求体：

```json
{
  "name": "张三",
  "gender": "男",
  "phone": "13800000002",
  "certificateNo": "CERT-001",
  "position": "大副",
  "shipId": 1,
  "status": "ON_DUTY",
  "remark": "岗位调整"
}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "修改成功",
  "data": null
}
```

### 8.5 修改船员状态

```http
PATCH /crew-members/{id}/status
```

请求体：

```json
{
  "status": "ON_LEAVE"
}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "状态修改成功",
  "data": null
}
```

### 8.6 删除船员

```http
DELETE /crew-members/{id}
```

成功响应：`204 No Content`

删除约束：

1. 已关联历史运输任务的船员建议改为离职，不建议物理删除。
2. 当前处于在岗状态且所属船舶正在运输中的船员不能删除。

## 9. 货物运输管理接口

### 9.1 分页查询运输任务列表

```http
GET /transport-orders
```

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| keyword | string | 否 | 按任务编号、货物名称或客户名称模糊查询 |
| shipId | number | 否 | 船舶 ID |
| originPort | string | 否 | 起运港 |
| destinationPort | string | 否 | 目的港 |
| status | string | 否 | 运输状态，见 TransportStatus |
| startDate | string | 否 | 预计出发开始日期，格式 `yyyy-MM-dd` |
| endDate | string | 否 | 预计出发结束日期，格式 `yyyy-MM-dd` |
| page | number | 否 | 页码 |
| pageSize | number | 否 | 每页数量 |

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "orderNo": "TRANS-20260429-001",
        "cargoName": "电子设备",
        "cargoType": "集装箱货物",
        "cargoWeight": 1200.50,
        "originPort": "上海港",
        "destinationPort": "深圳港",
        "shipId": 1,
        "shipName": "远航一号",
        "customerName": "上海某贸易有限公司",
        "plannedDepartureTime": "2026-05-01 08:00:00",
        "plannedArrivalTime": "2026-05-05 18:00:00",
        "status": "PENDING",
        "settlementStatus": "UNSETTLED"
      }
    ],
    "page": 1,
    "pageSize": 10,
    "total": 1,
    "totalPages": 1
  }
}
```

### 9.2 获取运输任务详情

```http
GET /transport-orders/{id}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "TRANS-20260429-001",
    "cargoName": "电子设备",
    "cargoType": "集装箱货物",
    "cargoWeight": 1200.50,
    "originPort": "上海港",
    "destinationPort": "深圳港",
    "shipId": 1,
    "shipName": "远航一号",
    "customerName": "上海某贸易有限公司",
    "customerPhone": "021-88888888",
    "plannedDepartureTime": "2026-05-01 08:00:00",
    "plannedArrivalTime": "2026-05-05 18:00:00",
    "actualDepartureTime": null,
    "actualArrivalTime": null,
    "status": "PENDING",
    "settlementId": null,
    "settlementStatus": "UNSETTLED",
    "remark": "注意防潮",
    "createdAt": "2026-04-29 10:00:00",
    "updatedAt": "2026-04-29 10:00:00"
  }
}
```

### 9.3 新增运输任务

```http
POST /transport-orders
```

请求体：

```json
{
  "cargoName": "电子设备",
  "cargoType": "集装箱货物",
  "cargoWeight": 1200.50,
  "originPort": "上海港",
  "destinationPort": "深圳港",
  "shipId": 1,
  "customerName": "上海某贸易有限公司",
  "customerPhone": "021-88888888",
  "plannedDepartureTime": "2026-05-01 08:00:00",
  "plannedArrivalTime": "2026-05-05 18:00:00",
  "remark": "注意防潮"
}
```

成功响应：`201 Created`

```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "id": 1,
    "orderNo": "TRANS-20260429-001"
  }
}
```

校验规则：

1. `cargoName` 必填，长度 2-100。
2. `cargoType` 必填。
3. `cargoWeight` 必填，必须大于 0，单位为吨。
4. `originPort` 和 `destinationPort` 必填，且不能相同。
5. `shipId` 必填，必须对应已存在且未停用的船舶。
6. `plannedArrivalTime` 必须晚于 `plannedDepartureTime`。
7. 新增运输任务默认状态为 `PENDING`。

业务规则：

1. 船舶处于 `MAINTENANCE` 或 `DISABLED` 状态时不能创建运输任务。
2. 船舶载重量不能小于货物重量。
3. 若船舶已存在时间冲突的未完成运输任务，应返回 `409`。

### 9.4 修改运输任务

```http
PUT /transport-orders/{id}
```

请求体：

```json
{
  "cargoName": "电子设备",
  "cargoType": "集装箱货物",
  "cargoWeight": 1200.50,
  "originPort": "上海港",
  "destinationPort": "深圳港",
  "shipId": 1,
  "customerName": "上海某贸易有限公司",
  "customerPhone": "021-88888888",
  "plannedDepartureTime": "2026-05-01 08:00:00",
  "plannedArrivalTime": "2026-05-05 18:00:00",
  "remark": "客户要求优先运输"
}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "修改成功",
  "data": null
}
```

修改约束：

1. 已到达或已取消的运输任务不允许修改核心运输信息。
2. 已结算的运输任务不允许修改货物重量、客户信息和船舶信息。

### 9.5 修改运输任务状态

```http
PATCH /transport-orders/{id}/status
```

请求体：

```json
{
  "status": "IN_TRANSIT",
  "actualDepartureTime": "2026-05-01 08:10:00",
  "actualArrivalTime": null
}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "状态修改成功",
  "data": null
}
```

状态流转规则：

1. `PENDING` 可变更为 `IN_TRANSIT` 或 `CANCELLED`。
2. `IN_TRANSIT` 可变更为 `ARRIVED`。
3. `ARRIVED` 和 `CANCELLED` 为终态，不允许继续变更。
4. 状态变为 `IN_TRANSIT` 时应记录实际出发时间。
5. 状态变为 `ARRIVED` 时应记录实际到达时间。

### 9.6 删除运输任务

```http
DELETE /transport-orders/{id}
```

成功响应：`204 No Content`

删除约束：

1. 只有 `PENDING` 或 `CANCELLED` 状态的运输任务允许删除。
2. 已生成结算记录的运输任务不允许删除。

## 10. 财务结算管理接口

### 10.1 分页查询结算记录

```http
GET /settlements
```

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| keyword | string | 否 | 按结算编号、运输任务编号或客户名称模糊查询 |
| transportOrderId | number | 否 | 运输任务 ID |
| customerName | string | 否 | 客户名称 |
| status | string | 否 | 结算状态，见 SettlementStatus |
| startDate | string | 否 | 创建开始日期，格式 `yyyy-MM-dd` |
| endDate | string | 否 | 创建结束日期，格式 `yyyy-MM-dd` |
| page | number | 否 | 页码 |
| pageSize | number | 否 | 每页数量 |

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "settlementNo": "SETTLE-20260429-001",
        "transportOrderId": 1,
        "transportOrderNo": "TRANS-20260429-001",
        "customerName": "上海某贸易有限公司",
        "freightAmount": 20000.00,
        "additionalFee": 1000.00,
        "discountAmount": 500.00,
        "receivableAmount": 20500.00,
        "receivedAmount": 10000.00,
        "status": "PARTIAL",
        "createdAt": "2026-04-29 10:00:00"
      }
    ],
    "page": 1,
    "pageSize": 10,
    "total": 1,
    "totalPages": 1
  }
}
```

### 10.2 获取结算详情

```http
GET /settlements/{id}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "settlementNo": "SETTLE-20260429-001",
    "transportOrderId": 1,
    "transportOrderNo": "TRANS-20260429-001",
    "customerName": "上海某贸易有限公司",
    "freightAmount": 20000.00,
    "additionalFee": 1000.00,
    "discountAmount": 500.00,
    "receivableAmount": 20500.00,
    "receivedAmount": 10000.00,
    "status": "PARTIAL",
    "settledAt": null,
    "remark": "已收部分款项",
    "createdAt": "2026-04-29 10:00:00",
    "updatedAt": "2026-04-29 10:00:00"
  }
}
```

### 10.3 新增结算记录

```http
POST /settlements
```

请求体：

```json
{
  "transportOrderId": 1,
  "freightAmount": 20000.00,
  "additionalFee": 1000.00,
  "discountAmount": 500.00,
  "receivedAmount": 0.00,
  "remark": "待客户付款"
}
```

成功响应：`201 Created`

```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "id": 1,
    "settlementNo": "SETTLE-20260429-001"
  }
}
```

计算规则：

```text
receivableAmount = freightAmount + additionalFee - discountAmount
```

校验规则：

1. `transportOrderId` 必填，必须对应已存在运输任务。
2. 一个运输任务只能创建一条结算记录。
3. `freightAmount` 必填，必须大于等于 0。
4. `additionalFee` 和 `discountAmount` 默认为 0，不能小于 0。
5. `receivedAmount` 不能小于 0，且不能大于 `receivableAmount`。
6. `receivedAmount` 为 0 时状态为 `UNSETTLED`。
7. `receivedAmount` 大于 0 且小于应收金额时状态为 `PARTIAL`。
8. `receivedAmount` 等于应收金额时状态为 `SETTLED`。

### 10.4 修改结算记录

```http
PUT /settlements/{id}
```

请求体：

```json
{
  "freightAmount": 20000.00,
  "additionalFee": 1000.00,
  "discountAmount": 500.00,
  "receivedAmount": 20500.00,
  "remark": "已完成付款"
}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "修改成功",
  "data": null
}
```

### 10.5 修改实收金额

```http
PATCH /settlements/{id}/payment
```

请求体：

```json
{
  "receivedAmount": 20500.00,
  "paymentTime": "2026-05-06 10:00:00"
}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "SETTLED",
    "settledAt": "2026-05-06 10:00:00"
  }
}
```

业务规则：

1. 后端根据实收金额自动计算结算状态。
2. 当状态变为 `SETTLED` 时记录结算完成时间。
3. 实收金额不能大于应收金额。

### 10.6 删除结算记录

```http
DELETE /settlements/{id}
```

成功响应：`204 No Content`

删除约束：

1. 已结算记录原则上不允许删除。
2. 仅允许删除未结算且录入错误的记录。

## 11. 基础字典接口

基础字典用于维护船舶类型、货物类型、港口信息、船员岗位等下拉选项。

### 11.1 查询字典项

```http
GET /dictionaries/{type}
```

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| type | string | 是 | 字典类型，例如 `SHIP_TYPE`、`CARGO_TYPE`、`PORT`、`CREW_POSITION` |

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "dictType": "SHIP_TYPE",
      "label": "集装箱船",
      "value": "CONTAINER_SHIP",
      "sort": 1,
      "enabled": true
    }
  ]
}
```

### 11.2 新增字典项

```http
POST /dictionaries
```

请求体：

```json
{
  "dictType": "PORT",
  "label": "上海港",
  "value": "SHANGHAI_PORT",
  "sort": 1,
  "enabled": true
}
```

成功响应：`201 Created`

```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "id": 1
  }
}
```

### 11.3 修改字典项

```http
PUT /dictionaries/{id}
```

请求体：

```json
{
  "label": "上海港",
  "value": "SHANGHAI_PORT",
  "sort": 1,
  "enabled": true
}
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "修改成功",
  "data": null
}
```

### 11.4 删除字典项

```http
DELETE /dictionaries/{id}
```

成功响应：`204 No Content`

删除约束：

1. 已被业务数据使用的字典项不建议删除。
2. 可通过 `enabled=false` 停用字典项。

## 12. 首页统计接口

### 12.1 获取首页统计数据

```http
GET /dashboard/summary
```

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "shipCount": 12,
    "crewCount": 86,
    "pendingTransportCount": 5,
    "inTransitCount": 3,
    "unsettledCount": 4,
    "totalReceivableAmount": 180000.00,
    "totalReceivedAmount": 120000.00
  }
}
```

### 12.2 获取近期运输任务

```http
GET /dashboard/recent-transport-orders
```

查询参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| limit | number | 否 | 5 | 返回数量 |

成功响应：`200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "orderNo": "TRANS-20260429-001",
      "cargoName": "电子设备",
      "shipName": "远航一号",
      "originPort": "上海港",
      "destinationPort": "深圳港",
      "status": "PENDING",
      "plannedDepartureTime": "2026-05-01 08:00:00"
    }
  ]
}
```

## 13. 权限建议

权限控制属于课程设计的辅助能力。若时间有限，可只实现管理员和普通用户两类角色，并在前端根据角色控制菜单显示；后端不要求实现复杂权限拦截器。以下权限标识用于后续扩展或答辩说明。

当前角色接口中的 `permissions` 字段允许前端提交，但数据库未设计权限明细表，后端不会落库保存。实际演示可根据 `roleCode` 控制菜单和按钮；`/auth/me` 会按角色编码返回一组固定权限标识，方便前端使用。

| 权限标识 | 说明 |
| --- | --- |
| user:read | 查看用户 |
| user:create | 新增用户 |
| user:update | 修改用户 |
| user:delete | 删除用户 |
| ship:read | 查看船舶 |
| ship:create | 新增船舶 |
| ship:update | 修改船舶 |
| ship:delete | 删除船舶 |
| crew:read | 查看船员 |
| crew:create | 新增船员 |
| crew:update | 修改船员 |
| crew:delete | 删除船员 |
| transport:read | 查看运输任务 |
| transport:create | 新增运输任务 |
| transport:update | 修改运输任务 |
| transport:delete | 删除运输任务 |
| settlement:read | 查看结算 |
| settlement:create | 新增结算 |
| settlement:update | 修改结算 |
| settlement:delete | 删除结算 |
| dictionary:manage | 管理基础字典 |

## 14. 前端调用注意事项

1. 前端应在登录成功后保存 `loginToken` 和当前用户基础信息，后续请求可统一放入 `Authorization` 请求头。
2. 接收到 `401` 时应清除本地登录信息并跳转登录页。
3. 接收到 `403` 时提示用户无权限操作。
4. 新增和修改表单应根据本文档校验规则进行前端预校验。
5. 删除操作必须弹出确认提示。
6. 列表页面应统一支持分页参数 `page` 和 `pageSize`。
7. 金额和重量字段前端显示时保留两位小数，提交时使用 number 类型。
8. 日期时间格式统一为 `yyyy-MM-dd HH:mm:ss`。
9. 用户密码输入框按普通明文密码字段提交给后端，后端直接与数据库明文密码比较；这是课程设计简化实现，不需要在前端额外做加密处理。

## 15. 接口实现优先级

课程设计开发可按以下顺序实现：

1. 登录认证接口：`/auth/login`、`/auth/me`。
2. 船舶管理接口：`/ships`。
3. 船员管理接口：`/crew-members`。
4. 运输任务接口：`/transport-orders`。
5. 财务结算接口：`/settlements`。
6. 首页统计接口：`/dashboard/summary`。
7. 用户、角色和字典管理接口。

若开发时间紧张，可以先完成核心业务模块的增删改查，再补充权限控制、字典维护和首页统计。
