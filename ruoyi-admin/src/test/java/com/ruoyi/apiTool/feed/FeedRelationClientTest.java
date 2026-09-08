package com.ruoyi.apiTool.feed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.apiTool.client.BlackLakeApiClient;
import com.ruoyi.apiTool.client.FeedRelationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 黑湖投料关系客户端测试。
 */
class FeedRelationClientTest {

    private static final long MATERIAL_ID = 1787204304660640L;
    private static final long TASK_ID = 1788854522955181L;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private BlackLakeApiClient apiClient;
    private FeedRelationClient client;

    @BeforeEach
    void setUp() {
        apiClient = mock(BlackLakeApiClient.class);
        client = new FeedRelationClient(apiClient);
    }

    @Test
    void shouldParseArrayResponseAndMaterialCodeFromNestedBaseInfo() {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("code", 200);
        ObjectNode relation = response.putArray("data").addObject();
        ObjectNode alternative = relation.putObject("originalAlternativeMaterial");
        alternative.putObject("alternativeFeedKey").put("materialId", MATERIAL_ID);
        alternative.putObject("materialInfo").putObject("baseInfo").put("code", "ZZP-SH06");

        when(apiClient.post(eq("/mfg/open/v1/feed/_get_feed_relation"), any()))
                .thenReturn(response);

        FeedRelationClient.FeedRelationResult result = client.getFeedRelation(MATERIAL_ID, TASK_ID);

        assertEquals("ZZP-SH06", result.getMaterialCode());
        assertEquals(MATERIAL_ID, result.getFeedKey().path("materialId").asLong());
    }
}
