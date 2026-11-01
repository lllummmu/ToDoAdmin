package com.todoapp.controller.api;

import com.todoapp.dao.UserDAO;
import com.todoapp.model.User;
import com.todoapp.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * GET /api/users 用户列表（任务指派下拉框用）
 */
public class ApiUserServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User u = session == null ? null : (User) session.getAttribute("user");
        if (u == null) { JsonUtil.fail(resp, 401, 401, "未登录"); return; }
        try {
            List<User> list = userDAO.getAllUsers();
            JsonUtil.ok(resp, list);
        } catch (Exception e) {
            JsonUtil.fail(resp, 500, 500, "查询失败");
        }
    }
}
