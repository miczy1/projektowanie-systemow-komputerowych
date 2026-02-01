package com.veriflow.veriflow.database;

import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:h2:~/veriflow_db";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public DatabaseManager() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE, " +
                    "password VARCHAR(50), " +
                    "email VARCHAR(100))";
            stmt.execute(sql);

            // Login: admin, Hasło: admin123
            stmt.execute("MERGE INTO users KEY(username) VALUES (1, 'admin', 'admin123', 'admin@veriflow.com')");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public String getUserEmail(String username, String password) {
        String query = "SELECT email FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
