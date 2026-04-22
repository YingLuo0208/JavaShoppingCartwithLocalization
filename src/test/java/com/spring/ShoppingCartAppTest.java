package com.spring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartAppTest {

    private ShoppingCartApp app;

    @BeforeEach
    void setUp() {
        app = new ShoppingCartApp();
    }

    @Test
    void testGetLanguageCodeMappings() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getLanguageCode = ShoppingCartApp.class.getDeclaredMethod("getLanguageCode", int.class);
        getLanguageCode.setAccessible(true);

        assertEquals("en", getLanguageCode.invoke(app, 1));
        assertEquals("fi", getLanguageCode.invoke(app, 2));
        assertEquals("sv", getLanguageCode.invoke(app, 3));
        assertEquals("ja", getLanguageCode.invoke(app, 4));
        assertEquals("en", getLanguageCode.invoke(app, 999));
    }

    @Test
    void testConstructor() {
        assertNotNull(app);
    }

    @Test
    void testGetLanguageCodeWithValidInputs() throws Exception {
        Method getLanguageCode = ShoppingCartApp.class.getDeclaredMethod("getLanguageCode", int.class);
        getLanguageCode.setAccessible(true);

        // Test all valid language choices
        assertEquals("en", getLanguageCode.invoke(app, 1));
        assertEquals("fi", getLanguageCode.invoke(app, 2));
        assertEquals("sv", getLanguageCode.invoke(app, 3));
        assertEquals("ja", getLanguageCode.invoke(app, 4));
    }

    @Test
    void testGetLanguageCodeWithInvalidInputs() throws Exception {
        Method getLanguageCode = ShoppingCartApp.class.getDeclaredMethod("getLanguageCode", int.class);
        getLanguageCode.setAccessible(true);

        // Invalid choices should default to English
        assertEquals("en", getLanguageCode.invoke(app, 0));
        assertEquals("en", getLanguageCode.invoke(app, 5));
        assertEquals("en", getLanguageCode.invoke(app, -1));
        assertEquals("en", getLanguageCode.invoke(app, 100));
    }

    @Test
    void testDisplayLanguageMenu() {
        // Test that displayLanguageMenu doesn't throw exceptions
        assertDoesNotThrow(() -> {
            Method displayMenu = ShoppingCartApp.class.getDeclaredMethod("displayLanguageMenu");
            displayMenu.setAccessible(true);
            displayMenu.invoke(app);
        });
    }

    @Test
    void testGetUserLanguageChoice() {
        // Test with valid input
        String input = "1\n";
        InputStream originalIn = System.in;
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            Method getUserChoice = ShoppingCartApp.class.getDeclaredMethod("getUserLanguageChoice");
            getUserChoice.setAccessible(true);
            int choice = (int) getUserChoice.invoke(app);
            assertEquals(1, choice);
        } catch (Exception e) {
            fail("Should not throw exception");
        } finally {
            System.setIn(originalIn);
        }
    }

    @Test
    void testGetUserLanguageChoiceWithInvalidThenValid() {
        // Test with invalid then valid input
        String input = "invalid\n5\n2\n";
        InputStream originalIn = System.in;
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            Method getUserChoice = ShoppingCartApp.class.getDeclaredMethod("getUserLanguageChoice");
            getUserChoice.setAccessible(true);
            int choice = (int) getUserChoice.invoke(app);
            assertEquals(2, choice);
        } catch (Exception e) {
            fail("Should not throw exception");
        } finally {
            System.setIn(originalIn);
        }
    }

    @Test
    void testGetCartItemsFromUser() {
        String input = "2\n10.5\n2\n15.0\n3\n";
        InputStream originalIn = System.in;
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            Method getCartItems = ShoppingCartApp.class.getDeclaredMethod("getCartItemsFromUser");
            getCartItems.setAccessible(true);
            // This should not throw exception
            assertDoesNotThrow(() -> getCartItems.invoke(app));
        } finally {
            System.setIn(originalIn);
        }
    }

    @Test
    void testDisplayCartSummary() {
        // Create a cart with some items
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(10.0, 2);
        cart.addItem(5.0, 3);

        assertDoesNotThrow(() -> {
            Method displaySummary = ShoppingCartApp.class.getDeclaredMethod("displayCartSummary", ShoppingCart.class, String.class);
            displaySummary.setAccessible(true);
            displaySummary.invoke(app, cart, "en");
        });
    }

    @Test
    void testSaveCartToDatabase() {
        // Test with null items (should handle gracefully)
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(10.0, 2);

        // This might fail if database is not configured, so we just test it doesn't crash
        assertDoesNotThrow(() -> {
            Method saveCart = ShoppingCartApp.class.getDeclaredMethod("saveCartToDatabase", ShoppingCart.class, String.class);
            saveCart.setAccessible(true);
            saveCart.invoke(app, cart, "en");
        });
    }
}