package com.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ShoppingCart logic and locale selection.
 */
class ShoppingCartTest {

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    // ---- Tests for calculateItemCost() ----

    @Test
    void testCalculateItemCost_normalValues() {
        assertEquals(20.0, cart.calculateItemCost(4.0, 5));
    }

    @Test
    void testCalculateItemCost_zeroQuantity() {
        assertEquals(0.0, cart.calculateItemCost(10.0, 0));
    }

    @Test
    void testCalculateItemCost_zeroPrice() {
        assertEquals(0.0, cart.calculateItemCost(0.0, 5));
    }

    @Test
    void testCalculateItemCost_negativePriceThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            cart.calculateItemCost(-1.0, 2);
        });
    }

    @Test
    void testCalculateItemCost_negativeQuantityThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            cart.calculateItemCost(10.0, -1);
        });
    }

    // ---- Tests for calculateTotalCost() ----

    @Test
    void testCalculateTotalCost_emptyCart() {
        assertEquals(0.0, cart.calculateTotalCost());
    }

    @Test
    void testCalculateTotalCost_singleItem() {
        cart.addItem(10.0, 3);
        assertEquals(30.0, cart.calculateTotalCost());
    }

    @Test
    void testCalculateTotalCost_multipleItems() {
        cart.addItem(10.0, 2);
        cart.addItem(5.0, 4);
        cart.addItem(3.0, 1);
        assertEquals(43.0, cart.calculateTotalCost());
    }

    @Test
    void testClearCart() {
        cart.addItem(10.0, 2);
        cart.clear();
        assertEquals(0.0, cart.calculateTotalCost());
    }
    
    @Test
    void testGetTotalItems_sumsQuantities() {
        cart.addItem(10.0, 2);
        cart.addItem(5.0, 4);
        assertEquals(6, cart.getTotalItems());
    }

    @Test
    void testGetCartItems_isUnmodifiable() {
        cart.addItem(10.0, 1);
        List<CartItem> items = cart.getCartItems();
        CartItem newItem = new CartItem(1, 1, 1);
        assertThrows(UnsupportedOperationException.class, () -> items.add(newItem));
    }

    // ---- Tests for getLocale() in Main ----

    @Test
    void testGetLocale_english() {
        Locale locale = Main.getLocale(0);
        assertEquals("en", locale.getLanguage());
        assertEquals("", locale.getCountry());
    }

    @Test
    void testGetLocale_finnish() {
        Locale locale = Main.getLocale(1);
        assertEquals("fi", locale.getLanguage());
        assertEquals("FI", locale.getCountry());
    }

    @Test
    void testGetLocale_swedish() {
        Locale locale = Main.getLocale(2);
        assertEquals("sv", locale.getLanguage());
        assertEquals("SE", locale.getCountry());
    }

    @Test
    void testGetLocale_japanese() {
        Locale locale = Main.getLocale(3);
        assertEquals("ja", locale.getLanguage());
        assertEquals("JP", locale.getCountry());
    }

    @Test
    void testGetLocale_arabic() {
        Locale locale = Main.getLocale(4);
        assertEquals("ar", locale.getLanguage());
        assertEquals("AR", locale.getCountry());
    }

    @Test
    void testGetLocale_invalidChoiceDefaultsToEnglish() {
        Locale locale = Main.getLocale(99);
        assertEquals("en", locale.getLanguage());
        assertEquals("", locale.getCountry());
    }
}
