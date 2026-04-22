package com.spring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ShoppingCartAppStartTest {

    @Test
    void testAppCreation() {
        ShoppingCartApp app = new ShoppingCartApp();
        assertNotNull(app);
    }
}