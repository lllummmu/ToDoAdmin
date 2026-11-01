package com.todoapp.controller.api;

import com.google.gson.JsonObject;
import com.todoapp.model.User;
import com.todoapp.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * POST /api/upload  multipart 文件上传（字段名 file）
 * 文件保存到 webapp 的 /uploads 目录，返回可访问的相对 URL。
 */
public class UploadServlet extends HttpServlet {

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User u = session == null ? null : (User) session.getAttribute("user");
        if (u == null) { JsonUtil.fail(resp, 401, 401, "未登录"); return; }

        Part part;
        try {
            part = req.getPart("file");
        } catch (Exception e) {
            JsonUtil.fail(resp, 400, 400, "缺少文件字段 file");
            return;
        }
        if (part == null || part.getSize() == 0) {
            JsonUtil.fail(resp, 400, 400, "文件为空");
            return;
        }
        if (part.getSize() > MAX_SIZE) {
            JsonUtil.fail(resp, 400, 400, "文件不能超过 10MB");
            return;
        }

        String submitted = part.getSubmittedFileName();
        String ext = "";
        if (submitted != null && submitted.contains(".")) {
            ext = submitted.substring(submitted.lastIndexOf('.'));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        String dir = getServletContext().getRealPath("/uploads");
        File uploadDir = new File(dir);
        if (!uploadDir.exists()) uploadDir.mkdirs();
        part.write(new File(uploadDir, fileName).getAbsolutePath());

        JsonObject data = new JsonObject();
        data.addProperty("url", "uploads/" + fileName);
        data.addProperty("name", submitted == null ? fileName : submitted);
        data.addProperty("size", part.getSize());
        JsonUtil.ok(resp, data);
    }
}
