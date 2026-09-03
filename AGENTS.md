# AGENTS.md

本文件为 AI 编码代理（Devin / Cursor / Windsurf 等）在本仓库中工作时的指引。
项目是基于 **RuoYi-Vue 3.9.0** 二次开发的「大仕城光学科技 — 镜检报工系统」，集成黑湖（BlackLake）MES 开放 API。

---

## 1. 项目概览

- **业务定位**：在若依通用后台之上，新增「镜检（Inspection）」业务模块，通过扫码对接黑湖 MES，完成镜检工序的扫码识别、不良项记录、批量良品/不良品报工与统计。
- **后端**：Java 8 + Spring Boot 2.5.15 + Spring Security 5.7 + MyBatis（PageHelper）+ Druid + Redis + JWT + Quartz + Swagger3（springfox）。
- **前端**：Vue 3.5 + Element Plus 2.9 + Vite 6 + Pinia 3 + Vue Router 4 + Axios + ECharts + sass-embedded。
- **数据库**：MySQL，库名 `dsc_admin`（开发），字符集 utf8。SQL 脚本位于 `sql/`：
  - `dsc.sql`：业务表（镜检主表/记录/报告/不良项、黑湖用户等）
  - `ry_20250522.sql`：若依基础表
  - `quartz.sql`：Quartz 定时任务表
- **构建产物**：后端 `ruoyi-admin/target/ruoyi-admin.jar`；前端 `dsc-ry-Vue3-master/dist`。

---

## 2. 仓库结构

```
kecheng-heihu/
├── pom.xml                     # 父 POM，多模块聚合
├── ruoyi-admin/                # 启动模块 + 业务 Controller（镜检/不良项/黑湖对接）
│   └── src/main/java/com/ruoyi/
│       ├── RuoYiApplication.java   # 启动类 @EnableScheduling
│       ├── apiTool/            # 黑湖 API 对接（token、报工、物料、工序、任务、仓库、定时同步）
│       ├── badItem/            # 不良项配置（动态建表 CreateBadItemsTable）
│       ├── inspection/         # 镜检主表/记录/报告/汇总（核心业务）
│       ├── userTools/          # 雪花 ID 生成器等用户工具
│       └── web/                # 若依自带 system/monitor/tool/common Controller + Swagger 配置
├── ruoyi-framework/            # 安全、AOP、数据源、拦截器、Web 服务
├── ruoyi-system/               # 系统管理 domain/mapper/service（用户/角色/菜单/字典/部门/岗位/配置/通知/日志）
├── ruoyi-quartz/               # 定时任务调度
├── ruoyi-generator/            # 代码生成器（Velocity 模板在 resources/vm）
├── ruoyi-common/               # 通用工具、注解、常量、枚举、异常、核心域模型、Redis、XSS 过滤
├── dsc-ry-Vue3-master/         # 前端工程（Vue3 + Vite）
│   └── src/
│       ├── api/                # 接口封装：inspection/ badItem/ blackLackApi/ system/ monitor/ tool/
│       ├── views/              # 页面：inspection/{record,report,summary} badItem system monitor tool
│       ├── components/ layout/ store/ router/ utils/ plugins/ directive/ assets/
├── sql/                        # 数据库脚本
├── doc/                        # 部署/使用手册
├── ry.bat / ry.sh              # jar 启动/停止/重启/状态脚本
└── README.md                   # 若依官方说明
```

### 2.1 后端模块依赖

`ruoyi-admin` → `ruoyi-framework` → `ruoyi-system` → `ruoyi-common`；`ruoyi-quartz`、`ruoyi-generator` 为可选功能模块，被 admin 引入。版本统一由父 POM 的 `<dependencyManagement>` 控制，**修改版本号必须改父 POM 的 `<properties>`**。

### 2.2 业务模块约定

新增业务模块一律放在 `ruoyi-admin/src/main/java/com/ruoyi/<业务名>/` 下，按 `controller / domain / mapper / service / service.impl` 分包；对应 Mapper XML 放在 `ruoyi-admin/src/main/resources/mapper/<业务名>/`。已有业务模块：

| 包名 | 说明 | RequestMapping 前缀 |
|------|------|---------------------|
| `inspection` | 镜检主表/记录/报告/汇总 | `/inspection/{summary,record,report}` |
| `badItem` | 不良项配置（动态建表） | `/badItem/...` |
| `apiTool` | 黑湖开放 API 对接 + 扫码报工入口 | `/blacklackUser/BlacklackUser/...` |

> `apiTool.controller.BlacklackUserController` 是扫码端的核心入口（`queryScanTaskResult`、`addOrReadInspectionMain`、`reportBadItemOne`、`reReportBadItemOne`），调用 `apiTool` 下的各 `Api*ForBlackLack` 服务与黑湖 API 通信。

---

## 3. 开发与运行

### 3.1 环境要求

- JDK **1.8**（父 POM 锁定 `java.version=1.8`，不要升级到 11+，否则启动失败）。
- Maven 3.6+，使用阿里云镜像（父 POM 已配置 `maven.aliyun.com`）。
- MySQL 5.7/8.0，Redis 5+。
- Node 18+，pnpm（前端 `pnpm-lock.yaml` 为准）。
- 文件上传目录：`ruoyi.profile=D:/ruoyi/uploadPath`（Windows）/ `/home/ruoyi/uploadPath`（Linux），见 `application.yml`。

### 3.2 后端

```bash
# 编译全部模块（在仓库根目录）
mvn clean install -DskipTests

# 启动 admin（开发）
cd ruoyi-admin && mvn spring-boot:run
# 或打包后用脚本
./ry.sh start    # Linux
ry.bat           # Windows
```

- 启动类：`com.ruoyi.RuoYiApplication`，端口 **8080**（`application.yml`）。
- 默认激活 `druid` profile，数据源在 `application-druid.yml`。
- Swagger：`http://localhost:8080/swagger-ui/index.html`，路径前缀 `/dev-api`。

### 3.3 前端

```bash
cd dsc-ry-Vue3-master
pnpm install
pnpm dev          # 开发，端口 80，代理 /dev-api -> http://localhost:8080
pnpm build:prod   # 生产构建到 dist/
pnpm build:stage  # 预发构建
```

- 开发环境标题：`大仕城光学科技`（`.env.development` 的 `VITE_APP_TITLE`）。
- 接口前缀：`VITE_APP_BASE_API=/dev-api`，由 Vite 代理转发到后端 8080。

### 3.4 数据库初始化

按顺序执行：`ry_20250522.sql` → `quartz.sql` → `dsc.sql`，并创建库 `dsc_admin`。修改 `application-druid.yml` 中的 `url / username / password`。

---

## 4. 代码规范

### 4.1 后端（Java）

- **包结构**：业务模块按 `controller / domain / mapper / service / service.impl` 分层；接口命名 `IXxxService`，实现 `XxxServiceImpl`。
- **Controller**：继承 `BaseController`；列表接口先 `startPage()` 再查，返回 `TableDataInfo`；增删改返回 `AjaxResult`；导出用 `ExcelUtil`。
- **权限**：使用 `@PreAuthorize("@ss.hasPermi('业务:资源:动作')")`，权限串需在菜单管理中维护。
- **日志**：增删改用 `@Log(title=..., businessType=BusinessType.{INSERT,UPDATE,DELETE,EXPORT})`。
- **MyBatis**：Mapper XML 与接口同名同包；resultMap + parameterType 全限定类名；动态 SQL 用 `<if>` / `<foreach>`；分页依赖 PageHelper，**不要在 SQL 里写 limit**。
- **事务**：跨表写操作加 `@Transactional(rollbackFor = Exception.class)`（参考 `BlacklackUserController.reportBadItemOne`）。
- **工具类**：优先复用 `ruoyi-common` 中的 `DateUtils`、`Convert`、`StringUtils`、`SecurityUtils`、`ExcelUtil` 等，不要引入重复工具类。
- **注释**：保留原有中文注释与 `@author / @date`；新增类按现有风格补 Javadoc。
- **不要**升级 Spring Boot / Spring Security / MyBatis 等大版本，会破坏若依兼容性。
- **不要**改动 `application.yml` 中已注释的端口/数据源行（保留为切换备份）。

### 4.2 前端（Vue3 + JS）

- **语言**：项目使用 **JavaScript**（非 TypeScript），`.eslintrc.js` 虽引入了 `@typescript-eslint`，但源码以 `.js`/`.vue` 为主，新增文件保持 JS。
- **ESLint + Prettier**：`semi:false, singleQuote:true, tabWidth:2, printWidth:100, trailingComma:'none'`，`vueIndentScriptAndStyle:true`。提交前确保 `pnpm lint` 通过（若配置了 lint 脚本）。
- **API 层**：每个业务在 `src/api/<业务>/<模块>.js` 中封装，统一 `import request from '@/utils/request'`，函数名 `listXxx / getXxx / addXxx / updateXxx / delXxx`。
- **页面**：放在 `src/views/<业务>/<模块>/index.vue`，使用 Element Plus + `<script setup>`（参考已有页面）。
- **路由/菜单**：由后端动态下发，前端不要硬编码业务菜单；新增页面需在后端「菜单管理」配置。
- **状态**：使用 Pinia（`src/store`），不要引入 vuex。
- **路径别名**：`@` → `src/`，`~` → 项目根（见 `vite.config.js`）。

---

## 5. 黑湖（BlackLake）对接要点

- **Token**：`AccessTokenService` 通过 `https://v3-ali.blacklake.cn/api/openapi/...` 获取 `access_token`，缓存到 Redis key `blacklake-zs:access_token`（1 小时）。**不要把 appKey/appSecret 提交到公开仓库**，当前 `AccessTokenService.java` 中硬编码的密钥应迁移到配置或环境变量。
- **API 封装**：`apiTool` 下每个 `Api*ForBlackLack.java` 对应一类黑湖接口（报工、报工记录、物料详情、工序列表、任务、用户、仓库明细）。
- **定时任务**：`MyScheduledTask` 使用 `@Scheduled`（非 Quartz），每分钟轮询执行 `ScheduleReport.goodReportBatch()` 进行批量报工；用 `AtomicBoolean` 防重入。`RuoYiApplication` 已加 `@EnableScheduling`。
- **扫码流程**：扫码 → `queryScanTaskResult` 查报工记录 + 校验镜检工序 → `addOrReadInspectionMain` 新建/读取主表 → `reportBadItemOne` 累加不良项 → 批量良品/不良品报工回黑湖。
- **配置项**：`warehouse_id`（仓库）、`debounce`（防抖时间）、前工序配置等通过 `sys_config` 表维护，用 `ISysConfigService.selectConfigByKey` 读取。

---

## 6. 验证清单

完成后请按以下顺序自检：

1. **后端编译**：`mvn -q clean compile -DskipTests`（根目录）无 ERROR。
2. **后端启动**：`ruoyi-admin` 能在 8080 启动，`/swagger-ui/index.html` 可访问。
3. **前端构建**：`cd dsc-ry-Vue3-master && pnpm install && pnpm build:prod` 无报错。
4. **数据库**：新加的表/字段在 `sql/dsc.sql` 中补对应 DDL；Mapper XML 与接口签名一致。
5. **权限**：新接口加了 `@PreAuthorize` 并在 `sys_menu` 配置了权限串。
6. **日志**：增删改接口加了 `@Log`。
7. **回归**：扫码 → 主表新建 → 不良项累加 → 批量报工链路未被破坏。

---

## 7. 提交与分支

- 提交信息中文描述「为什么」改，遵循若依社区风格；不要把数据库密码、黑湖 appSecret 写进提交信息或代码。
- 不要修改 `.gitignore` 中已忽略的 `target/`、`.idea/` 等规则。
- `application-druid.yml` 中的真实密码 `Kecheng123asd!@#` 与 Druid 控制台密码属于环境敏感信息，**改动时务必提示用户，不要擅自提交到远端**。

---

## 8. 常见坑

- 升级 Java 版本 → 编译报错（父 POM 锁 1.8）。
- 前端误用 TypeScript 语法 → ESLint/构建失败（项目是 JS）。
- 在 Mapper SQL 里手写 `limit` → 与 PageHelper 冲突，分页失效。
- 忘记 `@MapperScan`：`RuoYiApplication` 当前只扫 `com.ruoyi.apiTool.mapper`，新增模块的 Mapper 需确认是否被 MyBatis 扫描（若依通过 `MapperScan` 与 `mapperLocations:classpath*:mapper/**/*Mapper.xml` 覆盖，通常无需额外配置，但自定义接口需在已扫描包下）。
- 黑湖 token 过期未刷新 → 批量报工定时任务失败，检查 Redis key 与 `AccessTokenService` 日志。
- `inspection_summary` 主表的不良项字段 `defect1..defect20` 与前端 `no` 索引（0..19）一一对应，修改时注意偏移。
