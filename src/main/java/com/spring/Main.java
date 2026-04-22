package com.spring;

import javafx.application.Application;
import java.util.Locale;

/**
 * Main entry point for the Shopping Cart Application.
 * 购物车应用主入口点。
 */
public class Main {

    private static final Locale[] SUPPORTED_LOCALES = {
            Locale.ENGLISH,
            Locale.forLanguageTag("fi-FI"),
            Locale.forLanguageTag("sv-SE"),
            Locale.forLanguageTag("ja-JP"),
            Locale.forLanguageTag("ar-AR")
    };

    public static void main(String[] args) {
        // Launch JavaFX GUI
        Application.launch(ShoppingCartGUI.class, args);
    }

    /**
     * 根据索引获取 Locale 对象
     * @param index 语言索引 (0: English, 1: Finnish, 2: Swedish, 3: Japanese, 4: Arabic)
     * @return 对应的 Locale 对象
     */
    public static Locale getLocale(int index) {
        if (index >= 0 && index < SUPPORTED_LOCALES.length) {
            return SUPPORTED_LOCALES[index];
        }
        return SUPPORTED_LOCALES[0]; // 默认返回英文
    }
}
