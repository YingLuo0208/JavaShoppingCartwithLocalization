package com.spring;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalizationServiceTest {

    private final LocalizationService localizationService = new LocalizationService();

    @Test
    void testLoadMessages_fromResourceBundleEnglish() {
        Map<String, String> messages = localizationService.loadMessages("en");
        assertFalse(messages.isEmpty());
        assertEquals("Price", messages.get("enter.price"));
        assertEquals("Quantity", messages.get("enter.quantity"));
    }

    @Test
    void testLoadMessages_unknownLanguageFallsBackToEnglishBundle() {
        Map<String, String> messages = localizationService.loadMessages("unknown");
        assertFalse(messages.isEmpty());
        assertTrue(messages.containsKey("total.cost"));
    }

    @Test
    void testLoadMessages_supportedLanguages() {
        for (String languageCode : List.of("fi", "sv", "ja", "ar")) {
            Map<String, String> messages = localizationService.loadMessages(languageCode);
            assertFalse(messages.isEmpty());
            assertTrue(messages.containsKey("total.cost"));
        }
    }

    @Test
    void testGetLocaleFromCodeMappings() throws Exception {
        Method getLocaleFromCode = LocalizationService.class.getDeclaredMethod("getLocaleFromCode", String.class);
        getLocaleFromCode.setAccessible(true);

        Locale english = (Locale) getLocaleFromCode.invoke(localizationService, "en");
        Locale finnish = (Locale) getLocaleFromCode.invoke(localizationService, "fi");
        Locale swedish = (Locale) getLocaleFromCode.invoke(localizationService, "sv");
        Locale japanese = (Locale) getLocaleFromCode.invoke(localizationService, "ja");
        Locale arabic = (Locale) getLocaleFromCode.invoke(localizationService, "ar");
        Locale fallback = (Locale) getLocaleFromCode.invoke(localizationService, "unknown");

        assertEquals("en", english.getLanguage());
        assertEquals("US", english.getCountry());
        assertEquals("fi", finnish.getLanguage());
        assertEquals("FI", finnish.getCountry());
        assertEquals("sv", swedish.getLanguage());
        assertEquals("SE", swedish.getCountry());
        assertEquals("ja", japanese.getLanguage());
        assertEquals("JP", japanese.getCountry());
        assertEquals("ar", arabic.getLanguage());
        assertEquals("AR", arabic.getCountry());
        assertEquals("en", fallback.getLanguage());
        assertEquals("US", fallback.getCountry());
    }

    @Test
    void testFallbackMessagesContainsExpectedKeys() throws Exception {
        Method getFallbackMessages = LocalizationService.class.getDeclaredMethod("getFallbackMessages");
        getFallbackMessages.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, String> messages = (Map<String, String>) getFallbackMessages.invoke(localizationService);
        assertEquals("Total cost: ", messages.get("total.cost"));
        assertTrue(messages.containsKey("enter.num.items"));
        assertTrue(messages.containsKey("enter.price"));
        assertTrue(messages.containsKey("enter.quantity"));
    }
}
