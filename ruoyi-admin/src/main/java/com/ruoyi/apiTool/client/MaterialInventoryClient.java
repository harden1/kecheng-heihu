package com.ruoyi.apiTool.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruoyi.apiTool.domain.feed.InventoryDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 按当前工单二维码查询并解析黑湖物料库存明细。
 */
@Component
public class MaterialInventoryClient {

    private static final String INVENTORY_PATH = "/inventory/open/v1/material_inventory/_list";

    private final BlackLakeApiClient apiClient;

    public MaterialInventoryClient(BlackLakeApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 查询与输入二维码精确匹配的唯一可用库存。
     *
     * @param qrCode 当前工单扫码二维码
     * @return 唯一匹配且数量大于零的库存明细
     */
    public InventoryDetail queryByQrCode(String qrCode) {
        if (qrCode == null || qrCode.trim().isEmpty()) {
            throw new IllegalArgumentException("库存查询二维码不能为空");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("qrCodes", Collections.singletonList(qrCode));
        JsonNode response = apiClient.post(INVENTORY_PATH, body);
        JsonNode list = response.path("data").path("list");
        if (!list.isArray()) {
            throw new IllegalStateException("黑湖库存响应缺少必需字段 data.list");
        }

        JsonNode matched = null;
        for (JsonNode item : list) {
            // qrCode 是唯一匹配依据，不做 trim、忽略大小写等宽松比较。
            JsonNode itemQrCode = requiredNode(item, "qrCode");
            if (qrCode.equals(itemQrCode.asText())) {
                if (matched != null) {
                    throw new IllegalStateException("二维码 " + qrCode + " 匹配到多条库存，无法唯一确定投料库存");
                }
                matched = item;
            }
        }
        if (matched == null) {
            throw new IllegalStateException("未找到与二维码精确匹配的库存: " + qrCode);
        }

        return parseInventory(matched);
    }

    /**
     * 按已联调确认的响应路径解析投料必需字段，避免缺失值被 Jackson 默认为零。
     */
    private InventoryDetail parseInventory(JsonNode item) {
        InventoryDetail detail = new InventoryDetail();
        detail.setInventoryElementId(requiredLong(item, "id"));
        detail.setMaterialId(requiredLong(item, "material.id"));
        detail.setStorageLocationId(requiredLong(item, "storageLocationId"));
        detail.setQcStatus(requiredInteger(item, "qcStatus.code"));
        detail.setBatchNo(nullableText(item, "bizKeyAttr.batchNo"));

        BigDecimal amount = requiredDecimal(item, "amount.amount");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("黑湖库存数量必须大于零，amount.amount=" + amount);
        }
        detail.setAmount(amount);
        detail.setUnitId(requiredLong(item, "amount.unit.id"));
        detail.setQrCode(requiredText(item, "qrCode"));
        return detail;
    }

    private JsonNode requiredNode(JsonNode root, String fieldPath) {
        JsonNode node = findNode(root, fieldPath);
        if (node == null || node.isMissingNode() || node.isNull()) {
            throw new IllegalStateException("黑湖库存响应缺少必需字段 " + fieldPath);
        }
        return node;
    }

    private Long requiredLong(JsonNode root, String fieldPath) {
        JsonNode node = requiredNode(root, fieldPath);
        try {
            return Long.valueOf(node.asText());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("黑湖库存响应字段 " + fieldPath + " 不是有效整数", e);
        }
    }

    private Integer requiredInteger(JsonNode root, String fieldPath) {
        JsonNode node = requiredNode(root, fieldPath);
        try {
            return Integer.valueOf(node.asText());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("黑湖库存响应字段 " + fieldPath + " 不是有效整数", e);
        }
    }

    private BigDecimal requiredDecimal(JsonNode root, String fieldPath) {
        JsonNode node = requiredNode(root, fieldPath);
        try {
            return new BigDecimal(node.asText());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("黑湖库存响应字段 " + fieldPath + " 不是有效数量", e);
        }
    }

    private String requiredText(JsonNode root, String fieldPath) {
        JsonNode node = requiredNode(root, fieldPath);
        if (!node.isTextual() || node.textValue().isEmpty()) {
            throw new IllegalStateException("黑湖库存响应字段 " + fieldPath + " 不能为空");
        }
        return node.textValue();
    }

    private String nullableText(JsonNode root, String fieldPath) {
        JsonNode node = findNode(root, fieldPath);
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (!node.isTextual()) {
            throw new IllegalStateException("黑湖库存响应字段 " + fieldPath + " 格式无效");
        }
        return node.textValue();
    }

    private JsonNode findNode(JsonNode root, String fieldPath) {
        JsonNode current = root;
        String[] parts = fieldPath.split("\\.");
        for (String part : parts) {
            current = current.path(part);
        }
        return current;
    }
}
