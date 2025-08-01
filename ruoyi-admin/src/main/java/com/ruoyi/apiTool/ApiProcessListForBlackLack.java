package com.ruoyi.apiTool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ApiProcessListForBlackLack {
    @Autowired
    private AccessTokenService accessTokenService;

    public Map<String, String> getWareHouseDetailForBlackLack(String workOrderIdList,String processId) {

        // Access Token 和 X-AUTH 一致
        String accessToken = accessTokenService.getAccessToken(true);
        System.out.println("✅ Access Token: " + accessToken);
        // access_token 作为 query 参数拼接到 URL 上
        HttpUrl url = HttpUrl.parse("https://v3-ali.blacklake.cn/api/openapi/domain/web/v1/route\n" +
                        "/mfg/open/v1/produce_task/_list")
                .newBuilder()
                .addQueryParameter("access_token", accessToken)
                .build();
        // 请求体 JSON
        String jsonBody = "{ \"workOrderIdList\":[ \""+workOrderIdList+"\" ]}";
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
                    getWareHouseDetailForBlackLack(workOrderIdList, processId);
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
        System.out.println("Code: " + code);
        System.out.println("Message: " + message);
// 获取任务列表
        JsonNode taskList = root.path("data").path("list");

        System.out.println("找到的任务数量: " + taskList.size());
        System.out.println("----------------------------------");
        Map<String, String> result = new HashMap<>();
        // 遍历每个任务项
        for (int i = 0; i < taskList.size(); i++) {
            JsonNode task = taskList.get(i);
            // 提取关键信息
            String processId1 = task.path("processId").asText();

            // 打印任务信息
            System.out.println("工序ID: " + processId1);
            System.out.println("----------------------------------");
            if (processId1.equals(processId)){
                //返回下一个工序的id
                JsonNode task1 = taskList.get(i+1);
                // 提取关键信息
                String taskId = task1.path("taskId").asText();
                String taskCode = task1.path("taskCode").asText();
                String processId2 = task1.path("processId").asText();
                String processName = task1.path("processName").asText();
                String status = task1.path("taskStatus").path("message").asText();
                String workOrderCode = task1.path("workOrderCode").asText();
                result.put("taskId", taskId);
                result.put("taskCode", taskCode);
                result.put("processId", processId2);
                result.put("processName", processName);
                result.put("status", status);
                result.put("workOrderCode", workOrderCode);
                return result;
            }
        }

        return null;
    }
}

