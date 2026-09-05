package com.ruoyi.apiTool.feed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.apiTool.AccessTokenService;
import com.ruoyi.apiTool.client.BlackLakeApiClient;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 统一黑湖客户端测试。
 */
class BlackLakeApiClientTest {

    private AccessTokenService accessTokenService;
    private Call.Factory callFactory;
    private Call call;
    private BlackLakeApiClient client;

    @BeforeEach
    void setUp() {
        accessTokenService = mock(AccessTokenService.class);
        callFactory = mock(Call.Factory.class);
        call = mock(Call.class);
        when(accessTokenService.getAccessToken(true)).thenReturn("cached-token");
        when(callFactory.newCall(any(Request.class))).thenReturn(call);
        client = new BlackLakeApiClient(accessTokenService, new ObjectMapper(), callFactory);
    }

    @Test
    void shouldReturnJsonForSuccessfulResponse() throws Exception {
        when(call.execute()).thenAnswer(invocation -> response(200, "{\"code\":200,\"data\":{\"id\":1}}"));

        JsonNode result = client.post("/inventory/open/v1/material_inventory/_list",
                Collections.singletonMap("qrCodes", Collections.singletonList("QR-1")));

        assertEquals(200, result.path("code").asInt());
        assertEquals(1, result.path("data").path("id").asInt());
        verify(accessTokenService, times(1)).getAccessToken(true);
        verify(callFactory).newCall(org.mockito.ArgumentMatchers.argThat(request ->
                !request.url().toString().contains("\n")
                        && "cached-token".equals(request.header("X-AUTH"))
                        && "cached-token".equals(request.url().queryParameter("access_token"))
                        && request.headers("Content-Type").isEmpty()
                        && request.body() != null
                        && "application/json; charset=utf-8".equals(request.body().contentType().toString())));
    }

    @Test
    void shouldRejectNonSuccessfulHttpStatus() throws Exception {
        when(call.execute()).thenAnswer(invocation -> response(503, "{\"code\":503,\"message\":\"unavailable\"}"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.post("/test", Collections.emptyMap()));

        assertTrue(exception.getMessage().contains("HTTP 503"));
    }

    @Test
    void shouldRejectNonSuccessfulBusinessCode() throws Exception {
        when(call.execute()).thenAnswer(invocation -> response(200, "{\"code\":400001,\"message\":\"invalid request\"}"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.post("/test", Collections.emptyMap()));

        assertTrue(exception.getMessage().contains("400001"));
        assertTrue(exception.getMessage().contains("invalid request"));
        assertFalse(exception.getMessage().contains("cached-token"));
    }

    @Test
    void shouldDistinguishInvalidJsonFromNetworkFailure() throws Exception {
        when(call.execute()).thenAnswer(invocation -> response(200, "not-json"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.post("/test", Collections.emptyMap()));

        assertTrue(exception.getMessage().contains("响应 JSON 解析失败"));
        assertFalse(exception.getMessage().contains("cached-token"));
    }

    @Test
    void shouldRefreshTokenAndRetryOnlyOnce() throws Exception {
        Call retryCall = mock(Call.class);
        when(accessTokenService.getAccessToken(true)).thenReturn("expired-token", "refreshed-token");
        when(accessTokenService.getAccessToken(false)).thenReturn("refreshed-token");
        when(callFactory.newCall(any(Request.class))).thenReturn(call, retryCall);
        when(call.execute()).thenAnswer(invocation -> response(200,
                "{\"code\":400150,\"subCode\":\"USER-DOMAIN/SSO_TOKEN_FAIL\"}"));
        when(retryCall.execute()).thenAnswer(invocation -> response(200,
                "{\"code\":400150,\"subCode\":\"USER-DOMAIN/SSO_TOKEN_FAIL\"}"));

        assertThrows(IllegalStateException.class, () -> client.post("/test", Collections.emptyMap()));

        verify(accessTokenService, times(1)).getAccessToken(false);
        verify(callFactory, times(2)).newCall(any(Request.class));
    }

    @Test
    void shouldReturnRetriedResponseAfterTokenRefresh() throws Exception {
        Call retryCall = mock(Call.class);
        when(accessTokenService.getAccessToken(true)).thenReturn("expired-token", "refreshed-token");
        when(accessTokenService.getAccessToken(false)).thenReturn("refreshed-token");
        when(callFactory.newCall(any(Request.class))).thenReturn(call, retryCall);
        when(call.execute()).thenAnswer(invocation -> response(200,
                "{\"code\":400150,\"subCode\":\"USER-DOMAIN/SSO_TOKEN_FAIL\"}"));
        when(retryCall.execute()).thenAnswer(invocation -> response(200, "{\"code\":200}"));

        JsonNode result = client.post("/test", Collections.emptyMap());

        assertEquals(200, result.path("code").asInt());
        verify(accessTokenService, times(1)).getAccessToken(false);
        verify(callFactory, times(2)).newCall(any(Request.class));
    }

    @Test
    void shouldExposeConnectionFailureWithoutRetrying() throws Exception {
        when(call.execute()).thenThrow(new ConnectException("connection refused"));

        UncheckedIOException exception = assertThrows(UncheckedIOException.class,
                () -> client.post("/test", Collections.emptyMap()));

        assertTrue(exception.getCause() instanceof ConnectException);
        verify(callFactory, times(1)).newCall(any(Request.class));
    }

    @Test
    void shouldExposeReadTimeoutWithoutRetrying() throws Exception {
        when(call.execute()).thenThrow(new SocketTimeoutException("timeout"));

        UncheckedIOException exception = assertThrows(UncheckedIOException.class,
                () -> client.post("/test", Collections.emptyMap()));

        assertTrue(exception.getCause() instanceof SocketTimeoutException);
        assertFalse(exception.getMessage().contains("cached-token"));
        verify(callFactory, times(1)).newCall(any(Request.class));
    }

    private Response response(int statusCode, String json) {
        Request request = new Request.Builder().url("http://localhost/test").build();
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(statusCode)
                .message("test")
                .body(ResponseBody.create(MediaType.parse("application/json"), json))
                .build();
    }
}
