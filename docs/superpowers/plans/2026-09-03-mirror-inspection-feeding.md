# 镜检报工自动投料记录与手动重传 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 操作员点击现有“报工/完工”时，保留当前报工记录入队流程，同时为该条码和生产任务创建一条独立投料记录并自动上传黑湖；待上传或明确失败的投料记录可在新增“投料记录”页面手动重传。

**Architecture:** 使用当前工单扫码二维码查询库存，从库存响应取得原料、库存范围、数量和单位；使用库存物料 ID 与当前生产任务 ID 查询投料关系；组装并同步调用 `_bulk_feed`。投料记录独立保存在 `inspection_feed_record`，与 `inspection_summary` 关联；自动投料失败不阻断现有 `reportBatch` 入队和定时 `_progress_report`，成功记录禁止重复上传，网络超时等结果不确定场景标记为 `UNKNOWN` 并禁止直接重传。

**Tech Stack:** Java 8、Spring Boot 2.5.15、MyBatis、MySQL、OkHttp、Jackson、JUnit 5、Mockito、现有 Vue 3 前端。

## Global Constraints

- Excel 中第一个 `_bulk_feed` 是写接口；第二个库存接口和第三个投料关系接口是读接口。
- 固定调用顺序：创建待上传投料记录 → 库存查询 → 投料关系查询 → 批量投料 → 更新投料记录状态。
- 点击报工时，投料自动上传与现有 `reportBatch` 入队互不阻断；投料失败仍必须继续原报工入队流程。
- 库存查询的 `qrCodes` 使用当前工单扫码二维码，即 `inspection_summary.api_report.qrCode`。
- 投料数量 `opeAmount` 使用库存响应 `data.list[].amount.amount`，不让操作员手工填写。
- 投料单位 `opeUnitId` 使用库存响应 `data.list[].amount.unit.id`。
- 投料关系的 `materialId` 使用库存响应中的原料物料 ID；`taskId` 使用 `api_report.taskId`。
- 黑湖成功仅以业务响应 `code == 200` 判断，不依赖中文 `message`。
- Java 代码必须兼容 Java 8，数量统一使用 `BigDecimal`。
- 保留现有定时报工机制，不改成点击后同步调用 `_progress_report`。
- 新增独立 `inspection_feed_record` 表和“投料记录”管理页面，支持查询、查看详情和手动重传。
- 仅 `PENDING`、`FAILED` 允许手动重传；`SUCCESS` 禁止重传；`UNKNOWN` 必须人工核对后才能转为可重传状态。
- 同一 `summary_id + task_id + qr_code` 只创建一条投料记录，防止重复投料。
- 手动重传重新执行库存查询和投料关系查询，不直接复用可能过期的库存数量。
- 请求可能已到达黑湖但未收到响应时标记 `UNKNOWN`，禁止自动重试和直接手动重传。
- 本次不实现投料定时补传、不支持多标签投料。
- 不记录 access token、appKey、appSecret。

---

## Final Business Flow

```text
用户扫描当前工单二维码
→ queryScanTaskResult 获得 taskId、processId、lineId 等
→ addOrReadInspectionMain 将 taskId 和当前二维码写入 api_report
→ 用户进行镜检计数
→ 用户点击“报工/完工”
→ reportBatch 保留原有本地入队逻辑
    → 生成不良明细
    → inspection_summary.success_flag=0
→ 同一次点击触发投料编排服务（投料结果不回滚、不阻断上述报工入队）
    → 按 summaryId + taskId + qrCode 创建或读取唯一投料记录，初始状态 PENDING
    → 第二个接口：用 api_report.qrCode 查询库存
    → 第三个接口：用库存 materialId + api_report.taskId 查询投料关系
    → 保存本次 bulk_feed 请求快照
    → 第一个接口：把两个读接口的数据组装后写入 bulk_feed
    → code=200：投料记录更新为 SUCCESS
    → 明确业务失败：投料记录更新为 FAILED，可在投料记录页手动重传
    → 连接/读取超时且无法确认是否已执行：更新为 UNKNOWN，禁止直接重传
→ 原 MyScheduledTask / ScheduleReport 定时调用 _progress_report
```

投料失败不影响现有报工记录生成和定时报工。自动投料异常应转换为投料记录状态及可读错误信息，不得向上抛出导致 `reportBatch` 事务回滚；接口响应可携带投料状态提示，但报工入队成功仍按成功返回。手动重传仅处理投料记录，不重复创建或上传现有报工记录。

---

## Three BlackLake APIs

### 1. 写接口：批量投料

```text
POST /mfg/open/v1/feed/_bulk_feed
```

数据来源：

```text
feedKey                 ← 第三个投料关系接口
locationIds             ← 第二个库存接口 storageLocationId
qcStatuses              ← 第二个库存接口 qcStatus.code
batchNos                ← 第二个库存接口 bizKeyAttr.batchNo
inventoryElementId      ← 第二个库存接口 data.list[].id
inventoryIdentifier     ← 当前工单扫码二维码
 opeAmount              ← 第二个库存接口 amount.amount
 opeUnitId              ← 第二个库存接口 amount.unit.id
remark                  ← 固定“镜检投料”
skipWeakControlRule     ← SKIP_LOT_CONTROL、SKIP_EXPIRE_CHECK
taskId                  ← inspection_summary.api_report.taskId
```

### 2. 读接口：库存明细查询

```text
POST /inventory/open/v1/material_inventory/_list
```

请求：

```json
{
  "qrCodes": ["当前工单扫码二维码"]
}
```

必须读取：库存明细 ID、原料物料 ID、仓位、质量状态、批号、库存数量、单位 ID。

### 3. 读接口：获取投料关系

```text
POST /mfg/open/v1/feed/_get_feed_relation
```

请求：

```json
{
  "materialId": 1787651959776809,
  "taskId": 1787795203623449
}
```

`materialId` 来自第二个库存接口，`taskId` 来自当前镜检主记录。读取完整的：

```text
data.originalAlternativeMaterial.alternativeFeedKey
```

## Field Source and Required-Field Audit

### 第二个接口：库存查询入参

| 字段 | Excel 必填 | 当前流程要求 | 数据来源 | 是否纳入 |
|---|---|---|---|---|
| `qrCodes` | 否 | 是 | `inspection_summary.api_report.qrCode`，即进入镜检时扫描的当前工单二维码 | 是 |

虽然 Excel 将 `qrCodes` 标为非必填，但本项目必须靠它定位本次投料库存，因此按业务必传处理；不能为空、不能传前端临时值。

### 第三个接口：投料关系查询入参

| 字段 | Excel 必填 | 数据来源 | 是否纳入 |
|---|---|---|---|
| `materialId` | 是 | 第二个接口匹配库存明细中的原料物料 ID | 是 |
| `taskId` | 是 | `inspection_summary.api_report.taskId` | 是 |

库存响应中原料物料 ID 的精确 JSON 路径在 Excel 中没有给出，只能确定它来自 `data.list[]` 的物料信息。Task 9 联调时必须用真实脱敏响应确认路径，不能把当前镜检成品的 `api_report.materialId` 当成原料 `materialId`。

### 第一个接口：批量投料入参

| 字段 | Excel 必填 | 数据来源/赋值规则 | 是否纳入 |
|---|---|---|---|
| `feedItems` | 是 | 后端构造单元素数组；当前需求一次报工只处理当前二维码对应库存 | 是 |
| `feedItems[].feedKey` | 是 | 第三个接口 `data.originalAlternativeMaterial.alternativeFeedKey` 完整对象 | 是 |
| `feedKey.materialId` | 是 | 第三个接口返回值，禁止由本地重新拼接 | 是 |
| `feedKey.lineId` | 否 | 第三个接口返回值 | 是，原样保留 |
| `feedKey.seq` | 否 | 第三个接口返回值 | 是，原样保留，包括 `null` |
| `feedKey.subLineId` | 否 | 第三个接口返回值 | 是，原样保留，包括 `null` |
| `feedKey.originalAlternativeMaterialId` | 否 | 第三个接口返回值 | 是，原样保留 |
| `feedKey.originalAlternativeLineId` | 否 | 第三个接口返回值 | 是，原样保留 |
| `feedKey.originalAlternativeSubLineId` | 否 | 第三个接口返回值 | 是，原样保留，包括 `null` |
| `feedKey.priority` | 否 | 第三个接口返回值 | 是，原样保留，包括 `null` |
| `feedItems[].inventoryRange` | 是 | 后端根据第二个接口响应构造 | 是 |
| `inventoryRange.batchIdentifiers` | 否 | Excel 示例未提供来源 | 否，当前请求不传 |
| `inventoryRange.batchNoIds` | 否 | Excel 示例未提供来源 | 否，当前请求不传 |
| `inventoryRange.batchNos` | 否 | 第二个接口 `data.list[].bizKeyAttr.batchNo` | 是，非空时传 |
| `inventoryRange.bizKeyId` | 否 | Excel 示例未提供来源 | 否，当前请求不传 |
| `inventoryRange.inventoryElementId` | 否 | 第二个接口 `data.list[].id` | 是；Excel 备注建议明确库存场景都传 |
| `inventoryRange.inventoryIdentifier` | 否 | 当前工单扫码二维码 `api_report.qrCode` | 是；有码库存场景实际必须传 |
| `inventoryRange.locationIds` | 否 | 第二个接口 `data.list[].storageLocationId` | 是；Excel 备注说明出库必须传仓位 |
| `inventoryRange.qcStatuses` | 否 | 第二个接口 `data.list[].qcStatus.code` | 是 |
| `inventoryRange.version` | 否 | 第二个接口库存版本；Excel 示例没有传 | 否；联调确认黑湖是否要求乐观锁后再决定 |
| `feedItems[].opeAmountDetail` | 是 | 后端根据第二个接口数量和单位构造 | 是 |
| `opeAmountDetail.opeAmount` | 是 | 第二个接口 `data.list[].amount.amount` | 是 |
| `opeAmountDetail.opeUnitId` | 是 | 第二个接口 `data.list[].amount.unit.id` | 是 |
| `feedItems[].remark` | 否 | 固定字符串 `镜检投料` | 是 |
| `skipWeakControlRule` | 否 | 固定 `SKIP_LOT_CONTROL`、`SKIP_EXPIRE_CHECK` | 是 |
| `taskId` | 是 | `inspection_summary.api_report.taskId` | 是 |

### 必传字段结论

Excel 标记为必填的字段已经全部纳入计划：

```text
feedItems
feedItems[].feedKey
feedKey.materialId
feedItems[].inventoryRange
feedItems[].opeAmountDetail
opeAmountDetail.opeAmount
opeAmountDetail.opeUnitId
taskId
```

此外，虽然 Excel 标记为非必填，但当前有码库存投料场景仍纳入以下业务必传字段：

```text
qrCodes
inventoryRange.locationIds
inventoryRange.inventoryElementId
inventoryRange.inventoryIdentifier
inventoryRange.qcStatuses
```

`batchNos` 有值时传；`batchIdentifiers`、`batchNoIds`、`bizKeyId` 当前无来源且非必填，不传；`version` 等待真实接口联调确认。

### `feedKey` 完整性要求

Excel 明确要求使用第三个接口返回的 `alternativeFeedKey` 完整对象。实现时不能只挑选已知字段后重新组装，否则黑湖后续增加或按场景返回额外属性时可能丢字段。`FeedKey` 必须通过以下一种方式保真：

1. 直接将 `alternativeFeedKey` 保存为 `JsonNode` 并原样写入 `_bulk_feed`；推荐。
2. 若使用 Java DTO，则通过 `@JsonAnySetter` 保存未知字段，并验证序列化后字段集合与原响应一致。

对应单元测试必须比较输入和输出 `feedKey` 的完整 JSON 树，而不只是断言 `materialId`、`lineId` 等已知字段。

---

## File Structure

### New backend files

```text
ruoyi-admin/src/main/java/com/ruoyi/apiTool/client/BlackLakeApiClient.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/client/MaterialInventoryClient.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/client/FeedRelationClient.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/client/BulkFeedClient.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/domain/feed/BlackLakeResult.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/domain/feed/InventoryDetail.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/domain/feed/BulkFeedRequest.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/domain/feed/InspectionFeedRecord.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/domain/feed/ReportBatchResult.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/mapper/InspectionFeedRecordMapper.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/service/IInspectionFeedRecordService.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/service/impl/InspectionFeedRecordServiceImpl.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/service/FeedUploadService.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/service/ReportBatchEnqueueService.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/controller/InspectionFeedRecordController.java
ruoyi-admin/src/main/resources/mapper/apiTool/InspectionFeedRecordMapper.xml
```

### Modified backend files

```text
ruoyi-admin/pom.xml
ruoyi-admin/src/main/java/com/ruoyi/apiTool/controller/BlacklackUserController.java
sql/dsc.sql
```

### New frontend files

```text
dsc-ry-Vue3-master/src/api/inspection/feedRecord.js
dsc-ry-Vue3-master/src/views/inspection/feedRecord/index.vue
```

投料记录菜单由后端 `sys_menu` 动态下发；迁移脚本增加菜单及 `inspection:feedRecord:list/query/retry` 权限，不在前端硬编码路由。

### New migration and tests

```text
sql/20260903_add_inspection_feed_record.sql
ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/BlackLakeApiClientTest.java
ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/MaterialInventoryClientTest.java
ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/FeedRelationClientTest.java
ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/BulkFeedClientTest.java
ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/InspectionFeedRecordServiceTest.java
ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/FeedUploadServiceTest.java
ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/ReportBatchFeedIntegrationTest.java
ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/InspectionFeedRecordControllerTest.java
ruoyi-admin/src/test/resources/blacklake/inventory-success.json
ruoyi-admin/src/test/resources/blacklake/inventory-empty.json
ruoyi-admin/src/test/resources/blacklake/feed-relation-success.json
ruoyi-admin/src/test/resources/blacklake/feed-relation-empty.json
ruoyi-admin/src/test/resources/blacklake/bulk-feed-success.json
ruoyi-admin/src/test/resources/blacklake/bulk-feed-failed.json
```

### Explicitly unchanged

```text
ruoyi-admin/src/main/java/com/ruoyi/apiTool/MyScheduledTask.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/ScheduleReport.java
```

现有 `inspection_summary`、`inspection_report` 和对应报工重传逻辑不承载投料状态；投料生命周期全部由 `inspection_feed_record` 管理。

---

### Task 1: 建立测试基础和统一黑湖客户端

**Files:**
- Modify: `ruoyi-admin/pom.xml`
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/client/BlackLakeApiClient.java`
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/domain/feed/BlackLakeResult.java`
- Test: `ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/BlackLakeApiClientTest.java`

**Interfaces:**
- Consumes: `AccessTokenService.getAccessToken(boolean)`、OkHttp、Jackson。
- Produces: `JsonNode post(String path, Object requestBody)`。

- [ ] **Step 1: 增加测试依赖**

在 `ruoyi-admin/pom.xml` 增加：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: 写统一客户端失败测试**

覆盖正常响应、HTTP 非 2xx、黑湖业务码非 200、Token 失效后仅重试一次、连接失败、读取超时。使用可注入的 OkHttp `Call.Factory`，测试不得访问真实黑湖。

核心断言：

```java
assertEquals(200, result.path("code").asInt());
verify(accessTokenService, times(1)).getAccessToken(false);
```

- [ ] **Step 3: 运行测试并确认失败**

```bash
mvn -pl ruoyi-admin -am -Dtest=BlackLakeApiClientTest test
```

Expected: FAIL，因为客户端尚未实现。

- [ ] **Step 4: 实现统一客户端**

核心 Token 重试逻辑：

```java
public JsonNode post(String path, Object requestBody) {
    return post(path, requestBody, false);
}

private JsonNode post(String path, Object requestBody, boolean retried) {
    String token = accessTokenService.getAccessToken(true);
    JsonNode response = execute(path, requestBody, token);
    if (isTokenExpired(response) && !retried) {
        accessTokenService.getAccessToken(false);
        return post(path, requestBody, true);
    }
    return response;
}
```

不复制现有客户端中“刷新 Token 后递归调用但丢弃返回值”的错误。基础 URL 不包含换行；复用单例 OkHttpClient。

- [ ] **Step 5: 运行测试**

```bash
mvn -pl ruoyi-admin -am -Dtest=BlackLakeApiClientTest test
```

Expected: PASS。

- [ ] **Step 6: 提交**

Commit subject: `feat: 统一黑湖接口调用与令牌重试`

---

### Task 2: 实现第二个库存读取接口

**Files:**
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/client/MaterialInventoryClient.java`
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/domain/feed/InventoryDetail.java`
- Test: `ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/MaterialInventoryClientTest.java`
- Test resources: `inventory-success.json`、`inventory-empty.json`

**Interfaces:**
- Consumes: `BlackLakeApiClient.post`。
- Produces: `InventoryDetail queryByQrCode(String qrCode)`。

- [ ] **Step 1: 写库存响应解析失败测试**

至少断言：

```java
assertEquals(Long.valueOf(1787795331084898L), result.getInventoryElementId());
assertEquals(Long.valueOf(1783078034914734L), result.getStorageLocationId());
assertEquals(Integer.valueOf(1), result.getQcStatus());
assertEquals("CS0824001", result.getBatchNo());
assertEquals(new BigDecimal("3"), result.getAmount());
assertEquals(Long.valueOf(1749025381704132L), result.getUnitId());
```

同时断言原料 `materialId` 被正确提取，供第三个接口使用。

- [ ] **Step 2: 运行测试并确认失败**

```bash
mvn -pl ruoyi-admin -am -Dtest=MaterialInventoryClientTest test
```

Expected: FAIL。

- [ ] **Step 3: 实现库存查询**

请求：

```java
Map<String, Object> body = new HashMap<>();
body.put("qrCodes", Collections.singletonList(qrCode));
```

只接受与当前二维码精确匹配的库存明细。空列表、无法唯一匹配、库存数量小于等于零均返回明确错误。数量使用 `BigDecimal`。

现有 `ApiWareHouseDetail` 暂不删除，新增报工投料逻辑不再依赖它。

- [ ] **Step 4: 运行测试**

```bash
mvn -pl ruoyi-admin -am -Dtest=MaterialInventoryClientTest test
```

Expected: PASS。

- [ ] **Step 5: 提交**

Commit subject: `feat: 查询报工二维码库存明细`

---

### Task 3: 实现第三个投料关系读取接口

**Files:**
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/client/FeedRelationClient.java`
- Test: `ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/FeedRelationClientTest.java`
- Test resources: `feed-relation-success.json`、`feed-relation-empty.json`

**Interfaces:**
- Consumes: 库存响应 `materialId`、镜检主记录 `taskId`。
- Produces: `JsonNode getFeedRelation(Long materialId, Long taskId)`，返回完整 `alternativeFeedKey` JSON 对象。

- [ ] **Step 1: 写投料关系解析失败测试**

断言完整保留：

```text
materialId
seq
lineId
subLineId
originalAlternativeMaterialId
originalAlternativeLineId
originalAlternativeSubLineId
priority
```

- [ ] **Step 2: 运行测试并确认失败**

```bash
mvn -pl ruoyi-admin -am -Dtest=FeedRelationClientTest test
```

Expected: FAIL。

- [ ] **Step 3: 实现关系查询**

请求体：

```java
Map<String, Object> body = new HashMap<>();
body.put("materialId", materialId);
body.put("taskId", taskId);
```

从 `data.originalAlternativeMaterial.alternativeFeedKey` 读取完整 `JsonNode`，不转换成会丢失未知字段的固定 DTO。节点不存在时返回“当前库存物料与镜检生产任务不存在投料关系”。

- [ ] **Step 4: 运行测试**

```bash
mvn -pl ruoyi-admin -am -Dtest=FeedRelationClientTest test
```

Expected: PASS。

- [ ] **Step 5: 提交**

Commit subject: `feat: 查询镜检任务投料关系`

---

### Task 4: 实现第一个批量投料写接口

**Files:**
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/client/BulkFeedClient.java`
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/domain/feed/BulkFeedRequest.java`
- Test: `ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/BulkFeedClientTest.java`
- Test resources: `bulk-feed-success.json`、`bulk-feed-failed.json`

**Interfaces:**
- Consumes: `InventoryDetail`、完整 `alternativeFeedKey` JsonNode、当前 `taskId` 和二维码。
- Produces: `BlackLakeResult bulkFeed(BulkFeedRequest request)`。

- [ ] **Step 1: 写请求组装失败测试**

验证请求体：

```json
{
  "feedItems": [
    {
      "feedKey": {},
      "inventoryRange": {
        "locationIds": [],
        "qcStatuses": [],
        "batchNos": [],
        "inventoryElementId": 0,
        "inventoryIdentifier": ""
      },
      "opeAmountDetail": {
        "opeAmount": 0,
        "opeUnitId": 0
      },
      "remark": "镜检投料"
    }
  ],
  "skipWeakControlRule": ["SKIP_LOT_CONTROL", "SKIP_EXPIRE_CHECK"],
  "taskId": 0
}
```

测试中的零值仅表示结构位置；实际组装必须全部取自第二、第三个接口及当前镜检主记录。批号为空时不发送空批号数组。

- [ ] **Step 2: 运行测试并确认失败**

```bash
mvn -pl ruoyi-admin -am -Dtest=BulkFeedClientTest test
```

Expected: FAIL。

- [ ] **Step 3: 实现批量投料**

`opeAmount` 必须直接使用 `InventoryDetail.amount`；不得使用镜检良品数、总数或前端参数。成功只检查黑湖 `code == 200`。

- [ ] **Step 4: 运行测试**

```bash
mvn -pl ruoyi-admin -am -Dtest=BulkFeedClientTest test
```

Expected: PASS。

- [ ] **Step 5: 提交**

Commit subject: `feat: 写入黑湖镜检投料`

---

### Task 5: 建立独立投料记录与状态机

**Files:**
- Create: `sql/20260903_add_inspection_feed_record.sql`
- Modify: `sql/dsc.sql`
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/domain/feed/InspectionFeedRecord.java`
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/mapper/InspectionFeedRecordMapper.java`
- Create: `ruoyi-admin/src/main/resources/mapper/apiTool/InspectionFeedRecordMapper.xml`
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/service/IInspectionFeedRecordService.java`
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/service/impl/InspectionFeedRecordServiceImpl.java`
- Test: `ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/InspectionFeedRecordServiceTest.java`

**Interfaces:**
- Consumes: `InspectionSummary.id`、`api_report.qrCode`、`api_report.taskId`。
- Produces: `InspectionFeedRecord getOrCreate(Long summaryId, String qrCode, Long taskId)`、状态抢占和结果更新方法。

- [ ] **Step 1: 写迁移脚本和完整建表定义**

```sql
CREATE TABLE `inspection_feed_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '投料记录ID',
  `summary_id` bigint NOT NULL COMMENT '镜检主记录ID',
  `qr_code` varchar(256) NOT NULL COMMENT '扫码二维码',
  `work_order_code` varchar(64) DEFAULT NULL COMMENT '工单号',
  `task_id` bigint NOT NULL COMMENT '黑湖生产任务ID',
  `material_id` bigint DEFAULT NULL COMMENT '库存原料物料ID',
  `inventory_element_id` bigint DEFAULT NULL COMMENT '库存明细ID',
  `feed_amount` decimal(20,6) DEFAULT NULL COMMENT '投料数量',
  `unit_id` bigint DEFAULT NULL COMMENT '投料单位ID',
  `request_json` longtext COMMENT '最近一次批量投料请求JSON',
  `response_json` longtext COMMENT '最近一次黑湖响应JSON',
  `upload_status` varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/SUCCESS/FAILED/UNKNOWN',
  `error_message` varchar(1000) DEFAULT NULL COMMENT '失败原因',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '手动重传次数',
  `feed_time` datetime DEFAULT NULL COMMENT '上传成功时间',
  `create_by` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feed_summary_task_qr` (`summary_id`,`task_id`,`qr_code`),
  KEY `idx_feed_status_time` (`upload_status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='镜检投料上传记录';
```

迁移脚本同时插入“投料记录”菜单和 `inspection:feedRecord:list/query/retry` 权限；菜单父级沿用现有镜检管理目录的实际 `menu_id`，执行前通过 SQL 查询确认，不硬编码未经核对的 ID。

- [ ] **Step 2: 写 Mapper 状态机失败测试**

覆盖唯一记录创建、并发抢占、成功禁止抢占、`UNKNOWN` 禁止抢占，以及仅 `PENDING/FAILED` 可以从页面重传。

- [ ] **Step 3: 运行测试并确认失败**

```bash
mvn -pl ruoyi-admin -am -Dtest=InspectionFeedRecordServiceTest test
```

Expected: FAIL，因为记录实体、Mapper 和服务尚未实现。

- [ ] **Step 4: 实现记录实体与 Mapper**

Mapper 接口至少提供：

```java
InspectionFeedRecord selectById(Long id);
InspectionFeedRecord selectByBusinessKey(Long summaryId, Long taskId, String qrCode);
List<InspectionFeedRecord> selectList(InspectionFeedRecord query);
int insert(InspectionFeedRecord record);
int claimAutoUpload(Long id);
int claimManualRetry(Long id);
int markSuccess(Long id, String requestJson, String responseJson, Date feedTime);
int markFailed(Long id, String requestJson, String responseJson, String errorMessage);
int markUnknown(Long id, String requestJson, String responseJson, String errorMessage);
```

自动抢占条件：

```sql
UPDATE inspection_feed_record
SET upload_status = 'PROCESSING', error_message = NULL
WHERE id = #{id} AND upload_status = 'PENDING'
```

手动抢占条件：

```sql
UPDATE inspection_feed_record
SET upload_status = 'PROCESSING', retry_count = retry_count + 1, error_message = NULL
WHERE id = #{id} AND upload_status IN ('PENDING', 'FAILED')
```

- [ ] **Step 5: 实现独立记录事务服务**

`getOrCreate`、抢占和状态落库使用独立 Spring Bean；状态写入使用 `Propagation.REQUIRES_NEW`，保证投料失败状态不会被 `reportBatch` 的事务回滚。并发插入遇到唯一键冲突时重新按业务键查询，不创建第二条投料记录。

- [ ] **Step 6: 运行测试并提交**

```bash
mvn -pl ruoyi-admin -am -Dtest=InspectionFeedRecordServiceTest test
```

Expected: 状态机相关测试 PASS。

Commit subject: `feat: 增加镜检投料上传记录`

---

### Task 6: 实现自动投料和手动重传编排

**Files:**
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/service/FeedUploadService.java`
- Modify: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/service/impl/InspectionFeedRecordServiceImpl.java`
- Test: `ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/FeedUploadServiceTest.java`

**Interfaces:**
- Consumes: `MaterialInventoryClient`、`FeedRelationClient`、`BulkFeedClient`、`IInspectionFeedRecordService`。
- Produces: `InspectionFeedRecord autoUpload(Long summaryId)`、`InspectionFeedRecord manualRetry(Long recordId)`。

- [ ] **Step 1: 写自动上传和手动重传失败测试**

覆盖：

1. 自动上传先创建 `PENDING` 记录，再依次调用库存、关系、批量投料。
2. `code=200` 保存 `SUCCESS`、请求响应和投料时间。
3. 库存为空、关系不存在、黑湖明确业务拒绝保存 `FAILED`。
4. 连接失败且确认请求未发送保存 `FAILED`。
5. 写请求发送后读取超时保存 `UNKNOWN`。
6. `SUCCESS`、`PROCESSING`、`UNKNOWN` 均禁止手动重传。
7. `PENDING`、`FAILED` 手动重传时重新查询库存和投料关系，并增加 `retry_count`。
8. 同一业务键重复触发只复用原记录，不再次上传成功记录。

- [ ] **Step 2: 运行测试并确认失败**

```bash
mvn -pl ruoyi-admin -am -Dtest=FeedUploadServiceTest test
```

Expected: FAIL，因为编排服务尚未实现。

- [ ] **Step 3: 实现统一上传方法**

```java
private InspectionFeedRecord upload(InspectionFeedRecord record, boolean manual) {
    boolean claimed = manual
        ? feedRecordService.claimManualRetry(record.getId())
        : feedRecordService.claimAutoUpload(record.getId());
    if (!claimed) {
        return feedRecordService.selectById(record.getId());
    }

    InventoryDetail inventory = materialInventoryClient.queryByQrCode(record.getQrCode());
    JsonNode feedKey = feedRelationClient.getFeedRelation(inventory.getMaterialId(), record.getTaskId());
    BulkFeedRequest request = buildRequest(record.getTaskId(), record.getQrCode(), inventory, feedKey);
    BlackLakeResult result = bulkFeedClient.bulkFeed(request);
    feedRecordService.markSuccess(record.getId(), inventory, request, result);
    return feedRecordService.selectById(record.getId());
}
```

明确业务异常保存 `FAILED`；只有无法确认 `_bulk_feed` 是否已被黑湖执行的异常保存 `UNKNOWN`。异常处理完成后返回记录状态，不删除记录。

- [ ] **Step 4: 实现自动入口和重传入口**

```java
public InspectionFeedRecord autoUpload(Long summaryId) {
    InspectionSummary summary = inspectionSummaryService.selectInspectionSummaryByIdNoDetailAndBadItems(summaryId);
    Map<String, Object> reportInfo = parseApiReport(summary.getApiReport());
    String qrCode = requiredText(reportInfo, "qrCode");
    Long taskId = requiredLong(reportInfo, "taskId");
    InspectionFeedRecord record = feedRecordService.getOrCreate(summary, qrCode, taskId);
    return upload(record, false);
}

public InspectionFeedRecord manualRetry(Long recordId) {
    InspectionFeedRecord record = feedRecordService.selectById(recordId);
    if (record == null) {
        throw new ServiceException("投料记录不存在");
    }
    return upload(record, true);
}
```

- [ ] **Step 5: 运行测试并提交**

```bash
mvn -pl ruoyi-admin -am -Dtest=FeedUploadServiceTest test
```

Expected: PASS。

Commit subject: `feat: 编排自动投料与手动重传`

---

### Task 7: 接入现有 reportBatch 且不阻断报工

**Files:**
- Modify: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/controller/BlacklackUserController.java`
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/domain/feed/ReportBatchResult.java`
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/service/ReportBatchEnqueueService.java`
- Test: `ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/ReportBatchFeedIntegrationTest.java`
- Verify unchanged: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/MyScheduledTask.java`
- Verify unchanged: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/ScheduleReport.java`

**Interfaces:**
- Consumes: `FeedUploadService.autoUpload(Long summaryId)`。
- Produces: 点击现有 `/blacklackUser/BlacklackUser/reportBatch` 后，原报工入队成功且返回本次投料状态。

- [ ] **Step 1: 写 Controller 集成失败测试**

覆盖：

1. 投料成功时原 `success_flag=0` 和 `inspection_report` 入队逻辑保持不变。
2. 投料明确失败时原报工仍入队，投料记录为 `FAILED`。
3. 投料状态 `UNKNOWN` 时原报工仍入队，且不再次调用 `_bulk_feed`。
4. 投料编排出现未预期异常时记录错误日志，但不得回滚已创建的报工记录。
5. 已存在 `SUCCESS` 投料记录时重复进入不再次投料。

- [ ] **Step 2: 运行测试并确认失败**

```bash
mvn -pl ruoyi-admin -am -Dtest=ReportBatchFeedIntegrationTest test
```

Expected: FAIL，因为 `reportBatch` 尚未触发独立投料记录上传。

- [ ] **Step 3: 保留原事务并在提交后触发投料**

不要在当前 `reportBatch` 事务内部直接同步调用黑湖后再返回。将现有入队逻辑提取到事务 Service，Controller 在入队事务成功提交后调用：

```java
ReportBatchResult reportResult = reportBatchEnqueueService.enqueue(params);
InspectionFeedRecord feedRecord = null;
String feedMessage = null;
try {
    feedRecord = feedUploadService.autoUpload(reportResult.getSummaryId());
} catch (Exception e) {
    feedMessage = e.getMessage();
    log.error("自动投料异常，summaryId={}", reportResult.getSummaryId(), e);
}
return AjaxResult.success(buildResult(reportResult, feedRecord, feedMessage));
```

返回体保留原 `summary`，新增 `feedStatus`、`feedRecordId`、`feedMessage`；前端现有成功判断不应因附加字段改变。禁止把投料异常继续抛出到原报工事务边界。

- [ ] **Step 4: 验证定时报工机制不变**

不修改 `MyScheduledTask` 的一分钟调度，不修改 `ScheduleReport.goodReportBatch()` 和 `badReportOne()`。投料记录不会被现有 `_progress_report` 定时任务处理。

- [ ] **Step 5: 运行测试并提交**

```bash
mvn -pl ruoyi-admin -am -Dtest=ReportBatchFeedIntegrationTest test
mvn -pl ruoyi-admin -am test
```

Expected: PASS；投料失败不影响原报工入队。

Commit subject: `feat: 报工时自动创建并上传投料记录`

---

### Task 8: 增加投料记录管理与手动重传页面

**Files:**
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/controller/InspectionFeedRecordController.java`
- Create: `ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/InspectionFeedRecordControllerTest.java`
- Create: `dsc-ry-Vue3-master/src/api/inspection/feedRecord.js`
- Create: `dsc-ry-Vue3-master/src/views/inspection/feedRecord/index.vue`

**Interfaces:**
- Produces: `GET /inspection/feedRecord/list`、`GET /inspection/feedRecord/{id}`、`POST /inspection/feedRecord/{id}/retry`。
- Consumes: `IInspectionFeedRecordService`、`FeedUploadService.manualRetry(Long recordId)`。

- [ ] **Step 1: 写 Controller 权限和状态测试**

断言列表分页、详情查询、`PENDING/FAILED` 重传成功、`SUCCESS/PROCESSING/UNKNOWN` 重传返回明确错误，并验证权限串分别为 `inspection:feedRecord:list/query/retry`。

- [ ] **Step 2: 实现后端管理接口**

```java
@GetMapping("/list")
@PreAuthorize("@ss.hasPermi('inspection:feedRecord:list')")
public TableDataInfo list(InspectionFeedRecord query) {
    startPage();
    return getDataTable(feedRecordService.selectList(query));
}

@PostMapping("/{id}/retry")
@PreAuthorize("@ss.hasPermi('inspection:feedRecord:retry')")
@Log(title = "投料记录", businessType = BusinessType.UPDATE)
public AjaxResult retry(@PathVariable Long id) {
    return success(feedUploadService.manualRetry(id));
}
```

列表支持按二维码、工单号、任务 ID、上传状态、创建时间查询。详情返回请求 JSON、响应 JSON和错误原因，不返回 Token 或应用密钥。

- [ ] **Step 3: 实现前端 API 和页面**

页面表格至少显示：投料记录 ID、二维码、工单号、任务 ID、原料 ID、投料数量、状态、重传次数、失败原因、创建时间、成功时间。状态文案为“待上传/上传中/成功/失败/结果未知”。

“重新上传”按钮仅在 `PENDING`、`FAILED` 显示；点击前二次确认，成功后刷新列表；`SUCCESS`、`PROCESSING`、`UNKNOWN` 不提供重传按钮。详情抽屉格式化展示请求和响应 JSON。

- [ ] **Step 4: 运行后端和前端验证**

```bash
mvn -pl ruoyi-admin -am -Dtest=InspectionFeedRecordControllerTest test
cd dsc-ry-Vue3-master
pnpm build:prod
```

Expected: Controller 测试 PASS，Vite 构建成功。

- [ ] **Step 5: 提交**

Commit subject: `feat: 增加投料记录查询与手动重传`

---

### Task 9: 黑湖联调与最终验收

**Files:**
- Update: `ruoyi-admin/src/test/resources/blacklake/*.json`，仅在真实脱敏响应与文档结构不同时更新。
- Modify production files only when integration proves a documented field path is inaccurate.

- [ ] **Step 1: 联调库存读取接口**

使用当前工单二维码调用库存接口，确认只返回一条可用库存，并核实原料 `materialId`、库存 ID、仓位、质量状态、批号、数量和单位的真实 JSON 路径。

- [ ] **Step 2: 联调投料关系读取接口**

使用库存 `materialId + taskId` 查询关系，确认 `data.originalAlternativeMaterial.alternativeFeedKey` 完整存在。

- [ ] **Step 3: 联调批量投料写接口**

使用测试工单和测试库存，确认 `opeAmount` 等于库存接口返回数量，黑湖返回 `code=200`，库存和任务投料记录发生预期变化。禁止直接用生产库存试错。

- [ ] **Step 4: 验证完整成功链路**

```text
扫描工单
→ 镜检计数
→ 点击完工
→ 原 reportBatch 入队成功
→ 创建 PENDING 投料记录
→ 自动读取库存
→ 自动读取投料关系
→ bulk_feed 成功
→ 投料记录更新为 SUCCESS
→ 原定时 progress_report 成功
```

- [ ] **Step 5: 验证失败互不阻断链路**

覆盖库存为空、库存数量为零、没有投料关系、黑湖业务拒绝、无法连接黑湖。每种情况都必须保留原报工待上传数据，同时创建可查询的投料记录并保存 `FAILED` 和失败原因；在投料记录页面点击“重新上传”后重新执行三个接口，成功时更新为 `SUCCESS`。

- [ ] **Step 6: 验证防重复和未知状态链路**

验证同一 `summaryId + taskId + qrCode` 只产生一条投料记录；成功记录不显示重传按钮且后端拒绝重传。模拟 `_bulk_feed` 读取超时后确认状态为 `UNKNOWN`，页面不可直接重传；并发点击报工或重传时只有一个请求能进入 `PROCESSING`。

- [ ] **Step 7: 运行全部验证命令**

```bash
mvn clean test
mvn clean package -DskipTests
cd dsc-ry-Vue3-master
pnpm build:prod
```

Expected: Maven 测试和打包均为 BUILD SUCCESS，Vite 构建成功。

- [ ] **Step 8: 最终提交**

仅在联调产生 fixture 或字段适配变更时提交。Commit subject: `test: 完善报工前自动投料联调覆盖`

---

## Implementation Checkpoints

1. **读取检查点：** 第二、第三个读取接口能够从当前二维码和 taskId 得到完整库存与投料关系。
2. **写入检查点：** 第一个 `_bulk_feed` 请求完全由两个读取接口的数据自动组装。
3. **记录检查点：** 点击报工先保留原报工入队结果，并为同一 `summaryId + taskId + qrCode` 创建唯一投料记录。
4. **互不阻断检查点：** 投料失败或结果未知不回滚、不阻断原 `reportBatch` 入队和定时报工。
5. **手动重传检查点：** 仅 `PENDING/FAILED` 可重传；重传重新查询库存和关系；`SUCCESS/PROCESSING/UNKNOWN` 均被前后端阻止。
6. **防重检查点：** 成功记录和并发请求不会再次调用 `_bulk_feed`，网络超时不会被错误归类为可直接重传的失败。
7. **页面检查点：** 投料记录页面可筛选、查看请求响应详情和失败原因，并执行授权后的手动重传。
8. **回归检查点：** 原 `MyScheduledTask`、`ScheduleReport` 和现有报工重新上传功能保持不变。

## Rollout Notes

- 先备份测试库，再执行 `sql/20260903_add_inspection_feed_record.sql`。
- 上线顺序：数据库表及菜单权限 → 后端接口与自动上传 → 前端投料记录页面。
- 首次上线重点监控 `inspection_feed_record.upload_status` 中的 `FAILED`、`UNKNOWN` 和长时间停留的 `PROCESSING`。
- 本次新增独立投料记录表、投料记录 REST 接口和前端管理页面；不修改现有定时报工任务，不增加投料定时补传。
- 回滚应用版本时 `inspection_feed_record` 可保留，禁止删除已保存的请求、响应和失败审计信息。
- 在生产联调前必须使用黑湖测试工单和测试库存，确认手动重传不会对同一库存产生重复扣减。
