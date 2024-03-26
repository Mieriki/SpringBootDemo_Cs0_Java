package com.example.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

public record RestBean<T> (Integer code, T data, String massage) {
    public static <T> RestBean<T> success() {
        return success(null, "请求成功");
    }

    public static <T> RestBean<T> success(T data, String massage) {
        return new RestBean<>(200, data, massage);
    }

    public static <T> RestBean<T> failure(Integer code, String massage) {
        return new RestBean<>(code, null, massage);
    }
    public String asJosnString() {
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }
}
