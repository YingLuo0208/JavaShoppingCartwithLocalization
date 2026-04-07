package com.spring;

/**
 * Represents a single item in the shopping cart.
 * 表示购物车中的单个商品。
 */
public class CartItem {
    private double price;
    private int quantity;
    private double subtotal;

    public CartItem(double price, int quantity, double subtotal) {
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }
}