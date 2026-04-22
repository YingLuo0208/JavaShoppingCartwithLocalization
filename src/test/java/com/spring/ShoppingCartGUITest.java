package com.spring;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShoppingCartGUITest {

    @Test
    void testCartItemDisplayGetters() {
        ShoppingCartGUI.CartItemDisplay display = new ShoppingCartGUI.CartItemDisplay(1, 9.9, 2, 19.8);
        assertEquals(1, display.getNumber());
        assertEquals(9.9, display.getPrice());
        assertEquals(2, display.getQuantity());
        assertEquals(19.8, display.getSubtotal());
    }

    @Test
    void testLanguageMapContainsExpectedValues() throws Exception {
        Field languageMapField = ShoppingCartGUI.class.getDeclaredField("LANGUAGE_MAP");
        languageMapField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, String> languageMap = (Map<String, String>) languageMapField.get(null);

        assertEquals("en", languageMap.get("English"));
        assertEquals("fi", languageMap.get("Suomi (Finnish)"));
        assertEquals("sv", languageMap.get("Svenska (Swedish)"));
        assertEquals("ja", languageMap.get("日本語 (Japanese)"));
        assertEquals("ar", languageMap.get("العربية (Arabic)"));
    }
}
