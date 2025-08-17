package com.pos.puntoventaocr;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.utils.AlertUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Verificar conexión a base de datos al inicio
            if (!DatabaseConnection.testConnection()) {
                AlertUtils.mostrarError("Error de Conexión",
                        "No se pudo conectar a la base de datos.\n" +
                                "Verifique que MySQL esté ejecutándose y que la base de datos 'punto_venta_ocr' exista.");
                return;
            }

            // Cargar la vista de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            // Crear la escena
            Scene scene = new Scene(root);

            // Cargar estilos CSS
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            // Configurar la ventana principal
            primaryStage.setTitle("Sistema POS con OCR - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            // Cargar ícono de la aplicación (opcional)
            try {
                Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("No se pudo cargar el ícono de la aplicación");
            }

            // Configurar evento de cierre
            primaryStage.setOnCloseRequest(event -> {
                // Cerrar conexión a base de datos al salir
                DatabaseConnection.closeConnection();
                System.out.println("Aplicación cerrada correctamente");
            });

            // Mostrar la ventana
            primaryStage.show();

            System.out.println("Sistema POS iniciado correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.mostrarError("Error de Inicio",
                    "No se pudo iniciar la aplicación: " + e.getMessage());
        }
    }

    /**
     * Método principal que inicia la aplicación
     */
    public static void main(String[] args) {
        System.out.println("Iniciando Sistema POS con OCR...");

        // Configurar propiedades del sistema antes de iniciar JavaFX
        System.setProperty("java.awt.headless", "false");

        try {
            // Iniciar aplicación JavaFX
            launch(args);
        } catch (Exception e) {
            System.err.println("Error fatal al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método llamado antes de que se cierre la aplicación
     */
    @Override
    public void stop() {
        try {
            // Cleanup al cerrar la aplicación
            DatabaseConnection.closeConnection();
            System.out.println("Recursos liberados correctamente");
        } catch (Exception e) {
            System.err.println("Error al liberar recursos: " + e.getMessage());
        }
    }
}