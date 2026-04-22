package com.spring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CartServiceTest {

    @Test
    void testSaveCart_nullItemsReturnsMinusOne() {
        CartService service = new CartService();
        int recordId = service.saveCart(0, 0.0, "en", null);
        assertEquals(-1, recordId);
    }
}
