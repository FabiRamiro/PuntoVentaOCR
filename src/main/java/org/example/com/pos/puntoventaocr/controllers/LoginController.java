package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.UsuarioDAO;
import com.pos.puntoventaocr.models.Usuario;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblVersion;
    @FXML private ProgressIndicator progressLogin;
    @FXML private CheckBox chkRecordarUsuario;

    private UsuarioDAO usuarioDAO;
    private static final String VERSION = "v1.0.0";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usuarioDAO = new UsuarioDAO();
        lblVersion.setText(VERSION);
        progressLogin.setVisible(false);

        // Configurar eventos de teclado
        txtPassword.setOnKeyPressed(this::handleKeyPressed);
        txtUsuario.setOnKeyPressed(this::handleKeyPressed);

        // Cargar último usuario si está guardado
        cargarUltimoUsuario();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        realizarLogin();
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            realizarLogin();
        }
    }

    private void realizarLogin() {
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        // Validaciones básicas
        if (usuario.isEmpty()) {
            AlertUtils.mostrarError("Error", "Ingrese el nombre de usuario");
            txtUsuario.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            AlertUtils.mostrarError("Error", "Ingrese la contraseña");
            txtPassword.requestFocus();
            return;
        }

        // Mostrar indicador de progreso
        progressLogin.setVisible(true);
        btnLogin.setDisable(true);

        // Realizar autenticación en hilo separado
        new Thread(() -> {
            try {
                Usuario usuarioAutenticado = usuarioDAO.autenticar(usuario, password);

                javafx.application.Platform.runLater(() -> {
                    progressLogin.setVisible(false);
                    btnLogin.setDisable(false);

                    if (usuarioAutenticado != null) {
                        // Login exitoso
                        SessionManager.getInstance().setUsuarioActual(usuarioAutenticado);

                        if (chkRecordarUsuario.isSelected()) {
                            guardarUltimoUsuario(usuario);
                        }

                        abrirVentanaPrincipal();
                    } else {
                        // Login fallido
                        AlertUtils.mostrarError("Error de Autenticación",
                                "Usuario o contraseña incorrectos.\n" +
                                        "Verifique sus credenciales e intente nuevamente.");
                        txtPassword.clear();
                        txtUsuario.requestFocus();
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    progressLogin.setVisible(false);
                    btnLogin.setDisable(false);
                    AlertUtils.mostrarError("Error del Sistema",
                            "Error al conectar con la base de datos:\n" + e.getMessage());
                });
            }
        }).start();
    }

    private void abrirVentanaPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/principal.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Sistema POS - " + SessionManager.getInstance().getUsuarioActual().getNombreCompleto());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

            // Cerrar ventana de login
            Stage loginStage = (Stage) btnLogin.getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            AlertUtils.mostrarError("Error", "No se pudo abrir la ventana principal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRecuperarPassword(ActionEvent event) {
        // Mostrar diálogo para recuperación de contraseña
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Recuperar Contraseña");
        dialog.setHeaderText("Ingrese su nombre de usuario para recuperar la contraseña");

        ButtonType recuperarButtonType = new ButtonType("Recuperar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(recuperarButtonType, ButtonType.CANCEL);

        TextField usuarioField = new TextField();
        usuarioField.setPromptText("Nombre de usuario");

        dialog.getDialogPane().setContent(usuarioField);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == recuperarButtonType) {
                return usuarioField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(usuario -> {
            if (!usuario.trim().isEmpty()) {
                // Aquí implementarías la lógica de recuperación de contraseña
                AlertUtils.mostrarInformacion("Recuperación de Contraseña",
                        "Se ha enviado un correo con las instrucciones para recuperar su contraseña.");
            }
        });
    }

    @FXML
    private void handleSalir(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Salida");
        alert.setHeaderText("¿Está seguro que desea salir del sistema?");
        alert.setContentText("Se cerrará la aplicación completamente.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.exit(0);
            }
        });
    }

    private void cargarUltimoUsuario() {
        // Implementar lógica para cargar el último usuario guardado
        // Por ahora solo un ejemplo
        String ultimoUsuario = System.getProperty("ultimo.usuario", "");
        if (!ultimoUsuario.isEmpty()) {
            txtUsuario.setText(ultimoUsuario);
            chkRecordarUsuario.setSelected(true);
            txtPassword.requestFocus();
        }
    }

    private void guardarUltimoUsuario(String usuario) {
        // Implementar lógica para guardar el último usuario
        System.setProperty("ultimo.usuario", usuario);
    }
}