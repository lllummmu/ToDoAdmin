package com.todoapp.controller;

import com.todoapp.dao.UserDAO;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

public class RegisterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain;charset=UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            boolean ok = new UserDAO().register(username, password);
            resp.getWriter().write(ok ? "success" : "exist");
        } catch (Exception e) {
            resp.getWriter().write("error");
        }
    }
}