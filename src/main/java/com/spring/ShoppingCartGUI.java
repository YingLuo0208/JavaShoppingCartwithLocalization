package com.spring;

import javafx.application.Application;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JavaFX GUI for the Shopping Cart Application.
 * 购物车应用的JavaFX图形界面。
 */
public class ShoppingCartGUI extends Application {
    private static final String DEFAULT_READY_STATUS = "Ready";
    private static final String STATUS_COLOR_SUCCESS = "green";
    private static final String STATUS_COLOR_ERROR = "red";
    private static final String KEY_ENTER_PRICE = "enter.price";
    private static final String KEY_ENTER_QUANTITY = "enter.quantity";
    private static final String FALLBACK_PRICE = "Price";
    private static final String FALLBACK_QUANTITY = "Quantity";
    private static final String TABLE_HEADER_SUBTOTAL = "Subtotal";
    private static final String TITLE_AUTHOR_SUFFIX = " - Ying Luo";

    private ComboBox<String> languageComboBox;
    private Label titleLabel;
    private Label languageLabel;
    private Label numItemsLabel;
    private TextField numItemsField;
    private Button startButton;

    private VBox itemInputPanel;
    private List<CartItemInput> itemInputs;

    private TableView<CartItemDisplay> tableView;
    private Label totalLabel;
    private Button calculateButton;
    private Button saveButton;
    private Label statusLabel;

    private LocalizationService localizationService;
    private CartService cartService;
    private ShoppingCart cart;

    private String currentLanguage = "en";
    private Map<String, String> messages;
    private int numItems = 0;
    
    private BorderPane root;
    private Stage primaryStage;
    private PauseTransition statusResetTimer;

    // Language options
    private static final Map<String, String> LANGUAGE_MAP = new HashMap<>();
    static {
        LANGUAGE_MAP.put("English", "en");
        LANGUAGE_MAP.put("Suomi (Finnish)", "fi");
        LANGUAGE_MAP.put("Svenska (Swedish)", "sv");
        LANGUAGE_MAP.put("日本語 (Japanese)", "ja");
        LANGUAGE_MAP.put("العربية (Arabic)", "ar");
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialize services
        localizationService = new LocalizationService();
        cartService = new CartService();
        cart = new ShoppingCart();

        // Load default messages
        loadMessages();

        // Create UI
        root = new BorderPane();
        root.setPadding(new Insets(20));

        // Top: Language selection
        VBox topBox = createTopPanel();
        root.setTop(topBox);

        // Center: Main content
        VBox centerBox = createCenterPanel();
        root.setCenter(centerBox);

        // Bottom: Status bar
        statusLabel = new Label(DEFAULT_READY_STATUS);
        statusLabel.setStyle("-fx-text-fill: " + STATUS_COLOR_SUCCESS + ";");
        HBox bottomBox = new HBox(statusLabel);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Shopping Cart");
        primaryStage.setScene(scene);
        primaryStage.show();

        updateUILanguage();
    }

    private VBox createTopPanel() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(0, 0, 20, 0));

        titleLabel = new Label();
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        HBox langBox = new HBox(10);
        langBox.setAlignment(Pos.CENTER_LEFT);
        languageLabel = new Label("Language:");
        languageComboBox = new ComboBox<>(FXCollections.observableArrayList(LANGUAGE_MAP.keySet()));
        languageComboBox.setValue("English");
        languageComboBox.setOnAction(e -> {
            currentLanguage = LANGUAGE_MAP.get(languageComboBox.getValue());
            loadMessages();
            updateUILanguage();
        });
        langBox.getChildren().addAll(languageLabel, languageComboBox);

        vbox.getChildren().addAll(titleLabel, langBox);
        return vbox;
    }

    private VBox createCenterPanel() {
        VBox vbox = new VBox(15);

        // Number of items input
        HBox numItemsBox = new HBox(10);
        numItemsBox.setAlignment(Pos.CENTER_LEFT);
        numItemsLabel = new Label();
        numItemsField = new TextField();
        numItemsField.setPrefWidth(100);
        startButton = new Button();
        startButton.setOnAction(e -> createItemInputs());

        numItemsBox.getChildren().addAll(numItemsLabel, numItemsField, startButton);

        // Item input panel (dynamic)
        itemInputPanel = new VBox(10);
        itemInputPanel.setPadding(new Insets(10, 0, 10, 0));
        ScrollPane scrollPane = new ScrollPane(itemInputPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);

        // Table for displaying items
        tableView = new TableView<>();
        setupTableView();

        // Total and buttons
        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        totalLabel = new Label();
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        calculateButton = new Button();
        calculateButton.setOnAction(e -> calculateTotal());
        saveButton = new Button();
        saveButton.setOnAction(e -> saveToDatabase());

        bottomBox.getChildren().addAll(totalLabel, calculateButton, saveButton);

        vbox.getChildren().addAll(numItemsBox, scrollPane, tableView, bottomBox);
        return vbox;
    }

    private void setupTableView() {
        TableColumn<CartItemDisplay, Integer> colNumber = new TableColumn<>("#");
        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        colNumber.setPrefWidth(50);

        TableColumn<CartItemDisplay, Double> colPrice = new TableColumn<>(FALLBACK_PRICE);
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setPrefWidth(100);

        TableColumn<CartItemDisplay, Integer> colQuantity = new TableColumn<>(FALLBACK_QUANTITY);
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQuantity.setPrefWidth(100);

        TableColumn<CartItemDisplay, Double> colSubtotal = new TableColumn<>(TABLE_HEADER_SUBTOTAL);
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSubtotal.setPrefWidth(120);

        tableView.getColumns().addAll(colNumber, colPrice, colQuantity, colSubtotal);
    }

    private void createItemInputs() {
        try {
            numItems = Integer.parseInt(numItemsField.getText());
            if (numItems <= 0) {
                showStatus("Please enter a positive number", STATUS_COLOR_ERROR);
                return;
            }

            itemInputPanel.getChildren().clear();
            itemInputs = FXCollections.observableArrayList();

            for (int i = 0; i < numItems; i++) {
                CartItemInput input = new CartItemInput(i + 1, messages);
                itemInputs.add(input);
                itemInputPanel.getChildren().add(input.getPane());
            }

            showStatus("Enter " + numItems + " item(s)", STATUS_COLOR_SUCCESS);
        } catch (NumberFormatException e) {
            showStatus("Please enter a valid number", STATUS_COLOR_ERROR);
        }
    }

    private void calculateTotal() {
        if (itemInputs == null || itemInputs.isEmpty()) {
            showStatus("Please create item inputs first", STATUS_COLOR_ERROR);
            return;
        }

        try {
            cart.clear();
            tableView.getItems().clear();

            for (CartItemInput input : itemInputs) {
                double price = input.getPrice();
                int quantity = input.getQuantity();

                if (price < 0 || quantity < 0) {
                    showStatus("Price and quantity cannot be negative", STATUS_COLOR_ERROR);
                    return;
                }

                cart.addItem(price, quantity);

                CartItemDisplay display = new CartItemDisplay(
                        input.getItemNumber(),
                        price,
                        quantity,
                        price * quantity
                );
                tableView.getItems().add(display);
            }

            double total = cart.calculateTotalCost();
            totalLabel.setText(String.format("%s %.2f",
                    messages.getOrDefault("total.cost", "Total cost: "), total));

            showStatus("Calculation completed", STATUS_COLOR_SUCCESS);
        } catch (NumberFormatException e) {
            showStatus("Please enter valid numeric values", STATUS_COLOR_ERROR);
        } catch (IllegalArgumentException e) {
            showStatus("Error: " + e.getMessage(), STATUS_COLOR_ERROR);
        }
    }

    private void saveToDatabase() {
        if (cart.getCartItems().isEmpty()) {
            showStatus("Please calculate total first", STATUS_COLOR_ERROR);
            return;
        }

        int totalItems = cart.getTotalItems();
        double totalCost = cart.calculateTotalCost();

        int recordId = cartService.saveCart(totalItems, totalCost, currentLanguage, cart.getCartItems());

        if (recordId > 0) {
            showStatus("Saved to database! Record ID: " + recordId, STATUS_COLOR_SUCCESS);
        } else {
            showStatus("Failed to save to database", STATUS_COLOR_ERROR);
        }
    }

    private void loadMessages() {
        messages = localizationService.loadMessages(currentLanguage);
        if (messages.isEmpty()) {
            // Fallback to hardcoded English
            messages = new HashMap<>();
            messages.put("enter.num.items", "Enter number of items: ");
            messages.put(KEY_ENTER_PRICE, FALLBACK_PRICE);
            messages.put(KEY_ENTER_QUANTITY, FALLBACK_QUANTITY);
            messages.put("total.cost", "Total cost: ");
        }
    }

    private void updateUILanguage() {
        // Update window title with author name (author name stays in English)
        String localizedTitle = messages.getOrDefault("app.title", "Shopping Cart");
        primaryStage.setTitle(localizedTitle + TITLE_AUTHOR_SUFFIX);
        
        // Update title label with author name (author name stays in English)
        titleLabel.setText(localizedTitle + TITLE_AUTHOR_SUFFIX);
        languageLabel.setText(messages.getOrDefault("language.label", "Language:"));
        
        // Update input labels and buttons
        numItemsLabel.setText(messages.getOrDefault("enter.num.items", "Enter number of items: "));
        startButton.setText(messages.getOrDefault("button.start", "Start"));
        calculateButton.setText(messages.getOrDefault("button.calculate", "Calculate Total"));
        saveButton.setText(messages.getOrDefault("button.save", "Save to Database"));

        // Update table columns
        if (tableView.getColumns().size() >= 4) {
            tableView.getColumns().get(1).setText(messages.getOrDefault(KEY_ENTER_PRICE, FALLBACK_PRICE));
            tableView.getColumns().get(2).setText(messages.getOrDefault(KEY_ENTER_QUANTITY, FALLBACK_QUANTITY));
            tableView.getColumns().get(3).setText(TABLE_HEADER_SUBTOTAL);
        }

        // Update existing item inputs if any
        if (itemInputs != null) {
            for (CartItemInput input : itemInputs) {
                input.updateLabels(messages);
            }
        }
        
        // Set RTL layout for Arabic
        if ("ar".equals(currentLanguage)) {
            root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        } else {
            root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }
    }

    private void showStatus(String message, String color) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + color + ";");

        if (statusResetTimer != null) {
            statusResetTimer.stop();
        }

        statusResetTimer = new PauseTransition(Duration.seconds(3));
        statusResetTimer.setOnFinished(event -> {
            if (statusLabel.getText().equals(message)) {
                statusLabel.setText(DEFAULT_READY_STATUS);
                statusLabel.setStyle("-fx-text-fill: " + STATUS_COLOR_SUCCESS + ";");
            }
        });
        statusResetTimer.playFromStart();
    }

    // Inner class for item input row
    private class CartItemInput {
        private int itemNumber;
        private Label priceLabel;
        private TextField priceField;
        private Label quantityLabel;
        private TextField quantityField;
        private HBox pane;

        public CartItemInput(int number, Map<String, String> messages) {
            this.itemNumber = number;
            createPane(messages);
        }

        private void createPane(Map<String, String> messages) {
            pane = new HBox(10);
            pane.setAlignment(Pos.CENTER_LEFT);
            pane.setPadding(new Insets(5));
            pane.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5px;");

            Label numLabel = new Label("Item " + itemNumber + ":");
            numLabel.setPrefWidth(80);
            numLabel.setStyle("-fx-font-weight: bold;");

            priceLabel = new Label(messages.getOrDefault(KEY_ENTER_PRICE, FALLBACK_PRICE) + ":");
            priceLabel.setPrefWidth(60);
            priceField = new TextField();
            priceField.setPrefWidth(100);
            priceField.setPromptText("0.00");

            quantityLabel = new Label(messages.getOrDefault(KEY_ENTER_QUANTITY, FALLBACK_QUANTITY) + ":");
            quantityLabel.setPrefWidth(70);
            quantityField = new TextField();
            quantityField.setPrefWidth(100);
            quantityField.setPromptText("0");

            pane.getChildren().addAll(numLabel, priceLabel, priceField, quantityLabel, quantityField);
        }

        public HBox getPane() {
            return pane;
        }

        public int getItemNumber() {
            return itemNumber;
        }

        public double getPrice() {
            return Double.parseDouble(priceField.getText());
        }

        public int getQuantity() {
            return Integer.parseInt(quantityField.getText());
        }

        public void updateLabels(Map<String, String> messages) {
            priceLabel.setText(messages.getOrDefault(KEY_ENTER_PRICE, FALLBACK_PRICE) + ":");
            quantityLabel.setText(messages.getOrDefault(KEY_ENTER_QUANTITY, FALLBACK_QUANTITY) + ":");
        }
    }

    // Inner class for table display
    public static class CartItemDisplay {
        private int number;
        private double price;
        private int quantity;
        private double subtotal;

        public CartItemDisplay(int number, double price, int quantity, double subtotal) {
            this.number = number;
            this.price = price;
            this.quantity = quantity;
            this.subtotal = subtotal;
        }

        public int getNumber() { return number; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public double getSubtotal() { return subtotal; }
    }
}
