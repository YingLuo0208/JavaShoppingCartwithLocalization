package com.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ShoppingCart logic and locale selection.
 * Uses JUnit 5. JaCoCo will measure coverage when running 'mvn test'.
 * ShoppingCart逻辑和区域选择的单元测试。
 * 在运行'mvn test'时使用JUnit 5，JaCoCo将测量覆盖率。
 */
public class ShoppingCartTest {

    private ShoppingCart cart;

    @BeforeEach
    public void setUp() {
        cart = new ShoppingCart();
    }

    // ---- Tests for calculateItemCost() ----

    @Test
    public void testCalculateItemCost_normalValues() {
        assertEquals(20.0, cart.calculateItemCost(4.0, 5));
    }

    @Test
    public void testCalculateItemCost_zeroQuantity() {
        assertEquals(0.0, cart.calculateItemCost(10.0, 0));
    }

    @Test
    public void testCalculateItemCost_zeroPrice() {
        assertEquals(0.0, cart.calculateItemCost(0.0, 5));
    }

    @Test
    public void testCalculateItemCost_negativePriceThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            cart.calculateItemCost(-1.0, 2);
        });
    }

    @Test
    public void testCalculateItemCost_negativeQuantityThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            cart.calculateItemCost(10.0, -1);
        });
    }

    // ---- Tests for calculateTotalCost() ----

    @Test
    public void testCalculateTotalCost_emptyCart() {
        assertEquals(0.0, cart.calculateTotalCost());
    }

    @Test
    public void testCalculateTotalCost_singleItem() {
        cart.addItem(10.0, 3);
        assertEquals(30.0, cart.calculateTotalCost());
    }

    @Test
    public void testCalculateTotalCost_multipleItems() {
        cart.addItem(10.0, 2);
        cart.addItem(5.0, 4);
        cart.addItem(3.0, 1);
        assertEquals(43.0, cart.calculateTotalCost());
    }

    @Test
    public void testClearCart() {
        cart.addItem(10.0, 2);
        cart.clear();
        assertEquals(0.0, cart.calculateTotalCost());
    }

    // ---- Tests for getLocale() in Main ----

    @Test
    public void testGetLocale_english() {
        Locale locale = Main.getLocale(1);
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
    }

    @Test
    public void testGetLocale_finnish() {
        Locale locale = Main.getLocale(2);
        assertEquals("fi", locale.getLanguage());
        assertEquals("FI", locale.getCountry());
    }

    @Test
    public void testGetLocale_swedish() {
        Locale locale = Main.getLocale(3);
        assertEquals("sv", locale.getLanguage());
        assertEquals("SE", locale.getCountry());
    }

    @Test
    public void testGetLocale_japanese() {
        Locale locale = Main.getLocale(4);
        assertEquals("ja", locale.getLanguage());
        assertEquals("JP", locale.getCountry());
    }

    @Test
    public void testGetLocale_invalidChoiceDefaultsToEnglish() {
        Locale locale = Main.getLocale(99);
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
    }
}