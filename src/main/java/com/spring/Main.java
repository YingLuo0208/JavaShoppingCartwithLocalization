package com.spring;

import javafx.application.Application;

/**
 * Main entry point for the Shopping Cart Application.
 * 购物车应用主入口点。
 */
public class Main {
    public static void main(String[] args) {
        // Launch JavaFX GUI
        Application.launch(ShoppingCartGUI.class, args);
        
        // For command-line version, uncomment this:
        // ShoppingCartApp app = new ShoppingCartApp();
        // app.start();
    }
}