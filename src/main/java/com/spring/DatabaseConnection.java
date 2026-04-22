package com.spring;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Database connection management class for MariaDB.
 * MariaDB数据库连接管理类。
 */
public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    // Support environment variables for flexible configuration
    private static final String URL = chooseValue(
        System.getenv("DB_URL"),
        "jdbc:mariadb://localhost:3306/shopping_cart_localization?useUnicode=true&characterEncoding=UTF-8"
    );
    
    private static final String USERNAME = chooseValue(System.getenv("DB_USER"), "root");
    
    private static final String PASSWORD = chooseValue(System.getenv("DB_PASSWORD"), "root");

    private DatabaseConnection() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Get a connection to the database.
     * 获取数据库连接。
     *
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return getConnection("org.mariadb.jdbc.Driver", URL, USERNAME, PASSWORD);
    }

    static Connection getConnection(String driverClass, String url, String username, String password) throws SQLException {
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MariaDB JDBC Driver not found", e);
        }
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Close a database connection.
     * 关闭数据库连接。
     *
     * @param connection the connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.severe("Error closing connection: " + e.getMessage());
            }
        }
    }

    static String chooseValue(String valueFromEnvironment, String defaultValue) {
        return valueFromEnvironment != null ? valueFromEnvironment : defaultValue;
    }
}
