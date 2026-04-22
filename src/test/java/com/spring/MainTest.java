package com.spring;

import javafx.application.Application;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MainTest {

    @Test
    void testMainUsesConfiguredLauncher() {
        AtomicReference<Class<? extends Application>> appClassRef = new AtomicReference<>();
        AtomicReference<String[]> argsRef = new AtomicReference<>();

        try {
            Main.setApplicationLauncher((appClass, args) -> {
                appClassRef.set(appClass);
                argsRef.set(args);
            });

            String[] args = {"--test"};
            Main.main(args);

            assertEquals(ShoppingCartGUI.class, appClassRef.get());
            assertNotNull(argsRef.get());
            assertEquals("--test", argsRef.get()[0]);
        } finally {
            Main.setApplicationLauncher(Application::launch);
        }
    }

    @Test
    void testGetLocaleNegativeIndexDefaultsToEnglish() {
        Locale locale = Main.getLocale(-1);
        assertEquals("en", locale.getLanguage());
        assertEquals("", locale.getCountry());
    }
}
