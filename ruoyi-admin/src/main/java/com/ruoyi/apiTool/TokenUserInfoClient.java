package com.ruoyi.apiTool;

import okhttp3.*;
import java.io.IOException;

/**
 * 测试接口用
 */

public class TokenUserInfoClient {

    public static void main(String[] args) throws IOException {
        // 替换为你的实际 token 和 userAccessToken
        String appAccessToken = "your_app_access_token";
        String userAccessToken = "your_user_access_token";

        OkHttpClient client = new OkHttpClient();

        // 构造 JSON 请求体
        String jsonBody = "{"
                + "\"grantType\":\"authorization_code\","
                + "\"userAccessToken\":\"" + userAccessToken + "\""
                + "}";

        // 创建请求体
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), jsonBody);

        // 构造请求 URL（access_token 是 query 参数）
        HttpUrl url = HttpUrl.parse("https://v3-ali.blacklake.cn/api/openapi/open/v1/access_token/_get_user_info")
                .newBuilder()
                .addQueryParameter("access_token", appAccessToken)
                .build();

        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        // 发送请求
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("响应内容：" + response.body().string());
            } else {
                System.err.println("请求失败，状态码：" + response.code());
            }
        }
    }
}