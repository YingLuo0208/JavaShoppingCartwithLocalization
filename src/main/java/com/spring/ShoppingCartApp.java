package com.spring;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Main application class for the localized shopping cart.
 * 本地化购物车应用主类。
 */
public class ShoppingCartApp {
    private static final Logger LOGGER = Logger.getLogger(ShoppingCartApp.class.getName());

    private final LocalizationService localizationService;
    private final CartService cartService;

    public ShoppingCartApp() {
        this.localizationService = new LocalizationService();
        this.cartService = new CartService();
    }

    public void start() {
        // Set console output to UTF-8
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        ShoppingCart cart = new ShoppingCart();

        // Display language selection menu
        LOGGER.info("Select language / Valitse kieli / Välj språk / 言語を選択:");
        LOGGER.info("1. English");
        LOGGER.info("2. Finnish (Suomi)");
        LOGGER.info("3. Swedish (Svenska)");
        LOGGER.info("4. Japanese (日本語)");
        LOGGER.info("Enter choice (1-4): ");

        int langChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        // Get language code
        String languageCode = getLanguageCode(langChoice);

        // Load messages from database
        Map<String, String> messages = localizationService.loadMessages(languageCode);

        // Ask user how many items they want to purchase
        LOGGER.info(messages.getOrDefault("enter.num.items", "Enter the number of items to purchase: "));
        int numItems = scanner.nextInt();

        // Loop through each item and collect price and quantity
        for (int i = 1; i <= numItems; i++) {
            LOGGER.info(messages.getOrDefault("enter.price", "Enter price for item") + " " + i + ": ");
            double price = scanner.nextDouble();

            LOGGER.info(messages.getOrDefault("enter.quantity", "Enter quantity for item") + " " + i + ": ");
            int quantity = scanner.nextInt();

            cart.addItem(price, quantity);
        }

        // Calculate and display the total cost
        double total = cart.calculateTotalCost();
        int totalItems = cart.getTotalItems();

        LOGGER.info(String.format("%s %.2f",
                messages.getOrDefault("total.cost", "Total cost: "), total));

        LOGGER.info("");
        LOGGER.info("--- Cart Summary ---");
        LOGGER.info("Total items: " + totalItems);
        LOGGER.info("Total cost: " + String.format("%.2f", total));
        LOGGER.info("Language: " + languageCode);

        // Save to database
        LOGGER.info("");
        LOGGER.info("Saving cart to database...");
        int recordId = cartService.saveCart(totalItems, total, languageCode, cart.getCartItems());

        if (recordId > 0) {
            LOGGER.info("✓ Cart saved successfully! (Record ID: " + recordId + ")");
        } else {
            LOGGER.info("✗ Failed to save cart to database.");
        }

        scanner.close();
    }

    /**
     * Convert user menu choice to language code.
     * 将用户菜单选择转换为语言代码。
     */
    private String getLanguageCode(int choice) {
        switch (choice) {
            case 2:
                return "fi";
            case 3:
                return "sv";
            case 4:
                return "ja";
            default:
                return "en";
        }
    }
}
