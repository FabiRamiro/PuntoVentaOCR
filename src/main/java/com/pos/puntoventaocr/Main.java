package com.pos.puntoventaocr;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.utils.AlertUtils;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
            // Verificar conexión a base de datos
            DatabaseConnection.getConnection();

            // Cargar ventana de login
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 450, 500);

            stage.setTitle("Sistema de Punto de Venta - Login");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(400);
            stage.setMinHeight(450);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            AlertUtils.showError("Error de Conexión",
                "No se pudo conectar a la base de datos: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}