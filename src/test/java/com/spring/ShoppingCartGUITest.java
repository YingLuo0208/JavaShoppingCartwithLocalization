package com.spring;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShoppingCartGUITest {

    @BeforeAll
    static void initJavaFxRuntime() throws Exception {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
            // JavaFX toolkit already initialized.
        }
        runOnFxThreadAndWait(() -> { });
    }

    @Test
    void testCartItemDisplayGetters() {
        ShoppingCartGUI.CartItemDisplay display = new ShoppingCartGUI.CartItemDisplay(1, 9.9, 2, 19.8);
        assertEquals(1, display.getNumber());
        assertEquals(9.9, display.getPrice());
        assertEquals(2, display.getQuantity());
        assertEquals(19.8, display.getSubtotal());
    }

    @Test
    void testLanguageMapContainsExpectedValues() throws Exception {
        Field languageMapField = ShoppingCartGUI.class.getDeclaredField("LANGUAGE_MAP");
        languageMapField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, String> languageMap = (Map<String, String>) languageMapField.get(null);
        assertEquals("en", languageMap.get("English"));
        assertEquals("fi", languageMap.get("Suomi (Finnish)"));
        assertEquals("sv", languageMap.get("Svenska (Swedish)"));
        assertEquals("ja", languageMap.get("日本語 (Japanese)"));
        assertEquals("ar", languageMap.get("العربية (Arabic)"));
    }

    @Test
    void testPanelCreationAndTableSetup() throws Exception {
        ShoppingCartGUI gui = createGuiFixture();
        runOnFxThreadAndWait(() -> {
            Object top = invoke(gui, "createTopPanel");
            Object center = invoke(gui, "createCenterPanel");
            assertNotNull(top);
            assertNotNull(center);
            assertEquals(4, getTableColumnsSize(gui));
        });
    }

    @Test
    void testCreateItemInputsBranches() throws Exception {
        ShoppingCartGUI gui = createGuiFixture();
        runOnFxThreadAndWait(() -> {
            invoke(gui, "createCenterPanel");
            setField(gui, "statusLabel", new Label("Ready"));

            TextField numItemsField = (TextField) getField(gui, "numItemsField");
            numItemsField.setText("0");
            invoke(gui, "createItemInputs");
            assertEquals("Please enter a positive number", getStatusText(gui));

            numItemsField.setText("abc");
            invoke(gui, "createItemInputs");
            assertEquals("Please enter a valid number", getStatusText(gui));

            numItemsField.setText("2");
            invoke(gui, "createItemInputs");
            VBox itemInputPanel = (VBox) getField(gui, "itemInputPanel");
            assertEquals(2, itemInputPanel.getChildren().size());
            assertEquals("Enter 2 item(s)", getStatusText(gui));
        });
    }

    @Test
    void testCalculateTotalBranches() throws Exception {
        ShoppingCartGUI gui = createGuiFixture();
        runOnFxThreadAndWait(() -> {
            invoke(gui, "createCenterPanel");
            setField(gui, "statusLabel", new Label("Ready"));

            invoke(gui, "calculateTotal");
            assertEquals("Please create item inputs first", getStatusText(gui));

            TextField numItemsField = (TextField) getField(gui, "numItemsField");
            numItemsField.setText("1");
            invoke(gui, "createItemInputs");
            invoke(gui, "calculateTotal");
            assertEquals("Please enter valid numeric values", getStatusText(gui));

            Object firstInput = getFirstItemInput(gui);
            TextField priceField = (TextField) getField(firstInput, "priceField");
            TextField quantityField = (TextField) getField(firstInput, "quantityField");

            priceField.setText("-1");
            quantityField.setText("1");
            invoke(gui, "calculateTotal");
            assertEquals("Price and quantity cannot be negative", getStatusText(gui));

            priceField.setText("2.5");
            quantityField.setText("4");
            invoke(gui, "calculateTotal");
            Label totalLabel = (Label) getField(gui, "totalLabel");
            assertTrue(totalLabel.getText().contains("10.00"));
            assertEquals("Calculation completed", getStatusText(gui));
            assertEquals(1, getTableItems(gui).size());
        });
    }

    @Test
    void testSaveToDatabaseBranches() throws Exception {
        ShoppingCartGUI gui = createGuiFixture();
        runOnFxThreadAndWait(() -> {
            invoke(gui, "createCenterPanel");
            setField(gui, "statusLabel", new Label("Ready"));

            invoke(gui, "saveToDatabase");
            assertEquals("Please calculate total first", getStatusText(gui));

            ShoppingCart cart = (ShoppingCart) getField(gui, "cart");
            cart.addItem(10.0, 2);

            setField(gui, "cartService", new StubCartService(123));
            invoke(gui, "saveToDatabase");
            assertEquals("Saved to database! Record ID: 123", getStatusText(gui));

            setField(gui, "cartService", new StubCartService(-1));
            invoke(gui, "saveToDatabase");
            assertEquals("Failed to save to database", getStatusText(gui));
        });
    }

    @Test
    void testLoadMessagesFallbackAndUpdateLanguageOrientation() throws Exception {
        ShoppingCartGUI gui = createGuiFixture();
        runOnFxThreadAndWait(() -> {
            setField(gui, "localizationService", new StubLocalizationService(new HashMap<>()));
            invoke(gui, "loadMessages");

            @SuppressWarnings("unchecked")
            Map<String, String> messages = (Map<String, String>) getField(gui, "messages");
            assertEquals("Total cost: ", messages.get("total.cost"));

            BorderPane root = new BorderPane();
            setField(gui, "root", root);
            setField(gui, "primaryStage", new Stage());
            invoke(gui, "createTopPanel");
            invoke(gui, "createCenterPanel");

            messages.put("app.title", "Test Cart");
            messages.put("language.label", "Language:");
            messages.put("enter.num.items", "Enter count:");
            messages.put("button.start", "Start");
            messages.put("button.calculate", "Calculate");
            messages.put("button.save", "Save");
            messages.put("enter.price", "Price");
            messages.put("enter.quantity", "Quantity");
            setField(gui, "messages", messages);

            setField(gui, "currentLanguage", "ar");
            invoke(gui, "updateUILanguage");
            assertEquals(NodeOrientation.RIGHT_TO_LEFT, root.getNodeOrientation());

            setField(gui, "currentLanguage", "en");
            invoke(gui, "updateUILanguage");
            assertEquals(NodeOrientation.LEFT_TO_RIGHT, root.getNodeOrientation());
        });
    }

    @Test
    void testShowStatusUpdatesLabelAndStyle() throws Exception {
        ShoppingCartGUI gui = createGuiFixture();
        runOnFxThreadAndWait(() -> {
            setField(gui, "statusLabel", new Label("Ready"));
            invoke(gui, "showStatus", "Done", "green");
            Label status = (Label) getField(gui, "statusLabel");
            assertEquals("Done", status.getText());
            assertTrue(status.getStyle().contains("green"));
        });
    }

    private ShoppingCartGUI createGuiFixture() throws Exception {
        ShoppingCartGUI gui = new ShoppingCartGUI();
        setField(gui, "localizationService", new StubLocalizationService(defaultMessages()));
        setField(gui, "cartService", new StubCartService(1));
        setField(gui, "cart", new ShoppingCart());
        setField(gui, "messages", defaultMessages());
        setField(gui, "currentLanguage", "en");
        return gui;
    }

    private static Map<String, String> defaultMessages() {
        Map<String, String> messages = new HashMap<>();
        messages.put("app.title", "Shopping Cart");
        messages.put("language.label", "Language:");
        messages.put("enter.num.items", "Enter number of items: ");
        messages.put("button.start", "Start");
        messages.put("button.calculate", "Calculate Total");
        messages.put("button.save", "Save to Database");
        messages.put("enter.price", "Price");
        messages.put("enter.quantity", "Quantity");
        messages.put("total.cost", "Total cost: ");
        return messages;
    }

    private static void runOnFxThreadAndWait(Runnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(10, TimeUnit.SECONDS)) {
            throw new AssertionError("Timed out waiting for JavaFX action.");
        }
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
    }

    @SuppressWarnings("unchecked")
    private ObservableList<ShoppingCartGUI.CartItemDisplay> getTableItems(ShoppingCartGUI gui) {
        return ((javafx.scene.control.TableView<ShoppingCartGUI.CartItemDisplay>) getField(gui, "tableView")).getItems();
    }

    private int getTableColumnsSize(ShoppingCartGUI gui) {
        return ((javafx.scene.control.TableView<?>) getField(gui, "tableView")).getColumns().size();
    }

    private Object getFirstItemInput(ShoppingCartGUI gui) {
        @SuppressWarnings("unchecked")
        java.util.List<Object> itemInputs = (java.util.List<Object>) getField(gui, "itemInputs");
        return itemInputs.get(0);
    }

    private String getStatusText(ShoppingCartGUI gui) {
        return ((Label) getField(gui, "statusLabel")).getText();
    }

    private Object invoke(Object target, String methodName, Object... args) {
        try {
            Class<?>[] argTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                argTypes[i] = args[i].getClass();
            }
            Method method = findMethod(target.getClass(), methodName, argTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Method findMethod(Class<?> type, String methodName, Class<?>[] argTypes) throws NoSuchMethodException {
        for (Method method : type.getDeclaredMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != argTypes.length) {
                continue;
            }
            boolean match = true;
            Class<?>[] params = method.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                if (!params[i].isAssignableFrom(argTypes[i])) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return method;
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    private Object getField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class StubLocalizationService extends LocalizationService {
        private final Map<String, String> messages;

        private StubLocalizationService(Map<String, String> messages) {
            this.messages = messages;
        }

        @Override
        public Map<String, String> loadMessages(String languageCode) {
            return new HashMap<>(messages);
        }
    }

    private static class StubCartService extends CartService {
        private final int result;

        private StubCartService(int result) {
            this.result = result;
        }

        @Override
        public int saveCart(int totalItems, double totalCost, String language, java.util.List<CartItem> items) {
            return result;
        }
    }
}
