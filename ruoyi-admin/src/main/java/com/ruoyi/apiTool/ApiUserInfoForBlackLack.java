package com.ruoyi.apiTool;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.apiTool.domain.BlacklackUser;
import com.ruoyi.apiTool.mapper.BlacklackUserMapper;
import com.ruoyi.apiTool.service.IBlacklackUserService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Component
public class ApiUserInfoForBlackLack {
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private IBlacklackUserService blacklackUserService;
    @Autowired
    private BlacklackUserMapper blacklackUserMapper;
    public void getUserApiForBlacklack() {
        if (accessTokenService == null) {
            System.err.println("❌ accessTokenService 未初始化");
            return;
        }
        // 请求地址
        String url = "https://v3-ali.blacklake.cn/api/openapi/domain/web/v1/route/user/open/v1/user/_list"; // 替换为真实接口地址
        // Access Token 和 X-AUTH 一致
        String accessToken = accessTokenService.getAccessToken(true);
        System.out.println("✅ Access Token: " + accessToken);
        OkHttpClient client = new OkHttpClient();

        // 请求体 JSON
        String jsonBody = "{ \"access_token\": \"" + accessToken + "\" }";

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

                    // 处理失效逻辑（如刷新 token、抛异常等）
                    //重新获取token
                    accessToken = accessTokenService.getAccessToken(false);
                    //间隔1s
                    Thread.sleep(1000);
                    // 重新请求数据
                    getUserApiForBlacklack();

                } else {
                    System.out.println("✅ Token 有效，继续处理...");
                    // 可读取 data 节点等 jsonNode.path("data")
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
        String userJson = responseStr;
        // 1. 读取 JSON 文件
        ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = null;
            try {
                root = objectMapper.readTree(userJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            JsonNode users = root.path("data").path("list");
            // 2. 连接数据库
            List<BlacklackUser> blacklackUserList = new ArrayList<>();
            for (JsonNode user : users) {
                long id = user.path("id").asLong();
                String username = user.path("username").asText();
                String name = user.path("name").asText();
                int active = user.path("active").asInt();
                // 角色（只取第一个）
                JsonNode roleList = user.path("roleList");
                String roleName = null;
                if (roleList.isArray() && roleList.size() > 0) {
                    roleName = roleList.get(0).path("name").asText(null);
                }
                // 部门（只取第一个）
                JsonNode deptList = user.path("departmentVOList");
                String departmentName = null;
                if (deptList.isArray() && deptList.size() > 0) {
                    departmentName = deptList.get(0).path("name").asText(null);
                }
                // 时间戳转换（毫秒转秒 -> Timestamp）
                Timestamp createdAt = new Timestamp(user.path("createdAt").asLong());
                Timestamp updatedAt = new Timestamp(user.path("updatedAt").asLong());
                // 设置参数
                BlacklackUser blacklackUser = new BlacklackUser();
                blacklackUser.setId(id);
                blacklackUser.setUsername(username);
                blacklackUser.setName(name);
                blacklackUser.setRoleName(roleName);
                blacklackUser.setDepartmentName(departmentName);
                blacklackUser.setActive((long) active);
                blacklackUser.setCreatedAt(createdAt);
                blacklackUser.setUpdatedAt(updatedAt);
                blacklackUserList.add(blacklackUser);
            }
            //打印数据
            for (BlacklackUser user : blacklackUserList) {
                System.out.println(user);
            }
        blacklackUserService.replaceAll(blacklackUserList);


        System.out.println("✅ 数据已插入 MySQL");
    }
}




