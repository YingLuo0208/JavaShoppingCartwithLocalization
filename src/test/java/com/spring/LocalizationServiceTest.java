package com.spring;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocalizationServiceTest {

    private final LocalizationService localizationService = new LocalizationService();

    @Test
    public void testLoadMessages_fromResourceBundleEnglish() {
        Map<String, String> messages = localizationService.loadMessages("en");
        assertFalse(messages.isEmpty());
        assertEquals("Price", messages.get("enter.price"));
        assertEquals("Quantity", messages.get("enter.quantity"));
    }

    @Test
    public void testLoadMessages_unknownLanguageFallsBackToEnglishBundle() {
        Map<String, String> messages = localizationService.loadMessages("unknown");
        assertFalse(messages.isEmpty());
        assertTrue(messages.containsKey("total.cost"));
    }
}
