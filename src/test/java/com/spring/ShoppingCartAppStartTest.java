package com.spring;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ShoppingCartAppStartTest {

    @Test
    void testStartWithOneItemDoesNotThrow() {
        String input = "1\n1\n12.5\n2\n";
        assertStartDoesNotThrow(input);
    }

    @Test
    void testStartWithZeroItemsDoesNotThrow() {
        String input = "2\n0\n";
        assertStartDoesNotThrow(input);
    }

    private void assertStartDoesNotThrow(String input) {
        InputStream originalIn = System.in;
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            ShoppingCartApp app = new ShoppingCartApp();
            assertDoesNotThrow(app::start);
        } finally {
            System.setIn(originalIn);
        }
    }
}
