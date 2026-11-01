package com.todoapp.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一 JSON 响应工具：{ "code": 0, "msg": "ok", "data": ... }
 * code=0 成功；非 0 为业务/鉴权错误码。
 */
public class JsonUtil {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                    (src, typeOfSrc, context) -> src == null ? null
                            : new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .create();

    public static void ok(HttpServletResponse resp, Object data) throws IOException {
        write(resp, 200, 0, "ok", data);
    }

    public static void fail(HttpServletResponse resp, int httpStatus, int code, String msg) throws IOException {
        write(resp, httpStatus, code, msg, null);
    }

    private static void write(HttpServletResponse resp, int httpStatus, int code, String msg, Object data) throws IOException {
        resp.setStatus(httpStatus);
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("msg", msg);
        body.put("data", data);
        resp.getWriter().write(GSON.toJson(body));
    }
}
