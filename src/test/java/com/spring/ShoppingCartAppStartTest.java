package com.spring;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ShoppingCartAppStartTest {

    @Test
    void testStartSavesCartSuccessfully() {
        TestLocalizationService localizationService = new TestLocalizationService();
        TestCartService cartService = new TestCartService(99);
        String input = "3\n2\n12.5\n2\n5.0\n1\n";

        assertStartDoesNotThrow(input, localizationService, cartService);
        assertEquals("sv", localizationService.lastLanguageCode);
        assertEquals(3, cartService.savedTotalItems);
        assertEquals("sv", cartService.savedLanguageCode);
        assertEquals(2, cartService.savedItems.size());
    }

    @Test
    void testStartSaveFailurePathDoesNotThrow() {
        TestLocalizationService localizationService = new TestLocalizationService();
        TestCartService cartService = new TestCartService(-1);
        String input = "4\n1\n8.0\n3\n";

        assertStartDoesNotThrow(input, localizationService, cartService);
        assertEquals("ja", localizationService.lastLanguageCode);
        assertEquals(3, cartService.savedTotalItems);
        assertEquals("ja", cartService.savedLanguageCode);
    }

    @Test
    void testStartWithDefaultEnglishAndZeroItems() {
        TestLocalizationService localizationService = new TestLocalizationService();
        TestCartService cartService = new TestCartService(7);
        String input = "1\n0\n";

        assertStartDoesNotThrow(input, localizationService, cartService);
        assertEquals("en", localizationService.lastLanguageCode);
        assertEquals(0, cartService.savedTotalItems);
        assertEquals("en", cartService.savedLanguageCode);
        assertEquals(0, cartService.savedItems.size());
    }

    @Test
    void testStartUsesFallbackPromptsWhenMessagesMissing() {
        TestLocalizationService localizationService = new TestLocalizationService();
        localizationService.returnEmptyMessages = true;
        TestCartService cartService = new TestCartService(8);
        String input = "99\n0\n";

        assertStartDoesNotThrow(input, localizationService, cartService);
        assertEquals("en", localizationService.lastLanguageCode);
        assertEquals(0, cartService.savedTotalItems);
        assertEquals("en", cartService.savedLanguageCode);
    }

    private void assertStartDoesNotThrow(String input, LocalizationService localizationService, CartService cartService) {
        InputStream originalIn = System.in;
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            ShoppingCartApp app = new ShoppingCartApp(localizationService, cartService);
            assertDoesNotThrow(app::start);
        } finally {
            System.setIn(originalIn);
        }
    }

    private static class TestLocalizationService extends LocalizationService {
        private String lastLanguageCode;
        private boolean returnEmptyMessages;

        @Override
        public Map<String, String> loadMessages(String languageCode) {
            lastLanguageCode = languageCode;
            if (returnEmptyMessages) {
                return new HashMap<>();
            }
            Map<String, String> messages = new HashMap<>();
            messages.put("enter.num.items", "Enter items:");
            messages.put("enter.price", "Price");
            messages.put("enter.quantity", "Quantity");
            messages.put("total.cost", "Total cost:");
            return messages;
        }
    }

    private static class TestCartService extends CartService {
        private final int returnRecordId;
        private int savedTotalItems;
        private String savedLanguageCode;
        private List<CartItem> savedItems;

        private TestCartService(int returnRecordId) {
            this.returnRecordId = returnRecordId;
        }

        @Override
        public int saveCart(int totalItems, double totalCost, String language, List<CartItem> items) {
            this.savedTotalItems = totalItems;
            this.savedLanguageCode = language;
            this.savedItems = items;
            return returnRecordId;
        }
    }
}
