package com.ruoyi.apiTool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报工记录
 */

@Component
public class ApiReportRecordForBlackLack {
    @Autowired
    private AccessTokenService accessTokenService;

    public  Map<String, String>  getReportRecordDetailForBlackLack(String qrCode) {

        // Access Token 和 X-AUTH 一致
        String accessToken = accessTokenService.getAccessToken(true);
//        System.out.println("✅ Access Token: " + accessToken);
        // access_token 作为 query 参数拼接到 URL 上
        HttpUrl url = HttpUrl.parse("https://v3-ali.blacklake.cn/api/openapi/domain/web/v1/route/mfg/open/v1/progress_report/_list")
                .newBuilder()
                .addQueryParameter("access_token", accessToken)
                .build();
        // 1. 创建 Map 并设置参数
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("qrCode", qrCode);
        jsonMap.put("size", "10");

        // 2. 转换成 JSON 字符串
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = null;
        try {
            jsonBody = objectMapper.writeValueAsString(jsonMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 3. 创建 RequestBody
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
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
//                System.out.println("✅ *************************查询工序数据*******************：");
//                System.out.println(responseStr);
                // 用 Jackson 解析为 JsonNode
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(responseStr);
                int code = jsonNode.path("code").asInt();
                String subCode = jsonNode.path("subCode").asText();
                if (code == 400150 && "USER-DOMAIN/SSO_TOKEN_FAIL".equals(subCode)) {
//                    System.out.println("❌ Token 已失效，请重新登录。");
                    // 处理失效逻辑（如刷新 token、抛异常等）//间隔1s //重新获取token
                    Thread.sleep(1000);
                    // 重新请求数据
                    accessTokenService.getAccessToken(false);
                    getReportRecordDetailForBlackLack(qrCode);
                } else {
//                    System.out.println("✅ Token 有效，继续处理...");
                }
            } else {
//                System.err.println("❌ 请求失败，HTTP状态码: " + response.code());
//                System.err.println(response.body().string());
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
//        System.out.println("Code: " + code);
//        System.out.println("Message: " + message);
        Map<String, String> result = new HashMap<>();
        //找到第一个不为0的数
        JsonNode dataNode = root.path("data");
        JsonNode listNode = dataNode.path("list");

        JsonNode firstNotZeroItem = null;

        if (listNode.isArray()) {
            for (JsonNode item : listNode) {

                JsonNode amountNode = item
                        .path("reportBaseAmount")
                        .path("amount");

                if (!amountNode.isMissingNode() && !amountNode.isNull()) {

                    // 统一用 BigDecimal 处理
                    BigDecimal amount = new BigDecimal(amountNode.asText("0"));

                    if (amount.compareTo(BigDecimal.ZERO) != 0) {
                        firstNotZeroItem = item;
                        break;
                    }
                }
            }
        }
//        JsonNode item = root.path("data").path("list").get(0);
//        System.out.println( "报工记录item = " + root.path("data"));
        JsonNode item = firstNotZeroItem;
//        System.out.println( "报工记录item = " + item);

        if (item != null) {
            String processCode = item.get("processCode").asText();
            String materialId = item.path("materialInfo").path("baseInfo").path("id").asText();
            String workOrderId = item.path("workOrderId").asText();
            String taskId = item.path("taskId").asText();
            String materialCode = item.path("materialInfo").path("baseInfo").path("code").asText();
            String batchNo = item.path("batchNo").asText();
            String batchNoId = item.path("batchNoId").asText();
            String workOrderCode = item.path("workOrderCode").asText();
            String processId = item.path("processId").asText();
            String amount = item.path("reportBaseAmountDisplay").path("amount").asText();
            String name = item.path("materialInfo").path("baseInfo").path("name").asText();
            String specification = item.path("materialInfo").path("baseInfo").path("specification").asText();

//            System.out.println("materialId = " + materialId);
//            System.out.println("workOrderId = " + workOrderId);
//            System.out.println("taskId = " + taskId);
//            System.out.println("materialCode = " + materialCode);
//            System.out.println("batchNo = " + batchNo);
//            System.out.println("batchNoId = " + batchNoId);
//            System.out.println("processId = " + processId);
//            System.out.println("amount = " + amount);
//            System.out.println("name = " + name);
//            System.out.println("specification = " + specification);

            result.put("materialId", materialId);
            result.put("workOrderId", workOrderId);
            result.put("taskId", taskId);
            result.put("materialCode", materialCode);
            result.put("batchNo", batchNo);
            result.put("batchNoId", batchNoId);
            result.put("qrCode", qrCode);
            result.put("processId", processId);
            result.put("amount", amount);
            result.put("name", name);
            result.put("specification", specification);
            result.put("workOrderCode", workOrderCode);
            result.put("processCode", processCode);
            //打印结果
//            System.out.println("Material ID: " + result.get("materialId"));
//            System.out.println("Work Order ID: " + result.get("workOrderId"));
//            System.out.println("Task ID: " + result.get("taskId"));
//            System.out.println("✅ 获取成功");
            // 返回前端
            return result;
        }else {
//            System.out.println("❌ 获取失败");
            return null;
        }

        // 存入数据库主表json字段
    }
}

