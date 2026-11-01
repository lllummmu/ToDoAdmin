package com.todoapp.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/todo_app?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "******";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
