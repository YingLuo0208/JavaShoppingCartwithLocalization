package com.spring;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShoppingCartAppTest {

    @Test
    void testGetLanguageCodeMappings() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ShoppingCartApp app = new ShoppingCartApp();
        Method getLanguageCode = ShoppingCartApp.class.getDeclaredMethod("getLanguageCode", int.class);
        getLanguageCode.setAccessible(true);

        assertEquals("en", getLanguageCode.invoke(app, 1));
        assertEquals("fi", getLanguageCode.invoke(app, 2));
        assertEquals("sv", getLanguageCode.invoke(app, 3));
        assertEquals("ja", getLanguageCode.invoke(app, 4));
        assertEquals("en", getLanguageCode.invoke(app, 999));
    }
}
