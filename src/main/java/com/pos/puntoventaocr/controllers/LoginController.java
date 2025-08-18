package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.UsuarioDAO;
import com.pos.puntoventaocr.dao.BitacoraDAO;
import com.pos.puntoventaocr.models.Usuario;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblMensaje;
    @FXML private ProgressIndicator progressIndicator;

    private UsuarioDAO usuarioDAO;
    private BitacoraDAO bitacoraDAO;

    @FXML
    public void initialize() {
        usuarioDAO = new UsuarioDAO();
        bitacoraDAO = new BitacoraDAO();
        progressIndicator.setVisible(false);

        // Configurar eventos
        btnLogin.setOnAction(e -> iniciarSesion());
        txtPassword.setOnAction(e -> iniciarSesion()); // Enter en password

        // Validaciones en tiempo real
        txtUsuario.textProperty().addListener((obs, oldText, newText) -> {
            lblMensaje.setText("");
            habilitarBotonLogin();
        });

        txtPassword.textProperty().addListener((obs, oldText, newText) -> {
            lblMensaje.setText("");
            habilitarBotonLogin();
        });

        // Foco inicial en usuario
        Platform.runLater(() -> txtUsuario.requestFocus());
    }

    @FXML
    private void iniciarSesion() {
        String nombreUsuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        // Validaciones
        if (nombreUsuario.isEmpty()) {
            mostrarMensaje("Por favor ingrese su nombre de usuario", "error");
            txtUsuario.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            mostrarMensaje("Por favor ingrese su contraseña", "error");
            txtPassword.requestFocus();
            return;
        }

        // Deshabilitar controles durante la autenticación
        deshabilitarControles(true);
        mostrarMensaje("Autenticando...", "info");

        // Crear tarea en segundo plano
        Task<Boolean> loginTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return usuarioDAO.autenticar(nombreUsuario, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            if (loginTask.getValue()) {
                // Autenticación exitosa
                Usuario usuario = usuarioDAO.obtenerPorNombreUsuario(nombreUsuario);
                if (usuario != null) {
                    // Establecer sesión
                    SessionManager.getInstance().setUsuarioActual(usuario);

                    // Registrar en bitácora
                    bitacoraDAO.registrarLogin(usuario.getIdUsuario(), "127.0.0.1");

                    mostrarMensaje("Bienvenido " + usuario.getNombreCompleto(), "success");

                    // Cargar ventana principal
                    cargarVentanaPrincipal();
                } else {
                    mostrarMensaje("Error al obtener datos del usuario", "error");
                    deshabilitarControles(false);
                }
            } else {
                mostrarMensaje("Credenciales incorrectas", "error");
                deshabilitarControles(false);
                txtPassword.clear();
                txtUsuario.requestFocus();
            }
        });

        loginTask.setOnFailed(e -> {
            mostrarMensaje("Error de conexión", "error");
            deshabilitarControles(false);
        });

        new Thread(loginTask).start();
    }

    private void cargarVentanaPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/principal.fxml"));
            Scene scene = new Scene(loader.load());

            // Verificar que el stage esté disponible antes de acceder a él
            Stage stage = null;
            if (btnLogin.getScene() != null && btnLogin.getScene().getWindow() != null) {
                stage = (Stage) btnLogin.getScene().getWindow();
            }

            if (stage != null) {
                stage.setScene(scene);
                stage.setTitle("Sistema POS - " + SessionManager.getInstance().getUsuarioActual().getNombreCompleto());
                stage.centerOnScreen();
            } else {
                // Si no se puede obtener el stage, mostrar error
                AlertUtils.showError("Error", "No se pudo acceder a la ventana principal");
            }

        } catch (IOException e) {
            AlertUtils.showError("Error", "No se pudo cargar la ventana principal: " + e.getMessage());
        }
    }

    private void mostrarMensaje(String mensaje, String tipo) {
        lblMensaje.setText(mensaje);
        lblMensaje.getStyleClass().clear();

        switch (tipo) {
            case "error":
                lblMensaje.getStyleClass().add("label-error");
                break;
            case "success":
                lblMensaje.getStyleClass().add("label-success");
                break;
            case "info":
            default:
                lblMensaje.getStyleClass().add("label-info");
                break;
        }
    }

    private void deshabilitarControles(boolean deshabilitar) {
        txtUsuario.setDisable(deshabilitar);
        txtPassword.setDisable(deshabilitar);
        btnLogin.setDisable(deshabilitar);
        progressIndicator.setVisible(deshabilitar);
    }

    private void habilitarBotonLogin() {
        boolean camposCompletos = !txtUsuario.getText().trim().isEmpty() &&
                !txtPassword.getText().isEmpty();
        btnLogin.setDisable(!camposCompletos);
    }

    @FXML
    private void salir() {
        System.exit(0);
    }

    @FXML
    private void mostrarAyuda() {
        AlertUtils.showInfo("Ayuda",
            "Sistema de Punto de Venta OCR\n\n" +
            "Para iniciar sesión:\n" +
            "• Ingrese su nombre de usuario\n" +
            "• Ingrese su contraseña\n" +
            "• Presione 'Ingresar' o Enter\n\n" +
            "Si tiene problemas para acceder:\n" +
            "• Verifique que sus credenciales sean correctas\n" +
            "• Contacte al administrador del sistema\n\n" +
            "© 2025 Sistema POS OCR");
    }
}
