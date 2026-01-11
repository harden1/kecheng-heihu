package com.ruoyi.apiTool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.ConnectionPoolDataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @description: 获取物料详情
 * @author: wmin
 */

@Component
public class ApiMaterialDetailForBlackLack {
    @Autowired
    private AccessTokenService accessTokenService;


    public Map<String, String> getMaterialDetailForBlackLack(String MaterialCode) {

        // Access Token 和 X-AUTH 一致
        String accessToken = accessTokenService.getAccessToken(true);
//        System.out.println("✅ Access Token: " + accessToken);
        // access_token 作为 query 参数拼接到 URL 上
        HttpUrl url = HttpUrl.parse("\n" +
                        "https://v3-ali.blacklake.cn/api/openapi/domain/web/v1/route\n" +
                        "/material/open/v2/material/_detail_by_code")
                .newBuilder()
                .addQueryParameter("access_token", accessToken)
                .build();
        // 请求体 JSON

        String jsonBody = "{ \"code\":\""+MaterialCode+"\" }";
//        System.out.println(
//                "✅ 请求体 JSON: " + jsonBody
//        );
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
                System.out.println("✅ ❌物料请求成功，响应内容：");
                System.out.println(responseStr);
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
                    getMaterialDetailForBlackLack(MaterialCode);
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
        JsonNode item = root.path("data");
//        System.out.println("item: " + item);
        // 2. 提取花纹型号（从name字段）
        String patternModel = item.path("name").asText("未知型号");
        String unitId=item.path("unit").path("id").asText();
//        System.out.println("unitId = " + unitId);
        // 3. 提取颜色和度数（从customFields数组）
        String color = "未知颜色";
        String degree = "未知度数";

        JsonNode customFields = item.path("customFields");
        for (JsonNode field : customFields) {
            String fieldName = field.path("fieldName").asText();

            if ("颜色".equals(fieldName)) {
                color = field.path("fieldValue").asText("未知颜色");
            } else if ("度数".equals(fieldName)) {
                degree = field.path("fieldValue").asText("未知度数");
            }

            // 如果已经找到两个值，可以提前退出循环
            if (!"未知颜色".equals(color) && !"未知度数".equals(degree)) {
                break;
            }
        }

        // 打印提取结果
//        System.out.println("提取结果:");
//        System.out.println("花纹型号: " + patternModel);
//        System.out.println("颜色: " + color);
//        System.out.println("度数: " + degree);


        // 返回前端
        Map<String, String> result= new HashMap<>();
        result.put("name", String.valueOf(patternModel));
        result.put("color", String.valueOf(color));
        result.put("degree", String.valueOf(degree));
        result.put("unitId", String.valueOf(unitId));
        //取出想要的字符串返回到前端显示和后端建表
        return result;
    }
}
