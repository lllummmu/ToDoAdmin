package com.todoapp.controller;

import com.todoapp.dao.TaskDAO;
import com.todoapp.model.Task;
import com.todoapp.model.User;              // ✅ 关键
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TaskServlet extends HttpServlet {
    private TaskDAO taskDAO = new TaskDAO();
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>)
                    (src, typeOfSrc, context) -> src == null ? null : new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession();
        User u = (User) session.getAttribute("user");
        if (u == null) {
            resp.setStatus(403);
            return;
        }
        try {
            List<Task> list = taskDAO.getTasksByUser(u.getId());
            resp.getWriter().write(gson.toJson(list));
        } catch (Exception e) {
            resp.setStatus(500);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain;charset=UTF-8");
        HttpSession session = req.getSession();
        User u = (User) session.getAttribute("user");
        if (u == null) {
            resp.getWriter().write("not logged in");
            return;
        }
        String text = req.getParameter("text");
        String priority = req.getParameter("priority");
        String due = req.getParameter("due_date");
        String assignee = req.getParameter("assignee_id");

        Task t = new Task();
        t.setUserId(u.getId());
        t.setText(text);
        t.setPriority(priority);
        t.setDueDate(due != null && !due.isEmpty() ? LocalDateTime.parse(due) : null);
        t.setAssigneeId(assignee != null && !assignee.isEmpty() ? Integer.parseInt(assignee) : null);

        try {
            int newId = taskDAO.addTask(t);
            resp.getWriter().write(newId > 0 ? "success" : "fail");
        } catch (Exception e) {
            resp.getWriter().write("error");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain;charset=UTF-8");
        HttpSession session = req.getSession();
        User u = (User) session.getAttribute("user");
        if (u == null) {
            resp.getWriter().write("not logged in");
            return;
        }
        try {
            // 路径形如 /tasks/12，取最后的 id
            String path = req.getPathInfo();
            int id = Integer.parseInt(path.substring(1));
            // Tomcat 不解析 PUT 表单体，手动解析 completed=true/false
            String body = req.getReader().lines().collect(java.util.stream.Collectors.joining());
            boolean completed = body.contains("completed=true");
            boolean ok = taskDAO.updateCompleted(id, completed);
            resp.getWriter().write(ok ? "success" : "fail");
        } catch (Exception e) {
            resp.getWriter().write("error");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        int id = Integer.parseInt(req.getParameter("id"));
        try {
            boolean ok = new TaskDAO().deleteTask(id);
            resp.getWriter().write(ok ? "success" : "fail");
        } catch (Exception e) {
            resp.getWriter().write("error");
        }
    }
}