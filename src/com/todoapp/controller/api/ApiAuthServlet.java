package com.todoapp.controller.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.todoapp.dao.UserDAO;
import com.todoapp.model.User;
import com.todoapp.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * REST 认证接口：
 *   POST /api/login    {username, password} -> {id, username}
 *   POST /api/register {username, password}
 *   POST /api/logout
 *   GET  /api/me       当前登录用户
 */
public class ApiAuthServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    private String readBody(HttpServletRequest req) throws IOException {
        req.setCharacterEncoding("UTF-8");
        return req.getReader().lines().collect(Collectors.joining());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath().substring(4); // 去掉 /api 前缀
        JsonObject body;
        try {
            body = JsonParser.parseString(readBody(req)).getAsJsonObject();
        } catch (Exception e) {
            JsonUtil.fail(resp, 400, 400, "请求体不是合法 JSON");
            return;
        }

        switch (path) {
            case "/login": {
                String username = body.has("username") ? body.get("username").getAsString() : "";
                String password = body.get("password").getAsString();
                try {
                    User u = userDAO.login(username, password);
                    if (u == null) {
                        JsonUtil.fail(resp, 401, 401, "用户名或密码错误");
                        return;
                    }
                    req.getSession(true).setAttribute("user", u);
                    JsonObject data = new JsonObject();
                    data.addProperty("id", u.getId());
                    data.addProperty("username", u.getUsername());
                    JsonUtil.ok(resp, data);
                } catch (Exception e) {
                    JsonUtil.fail(resp, 500, 500, "服务器错误");
                }
                break;
            }
            case "/register": {
                String username = body.get("username").getAsString();
                String password = body.get("password").getAsString();
                if (username.trim().isEmpty() || password.isEmpty()) {
                    JsonUtil.fail(resp, 400, 400, "用户名和密码不能为空");
                    return;
                }
                try {
                    boolean okFlag = userDAO.register(username, password);
                    if (okFlag) JsonUtil.ok(resp, null);
                    else JsonUtil.fail(resp, 409, 409, "用户名已存在");
                } catch (Exception e) {
                    JsonUtil.fail(resp, 409, 409, "用户名已存在");
                }
                break;
            }
            case "/logout": {
                HttpSession session = req.getSession(false);
                if (session != null) session.invalidate();
                JsonUtil.ok(resp, null);
                break;
            }
            default:
                JsonUtil.fail(resp, 404, 404, "接口不存在");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath().substring(4); // 去掉 /api 前缀
        if (!"/me".equals(path)) {
            JsonUtil.fail(resp, 404, 404, "接口不存在");
            return;
        }
        HttpSession session = req.getSession(false);
        User u = session == null ? null : (User) session.getAttribute("user");
        if (u == null) {
            JsonUtil.fail(resp, 401, 401, "未登录");
            return;
        }
        JsonObject data = new JsonObject();
        data.addProperty("id", u.getId());
        data.addProperty("username", u.getUsername());
        JsonUtil.ok(resp, data);
    }
}
