package com.ruoyi.apiTool.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 查询黑湖投料关系，返回完整 alternativeFeedKey JSON 对象和物料编号。
 *
 * @author wmin
 * @date 2026-09-03
 */
@Component
public class FeedRelationClient {

    /** 投料关系查询接口路径 */
    private static final String FEED_RELATION_PATH = "/mfg/open/v1/feed/_get_feed_relation";

    private final BlackLakeApiClient apiClient;

    public FeedRelationClient(BlackLakeApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 根据库存原料物料ID和生产任务ID查询投料关系。
     * materialId 来自镜检主记录 api_report 中的物料ID，非库存接口返回的 materialId。
     *
     * @param materialId 原料物料ID（来自 api_report）
     * @param taskId     当前镜检主记录的生产任务ID
     * @return 投料关系结果，包含 alternativeFeedKey 和 materialCode
     * @throws IllegalStateException 当投料关系不存在时抛出
     */
    public FeedRelationResult getFeedRelation(Long materialId, Long taskId) {
        if (materialId == null) {
            throw new IllegalArgumentException("投料关系查询物料ID不能为空");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("投料关系查询任务ID不能为空");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("materialId", materialId);
        body.put("taskId", taskId);

        JsonNode response = apiClient.post(FEED_RELATION_PATH, body);
        JsonNode data = response.path("data");

        JsonNode alternativeFeedKey = data.path("originalAlternativeMaterial")
                .path("alternativeFeedKey");

        if (alternativeFeedKey.isMissingNode() || alternativeFeedKey.isNull()) {
            throw new IllegalStateException(
                    "当前库存物料与镜检生产任务不存在投料关系，materialId=" + materialId + "，taskId=" + taskId);
        }

        // 提取物料编号，用于后续库存查询入参 materialCodes
        String materialCode = data.path("materialCode").asText(null);
        if (materialCode == null || materialCode.isEmpty()) {
            throw new IllegalStateException(
                    "投料关系响应缺少物料编号 data.materialCode，materialId=" + materialId + "，taskId=" + taskId);
        }

        // 返回 feedKey 和 materialCode
        return new FeedRelationResult(alternativeFeedKey, materialCode);
    }

    /**
     * 投料关系查询结果，包含 alternativeFeedKey 和物料编号。
     */
    public static class FeedRelationResult {
        /** 完整的 alternativeFeedKey JSON 对象，保留所有已知和未知字段 */
        private final JsonNode feedKey;

        /** 物料编号，用于库存查询入参 materialCodes */
        private final String materialCode;

        public FeedRelationResult(JsonNode feedKey, String materialCode) {
            this.feedKey = feedKey;
            this.materialCode = materialCode;
        }

        public JsonNode getFeedKey() {
            return feedKey;
        }

        public String getMaterialCode() {
            return materialCode;
        }
    }
}
