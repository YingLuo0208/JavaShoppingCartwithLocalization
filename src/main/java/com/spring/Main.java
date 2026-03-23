package com.spring;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * Main class for the localized shopping cart console application.
 * Supports English, Finnish, Swedish, and Japanese based on user selection.
 * 本地化购物车控制台应用的主类。
 * 根据用户选择支持英语、芬兰语、瑞典语和日语。
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // Set console output to UTF-8 to support non-Latin characters (e.g., Japanese)
        // 设置控制台输出为UTF-8以支持非拉丁字符（例如，日语）
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        ShoppingCart cart = new ShoppingCart();

        // Display language selection menu
        // 显示语言选择菜单
        System.out.println("Select language / Valitse kieli / Valj sprak / Gengo wo sentaku:");
        System.out.println("1. English");
        System.out.println("2. Finnish (Suomi)");
        System.out.println("3. Swedish (Svenska)");
        System.out.println("4. Japanese (Nihongo)");
        System.out.print("Enter choice (1-4): ");

        int langChoice = scanner.nextInt();

        // Get Locale object based on user's language choice
        // 根据用户的语言选择获取Locale对象
        Locale locale = getLocale(langChoice);

        // Load the corresponding ResourceBundle (properties file) for the chosen locale
        // 加载所选区域对应的ResourceBundle（属性文件）
        ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle", locale);

        // Ask user how many items they want to purchase
        // 询问用户他们想购买多少件商品
        System.out.print(messages.getString("enter.num.items") + " ");
        int numItems = scanner.nextInt();

        // Loop through each item and collect price and quantity
        // 循环处理每个商品，收集价格和数量
        for (int i = 1; i <= numItems; i++) {
            System.out.print(messages.getString("enter.price") + " " + i + ": ");
            double price = scanner.nextDouble();

            System.out.print(messages.getString("enter.quantity") + " " + i + ": ");
            int quantity = scanner.nextInt();

            // Add item to cart
            // 将商品添加到购物车
            cart.addItem(price, quantity);
        }

        // Calculate and display the total cost
        // 计算并显示总费用
        double total = cart.calculateTotalCost();
        System.out.printf("%s %.2f%n", messages.getString("total.cost"), total);

        scanner.close();
    }

    /**
     * Returns the appropriate Locale based on the user's menu selection.
     * 根据用户的菜单选择返回相应的Locale。
     *
     * @param choice the number entered by the user (1-4)
     *               用户输入的数字（1-4）
     * @return the corresponding Locale object
     *         相应的Locale对象
     */
    public static Locale getLocale(int choice) {
        String language;
        String country;

        switch (choice) {
            case 2:
                language = "fi";
                country = "FI";
                break;
            case 3:
                language = "sv";
                country = "SE";
                break;
            case 4:
                language = "ja";
                country = "JP";
                break;
            default:
                language = "en";
                country = "US";
                break;
        }

        // Create Locale object using language and country code
        // 使用语言和国家代码创建Locale对象
        return new Locale(language, country);
    }
}