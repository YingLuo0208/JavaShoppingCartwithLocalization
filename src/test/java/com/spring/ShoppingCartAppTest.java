package com.spring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartAppTest {

    private ShoppingCartApp app;

    @BeforeEach
    void setUp() {
        app = new ShoppingCartApp();
    }

    @Test
    void testConstructor() {
        assertNotNull(app);
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
    void testGetLanguageCodeWithValidInputs() throws Exception {
        Method getLanguageCode = ShoppingCartApp.class.getDeclaredMethod("getLanguageCode", int.class);
        getLanguageCode.setAccessible(true);

        assertEquals("en", getLanguageCode.invoke(app, 1));
        assertEquals("fi", getLanguageCode.invoke(app, 2));
        assertEquals("sv", getLanguageCode.invoke(app, 3));
        assertEquals("ja", getLanguageCode.invoke(app, 4));
    }

    @Test
    void testGetLanguageCodeWithInvalidInputs() throws Exception {
        Method getLanguageCode = ShoppingCartApp.class.getDeclaredMethod("getLanguageCode", int.class);
        getLanguageCode.setAccessible(true);

        assertEquals("en", getLanguageCode.invoke(app, 0));
        assertEquals("en", getLanguageCode.invoke(app, 5));
        assertEquals("en", getLanguageCode.invoke(app, -1));
        assertEquals("en", getLanguageCode.invoke(app, 100));
    }
}