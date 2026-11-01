package com.todoapp.controller;

import com.todoapp.dao.UserDAO;
import com.todoapp.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain;charset=UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            User u = new UserDAO().login(username, password);
            if (u != null) {
                HttpSession session = req.getSession();
                session.setAttribute("user", u);
                resp.getWriter().write("success");
            } else {
                resp.getWriter().write("fail");
            }
        } catch (Exception e) {
            resp.getWriter().write("error");
        }
    }
}