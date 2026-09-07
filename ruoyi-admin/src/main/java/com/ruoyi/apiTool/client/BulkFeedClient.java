package com.ruoyi.apiTool.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.apiTool.domain.feed.BlackLakeResult;
import com.ruoyi.apiTool.domain.feed.BulkFeedRequest;
import com.ruoyi.apiTool.domain.feed.InventoryDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 调用黑湖批量投料写接口。
 * <p>
 * 请求体完全由库存查询和投料关系查询的结果组装，不使用前端参数或镜检良品数。
 *
 * @author wmin
 * @date 2026-09-03
 */
@Component
public class BulkFeedClient {

    /** 批量投料接口路径 */
    private static final String BULK_FEED_PATH = "/mfg/open/v1/feed/_bulk_feed";

    private final BlackLakeApiClient apiClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public BulkFeedClient(BlackLakeApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 调用黑湖批量投料接口。
     *
     * @param request 组装完成的批量投料请求
     * @return 黑湖业务响应
     */
    public BlackLakeResult bulkFeed(BulkFeedRequest request) {
        JsonNode response = apiClient.post(BULK_FEED_PATH, request);
        BlackLakeResult result = new BlackLakeResult();
        result.setCode(response.path("code").asInt());
        result.setMessage(response.path("message").asText());
        result.setData(response.path("data"));
        return result;
    }

    /**
     * 便捷方法：直接用库存明细和投料关系组装并调用批量投料。
     *
     * @param taskId             生产任务ID
     * @param inventoryIdentifier 当前工单扫码二维码
     * @param inventory           库存明细
     * @param feedKey             投料关系接口返回的完整 alternativeFeedKey
     * @return 黑湖业务响应
     */
    public BlackLakeResult bulkFeed(Long taskId, String inventoryIdentifier,
                                    InventoryDetail inventory, JsonNode feedKey) {
        BulkFeedRequest request = BulkFeedRequest.build(taskId, inventoryIdentifier, inventory, feedKey);
        return bulkFeed(request);
    }

    /**
     * 将请求体序列化为 JSON 字符串，用于持久化投料记录。
     *
     * @param request 批量投料请求
     * @return JSON 字符串
     */
    public String serializeRequest(BulkFeedRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new IllegalStateException("批量投料请求序列化失败", e);
        }
    }
}
