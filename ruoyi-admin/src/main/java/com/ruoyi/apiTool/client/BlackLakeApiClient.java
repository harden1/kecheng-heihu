package com.ruoyi.apiTool.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.apiTool.AccessTokenService;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * 统一封装黑湖开放 API 的 JSON POST 请求和令牌刷新逻辑。
 */
@Component
public class BlackLakeApiClient {

    private static final String BASE_URL = "https://v3-ali.blacklake.cn/api/openapi/domain/web/v1/route";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    private final AccessTokenService accessTokenService;
    private final ObjectMapper objectMapper;
    private final Call.Factory callFactory;

    /**
     * 使用共享 OkHttpClient 创建生产客户端。
     *
     * @param accessTokenService 令牌服务
     * @param objectMapper JSON 转换器
     */
    @Autowired
    public BlackLakeApiClient(AccessTokenService accessTokenService, ObjectMapper objectMapper) {
        this(accessTokenService, objectMapper, HTTP_CLIENT);
    }

    /**
     * 创建可注入请求工厂的客户端，便于隔离外部网络测试。
     *
     * @param accessTokenService 令牌服务
     * @param objectMapper JSON 转换器
     * @param callFactory OkHttp 请求工厂
     */
    public BlackLakeApiClient(AccessTokenService accessTokenService, ObjectMapper objectMapper,
                              Call.Factory callFactory) {
        this.accessTokenService = accessTokenService;
        this.objectMapper = objectMapper;
        this.callFactory = callFactory;
    }

    /**
     * 向黑湖接口发送 JSON POST 请求。
     *
     * @param path 路由后的接口路径
     * @param requestBody 请求对象
     * @return 业务码为 200 的完整响应
     */
    public JsonNode post(String path, Object requestBody) {
        return post(path, requestBody, false);
    }

    /**
     * Token 失效时只刷新并重试一次，避免递归重试失控。
     */
    private JsonNode post(String path, Object requestBody, boolean retried) {
        String token = accessTokenService.getAccessToken(true);
        JsonNode response = execute(path, requestBody, token);
        if (isTokenExpired(response) && !retried) {
            // 强制刷新会同步写入缓存，下一次请求再读取新 Token。
            accessTokenService.getAccessToken(false);
            return post(path, requestBody, true);
        }
        int code = response.path("code").asInt();
        if (code != 200) {
            String message = response.path("message").asText();
            throw new IllegalStateException("黑湖接口业务调用失败，code=" + code + "，message=" + message);
        }
        return response;
    }

    /**
     * 执行单次 HTTP 调用；网络异常保留原始类型供上层判定结果状态。
     */
    private JsonNode execute(String path, Object requestBody, String token) {
        HttpUrl url = buildUrl(path, token);
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("黑湖接口请求体序列化失败", e);
        }

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON, jsonBody))
                .addHeader("X-AUTH", token)
                .build();
        String responseContent;
        try (Response response = callFactory.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("黑湖接口 HTTP " + response.code());
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalStateException("黑湖接口响应体为空");
            }
            responseContent = responseBody.string();
        } catch (IOException e) {
            throw new UncheckedIOException("调用黑湖接口发生网络异常", e);
        }
        try {
            return objectMapper.readTree(responseContent);
        } catch (JsonProcessingException e) {
            // 响应格式错误属于明确解析失败，不能按连接或读取异常处理。
            throw new IllegalStateException("黑湖接口响应 JSON 解析失败", e);
        }
    }

    /**
     * 拼接不含换行的固定网关地址及认证参数。
     */
    private HttpUrl buildUrl(String path, String token) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        HttpUrl url = HttpUrl.parse(BASE_URL + normalizedPath);
        if (url == null) {
            throw new IllegalArgumentException("黑湖接口路径无效");
        }
        return url.newBuilder()
                .addQueryParameter("access_token", token)
                .build();
    }

    /**
     * 判断黑湖单点登录令牌失效响应。
     */
    private boolean isTokenExpired(JsonNode response) {
        return response.path("code").asInt() == 400150
                && "USER-DOMAIN/SSO_TOKEN_FAIL".equals(response.path("subCode").asText());
    }
}
