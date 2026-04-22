package com.spring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CartItemTest {

    @Test
    void testGetters() {
        CartItem item = new CartItem(10.5, 3, 31.5);
        assertEquals(10.5, item.getPrice());
        assertEquals(3, item.getQuantity());
        assertEquals(31.5, item.getSubtotal());
    }
}
