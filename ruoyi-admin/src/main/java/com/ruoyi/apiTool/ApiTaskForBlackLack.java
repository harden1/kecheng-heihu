package com.ruoyi.apiTool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.apiTool.domain.BlacklackUser;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 *
 * 任务列表
 * @author: wmin
 */

@Component
public class ApiTaskForBlackLack {
    @Autowired
    private AccessTokenService accessTokenService;

    public String getTackForBlackLack(String tackCode) {
        // Access Token 和 X-AUTH 一致
        String accessToken = accessTokenService.getAccessToken(true);
        System.out.println("✅ Access Token: " + accessToken);
        // access_token 作为 query 参数拼接到 URL 上
        HttpUrl url = HttpUrl.parse("\n" +
                        "https://v3-ali.blacklake.cn/api/openapi/domain/web/v1/route"+
                        "/mfg/open/v2/produce_task/_detail")
                .newBuilder()
                .addQueryParameter("access_token", accessToken)
                .build();
        // 请求体 JSON
        String jsonBody = "{ \"taskCode\": \""+tackCode+"\" }";
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
        try (
                Response response = client.newCall(request).execute()) {
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
                    getTackForBlackLack("");
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
        long taskId = root.get("data").get("taskId").asLong();
        String taskCode = root.get("data").get("taskCode").asText();

        System.out.println("Code: " + code);
        System.out.println("Message: " + message);
        System.out.println("Task ID: " + taskId);
        System.out.println("Task Code: " + taskCode);

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
