package com.ruoyi.apiTool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ApiBatchReportForBlackLack {
    @Autowired
    private AccessTokenService accessTokenService;

    public String batchReportForBlackLack(int currentUserId1, String qrCode1, int reportUnitId1, int reportProcessId1, int reportAmount1, int lineId1, int materialId1, int qcStatus1, int reportType1, int taskId1   ) {

        // Access Token 和 X-AUTH 一致
        String accessToken = accessTokenService.getAccessToken(true);
        System.out.println("✅ Access Token: " + accessToken);
        // access_token 作为 query 参数拼接到 URL 上
        HttpUrl url = HttpUrl.parse("\n" +
                        "https://v3-ali.blacklake.cn/api/openapi/domain/web/v1/route"+
                        "/mfg/open/v1/progress_report/_progress_report")
                .newBuilder()
                .addQueryParameter("access_token", accessToken)
                .build();
        // 请求体 JSON
        // 变量替换区

        // 构造最内层 customFields
        Map<String, Object> customField = new HashMap<>();
        customField.put("fieldCode", "cust_field21__c");
        customField.put("fieldValue", "测试");

        // 构造 progressReportMaterialItems
        Map<String, Object> materialItem = new HashMap<>();
        materialItem.put("reportAmount", reportAmount1);
        materialItem.put("reportUnitId", reportUnitId1);
        materialItem.put("qrCode", qrCode1);
        materialItem.put("qrCodeNum", 1);
        materialItem.put("customFields", Collections.singletonList(customField));

        // 构造 progressReportItems
        Map<String, Object> reportItem = new HashMap<>();
        reportItem.put("executorIds", Collections.singletonList(currentUserId1));
        reportItem.put("progressReportMaterialItems", Collections.singletonList(materialItem));

        // 构造 progressReportMaterial
        Map<String, Object> reportMaterial = new HashMap<>();
        reportMaterial.put("lineId", lineId1);
        reportMaterial.put("materialId", materialId1);
        reportMaterial.put("reportProcessId", reportProcessId1);

        // 构造最终 JSON 对象
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        jsonMap.put("progressReportItems", Collections.singletonList(reportItem));
        jsonMap.put("progressReportMaterial", reportMaterial);
        jsonMap.put("qcStatus", qcStatus1);
        jsonMap.put("reportType", reportType1);
        jsonMap.put("taskId", taskId1);
        // 如果需要可以加：jsonMap.put("storageLocationId", 1752892472337317L);

        // 转为 JSON 字符串
        ObjectMapper mapperJsonBody = new ObjectMapper();
        String jsonBody = null;
        try {
            jsonBody = mapperJsonBody.writeValueAsString(jsonMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 输出看看
        System.out.println("最终 JSON 请求体:\n" + jsonBody);
        // 创建 RequestBody 对象
        RequestBody body = RequestBody.create(
                okhttp3.MediaType.parse("application/json; charset=utf-8"),
                jsonBody
        );
        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("X-AUTH", accessToken)
                .build();
        // 发送请求
        OkHttpClient client = new OkHttpClient();
        String responseStr = null;
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                responseStr = response.body().string();
                System.out.println("✅ 请求成功，响应内容：");
                System.out.println(responseStr);
                // 用 Jackson 解析为 JsonNode
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(responseStr);
                int code = jsonNode.path("code").asInt();
                String subCode = jsonNode.path("subCode").asText();
                if (code == 400150 && "USER-DOMAIN/SSO_TOKEN_FAIL".equals(subCode)) {
                    System.out.println("❌ Token 已失效，请重新登录。");
                    // 处理失效逻辑（如刷新 token、抛异常等）//间隔1s //重新获取token
                    Thread.sleep(1000);
                    // 重新请求数据
                    accessTokenService.getAccessToken(false);
                    batchReportForBlackLack(currentUserId1, qrCode1, reportUnitId1, reportProcessId1, reportAmount1, lineId1, materialId1, qcStatus1, reportType1, taskId1);
                } else {
                    System.out.println("✅ Token 有效，继续处理...");
                }
            } else {
                System.err.println("❌ 请求失败，HTTP状态码: " + response.code());
                System.err.println(response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 1. 读取 JSON 文件
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(responseStr);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        int code = root.get("code").asInt();
        String message = root.get("message").asText();
//        long taskId = root.get("data").get("taskId").asLong();
//        String taskCode = root.get("data").get("taskCode").asText();

        System.out.println("Code: " + code);
        System.out.println("Message: " + message);
//        System.out.println("Task ID: " + taskId);
//        System.out.println("Task Code: " + taskCode);

        // 遍历 executors 数组
        JsonNode executors = root.get("data").get("executors");
        for (JsonNode executor : executors) {
            System.out.println("执行人: " + executor.get("name").asText());
        }
        // 返回前端
        return responseStr;

        // 存入数据库主表json字段
    }
}
