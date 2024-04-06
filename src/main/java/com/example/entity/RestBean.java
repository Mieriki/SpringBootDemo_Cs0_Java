package com.example.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

public record RestBean<T> (Integer code, T data, String message) {
    public static <T> RestBean<T> success() {
        return success(null, "请求成功");
    }

    public static <T> RestBean<T> success(T data, String message) {
        return new RestBean<>(200, data, message);
    }

    public static <T> RestBean<T> failure(Integer code, String message) {
        return new RestBean<>(code, null, message);
    }

    public static <T> RestBean<T> unauthoirzed(String message) {
        return new RestBean<>(401, null, message);
    }
    public static <T> RestBean<T> forbidden(String message) {
        return new RestBean<>(403, null, message);
    }
    public String asJosnString() {
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }
}
