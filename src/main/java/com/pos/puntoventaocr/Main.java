package com.pos.puntoventaocr;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.pos.puntoventaocr.config.DatabaseConnection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Verificar conexión a base de datos
            if (!DatabaseConnection.testConnection()) {
                System.err.println("Error: No se pudo conectar a la base de datos");
                System.exit(1);
            }

            // Cargar la vista de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();

            // Configurar la escena
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            // Configurar el stage
            primaryStage.setTitle("Sistema POS - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            // Si tienes un logo, descomenta esta línea
            // primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        // Cerrar conexiones de base de datos al cerrar la aplicación
        DatabaseConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}