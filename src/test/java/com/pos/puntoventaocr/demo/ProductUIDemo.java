package com.pos.puntoventaocr.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.math.BigDecimal;

/**
 * Demo application to show the improved UI styles and product detail panel
 */
public class ProductUIDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create main layout
        BorderPane root = new BorderPane();
        
        // Top section with title and filters
        VBox topSection = createTopSection();
        root.setTop(topSection);
        
        // Center section with table and detail panel
        HBox centerSection = createCenterSection();
        root.setCenter(centerSection);
        
        // Create scene and apply styles
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/com/pos/puntoventaocr/styles.css").toExternalForm());
        
        primaryStage.setTitle("Punto de Venta OCR - Mejoras en Interfaz de Productos");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Simulate selection of first row
        TableView<ProductDemo> table = (TableView<ProductDemo>) ((VBox) centerSection.getChildren().get(0)).getChildren().get(0);
        if (!table.getItems().isEmpty()) {
            table.getSelectionModel().select(0);
        }
    }
    
    private VBox createTopSection() {
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(15));
        
        Label title = new Label("Gestión de Productos - Interfaz Mejorada");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        HBox filters = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por nombre o código...");
        searchField.setPrefWidth(200);
        
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Todas", "Electrónicos", "Ropa", "Hogar");
        categoryCombo.setValue("Todas");
        categoryCombo.setPrefWidth(150);
        
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Todos", "Activos", "Inactivos");
        statusCombo.setValue("Todos");
        statusCombo.setPrefWidth(120);
        
        filters.getChildren().addAll(searchField, categoryCombo, statusCombo);
        topSection.getChildren().addAll(title, filters);
        
        return topSection;
    }
    
    private HBox createCenterSection() {
        HBox centerSection = new HBox();
        
        // Left side - Product table
        VBox tableContainer = new VBox();
        tableContainer.setPrefWidth(600);
        HBox.setMargin(tableContainer, new Insets(5, 10, 15, 15));
        
        TableView<ProductDemo> table = createProductTable();
        tableContainer.getChildren().add(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        // Vertical separator
        Separator separator = new Separator();
        separator.setOrientation(Orientation.VERTICAL);
        
        // Right side - Detail panel
        VBox detailPanel = createDetailPanel();
        detailPanel.setPrefWidth(300);
        detailPanel.getStyleClass().add("product-detail-panel");
        HBox.setMargin(detailPanel, new Insets(5, 15, 15, 10));
        
        centerSection.getChildren().addAll(tableContainer, separator, detailPanel);
        return centerSection;
    }
    
    private TableView<ProductDemo> createProductTable() {
        TableView<ProductDemo> table = new TableView<>();
        
        // Create columns
        TableColumn<ProductDemo, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colCodigo.setPrefWidth(100);
        
        TableColumn<ProductDemo, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(150);
        
        TableColumn<ProductDemo, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colCategoria.setPrefWidth(100);
        
        TableColumn<ProductDemo, String> colPrecio = new TableColumn<>("P. Venta");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setPrefWidth(80);
        
        TableColumn<ProductDemo, Integer> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStock.setPrefWidth(60);
        
        // Apply custom cell factories for stock colors
        colStock.setCellFactory(column -> new TableCell<ProductDemo, Integer>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(stock.toString());
                    
                    if (getTableRow() != null && getTableRow().isSelected()) {
                        if (stock == 0) {
                            setStyle("-fx-background-color: rgba(244, 67, 54, 0.2); -fx-text-fill: #C62828; -fx-font-weight: bold;");
                        } else if (stock <= 5) {
                            setStyle("-fx-background-color: rgba(255, 152, 0, 0.2); -fx-text-fill: #EF6C00; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #1976D2; -fx-font-weight: bold;");
                        }
                    } else {
                        if (stock == 0) {
                            setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                        } else if (stock <= 5) {
                            setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                        }
                    }
                }
            }
        });
        
        TableColumn<ProductDemo, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setPrefWidth(70);
        
        table.getColumns().addAll(colCodigo, colNombre, colCategoria, colPrecio, colStock, colEstado);
        
        // Add sample data
        table.getItems().addAll(
            new ProductDemo("12345", "Laptop Gaming", "Electrónicos", "$1,200.00", 15, "Activo"),
            new ProductDemo("67890", "Mouse Inalámbrico", "Electrónicos", "$25.99", 3, "Activo"),
            new ProductDemo("11111", "Teclado Mecánico", "Electrónicos", "$89.99", 0, "Activo"),
            new ProductDemo("22222", "Monitor 24\"", "Electrónicos", "$299.99", 8, "Activo"),
            new ProductDemo("33333", "Webcam HD", "Electrónicos", "$59.99", 12, "Activo")
        );
        
        return table;
    }
    
    private VBox createDetailPanel() {
        VBox detailPanel = new VBox(10);
        
        Label title = new Label("Detalles del Producto");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Separator separator1 = new Separator();
        
        // Product details grid
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(8);
        
        // Add detail fields
        addDetailField(detailsGrid, 0, "Código:", "12345");
        addDetailField(detailsGrid, 1, "Nombre:", "Laptop Gaming");
        addDetailField(detailsGrid, 2, "Descripción:", "Laptop para gaming de alta gama");
        addDetailField(detailsGrid, 3, "P. Compra:", "$800.00");
        
        Label precioVentaLabel = new Label("P. Venta:");
        precioVentaLabel.getStyleClass().add("detail-label");
        Label precioVentaValue = new Label("$1,200.00");
        precioVentaValue.getStyleClass().addAll("detail-value", "price");
        detailsGrid.add(precioVentaLabel, 0, 4);
        detailsGrid.add(precioVentaValue, 1, 4);
        
        Label stockLabel = new Label("Stock:");
        stockLabel.getStyleClass().add("detail-label");
        Label stockValue = new Label("15 Piezas");
        stockValue.getStyleClass().addAll("detail-value", "stock", "available");
        detailsGrid.add(stockLabel, 0, 5);
        detailsGrid.add(stockValue, 1, 5);
        
        Label gananciaLabel = new Label("Ganancia:");
        gananciaLabel.getStyleClass().add("detail-label");
        Label gananciaValue = new Label("$400.00 (50.0%)");
        gananciaValue.getStyleClass().addAll("detail-value", "profit");
        detailsGrid.add(gananciaLabel, 0, 6);
        detailsGrid.add(gananciaValue, 1, 6);
        
        Separator separator2 = new Separator();
        
        Label imageTitle = new Label("Imagen del Producto");
        imageTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Image panel with placeholder
        VBox imagePanel = new VBox();
        imagePanel.setPrefHeight(220);
        imagePanel.getStyleClass().add("image-preview");
        imagePanel.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Create placeholder
        Rectangle placeholder = new Rectangle(200, 200);
        placeholder.setFill(Color.LIGHTGRAY);
        placeholder.setStroke(Color.GRAY);
        placeholder.setStrokeWidth(1);
        
        Label noImageLabel = new Label("Sin imagen");
        noImageLabel.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 14px;");
        
        StackPane placeholderPane = new StackPane();
        placeholderPane.getChildren().addAll(placeholder, noImageLabel);
        
        imagePanel.getChildren().add(placeholderPane);
        
        detailPanel.getChildren().addAll(
            title, separator1, detailsGrid, separator2, imageTitle, imagePanel
        );
        
        return detailPanel;
    }
    
    private void addDetailField(GridPane grid, int row, String labelText, String valueText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("detail-label");
        Label value = new Label(valueText);
        value.getStyleClass().add("detail-value");
        grid.add(label, 0, row);
        grid.add(value, 1, row);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    // Demo data class
    public static class ProductDemo {
        private final String codigo;
        private final String nombre;
        private final String categoria;
        private final String precio;
        private final Integer stock;
        private final String estado;
        
        public ProductDemo(String codigo, String nombre, String categoria, String precio, Integer stock, String estado) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.categoria = categoria;
            this.precio = precio;
            this.stock = stock;
            this.estado = estado;
        }
        
        // Getters
        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
        public String getCategoria() { return categoria; }
        public String getPrecio() { return precio; }
        public Integer getStock() { return stock; }
        public String getEstado() { return estado; }
    }
}