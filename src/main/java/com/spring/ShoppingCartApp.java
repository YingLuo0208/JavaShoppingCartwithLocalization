package com.spring;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

/**
 * Main application class for the localized shopping cart.
 * 本地化购物车应用主类。
 */
public class ShoppingCartApp {

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
        System.out.println("Select language / Valitse kieli / Välj språk / 言語を選択:");
        System.out.println("1. English");
        System.out.println("2. Finnish (Suomi)");
        System.out.println("3. Swedish (Svenska)");
        System.out.println("4. Japanese (日本語)");
        System.out.print("Enter choice (1-4): ");

        int langChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        // Get language code
        String languageCode = getLanguageCode(langChoice);

        // Load messages from database
        Map<String, String> messages = localizationService.loadMessages(languageCode);

        // Ask user how many items they want to purchase
        System.out.print(messages.getOrDefault("enter.num.items", "Enter the number of items to purchase: "));
        int numItems = scanner.nextInt();

        // Loop through each item and collect price and quantity
        for (int i = 1; i <= numItems; i++) {
            System.out.print(messages.getOrDefault("enter.price", "Enter price for item") + " " + i + ": ");
            double price = scanner.nextDouble();

            System.out.print(messages.getOrDefault("enter.quantity", "Enter quantity for item") + " " + i + ": ");
            int quantity = scanner.nextInt();

            cart.addItem(price, quantity);
        }

        // Calculate and display the total cost
        double total = cart.calculateTotalCost();
        int totalItems = cart.getTotalItems();

        System.out.printf("%s %.2f%n",
                messages.getOrDefault("total.cost", "Total cost: "), total);

        System.out.println();
        System.out.println("--- Cart Summary ---");
        System.out.println("Total items: " + totalItems);
        System.out.println("Total cost: " + String.format("%.2f", total));
        System.out.println("Language: " + languageCode);

        // Save to database
        System.out.println();
        System.out.println("Saving cart to database...");
        int recordId = cartService.saveCart(totalItems, total, languageCode, cart.getCartItems());

        if (recordId > 0) {
            System.out.println("✓ Cart saved successfully! (Record ID: " + recordId + ")");
        } else {
            System.out.println("✗ Failed to save cart to database.");
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
