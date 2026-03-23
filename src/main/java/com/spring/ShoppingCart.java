package com.spring;

import java.util.ArrayList;
import java.util.List;

/**
 * ShoppingCart class handles the logic for calculating item costs and total cart cost.
 * 购物车类处理计算商品费用和购物车总费用的逻辑。
 */
public class ShoppingCart {

    // List to store the cost of each item added to the cart
    // 存储添加到购物车的每个商品费用的列表
    private List<Double> itemCosts = new ArrayList<>();

    /**
     * Calculates the cost of a single item (price multiplied by quantity).
     * 计算单个商品的费用（价格乘以数量）。
     *
     * @param price    the price of one unit of the item
     *                 商品单价
     * @param quantity the number of units
     *                 商品数量
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
     *                 每单位商品的价格
     * @param quantity the number of units
     *                 商品数量
     */
    public void addItem(double price, int quantity) {
        double cost = calculateItemCost(price, quantity);
        itemCosts.add(cost);
    }

    /**
     * Calculates the total cost of all items in the cart.
     * 计算购物车中所有商品的总费用。
     *
     * @return the sum of all item costs
     *         所有商品费用的总和
     */
    public double calculateTotalCost() {
        double total = 0;
        for (double cost : itemCosts) {
            total += cost;
        }
        return total;
    }

    /**
     * Clears all items from the shopping cart.
     * 清空购物车中的所有商品。
     */
    public void clear() {
        itemCosts.clear();
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
}