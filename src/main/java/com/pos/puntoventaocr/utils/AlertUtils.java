package com.pos.puntoventaocr.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Utilidad para mostrar diferentes tipos de alertas y diálogos en la aplicación
 */
public class AlertUtils {

    /**
     * Muestra una alerta de información
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();

        alert.showAndWait();
    }

    /**
     * Muestra una alerta de advertencia
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();

        alert.showAndWait();
    }

    /**
     * Muestra una alerta de error
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();

        alert.showAndWait();
    }

    /**
     * Muestra una alerta de confirmación
     */
    public static Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();

        return alert.showAndWait();
    }

    /**
     * Muestra un diálogo de confirmación simple que retorna true/false
     */
    public static boolean showConfirmationDialog(String title, String message) {
        Optional<ButtonType> result = showConfirmation(title, message);
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Muestra una alerta de éxito
     */
    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();

        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de entrada de texto
     */
    public static Optional<String> showTextDialog(String title, String message, String defaultText) {
        TextInputDialog dialog = new TextInputDialog(defaultText);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();

        return dialog.showAndWait();
    }

    /**
     * Alias en español para mostrar información
     */
    public static void mostrarInformacion(String title, String message) {
        showInfo(title, message);
    }

    /**
     * Alias en español para mostrar error
     */
    public static void mostrarError(String title, String message) {
        showError(title, message);
    }

    /**
     * Alias en español para mostrar diálogo de texto
     */
    public static Optional<String> mostrarDialogoTexto(String title, String message, String defaultText) {
        return showTextDialog(title, message, defaultText);
    }

    /**
     * Método de confirmación que devuelve boolean directamente
     */
    public static boolean showConfirmationBoolean(String title, String message) {
        return showConfirmationDialog(title, message);
    }

    /**
     * Alias para confirmDialog (mantener compatibilidad)
     */
    public static Optional<ButtonType> confirmDialog(String title, String message) {
        return showConfirmation(title, message);
    }

    /**
     * Método para mostrar diálogo de entrada de texto (alias)
     */
    public static Optional<String> showInputDialog(String title, String message, String defaultText) {
        return showTextDialog(title, message, defaultText);
    }
}
