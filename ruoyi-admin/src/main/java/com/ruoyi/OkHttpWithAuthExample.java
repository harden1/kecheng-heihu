package com.ruoyi;

import okhttp3.*;

import java.io.IOException;
import java.util.Base64;

public class OkHttpWithAuthExample {
    private static final String APP_KEY = "yourAppKey";
    private static final String SECRET = "yourSecret";
    private static final OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) throws Exception {
        String url = "https://api.example.com/endpoint";

        // 创建请求体（如果有必要）
        RequestBody body = new FormBody.Builder()
                .add("param1", "value1")
                .build();

        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .post(body) // 根据实际情况选择GET或POST
                .addHeader("App-Key", APP_KEY)
                .addHeader("Authorization", createBasicAuthHeader(APP_KEY, SECRET))
                .build();

        // 执行请求
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            System.out.println(response.body().string());
        }
    }

    // 创建基本认证的header
    private static String createBasicAuthHeader(String appKey, String secret) {
        String credentials = appKey + ":" + secret;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}