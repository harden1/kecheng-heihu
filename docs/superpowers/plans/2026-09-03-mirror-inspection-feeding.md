# 镜检报工前自动投料 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 操作员点击现有“报工/完工”时，系统先读取黑湖库存和投料关系，再调用批量投料写接口；投料成功后才执行现有 `reportBatch` 入队逻辑，黑湖 `_progress_report` 仍由原定时任务上传。

**Architecture:** 使用当前工单扫码二维码查询库存，从库存响应取得原料、库存范围、数量和单位；使用库存物料 ID 与当前生产任务 ID 查询投料关系；组装并同步调用 `_bulk_feed`。投料结果保存在 `inspection_summary`，用于防止重复投料；不增加前端投料页面、独立前端接口、断网投料、自动补投料或多标签逻辑。

**Tech Stack:** Java 8、Spring Boot 2.5.15、MyBatis、MySQL、OkHttp、Jackson、JUnit 5、Mockito、现有 Vue 3 前端。

## Global Constraints

- Excel 中第一个 `_bulk_feed` 是写接口；第二个库存接口和第三个投料关系接口是读接口。
- 固定调用顺序：库存查询 → 投料关系查询 → 批量投料 → 现有 `reportBatch` 入队。
- 库存查询的 `qrCodes` 使用当前工单扫码二维码，即 `inspection_summary.api_report.qrCode`。
- 投料数量 `opeAmount` 使用库存响应 `data.list[].amount.amount`，不让操作员手工填写。
- 投料单位 `opeUnitId` 使用库存响应 `data.list[].amount.unit.id`。
- 投料关系的 `materialId` 使用库存响应中的原料物料 ID；`taskId` 使用 `api_report.taskId`。
- 黑湖成功仅以业务响应 `code == 200` 判断，不依赖中文 `message`。
- Java 代码必须兼容 Java 8，数量统一使用 `BigDecimal`。
- 保留现有定时报工机制，不改成点击后同步调用 `_progress_report`。
- 本次不实现断网投料、不实现投料定时补传、不新增投料操作页面。
- 批量投料成功但后续本地入队失败时，重试必须跳过已成功投料，避免重复扣库存。
- 请求可能已到达黑湖但未收到响应时标记 `UNKNOWN`，禁止自动重试。
- 不记录 access token、appKey、appSecret。

---

## Final Business Flow

```text
用户扫描当前工单二维码
→ queryScanTaskResult 获得 taskId、processId、lineId 等
→ addOrReadInspectionMain 将 taskId 和当前二维码写入 api_report
→ 用户进行镜检计数
→ 用户点击“报工/完工”
→ reportBatch 首先调用投料编排服务
    → 第二个接口：用 api_report.qrCode 查询库存
    → 第三个接口：用库存 materialId + api_report.taskId 查询投料关系
    → 第一个接口：把两个读接口的数据组装后写入 bulk_feed
→ bulk_feed 返回 code=200，持久化 feed_status=SUCCESS
→ 继续执行 reportBatch 原有逻辑
    → 生成不良明细
    → inspection_summary.success_flag=0
→ 原 MyScheduledTask / ScheduleReport 定时调用 _progress_report
```

如果库存查询、关系查询或批量投料失败，`reportBatch` 立即返回错误，不生成待报工数据、不跳转报工记录页。

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

库存响应中原料物料 ID 的精确 JSON 路径在 Excel 中没有给出，只能确定它来自 `data.list[]` 的物料信息。Task 7 联调时必须用真实脱敏响应确认路径，不能把当前镜检成品的 `api_report.materialId` 当成原料 `materialId`。

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
ruoyi-admin/src/main/java/com/ruoyi/apiTool/service/FeedBeforeReportService.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/service/InspectionFeedStatusService.java
```

### Modified backend files

```text
ruoyi-admin/pom.xml
ruoyi-admin/src/main/java/com/ruoyi/apiTool/controller/BlacklackUserController.java
ruoyi-admin/src/main/java/com/ruoyi/inspection/domain/InspectionSummary.java
ruoyi-admin/src/main/java/com/ruoyi/inspection/mapper/InspectionSummaryMapper.java
ruoyi-admin/src/main/resources/mapper/inspection/InspectionSummaryMapper.xml
sql/dsc.sql
```

### New migration and tests

```text
sql/20260903_add_inspection_feed_status.sql
ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/BlackLakeApiClientTest.java
ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/MaterialInventoryClientTest.java
ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/FeedRelationClientTest.java
ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/BulkFeedClientTest.java
ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/FeedBeforeReportServiceTest.java
ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/ReportBatchFeedIntegrationTest.java
ruoyi-admin/src/test/resources/blacklake/inventory-success.json
ruoyi-admin/src/test/resources/blacklake/inventory-empty.json
ruoyi-admin/src/test/resources/blacklake/feed-relation-success.json
ruoyi-admin/src/test/resources/blacklake/feed-relation-empty.json
ruoyi-admin/src/test/resources/blacklake/bulk-feed-success.json
ruoyi-admin/src/test/resources/blacklake/bulk-feed-failed.json
```

### Explicitly unchanged

```text
dsc-ry-Vue3-master/src/**
ruoyi-admin/src/main/java/com/ruoyi/apiTool/MyScheduledTask.java
ruoyi-admin/src/main/java/com/ruoyi/apiTool/ScheduleReport.java
```

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

### Task 5: 增加投料状态和报工前编排服务

**Files:**
- Create: `sql/20260903_add_inspection_feed_status.sql`
- Modify: `sql/dsc.sql`
- Modify: `ruoyi-admin/src/main/java/com/ruoyi/inspection/domain/InspectionSummary.java`
- Modify: `ruoyi-admin/src/main/java/com/ruoyi/inspection/mapper/InspectionSummaryMapper.java`
- Modify: `ruoyi-admin/src/main/resources/mapper/inspection/InspectionSummaryMapper.xml`
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/service/InspectionFeedStatusService.java`
- Create: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/service/FeedBeforeReportService.java`
- Test: `ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/FeedBeforeReportServiceTest.java`

**Interfaces:**
- Consumes: `MaterialInventoryClient`、`FeedRelationClient`、`BulkFeedClient`、`InspectionSummaryMapper`。
- Produces: `void ensureFedBeforeReport(Long summaryId)`。

- [ ] **Step 1: 增加数据库字段**

```sql
ALTER TABLE inspection_summary
    ADD COLUMN feed_status varchar(16) DEFAULT NULL COMMENT '投料状态：PROCESSING/SUCCESS/FAILED/UNKNOWN',
    ADD COLUMN feed_request longtext DEFAULT NULL COMMENT '投料请求JSON',
    ADD COLUMN feed_response longtext DEFAULT NULL COMMENT '投料响应JSON',
    ADD COLUMN feed_time datetime DEFAULT NULL COMMENT '投料成功时间';
```

同步修改 `sql/dsc.sql` 中完整建表定义。

- [ ] **Step 2: 写编排服务失败测试**

覆盖：

1. `feed_status=SUCCESS` 时直接返回，不再次调用黑湖。
2. 空状态时依次调用库存、关系、批量投料。
3. 批量投料成功后保存 `SUCCESS`。
4. 库存或关系明确失败后保存 `FAILED`。
5. 无法连接黑湖时保存 `FAILED` 并返回错误。
6. 请求结果不确定时保存 `UNKNOWN`。
7. `UNKNOWN` 再次点击报工时阻止重复投料。
8. 两个并发请求只有一个能把状态改为 `PROCESSING`。

- [ ] **Step 3: 运行测试并确认失败**

```bash
mvn -pl ruoyi-admin -am -Dtest=FeedBeforeReportServiceTest test
```

Expected: FAIL。

- [ ] **Step 4: 增加 Mapper 状态切换**

接口至少包括：

```java
int claimFeed(Long id);
int markFeedSuccess(Long id, String requestJson, String responseJson);
int markFeedFailed(Long id, String requestJson, String responseJson);
int markFeedUnknown(Long id, String requestJson, String responseJson);
```

抢占 SQL：

```sql
UPDATE inspection_summary
SET feed_status = 'PROCESSING'
WHERE id = #{id}
  AND (feed_status IS NULL OR feed_status = 'FAILED')
```

- [ ] **Step 5: 实现独立状态事务服务**

`InspectionFeedStatusService` 的成功、失败和未知状态更新使用 `Propagation.REQUIRES_NEW`。它必须是独立 Spring Bean，避免同类内部调用导致事务代理失效。

这样即使投料成功后的原 `reportBatch` 数据库事务失败，`SUCCESS` 状态仍已提交，用户重试时不会重复调用 `_bulk_feed`。

- [ ] **Step 6: 实现报工前投料编排**

```java
public void ensureFedBeforeReport(Long summaryId) {
    InspectionSummary summary = inspectionSummaryMapper.selectInspectionSummaryByIdNoDetailAndBadItems(summaryId);
    if ("SUCCESS".equals(summary.getFeedStatus())) {
        return;
    }
    if ("UNKNOWN".equals(summary.getFeedStatus())) {
        throw new ServiceException("投料结果待核对，请勿重复报工");
    }
    if (!feedStatusService.claim(summaryId)) {
        throw new ServiceException("投料正在处理中，请勿重复提交");
    }

    Map<String, Object> reportInfo = parseApiReport(summary.getApiReport());
    String qrCode = reportInfo.get("qrCode").toString();
    Long taskId = Long.valueOf(reportInfo.get("taskId").toString());
    InventoryDetail inventory = materialInventoryClient.queryByQrCode(qrCode);
    JsonNode feedKey = feedRelationClient.getFeedRelation(inventory.getMaterialId(), taskId);
    BulkFeedRequest request = buildRequest(taskId, qrCode, inventory, feedKey);
    BlackLakeResult result = bulkFeedClient.bulkFeed(request);
    feedStatusService.markSuccess(summaryId, request, result);
}
```

异常分类后分别持久化 `FAILED` 或 `UNKNOWN`，再向 Controller 抛出可读错误。

- [ ] **Step 7: 确保查询 SQL 包含 `api_report` 和投料字段**

当前 `selectInspectionSummaryByIdNoDetailAndBadItems` 必须补充：

```text
api_report, feed_status, feed_request, feed_response, feed_time
```

否则编排服务无法取得二维码和 `taskId`。

- [ ] **Step 8: 运行测试**

```bash
mvn -pl ruoyi-admin -am -Dtest=FeedBeforeReportServiceTest test
```

Expected: PASS。

- [ ] **Step 9: 提交**

Commit subject: `feat: 报工前编排镜检投料`

---

### Task 6: 接入现有 reportBatch 并回归定时报工

**Files:**
- Modify: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/controller/BlacklackUserController.java`
- Test: `ruoyi-admin/src/test/java/com/ruoyi/apiTool/feed/ReportBatchFeedIntegrationTest.java`
- Verify unchanged: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/MyScheduledTask.java`
- Verify unchanged: `ruoyi-admin/src/main/java/com/ruoyi/apiTool/ScheduleReport.java`

**Interfaces:**
- Consumes: `FeedBeforeReportService.ensureFedBeforeReport(Long summaryId)`。
- Produces: 点击现有 `/blacklackUser/BlacklackUser/reportBatch` 时先投料，成功后执行原入队逻辑。

- [ ] **Step 1: 写 Controller 集成失败测试**

覆盖：

1. 投料失败时不更新 `success_flag=0`。
2. 投料失败时不插入 `inspection_report` 不良明细。
3. 投料成功后执行原有入队逻辑。
4. 已投料成功后再次进入方法不会再次调用 `_bulk_feed`。
5. 投料状态 `UNKNOWN` 时返回核对提示并停止。

- [ ] **Step 2: 运行测试并确认失败**

```bash
mvn -pl ruoyi-admin -am -Dtest=ReportBatchFeedIntegrationTest test
```

Expected: FAIL。

- [ ] **Step 3: 在 reportBatch 最前面接入投料**

在读取主记录、设置 `successFlag=0` 和插入不良明细之前调用：

```java
int mainId = (int) params.get("mainId");
feedBeforeReportService.ensureFedBeforeReport((long) mainId);
```

只有该方法正常返回后，才能继续执行当前 `reportBatch` 的原逻辑。

- [ ] **Step 4: 保留当前定时报工机制**

不修改 `MyScheduledTask` 的一分钟调度，不修改 `ScheduleReport.goodReportBatch()` 和 `badReportOne()` 的调用方式。投料在用户点击完工时已经成功，后续定时任务只处理原有 `_progress_report`。

- [ ] **Step 5: 运行集成测试和全部后端测试**

```bash
mvn -pl ruoyi-admin -am -Dtest=ReportBatchFeedIntegrationTest test
mvn -pl ruoyi-admin -am test
```

Expected: PASS。

- [ ] **Step 6: 提交**

Commit subject: `feat: 完工报工前自动完成投料`

---

### Task 7: 黑湖联调与最终验收

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
→ 自动读取库存
→ 自动读取投料关系
→ bulk_feed 成功
→ 原 reportBatch 入队成功
→ 定时 progress_report 成功
```

- [ ] **Step 5: 验证失败链路**

覆盖库存为空、库存数量为零、没有投料关系、黑湖业务拒绝、无法连接黑湖。每种情况均应停在点击完工页面，不生成待报工数据。

- [ ] **Step 6: 验证防重复链路**

模拟 bulk_feed 成功后原 reportBatch 数据库操作失败，再次点击完工时确认跳过 bulk_feed，只重试本地入队。模拟读取超时后确认状态为 `UNKNOWN`，再次点击被阻止。

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
3. **防重检查点：** bulk_feed 成功后即使本地入队失败，重试也不会重复投料。
4. **流程检查点：** 投料失败不进入原 reportBatch 队列；投料成功才进入。
5. **回归检查点：** 原 `MyScheduledTask` 和 `ScheduleReport` 仍按现有方式上传 progress_report。

## Rollout Notes

- 先备份测试库，再执行 `sql/20260903_add_inspection_feed_status.sql`。
- 上线顺序：数据库字段 → 后端；本次无前端代码变更。
- 首次上线重点监控 `inspection_summary.feed_status` 中的 `FAILED` 和 `UNKNOWN`。
- 本次不新增独立投料表、不新增 REST 接口、不新增前端投料页面、不修改定时任务。
- 回滚应用版本时新增状态字段可保留，不删除已保存的投料请求和响应审计信息。
