# XhBookstore Agent Guide

本文件是仓库级智能体说明。处理本仓库任务时，先阅读本文件，再按任务需要阅读对应模块代码和 `docs/` 文档。

## 项目概览

- 项目是书城管理系统，后端为 Java 17、Spring Boot 3.5、Spring Security、MyBatis、Redis、JWT；前端为 Vue 2、Vue CLI、Vuex、Element UI。
- Maven 聚合版本为 `3.9.2`，根目录 `pom.xml` 管理所有后端模块。
- 管理后台后端入口：`xhbookstore-admin`，默认端口 `8090`。
- 小程序 API 入口：`xhbookstore-api`，默认端口 `8091`，基础路径 `/api/mp/v1`。
- 管理后台前端：`xhbookstore-ui`，通过后端动态菜单和权限生成路由。
- 核心业务位于 `xhbookstore-system`：会员、会员卡、积分、图书借阅/归还/借转购、门店和权限数据。

## 模块职责

- `xhbookstore-admin`：管理端启动类和 Controller。
- `xhbookstore-api`：小程序用户端、员工端接口，独立 JWT、身份校验、幂等和上传逻辑。
- `xhbookstore-system`：领域对象、Mapper、Service 和主要业务事务。
- `xhbookstore-framework`：管理端安全、数据源、Web 和框架配置。
- `xhbookstore-common`：通用模型、注解、异常和工具。
- `xhbookstore-quartz`：定时任务。
- `xhbookstore-generator`：代码生成器。
- `xhbookstore-ui`：Vue 2 管理后台。
- `sql`：初始化 SQL 和按日期命名的迁移脚本。
- `docs/admin`、`docs/api`：管理端和小程序需求、接口、数据结构及业务流程文档。

## 业务规则

- 会员手机号全局唯一；会员删除为逻辑注销，历史业务数据应保留。
- 会员编号为 11 位，按门店 ERP 编码或部门 ID 加门店内序列生成。
- 同一会员可购买多张会员卡，但同一时刻只能有一张生效；待生效卡按付款时间排队。
- 会员卡退款以付款时间计算 7 天期限，只允许在 admin 后台办理。
- 积分增减必须在事务中锁定会员行，并同时维护积分订单、入账/出账明细和余额。
- 借阅模块不维护 `book_info`、book history、图书导入和库存；借书由员工逐册填写图书编号和书名，每册一条明细且最多 3 张图片。
- 借阅、归还和借转购必须保证订单、逐册明细、图片绑定、状态和业务日志闭环；批量操作先完整预校验，避免部分成功。
- 借阅积分由统一积分服务根据 `BORROW_BOOK` 查询 `xhbs_points_rule`，按成功逐册明细数计算并使用借阅单号幂等发放。
- 小程序用户只能访问自己的数据；员工端数据必须受角色和部门数据范围限制。
- 写接口可使用 `Idempotency-Key` 防止重复提交。

## 前端约定

- 前端入口为 `xhbookstore-ui/src/main.js`，路由位于 `src/router`，权限加载位于 `src/permission.js` 和 `src/store/modules/permission.js`。
- 页面放在 `src/views`，请求封装放在 `src/api`；新增或修改接口时同步检查 Controller、前端 API 模块、页面和权限点。
- 管理端菜单主要由后端 `GET /getRouters` 动态返回，不要只修改前端静态路由。
- 测试环境使用 `.env.staging`，API 前缀为 `/stage-api`；生产环境使用 `.env.production`，前缀为 `/prod-api`。
- 不要将构建产物、`node_modules` 或环境密钥提交到仓库。

## 开发与验证

- 后端完整验证：`mvn clean package`。
- 后端定向测试：`mvn -pl xhbookstore-system,xhbookstore-api -am test`。
- 前端安装：存在可信且已跟踪的 lockfile 时使用 `npm ci`；否则使用 `npm install`。
- 前端测试环境构建：`npm run build:stage`。
- 前端生产构建：`npm run build:prod`。
- 修改业务事务时，优先补充或更新对应单元测试；当前重点测试位于借阅服务、JWT 过滤器和图片上传校验。
- 交付前至少检查相关测试、构建结果和 `git status`，不要覆盖用户未提交的修改。

## 数据库与文档

- 基础库结构位于 `sql/xhbookstore_20260417.sql`，新变更应新增可审阅的迁移脚本，不直接重写已执行的历史迁移。
- 部署代码不等于自动执行 SQL；涉及表结构、索引、菜单或权限时，要明确列出需执行的迁移脚本。
- 代码与文档不一致时，以当前代码和数据库迁移为事实依据，并同步修正文档。部分旧文档可能仍标注“待实现”，阅读时应与 Controller、Service 和测试交叉确认。
- 新增页面或业务接口时，同步维护页面需求、接口文档、数据结构、权限设计和业务流程中的相关内容。

## 部署边界

- 测试和生产环境必须使用各自 profile 与环境变量，不把数据库、Redis、微信或 COS 密钥写入代码、日志、提交或本文件。
- 测试服务器采用版本目录和软链接部署，服务器脚本为 `/www/server/java/projects/deploy.sh`；发布包名称为 `xhbookstore-admin.jar`、`xhbookstore-api.jar` 和真正 ZIP 格式的 `dist.zip`。
- 测试环境服务由 `spring_xhbookstore-admin`、`spring_xhbookstore-api` 管理，UI 由 Nginx 提供。
- 每次部署应使用唯一版本名，部署后检查版本软链接、服务状态、监听端口、Nginx 代理和近期错误日志。
- 未经用户明确授权，不连接、部署、重启或修改生产环境，也不执行生产数据库迁移。

## 工作方式

- 修改前先定位完整调用链：前端页面/API → Controller → Service → Mapper/XML → SQL/表结构。
- 优先做最小、可验证的改动，保留现有接口兼容性和事务边界。
- 不提交、暂存、推送或部署，除非用户明确要求。
- 不使用破坏性 Git 命令，不删除不明文件，不回滚用户已有改动。
