package com.pos.puntoventaocr.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;

public class AlertUtils {

    /**
     * Muestra una alerta de error
     */
    public static void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        // Configurar el ícono de la ventana
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();

        alert.showAndWait();
    }

    /**
     * Muestra una alerta de información
     */
    public static void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();

        alert.showAndWait();
    }

    /**
     * Muestra una alerta de advertencia
     */
    public static void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();

        alert.showAndWait();
    }

    /**
     * Muestra una alerta de confirmación
     */
    public static boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Muestra una alerta de confirmación personalizada
     */
    public static Optional<ButtonType> mostrarConfirmacionPersonalizada(String titulo, String mensaje,
                                                                        String textoBotonOK, String textoBotonCancel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        // Personalizar botones
        ButtonType botonOK = new ButtonType(textoBotonOK);
        ButtonType botonCancel = new ButtonType(textoBotonCancel);
        alert.getButtonTypes().setAll(botonOK, botonCancel);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();

        return alert.showAndWait();
    }

    /**
     * Muestra un diálogo de entrada de texto
     */
    public static Optional<String> mostrarDialogoTexto(String titulo, String mensaje, String textoDefecto) {
        TextInputDialog dialog = new TextInputDialog(textoDefecto);
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);
        dialog.setContentText(mensaje);

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();

        return dialog.showAndWait();
    }

    /**
     * Muestra un diálogo simple de entrada de texto
     */
    public static Optional<String> mostrarDialogoTexto(String titulo, String mensaje) {
        return mostrarDialogoTexto(titulo, mensaje, "");
    }

    /**
     * Muestra una alerta de éxito
     */
    public static void mostrarExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText("Operación Exitosa");
        alert.setContentText(mensaje);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();

        alert.showAndWait();
    }

    /**
     * Muestra una alerta con detalles expandibles
     */
    public static void mostrarErrorConDetalles(String titulo, String mensaje, String detalles) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(mensaje);
        alert.setContentText("Haga clic en 'Mostrar Detalles' para ver más información.");

        // Crear el contenido expandible
        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(detalles);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setExpandableContent(textArea);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();

        alert.showAndWait();
    }

    /**
     * Muestra una alerta de validación
     */
    public static void mostrarErrorValidacion(String titulo, String campo, String tipoError) {
        String mensaje = String.format("Error en el campo '%s': %s", campo, tipoError);
        mostrarError(titulo, mensaje);
    }

    /**
     * Muestra una alerta de operación no permitida
     */
    public static void mostrarOperacionNoPermitida(String operacion) {
        mostrarAdvertencia("Operación No Permitida",
                "No tiene permisos suficientes para realizar la operación: " + operacion);
    }

    /**
     * Muestra una alerta de conexión perdida
     */
    public static void mostrarErrorConexion() {
        mostrarError("Error de Conexión",
                "Se ha perdido la conexión con la base de datos.\n" +
                        "Verifique su conexión a internet y vuelva a intentar.");
    }
}