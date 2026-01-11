package com.ruoyi.apiTool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @description: 获取 access_token
 * @author: wmin
 */

@Service
public class AccessTokenService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TOKEN_KEY = "blacklake-zs:access_token";
//    private static final String TOKEN_KEY = "blacklake-zs:access_token";
    /**
     * 获取 access_token
     *
     * @param tokenState 状态f:获取新token t:使用缓存的token
     */

    public String getAccessToken(boolean tokenState) {

        if (tokenState) {
            // 优先从 Redis 读取
            String cachedToken = redisTemplate.opsForValue().get(TOKEN_KEY);
            if (cachedToken != null) {
                return cachedToken;
            }
            // 向远程请求新 token
            return getToken();
        } else {
            // 向远程请求新 token
            return getToken();
        }
    }

    private String getToken() {
        String url = "https://v3-ali.blacklake.cn/api/openapi/domain/api/v1/access_token/_get_access_token";
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("appKey", "cli_1757299485943603");
        requestBody.put("appSecret", "d264188820584a67a9e644e48058b905");
//        requestBody.put("appKey", "cli_1749190336089356");
//        requestBody.put("appSecret", "34d5153660bf47f9ba61031123577ac5");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        try {
            // 解析 JSON 返回体
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode data = root.path("data");
            String token = data.path("appAccessToken").asText();
            long expire = data.path("expire").asLong(); // 单位是秒

            // 存入 Redis（1h）
            redisTemplate.opsForValue().set(TOKEN_KEY, token, 3600, TimeUnit.SECONDS);
            return token;
        } catch (Exception e) {
            throw new RuntimeException("获取 access_token 失败", e);
        }
    }

}