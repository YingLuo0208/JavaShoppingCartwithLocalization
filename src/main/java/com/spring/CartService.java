package com.spring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

/**
 * Service class for saving shopping cart records to the database.
 * 将购物车记录保存到数据库的服务类。
 */
public class CartService {

    /**
     * Save the entire shopping cart to the database.
     * 将整个购物车保存到数据库。
     *
     * @param totalItems total number of items
     * @param totalCost total cost of all items
     * @param language language code used
     * @param items list of cart items (price, quantity, subtotal)
     * @return the generated cart_record_id, or -1 if failed
     */
    public int saveCart(int totalItems, double totalCost, String language, List<CartItem> items) {
        if (items == null) {
            System.err.println("Error saving cart: items list cannot be null.");
            return -1;
        }

        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String recordSql = "INSERT INTO cart_records (total_items, total_cost, language, created_at) VALUES (?, ?, ?, ?)";
            int cartRecordId;

            try (PreparedStatement insertRecordStmt = conn.prepareStatement(recordSql, Statement.RETURN_GENERATED_KEYS)) {
                insertRecordStmt.setInt(1, totalItems);
                insertRecordStmt.setDouble(2, totalCost);
                insertRecordStmt.setString(3, language);
                insertRecordStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

                int affectedRows = insertRecordStmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating cart record failed, no rows affected.");
                }

                try (ResultSet generatedKeys = insertRecordStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        cartRecordId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating cart record failed, no ID obtained.");
                    }
                }
            }

            String itemSql = "INSERT INTO cart_items (cart_record_id, item_number, price, quantity, subtotal) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement insertItemStmt = conn.prepareStatement(itemSql)) {
                int itemNumber = 1;
                for (CartItem item : items) {
                    if (item == null) {
                        throw new SQLException("Cart item cannot be null.");
                    }
                    insertItemStmt.setInt(1, cartRecordId);
                    insertItemStmt.setInt(2, itemNumber++);
                    insertItemStmt.setDouble(3, item.getPrice());
                    insertItemStmt.setInt(4, item.getQuantity());
                    insertItemStmt.setDouble(5, item.getSubtotal());
                    insertItemStmt.addBatch();
                }

                insertItemStmt.executeBatch();
            }

            conn.commit();
            System.out.println("Cart saved successfully! Record ID: " + cartRecordId);
            return cartRecordId;

        } catch (SQLException e) {
            System.err.println("Error saving cart: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            return -1;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DatabaseConnection.closeConnection(conn);
                } catch (SQLException e) {
                    System.err.println("Error closing resources: " + e.getMessage());
                }
            }
        }
    }
}
