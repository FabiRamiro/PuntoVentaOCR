package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.UsuarioDAO;
import com.pos.puntoventaocr.dao.BitacoraDAO;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.regex.Pattern;

public class CambiarPasswordController {

    @FXML private PasswordField txtPasswordActual;
    @FXML private PasswordField txtNuevaPassword;
    @FXML private PasswordField txtConfirmarPassword;

    @FXML private ProgressBar progressFortaleza;
    @FXML private Label lblFortaleza;

    @FXML private Label lblReqLongitud;
    @FXML private Label lblReqMayuscula;
    @FXML private Label lblReqMinuscula;
    @FXML private Label lblReqNumero;

    @FXML private Button btnCambiar;
    @FXML private Button btnCancelar;

    private UsuarioDAO usuarioDAO;
    private BitacoraDAO bitacoraDAO;
    private SessionManager sessionManager;

    public void initialize() {
        usuarioDAO = new UsuarioDAO();
        bitacoraDAO = new BitacoraDAO();
        sessionManager = SessionManager.getInstance();

        configurarValidacion();
        configurarEventos();
    }

    private void configurarValidacion() {
        // Listener para validar contraseña en tiempo real
        txtNuevaPassword.textProperty().addListener((obs, oldText, newText) -> {
            validarFortalezaPassword(newText);
            validarFormulario();
        });

        txtConfirmarPassword.textProperty().addListener((obs, oldText, newText) -> {
            validarFormulario();
        });

        txtPasswordActual.textProperty().addListener((obs, oldText, newText) -> {
            validarFormulario();
        });
    }

    private void configurarEventos() {
        btnCambiar.setDisable(true);
    }

    @FXML
    private void cambiarPassword() {
        String passwordActual = txtPasswordActual.getText();
        String nuevaPassword = txtNuevaPassword.getText();
        String confirmarPassword = txtConfirmarPassword.getText();

        // Validaciones
        if (passwordActual.isEmpty() || nuevaPassword.isEmpty() || confirmarPassword.isEmpty()) {
            AlertUtils.showWarning("Advertencia", "Todos los campos son obligatorios");
            return;
        }

        if (!nuevaPassword.equals(confirmarPassword)) {
            AlertUtils.showError("Error", "Las contraseñas no coinciden");
            return;
        }

        if (passwordActual.equals(nuevaPassword)) {
            AlertUtils.showWarning("Advertencia", "La nueva contraseña debe ser diferente a la actual");
            return;
        }

        try {
            int idUsuario = sessionManager.getUsuarioActual().getIdUsuario();

            // Verificar contraseña actual
            if (!usuarioDAO.verificarPassword(idUsuario, passwordActual)) {
                AlertUtils.showError("Error", "La contraseña actual es incorrecta");
                return;
            }

            // Cambiar contraseña
            if (usuarioDAO.cambiarPassword(idUsuario, nuevaPassword)) {
                // Registrar en bitácora
                bitacoraDAO.registrarAccion(idUsuario, "CAMBIO_PASSWORD", "USUARIOS",
                    "Usuario cambió su contraseña");

                AlertUtils.showSuccess("Éxito", "Contraseña cambiada correctamente");

                // Cerrar ventana
                Stage stage = (Stage) btnCambiar.getScene().getWindow();
                stage.close();

            } else {
                AlertUtils.showError("Error", "No se pudo cambiar la contraseña");
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al cambiar contraseña: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void validarFortalezaPassword(String password) {
        if (password == null || password.isEmpty()) {
            progressFortaleza.setProgress(0);
            lblFortaleza.setText("Sin contraseña");
            return;
        }

        int puntaje = 0;

        // Longitud mínima 8 caracteres
        boolean longitudOk = password.length() >= 8;
        lblReqLongitud.setStyle(longitudOk ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        if (longitudOk) puntaje++;

        // Al menos una mayúscula
        boolean mayusculaOk = Pattern.compile("[A-Z]").matcher(password).find();
        lblReqMayuscula.setStyle(mayusculaOk ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        if (mayusculaOk) puntaje++;

        // Al menos una minúscula
        boolean minusculaOk = Pattern.compile("[a-z]").matcher(password).find();
        lblReqMinuscula.setStyle(minusculaOk ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        if (minusculaOk) puntaje++;

        // Al menos un número
        boolean numeroOk = Pattern.compile("[0-9]").matcher(password).find();
        lblReqNumero.setStyle(numeroOk ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        if (numeroOk) puntaje++;

        // Actualizar indicador de fortaleza
        double progreso = puntaje / 4.0;
        progressFortaleza.setProgress(progreso);

        if (puntaje == 4) {
            lblFortaleza.setText("Fuerte");
            lblFortaleza.setStyle("-fx-text-fill: green;");
        } else if (puntaje >= 3) {
            lblFortaleza.setText("Media");
            lblFortaleza.setStyle("-fx-text-fill: orange;");
        } else {
            lblFortaleza.setText("Débil");
            lblFortaleza.setStyle("-fx-text-fill: red;");
        }
    }

    private void validarFormulario() {
        boolean formularioValido =
            !txtPasswordActual.getText().isEmpty() &&
            !txtNuevaPassword.getText().isEmpty() &&
            !txtConfirmarPassword.getText().isEmpty() &&
            txtNuevaPassword.getText().equals(txtConfirmarPassword.getText()) &&
            txtNuevaPassword.getText().length() >= 8;

        btnCambiar.setDisable(!formularioValido);
    }
}
