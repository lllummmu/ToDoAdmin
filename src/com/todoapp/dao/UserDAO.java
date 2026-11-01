package com.todoapp.dao;

import com.todoapp.model.User;
import com.todoapp.util.DBUtil;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class UserDAO {
    public User login(String username, String password) throws Exception {
        String sql = "SELECT * FROM users WHERE username=? AND password=MD5(?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                return u;
            }
        }
        return null;
    }

    public boolean register(String username, String password) throws Exception {
        String sql = "INSERT INTO users(username, password) VALUES (?, MD5(?))";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            return ps.executeUpdate() > 0;
        }
    }

    public User getUserById(int id) throws Exception {
        String sql = "SELECT * FROM users WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                return u;
            }
        }
        return null;
    }

    public List<User> getAllUsers() throws Exception {
        String sql = "SELECT id, username FROM users";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<User> list = new ArrayList<>();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                list.add(u);
            }
            return list;
        }
    }
}