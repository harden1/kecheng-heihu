package com.ruoyi.apiTool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.system.domain.SysUserPost;
import com.ruoyi.system.mapper.SysUserPostMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 *
 * @description: 批量报工和不良项报工
 * @author: wmin
 */

@Component
public class ApiBatchReportForBlackLack {
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private SysUserPostMapper sysUserPostMapper;

    public Map<String,Object> batchReportForBlackLack(long currentUserId1,
                                          String qrCode1,
                                          long reportUnitId1,
                                          long reportProcessId1,
                                          int reportAmount1,
                                          long lineId1,
                                          long materialId1,
                                          int qcStatus1,
                                          int reportType1,
                                          long taskId1,
                                          String stopTime,
                                          long batchNoId,
                                          String batchNo,
                                          long reportStartTime,
                                          long reportEndTime) {
        Map<String, Object>  res=new HashMap<>();

        // Access Token 和 X-AUTH 一致
        String accessToken = accessTokenService.getAccessToken(true);
//        System.out.println("✅ Access Token: " + accessToken);
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
//        Map<String, Object> customField1 = new HashMap<>();
//        customField1.put("fieldCode", "cust_field21__c");
//        customField1.put("fieldValue", badItem);

        Map<String, Object> customField2 = new HashMap<>();
        customField2.put("fieldCode", "cust_field30__c");
        customField2.put("fieldValue", stopTime);
// 定义 string 数组

        // 构造 progressReportMaterialItems
        Map<String, Object> materialItem = new HashMap<>();


        materialItem.put("reportAmount", reportAmount1);
        materialItem.put("reportUnitId", reportUnitId1);
        materialItem.put("qrCode", qrCode1);
//        materialItem.put("qrCodeNum", 1);
//        materialItem.put("batchNoId", batchNoId);
        materialItem.put("batchNo", batchNo);
        materialItem.put("customFields", Arrays.asList( customField2));


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
        String[] skipWeakControlRule = {
                "ProgressReportPreProcessMaxReportableAmountRule",
                "ProgressReportMaxReportableAmountRule",
                "ProgressReportMaterialPlannedAmountRule"
        };
        // 添加到 map 中
        jsonMap.put("skipWeakControlRule", skipWeakControlRule);
        jsonMap.put("progressReportItems", Collections.singletonList(reportItem));
        jsonMap.put("progressReportMaterial", reportMaterial);
        jsonMap.put("qcStatus", qcStatus1);
        jsonMap.put("reportStartTime", reportStartTime);
        jsonMap.put("reportEndTime", reportEndTime);
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
//        System.out.println("最终 JSON 请求体:\n" + jsonBody);
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
//                System.out.println("✅ 请求成功，响应内容：");
//                System.out.println(responseStr);
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
                    batchReportForBlackLack(currentUserId1, qrCode1, reportUnitId1, reportProcessId1, reportAmount1,
                            lineId1, materialId1, qcStatus1, reportType1, taskId1, stopTime, batchNoId,
                            batchNo, reportStartTime, reportEndTime);
                } else {
//                    System.out.println("✅ Token 有效，继续处理...");
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
//        System.out.println("Code: " + code);
//        System.out.println("Message: " + message);

        res.put("message", message);
        res.put("code", code);
        res.put("data", root);
        res.put("response", responseStr);
        // 返回前端
        return res;
    }

    /**
     *  不良项目单个报工
     * @param currentUserId1
     * @param qrCode1
     * @param reportUnitId1
     * @param reportProcessId1
     * @param reportAmount1
     * @param lineId1
     * @param materialId1
     * @param qcStatus1
     * @param reportType1
     * @param taskId1
     * @param badItem
     * @param stopTime
     * @param batchNoId
     * @param batchNo
     * @param reportStartTime
     * @param reportEndTime
     * @return
     */
    public Map<String,Object> batchReportForBlackLackOne(long currentUserId1,
                                                      String qrCode1,
                                                      long reportUnitId1,
                                                      long reportProcessId1,
                                                      int reportAmount1,
                                                      long lineId1,
                                                      long materialId1,
                                                      int qcStatus1,
                                                      int reportType1,
                                                      long taskId1,
                                                      String badItem,
                                                      String stopTime,
                                                      long batchNoId,
                                                      String batchNo,
                                                      long reportStartTime,
                                                      long reportEndTime) {
        Map<String, Object>  res=new HashMap<>();

        // Access Token 和 X-AUTH 一致
        String accessToken = accessTokenService.getAccessToken(true);
//        System.out.println("✅ Access Token: " + accessToken);
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
        Map<String, Object> customField1 = new HashMap<>();
        customField1.put("fieldCode", "cust_field21__c");
        customField1.put("fieldValue", badItem);

        Map<String, Object> customField2 = new HashMap<>();
        customField2.put("fieldCode", "cust_field30__c");
        customField2.put("fieldValue", stopTime);


        // 构造 progressReportMaterialItems
        Map<String, Object> materialItem = new HashMap<>();
        materialItem.put("reportAmount", reportAmount1);
        materialItem.put("reportUnitId", reportUnitId1);
//        materialItem.put("qrCode", qrCode1);
//        materialItem.put("qrCodeNum", 1);
//        materialItem.put("batchNoId", batchNoId);
        materialItem.put("batchNo", batchNo);
        materialItem.put("customFields", Arrays.asList(customField1, customField2));


        // 构造 progressReportItems
        Map<String, Object> reportItem = new HashMap<>();
        reportItem.put("executorIds", Collections.singletonList(currentUserId1));
        reportItem.put("progressReportMaterialItems", Collections.singletonList(materialItem));

        // 构造 progressReportMaterial
        Map<String, Object> reportMaterial = new HashMap<>();
        reportMaterial.put("lineId", lineId1);
        reportMaterial.put("materialId", materialId1);
        //工序id
        reportMaterial.put("reportProcessId", reportProcessId1);

        // 构造最终 JSON 对象
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        String[] skipWeakControlRule = {
                "ProgressReportPreProcessMaxReportableAmountRule",
                "ProgressReportMaxReportableAmountRule",
                "ProgressReportMaterialPlannedAmountRule"
        };
        // 添加到 map 中
        jsonMap.put("skipWeakControlRule", skipWeakControlRule);
        jsonMap.put("progressReportItems", Collections.singletonList(reportItem));
        jsonMap.put("progressReportMaterial", reportMaterial);
        jsonMap.put("qcStatus", qcStatus1);
        jsonMap.put("reportStartTime", reportStartTime);
        jsonMap.put("reportEndTime", reportEndTime);
        jsonMap.put("reportType", reportType1);
        //任务id
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
        // System.out.println("最终 JSON 请求体:\n" + jsonBody);
        // 创建 RequestBody 对象
        RequestBody body = RequestBody.create(
                okhttp3.MediaType.parse("application/json; charset=utf-8"),
                jsonBody
        );
//        System.out.println("✅ 请求体:\n" + jsonBody);
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
//                System.out.println("✅ 请求成功，响应内容：");
//                System.out.println(responseStr);
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
                    batchReportForBlackLackOne(currentUserId1, qrCode1, reportUnitId1, reportProcessId1, reportAmount1,
                            lineId1, materialId1, qcStatus1, reportType1, taskId1, badItem, stopTime, batchNoId,
                            batchNo, reportStartTime, reportEndTime);
                } else {
//                    System.out.println("✅ Token 有效，继续处理...");
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
//        System.out.println("Code: " + code);
//        System.out.println("Message: " + message);
        res.put("message", message);
        res.put("code", code);
        res.put("data", root);
        res.put("response", responseStr);
        // 返回前端
        return res;
    }
}
