package com.spring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        Connection conn = null;
        PreparedStatement insertRecordStmt = null;
        PreparedStatement insertItemStmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);  // Start transaction

            // 1. Insert into cart_records
            String recordSql = "INSERT INTO cart_records (total_items, total_cost, language, created_at) VALUES (?, ?, ?, ?)";
            insertRecordStmt = conn.prepareStatement(recordSql, PreparedStatement.RETURN_GENERATED_KEYS);

            insertRecordStmt.setInt(1, totalItems);
            insertRecordStmt.setDouble(2, totalCost);
            insertRecordStmt.setString(3, language);
            insertRecordStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            int affectedRows = insertRecordStmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating cart record failed, no rows affected.");
            }

            generatedKeys = insertRecordStmt.getGeneratedKeys();
            int cartRecordId;
            if (generatedKeys.next()) {
                cartRecordId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating cart record failed, no ID obtained.");
            }

            // 2. Insert into cart_items
            String itemSql = "INSERT INTO cart_items (cart_record_id, item_number, price, quantity, subtotal) VALUES (?, ?, ?, ?, ?)";
            insertItemStmt = conn.prepareStatement(itemSql);

            int itemNumber = 1;
            for (CartItem item : items) {
                insertItemStmt.setInt(1, cartRecordId);
                insertItemStmt.setInt(2, itemNumber++);
                insertItemStmt.setDouble(3, item.getPrice());
                insertItemStmt.setInt(4, item.getQuantity());
                insertItemStmt.setDouble(5, item.getSubtotal());
                insertItemStmt.addBatch();
            }

            insertItemStmt.executeBatch();

            // Commit transaction
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
            // Close resources
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (insertRecordStmt != null) insertRecordStmt.close();
                if (insertItemStmt != null) insertItemStmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    DatabaseConnection.closeConnection(conn);
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}