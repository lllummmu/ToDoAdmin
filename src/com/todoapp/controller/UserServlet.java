package com.todoapp.controller;

import com.todoapp.dao.UserDAO;
import com.todoapp.model.User;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class UserServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            List<User> list = userDAO.getAllUsers();
            resp.getWriter().write(gson.toJson(list));
        } catch (Exception e) {
            resp.getWriter().write("[]");
        }
    }
}