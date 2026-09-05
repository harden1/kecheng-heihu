package com.ruoyi.apiTool.domain.feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 黑湖开放 API 通用业务响应。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BlackLakeResult {

    private Integer code;
    private String message;
    private JsonNode data;

    /**
     * 判断黑湖业务调用是否成功。
     *
     * @return code 为 200 时返回 true
     */
    public boolean isSuccess() {
        return Integer.valueOf(200).equals(code);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }
}
