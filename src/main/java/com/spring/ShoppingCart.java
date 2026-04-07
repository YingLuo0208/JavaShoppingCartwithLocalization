package com.spring;

import java.util.ArrayList;
import java.util.List;

/**
 * ShoppingCart class handles the logic for calculating item costs and total cart cost.
 * 购物车类处理计算商品费用和购物车总费用的逻辑。
 */
public class ShoppingCart {

    // List to store the cost of each item added to the cart
    private List<Double> itemCosts = new ArrayList<>();

    // List to store detailed item information for database saving
    private List<CartItem> cartItems = new ArrayList<>();

    /**
     * Calculates the cost of a single item (price multiplied by quantity).
     * 计算单个商品的费用（价格乘以数量）。
     *
     * @param price    the price of one unit of the item
     * @param quantity the number of units
     * @return the total cost for this item
     * @throws IllegalArgumentException if price or quantity is negative
     */
    public double calculateItemCost(double price, int quantity) {
        if (price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Price and quantity must not be negative.");
        }
        return price * quantity;
    }

    /**
     * Adds an item to the shopping cart.
     * 将商品添加到购物车。
     *
     * @param price    the price per unit
     * @param quantity the number of units
     */
    public void addItem(double price, int quantity) {
        double cost = calculateItemCost(price, quantity);
        itemCosts.add(cost);

        // Also store detailed item info
        cartItems.add(new CartItem(price, quantity, cost));
    }

    /**
     * Calculates the total cost of all items in the cart.
     * 计算购物车中所有商品的总费用。
     *
     * @return the sum of all item costs
     */
    public double calculateTotalCost() {
        double total = 0;
        for (double cost : itemCosts) {
            total += cost;
        }
        return total;
    }

    /**
     * Gets the total number of items (quantity sum, not item count).
     * 获取商品总数量（数量总和，不是商品种类数）。
     *
     * @return total quantity of all items
     */
    public int getTotalItems() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity();
        }
        return total;
    }

    /**
     * Clears all items from the shopping cart.
     * 清空购物车中的所有商品。
     */
    public void clear() {
        itemCosts.clear();
        cartItems.clear();
    }

    /**
     * Returns the list of individual item costs.
     * 返回单个商品费用的列表。
     *
     * @return list of item costs
     */
    public List<Double> getItemCosts() {
        return itemCosts;
    }

    /**
     * Returns the list of cart items with details.
     * 返回带有详细信息的购物车商品列表。
     *
     * @return list of CartItem objects
     */
    public List<CartItem> getCartItems() {
        return cartItems;
    }
}