package com.todoapp.dao;

import com.todoapp.model.Task;
import com.todoapp.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class TaskDAO {

    private Task mapRow(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getInt("user_id"));
        int assigneeRaw = rs.getInt("assignee_id");
        t.setAssigneeId(rs.wasNull() ? null : assigneeRaw);
        t.setText(rs.getString("text"));
        t.setRemark(rs.getString("remark"));
        t.setAttachment(rs.getString("attachment"));
        t.setCompleted(rs.getBoolean("completed"));
        t.setPriority(rs.getString("priority"));
        Timestamp due = rs.getTimestamp("due_date");
        t.setDueDate(due != null ? due.toLocalDateTime() : null);
        Timestamp created = rs.getTimestamp("created_at");
        t.setCreatedAt(created != null ? created.toLocalDateTime() : null);
        Timestamp updated = rs.getTimestamp("updated_at");
        t.setUpdatedAt(updated != null ? updated.toLocalDateTime() : null);
        return t;
    }

    public List<Task> getTasksByUser(int userId) throws Exception {
        String sql = "SELECT * FROM tasks WHERE user_id=? OR assignee_id=? ORDER BY id DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            List<Task> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        }
    }

    public Task getTaskById(int id) throws Exception {
        String sql = "SELECT * FROM tasks WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        }
    }

    /** 新增任务，返回生成的主键 id */
    public int addTask(Task task) throws Exception {
        String sql = "INSERT INTO tasks(user_id, assignee_id, text, remark, priority, due_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, task.getUserId());
            if (task.getAssigneeId() == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, task.getAssigneeId());
            ps.setString(3, task.getText());
            ps.setString(4, task.getRemark());
            ps.setString(5, task.getPriority());
            if (task.getDueDate() == null) ps.setNull(6, Types.TIMESTAMP);
            else ps.setObject(6, task.getDueDate());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    /** 整体更新（课程版保留） */
    public boolean updateTask(Task task) throws Exception {
        String sql = "UPDATE tasks SET text=?, completed=?, priority=?, due_date=?, assignee_id=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getText());
            ps.setBoolean(2, task.isCompleted());
            ps.setString(3, task.getPriority());
            ps.setObject(4, task.getDueDate());
            ps.setObject(5, task.getAssigneeId());
            ps.setInt(6, task.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /** 整体更新（含扩展字段）：调用方先 getTaskById 读出原值、覆盖要改的字段后再调用 */
    public boolean updateTaskFull(Task task) throws Exception {
        String sql = "UPDATE tasks SET text=?, remark=?, attachment=?, completed=?, priority=?, due_date=?, assignee_id=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getText());
            ps.setString(2, task.getRemark());
            ps.setString(3, task.getAttachment());
            ps.setBoolean(4, task.isCompleted());
            ps.setString(5, task.getPriority());
            if (task.getDueDate() == null) ps.setNull(6, Types.TIMESTAMP);
            else ps.setObject(6, task.getDueDate());
            if (task.getAssigneeId() == null) ps.setNull(7, Types.INTEGER);
            else ps.setInt(7, task.getAssigneeId());
            ps.setInt(8, task.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateCompleted(int id, boolean completed) throws Exception {
        String sql = "UPDATE tasks SET completed=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, completed);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteTask(int id) throws Exception {
        String sql = "DELETE FROM tasks WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
