package com.ruoyi.apiTool.feed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.apiTool.client.BlackLakeApiClient;
import com.ruoyi.apiTool.client.MaterialInventoryClient;
import com.ruoyi.apiTool.domain.feed.InventoryDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 黑湖物料库存查询客户端测试。
 */
class MaterialInventoryClientTest {

    private static final String QR_CODE = "QR-INSPECTION-001";
    private static final String MATERIAL_CODE = "ZZP-SH06";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private BlackLakeApiClient apiClient;
    private MaterialInventoryClient client;

    @BeforeEach
    void setUp() {
        apiClient = mock(BlackLakeApiClient.class);
        client = new MaterialInventoryClient(apiClient);
    }

    @Test
    void shouldQueryExactQrCodeAndParseInventoryDetail() throws Exception {
        when(apiClient.post(eq("/inventory/open/v1/material_inventory/_list"), any()))
                .thenReturn(readFixture("inventory-success.json"));

        InventoryDetail result = client.queryByQrCode(QR_CODE, MATERIAL_CODE);

        assertEquals(Long.valueOf(1787795331084898L), result.getInventoryElementId());
        assertEquals(Long.valueOf(1787651959776809L), result.getMaterialId());
        assertEquals(Long.valueOf(1783078034914734L), result.getStorageLocationId());
        assertEquals(Integer.valueOf(1), result.getQcStatus());
        assertEquals("CS0824001", result.getBatchNo());
        assertEquals(new BigDecimal("3"), result.getAmount());
        assertEquals(Long.valueOf(1749025381704132L), result.getUnitId());
        assertEquals(QR_CODE, result.getQrCode());

        ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        verify(apiClient).post(eq("/inventory/open/v1/material_inventory/_list"), bodyCaptor.capture());
        JsonNode body = objectMapper.valueToTree(bodyCaptor.getValue());
        assertEquals(objectMapper.readTree("{\"qrCodes\":[\"" + QR_CODE + "\"],\"materialCodes\":[\"" + MATERIAL_CODE + "\"]}"), body);
    }

    @Test
    void shouldAllowNullBatchNo() throws Exception {
        ObjectNode response = (ObjectNode) readFixture("inventory-success.json");
        matchingItem(response).with("bizKeyAttr").putNull("batchNo");
        when(apiClient.post(any(), any())).thenReturn(response);

        InventoryDetail result = client.queryByQrCode(QR_CODE, MATERIAL_CODE);

        assertNull(result.getBatchNo());
    }

    @Test
    void shouldNormalizeEmptyBatchNoToNull() throws Exception {
        ObjectNode response = (ObjectNode) readFixture("inventory-success.json");
        matchingItem(response).with("bizKeyAttr").put("batchNo", "");
        when(apiClient.post(any(), any())).thenReturn(response);

        InventoryDetail result = client.queryByQrCode(QR_CODE, MATERIAL_CODE);

        assertNull(result.getBatchNo());
    }

    @Test
    void shouldRejectBlankQrCodeWithoutCallingApi() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> client.queryByQrCode(" ", MATERIAL_CODE));

        assertTrue(exception.getMessage().contains("二维码不能为空"));
    }

    @Test
    void shouldRejectBlankMaterialCodeWithoutCallingApi() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> client.queryByQrCode(QR_CODE, " "));

        assertTrue(exception.getMessage().contains("物料编号不能为空"));
    }

    @Test
    void shouldRejectEmptyInventory() throws Exception {
        when(apiClient.post(any(), any())).thenReturn(readFixture("inventory-empty.json"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.queryByQrCode(QR_CODE, MATERIAL_CODE));

        assertTrue(exception.getMessage().contains("未找到与二维码精确匹配的库存"));
    }

    @Test
    void shouldRejectResponseWithoutExactQrCodeMatch() throws Exception {
        ObjectNode response = (ObjectNode) readFixture("inventory-success.json");
        matchingItem(response).put("qrCode", "QR-INSPECTION-001 ");
        when(apiClient.post(any(), any())).thenReturn(response);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.queryByQrCode(QR_CODE, MATERIAL_CODE));

        assertTrue(exception.getMessage().contains("未找到与二维码精确匹配的库存"));
    }

    @Test
    void shouldRejectMultipleExactMatches() throws Exception {
        ObjectNode response = (ObjectNode) readFixture("inventory-success.json");
        ArrayNode list = (ArrayNode) response.path("data").path("list");
        list.add(matchingItem(response).deepCopy());
        when(apiClient.post(any(), any())).thenReturn(response);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.queryByQrCode(QR_CODE, MATERIAL_CODE));

        assertTrue(exception.getMessage().contains("匹配到多条库存"));
    }

    @Test
    void shouldRejectNonPositiveAmount() throws Exception {
        ObjectNode response = (ObjectNode) readFixture("inventory-success.json");
        matchingItem(response).with("amount").put("amount", "0");
        when(apiClient.post(any(), any())).thenReturn(response);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.queryByQrCode(QR_CODE, MATERIAL_CODE));

        assertTrue(exception.getMessage().contains("库存数量必须大于零"));
    }

    @Test
    void shouldRejectEveryMissingRequiredFieldClearly() throws Exception {
        String[] fieldPaths = {
                "id", "material.id", "storageLocationId", "qcStatus.code",
                "amount.amount", "amount.unit.id", "qrCode"
        };
        for (String fieldPath : fieldPaths) {
            ObjectNode response = (ObjectNode) readFixture("inventory-success.json");
            removeField(matchingItem(response), fieldPath);
            when(apiClient.post(any(), any())).thenReturn(response);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> client.queryByQrCode(QR_CODE, MATERIAL_CODE), fieldPath);

            assertTrue(exception.getMessage().contains(fieldPath), fieldPath);
        }
    }

    private JsonNode readFixture(String name) throws IOException {
        try (InputStream input = getClass().getResourceAsStream("/blacklake/" + name)) {
            if (input == null) {
                throw new IllegalStateException("测试响应文件不存在: " + name);
            }
            return objectMapper.readTree(input);
        }
    }

    private ObjectNode matchingItem(ObjectNode response) {
        for (JsonNode item : response.path("data").path("list")) {
            if (QR_CODE.equals(item.path("qrCode").asText())) {
                return (ObjectNode) item;
            }
        }
        throw new IllegalStateException("测试数据缺少匹配库存");
    }

    private void removeField(ObjectNode item, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        ObjectNode parent = item;
        for (int i = 0; i < parts.length - 1; i++) {
            parent = (ObjectNode) parent.path(parts[i]);
        }
        parent.remove(parts[parts.length - 1]);
    }
}
