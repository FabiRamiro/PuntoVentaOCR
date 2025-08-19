package com.pos.puntoventaocr.test;

import com.pos.puntoventaocr.models.Producto;
import com.pos.puntoventaocr.models.Categoria;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Test application to demonstrate ProductoController functionality
 */
public class ProductoTestApp extends Application {
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ProductoTestApp.class.getResource("/com/pos/puntoventaocr/producto-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        
        // Add CSS styles
        String css = ProductoTestApp.class.getResource("/com/pos/puntoventaocr/styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        
        stage.setTitle("Punto de Venta OCR - Gestión de Productos");
        stage.setScene(scene);
        stage.show();
        
        // Create some sample data
        createSampleData();
    }
    
    private void createSampleData() {
        // This would normally be handled by the DAO, but for testing purposes
        // we could create sample products here
        System.out.println("ProductoController test application started successfully!");
        System.out.println("Features implemented:");
        System.out.println("- Product detail panel with image display");
        System.out.println("- Support for web URLs and local file images");
        System.out.println("- Double-click to view full-size image");
        System.out.println("- Improved table selection styles");
        System.out.println("- Color-coded stock levels");
        System.out.println("- Search and filter functionality");
    }

    public static void main(String[] args) {
        launch();
    }
}