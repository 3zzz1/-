# 智慧审计平台系统

智慧审计平台系统基于 RuoYi-Vue 前后端分离框架开发。本仓库当前重点实现“审计整改管理（审计成果落地监督模块）”，用于统一管理审计、巡视巡察和专项督查发现的问题，跟踪整改任务从下发到销号的完整过程。

系统提供桌面端和移动端页面，并针对审计处、被审单位、校领导及中介机构等角色实施菜单、按钮和数据权限隔离。

## 核心功能

- 问题台账：维护内源、外源问题及责任单位、直接责任人、涉及金额、法规依据和整改期限。
- 任务下发：支持单个或批量下发整改任务，指定单位、联络员和截止日期，并生成整改通知书。
- 确认与分办：联络员确认接收任务后，将问题分办给具体整改责任人。
- 整改方案：整改责任人编制方案、保存草稿、提交方案，并申请延期或长期持续整改。
- 两级审批：延期和长期持续整改先由被审单位负责人审批，再由审计处长或项目组长审批。
- 材料管理：上传合同、凭证、制度文件、图片和其他整改佐证材料。
- 整改报告：根据整改方案和材料生成 Word 整改报告，由单位负责人审批。
- 销号管理：整改责任人申请销号，审计处审核通过后正式归档；审核不通过时进入二次整改。
- 进展时间线：记录下发、确认、分办、方案提交、审批、材料上传、报告提交和销号等操作。
- 整改分析：展示完成率、逾期率、问题类型、涉及金额、重复问题及高频风险领域。
- 消息通知：按角色推送站内待办消息，并支持企业微信应用消息。
- 移动端适配：任务卡片、搜索表单、审批弹窗、材料上传和统计页面适配手机屏幕。

## 技术栈

### 后端

- Java 17
- Spring Boot 4.0.6
- Spring Security
- MyBatis
- MySQL 8
- Redis
- Maven 3.9+
- Apache POI
- RuoYi 3.9.2

### 前端

- Vue 3.4
- Vite 5
- Element Plus 2.4
- Pinia
- Vue Router
- ECharts 5
- Axios

## 项目结构

```text
智慧审计平台系统/
├─ backend/                         后端 Maven 多模块工程
│  ├─ audit-rectification/          审计整改业务模块
│  ├─ ruoyi-admin/                  Spring Boot 启动模块
│  ├─ ruoyi-common/                 通用组件
│  ├─ ruoyi-framework/              权限、Token 和框架配置
│  ├─ ruoyi-quartz/                 定时任务模块
│  └─ ruoyi-system/                 用户、角色、菜单等系统模块
├─ frontend/                        Vue 3 前端工程
├─ sql_new/                         表结构、菜单、角色和修复 SQL
├─ scripts/                         本地启动脚本
├─ upload/                          整改材料本地上传目录
└─ 网页全流程测试数据.txt            手工网页测试数据
```

## 环境要求

- JDK 17 或更高版本
- Maven 3.9+
- Node.js 18+
- npm 9+
- MySQL 8.0
- Redis 6 或更高版本

本地默认端口：

| 服务 | 地址或端口 |
|---|---|
| 前端 | `http://localhost:80` |
| 后端 | `http://localhost:8080` |
| MySQL | `localhost:3306` |
| Redis | `localhost:6379` |

## 数据库

开发环境默认数据库为 `ry-vue`。连接配置位于：

```text
backend/ruoyi-admin/src/main/resources/application-druid.yml
```

当前仓库中的本地开发配置使用 `root / 1234`。该密码只能用于本地开发，部署前必须通过环境配置或独立配置文件替换，禁止在生产环境继续使用。

### SQL 文件说明

| 文件 | 用途 |
|---|---|
| `ry_20260417.sql` | RuoYi 基础表和基础数据 |
| `quartz.sql` | Quartz 定时任务表 |
| `02_rectification.sql` | 审计整改业务表 |
| `03_permission_extend.sql` | 时效授权和数据权限扩展表 |
| `04_rectification_menu.sql` | 整改模块菜单及按钮权限 |
| `05_init_roles_users.sql` | 主要部门、角色和演示账号 |
| `07_rectification_data_repairs.sql` | 重复报告、历史时间线数据修复 |
| `08_rectification_role_permissions.sql` | 整改角色菜单权限统一配置 |
| `09_init_rect_responsible_categories.sql` | 整改责任人职责分类 |
| `10_init_formal_user_names.sql` | 正式演示人员姓名 |

`07_rectification_data_repairs.sql` 主要用于已有数据库的数据修复，全新数据库通常不需要执行。

### 初始化说明

`sql_new/init_database.bat` 当前只执行 RuoYi 基础表、整改业务表、权限扩展表和菜单数据，不会完整创建所有演示人员和最终角色权限。

使用仓库现有的已初始化数据库时，不要重复执行带有 `DROP TABLE` 的基础 SQL。需要重建空数据库时，建议先备份数据，并按照依赖关系执行：

```text
1. ry_20260417.sql
2. quartz.sql
3. 02_rectification.sql
4. 04_rectification_menu.sql
5. 05_init_roles_users.sql
6. 03_permission_extend.sql
7. 08_rectification_role_permissions.sql
8. 09_init_rect_responsible_categories.sql
9. 10_init_formal_user_names.sql
```

注意：`09_init_rect_responsible_categories.sql` 依赖数据库中已经存在 `rect_responsible` 角色和对应整改人账号。若从完全空白数据库开始，应先通过系统管理创建具体整改责任人角色和各学院账号，再执行该文件。

## 启动项目

### 1. 启动 Redis

```powershell
redis-server
```

确认 Redis 可用：

```powershell
redis-cli ping
```

正常返回：

```text
PONG
```

### 2. 安装后端业务模块

首次运行或修改 `audit-rectification` 后，需要把最新业务模块安装到本地 Maven 仓库：

```powershell
cd backend
mvn -pl audit-rectification -am -DskipTests install
```

如果跳过这一步，`ruoyi-admin` 可能仍然加载本地仓库中的旧版整改 JAR。

### 3. 启动后端

```powershell
cd backend/ruoyi-admin
mvn spring-boot:run
```

后端启动后访问：

```text
http://localhost:8080/captchaImage
```

返回 HTTP 200 表示后端、MySQL 和 Redis 基本连通。

也可以从项目根目录运行：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/start-backend.ps1
```

### 4. 启动前端

首次运行先安装依赖：

```powershell
cd frontend
npm install
```

启动开发服务器：

```powershell
npm run dev
```

也可以从项目根目录运行：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/start-frontend.ps1
```

浏览器访问：

```text
http://localhost
```

## 演示账号

以下账号默认密码均为 `admin123`：

| 账号 | 姓名 | 角色 |
|---|---|---|
| `admin` | 系统管理员 | 系统配置和用户权限管理 |
| `director01` | 张明远 | 审计处长 |
| `lead01` | 陈志强 | 项目组长/主审 |
| `staff01` | 王海峰 | 普通审计人员 |
| `school01` | 李建国 | 校领导 |
| `unit01` | 李志华 | 经济管理学院负责人 |
| `link01` | 陈晓玲 | 经济管理学院联络员 |
| `rect01` | 王芳 | 经济管理学院整改责任人 |
| `agent01` | 高俊杰 | 中介机构人员 |

正式部署时，应删除或停用演示账号，并为实际人员创建独立账号。

## 主要角色职责

- 系统管理员：用户、角色、菜单、字典、参数和系统运行配置，不参与整改业务审批。
- 审计处长：全量整改业务、任务下发、方案变更终审、销号审核和整改分析。
- 项目组长/主审：问题维护、任务下发、整改评价、销号审核和数据分析。
- 普通审计人员：查看分配的任务、报告、材料、进展和统计数据。
- 校领导：查看全校整改汇总、最终报告和销号结果，不操作底层整改流程。
- 单位负责人：审批本单位整改报告、延期和长期持续整改申请。
- 联络员：确认接收、协调和分办本单位任务，不编辑整改方案和报告。
- 整改责任人：编制方案、上传材料、提交报告、申请延期及销号。
- 中介机构人员：在有效项目授权期内只读查看对应项目的问题、任务和进展。

## 标准业务流程

```text
审计处新增问题
  -> 审计处下发整改任务
  -> 联络员确认接收并分办
  -> 整改责任人编制方案和上传材料
  -> 整改责任人生成并提交报告
  -> 被审单位负责人审批报告
  -> 整改责任人申请销号
  -> 审计处审核
  -> 通过后正式销号，或驳回后进入二次整改
```

延期和长期持续整改采用两级审批：

```text
整改责任人申请
  -> 被审单位负责人审批
  -> 审计处长或项目组长审批
  -> 通过后新期限或长期整改状态正式生效
```

完整网页手工测试内容见：[网页全流程测试数据.txt](./网页全流程测试数据.txt)。

## 企业微信配置

企业微信配置位于 `application.yml` 的 `audit.wecom` 节点，推荐通过环境变量注入：

```powershell
$env:WECOM_ENABLED='true'
$env:WECOM_CORP_ID='企业ID'
$env:WECOM_AGENT_ID='应用AgentId'
$env:WECOM_SECRET='应用Secret'
$env:WECOM_APP_URL='手机可访问的平台地址'
```

企业微信应用主页地址不能使用手机无法访问的 `localhost`，应配置为局域网地址、校园网域名或 HTTPS 公网地址。不要把 `Secret` 提交到 Git。

## 文件上传

整改佐证材料由 `RectMaterialController` 保存到：

```text
<后端启动目录>/upload/material/
```

当前实现基于 `System.getProperty("user.dir")`，因此启动目录不同会导致物理文件位置不同。数据库的 `rect_material.file_path` 保存实际完整路径。

RuoYi 通用上传目录配置为：

```text
D:/ruoyi/uploadPath
```

删除材料数据库记录不会自动删除磁盘上的历史文件，清理文件前应先核对数据库引用。

## 构建与检查

后端编译：

```powershell
cd backend
mvn -pl audit-rectification -am -DskipTests compile
```

前端生产构建：

```powershell
cd frontend
npm run build:prod
```

构建产物位于 `frontend/dist`，该目录已被 `.gitignore` 忽略。

## 常见问题

### 页面提示拒绝连接

分别检查三个服务：

```powershell
redis-cli ping
curl http://localhost:8080/captchaImage
curl http://localhost:80
```

如果只有 `80` 无响应，通常是前端 Vite 进程退出；如果 `8080` 无响应，应检查 MySQL、Redis 和后端日志。

### 登录页验证码返回 500

优先检查：

1. Redis 是否启动并可执行 `PING`。
2. MySQL 的 `ry-vue` 数据库是否可连接。
3. `application-druid.yml` 中的数据库账号和密码是否正确。
4. 后端 `8080` 端口是否被其他进程占用。

### 修改后端代码但功能没有变化

整改模块以 Maven JAR 形式被 `ruoyi-admin` 引用。修改后执行：

```powershell
cd backend
mvn -pl audit-rectification -am -DskipTests install
```

然后重启后端。

### 修改角色权限后仍提示无权限

先确认角色菜单权限已经写入数据库，再退出并重新登录。登录用户的角色和权限会进入 Token/Redis 缓存，旧登录状态不会自动获得新权限。

### VS Code Java 代码无故爆红

在命令面板执行：

```text
Java: Clean Java Language Server Workspace
Maven: Reload Projects
```

然后重新执行 Maven 编译，确认不是实际编译错误。

## 安全说明

- 生产环境必须修改数据库密码、Token 密钥和演示账号密码。
- 企业微信 `Secret`、数据库密码等敏感信息应通过环境变量或密钥服务管理。
- 不要提交上传材料、日志、构建产物和本地缓存。
- 中介机构和临时授权必须设置有效期，并在项目结束后自动或手动回收。

## 基础框架

本项目基于 RuoYi-Vue 进行二次开发。RuoYi 原始框架说明保留在 [frontend/README.md](./frontend/README.md)。
