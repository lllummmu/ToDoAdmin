package com.todoapp.controller.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.todoapp.dao.TaskDAO;
import com.todoapp.model.Task;
import com.todoapp.model.User;
import com.todoapp.util.JsonUtil;
import com.todoapp.util.SseBroadcaster;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST 任务接口：
 *   GET    /api/tasks        当前用户创建或被指派的任务
 *   POST   /api/tasks        新建任务（JSON）
 *   PUT    /api/tasks/{id}   局部更新（仅覆盖请求中出现的字段）
 *   DELETE /api/tasks/{id}
 * 增删改成功后通过 SSE 广播 task-changed 事件。
 */
public class ApiTaskServlet extends HttpServlet {

    private final TaskDAO taskDAO = new TaskDAO();

    private User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (User) session.getAttribute("user");
    }

    private JsonObject readJson(HttpServletRequest req) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String body = req.getReader().lines().collect(Collectors.joining());
        return JsonParser.parseString(body).getAsJsonObject();
    }

    private Integer getIntOrNull(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsInt() : null;
    }

    private String getStrOrNull(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = currentUser(req);
        if (u == null) { JsonUtil.fail(resp, 401, 401, "未登录"); return; }
        try {
            List<Task> list = taskDAO.getTasksByUser(u.getId());
            JsonUtil.ok(resp, list);
        } catch (Exception e) {
            JsonUtil.fail(resp, 500, 500, "查询失败");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = currentUser(req);
        if (u == null) { JsonUtil.fail(resp, 401, 401, "未登录"); return; }
        try {
            JsonObject body = readJson(req);
            String text = getStrOrNull(body, "text");
            if (text == null || text.trim().isEmpty()) {
                JsonUtil.fail(resp, 400, 400, "任务内容不能为空");
                return;
            }
            Task t = new Task();
            t.setUserId(u.getId());
            t.setText(text.trim());
            t.setRemark(getStrOrNull(body, "remark"));
            t.setPriority(body.has("priority") ? body.get("priority").getAsString() : "medium");
            String due = getStrOrNull(body, "dueDate");
            t.setDueDate(due != null && !due.isEmpty() ? LocalDateTime.parse(due) : null);
            t.setAssigneeId(getIntOrNull(body, "assigneeId"));

            int id = taskDAO.addTask(t);
            Task created = taskDAO.getTaskById(id);
            SseBroadcaster.broadcast("task-changed",
                    "{\"action\":\"created\",\"by\":\"" + u.getUsername() + "\"}");
            JsonUtil.ok(resp, created);
        } catch (Exception e) {
            JsonUtil.fail(resp, 500, 500, "创建失败");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = currentUser(req);
        if (u == null) { JsonUtil.fail(resp, 401, 401, "未登录"); return; }
        try {
            String path = req.getPathInfo();
            int id = Integer.parseInt(path.substring(1));
            Task t = taskDAO.getTaskById(id);
            if (t == null) { JsonUtil.fail(resp, 404, 404, "任务不存在"); return; }

            JsonObject body = readJson(req);
            if (body.has("text")) t.setText(getStrOrNull(body, "text"));
            if (body.has("remark")) t.setRemark(getStrOrNull(body, "remark"));
            if (body.has("attachment")) t.setAttachment(getStrOrNull(body, "attachment"));
            if (body.has("completed")) t.setCompleted(body.get("completed").getAsBoolean());
            if (body.has("priority")) t.setPriority(getStrOrNull(body, "priority"));
            if (body.has("dueDate")) {
                String due = getStrOrNull(body, "dueDate");
                t.setDueDate(due == null || due.isEmpty() ? null : LocalDateTime.parse(due));
            }
            if (body.has("assigneeId")) t.setAssigneeId(getIntOrNull(body, "assigneeId"));

            boolean ok = taskDAO.updateTaskFull(t);
            if (!ok) { JsonUtil.fail(resp, 500, 500, "更新失败"); return; }
            SseBroadcaster.broadcast("task-changed",
                    "{\"action\":\"updated\",\"by\":\"" + u.getUsername() + "\"}");
            JsonUtil.ok(resp, taskDAO.getTaskById(id));
        } catch (NumberFormatException e) {
            JsonUtil.fail(resp, 400, 400, "任务 id 不合法");
        } catch (Exception e) {
            JsonUtil.fail(resp, 500, 500, "更新失败");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = currentUser(req);
        if (u == null) { JsonUtil.fail(resp, 401, 401, "未登录"); return; }
        try {
            String path = req.getPathInfo();
            int id = Integer.parseInt(path.substring(1));
            boolean ok = taskDAO.deleteTask(id);
            if (!ok) { JsonUtil.fail(resp, 404, 404, "任务不存在"); return; }
            SseBroadcaster.broadcast("task-changed",
                    "{\"action\":\"deleted\",\"by\":\"" + u.getUsername() + "\"}");
            JsonUtil.ok(resp, null);
        } catch (NumberFormatException e) {
            JsonUtil.fail(resp, 400, 400, "任务 id 不合法");
        } catch (Exception e) {
            JsonUtil.fail(resp, 500, 500, "删除失败");
        }
    }
}
