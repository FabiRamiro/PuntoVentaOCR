package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import com.pos.puntoventaocr.models.Usuario;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class PrincipalController implements Initializable {

    @FXML private BorderPane mainContainer;
    @FXML private MenuBar menuBar;
    @FXML private Label lblUsuario;
    @FXML private Label lblFechaHora;
    @FXML private Label lblEstadoSesion;
    @FXML private ProgressBar progressBarSesion;

    // Menús principales
    @FXML private Menu menuVentas;
    @FXML private Menu menuProductos;
    @FXML private Menu menuUsuarios;
    @FXML private Menu menuReportes;
    @FXML private Menu menuOCR;
    @FXML private Menu menuSistema;

    // Items de menú - Ventas
    @FXML private MenuItem menuNuevaVenta;
    @FXML private MenuItem menuHistorialVentas;
    @FXML private MenuItem menuAnularVenta;

    // Items de menú - Productos
    @FXML private MenuItem menuGestionarProductos;
    @FXML private MenuItem menuCategorias;
    @FXML private MenuItem menuInventario;

    // Items de menú - Usuarios (solo administradores)
    @FXML private MenuItem menuGestionarUsuarios;
    @FXML private MenuItem menuRoles;

    // Items de menú - Reportes
    @FXML private MenuItem menuReporteVentas;
    @FXML private MenuItem menuReporteProductos;
    @FXML private MenuItem menuReporteStock;

    // Items de menú - OCR
    @FXML private MenuItem menuValidarTransferencia;
    @FXML private MenuItem menuHistorialOCR;

    // Items de menú - Sistema
    @FXML private MenuItem menuCambiarPassword;
    @FXML private MenuItem menuConfiguracion;
    @FXML private MenuItem menuAcercaDe;

    private Timer timerReloj;
    private SessionManager sessionManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sessionManager = SessionManager.getInstance();

        configurarInterfaz();
        iniciarReloj();
        configurarPermisos();
        actualizarInfoUsuario();
    }

    private void configurarInterfaz() {
        // Configurar la información inicial
        lblUsuario.setText(sessionManager.getNombreUsuarioDisplay());

        // Configurar progress bar de sesión
        progressBarSesion.setProgress(0.0);

        // Cargar vista inicial (dashboard o ventas)
        cargarVistaInicial();
    }

    private void iniciarReloj() {
        timerReloj = new Timer();
        timerReloj.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    // Actualizar fecha y hora
                    lblFechaHora.setText(LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

                    // Actualizar estado de sesión
                    actualizarEstadoSesion();
                });
            }
        }, 0, 1000); // Actualizar cada segundo
    }

    private void actualizarEstadoSesion() {
        long tiempoSesion = sessionManager.getTiempoSesionMinutos();
        lblEstadoSesion.setText("Sesión activa: " + tiempoSesion + " min");

        // Actualizar progress bar (basado en 8 horas = 480 minutos)
        double progreso = Math.min(tiempoSesion / 480.0, 1.0);
        progressBarSesion.setProgress(progreso);

        // Cambiar color si la sesión está cerca de expirar
        if (progreso > 0.9) {
            progressBarSesion.setStyle("-fx-accent: red;");
        } else if (progreso > 0.7) {
            progressBarSesion.setStyle("-fx-accent: orange;");
        } else {
            progressBarSesion.setStyle("-fx-accent: green;");
        }

        // Verificar si la sesión ha expirado
        if (sessionManager.sesionExpirada()) {
            cerrarSesionPorExpiracion();
        }
    }

    private void configurarPermisos() {
        Usuario usuario = sessionManager.getUsuarioActual();
        if (usuario == null) return;

        String rol = usuario.getRol().getNombreRol().toUpperCase();

        // Configurar visibilidad de menús según el rol
        menuUsuarios.setVisible(sessionManager.tienePermiso("GESTIONAR_USUARIOS"));
        menuOCR.setVisible(sessionManager.tienePermiso("VALIDAR_OCR"));

        // Configurar items específicos
        menuAnularVenta.setDisable(!sessionManager.tienePermiso("ANULAR_VENTAS"));
        menuGestionarProductos.setDisable(!sessionManager.tienePermiso("GESTIONAR_PRODUCTOS"));
        menuInventario.setDisable(!sessionManager.tienePermiso("GESTIONAR_INVENTARIO"));
        menuConfiguracion.setDisable(!sessionManager.tienePermiso("GESTIONAR_SISTEMA"));
    }

    private void actualizarInfoUsuario() {
        sessionManager.registrarActividad("Acceso al sistema principal");
    }

    private void cargarVistaInicial() {
        // Por defecto cargar la vista de ventas o dashboard
        if (sessionManager.tienePermiso("REALIZAR_VENTAS")) {
            handleNuevaVenta(null);
        } else {
            cargarVista("/views/dashboard.fxml");
        }
    }

    // === MÉTODOS DE NAVEGACIÓN ===

    @FXML
    private void handleNuevaVenta(ActionEvent event) {
        if (verificarPermiso("REALIZAR_VENTAS")) {
            cargarVista("/views/ventas/nueva_venta.fxml");
        }
    }

    @FXML
    private void handleHistorialVentas(ActionEvent event) {
        if (verificarPermiso("VER_REPORTES")) {
            cargarVista("/views/ventas/historial_ventas.fxml");
        }
    }

    @FXML
    private void handleAnularVenta(ActionEvent event) {
        if (verificarPermiso("ANULAR_VENTAS")) {
            cargarVista("/views/ventas/anular_venta.fxml");
        }
    }

    @FXML
    private void handleGestionarProductos(ActionEvent event) {
        if (verificarPermiso("GESTIONAR_PRODUCTOS")) {
            cargarVista("/views/productos/gestionar_productos.fxml");
        }
    }

    @FXML
    private void handleCategorias(ActionEvent event) {
        if (verificarPermiso("GESTIONAR_CATEGORIAS")) {
            cargarVista("/views/productos/categorias.fxml");
        }
    }

    @FXML
    private void handleInventario(ActionEvent event) {
        if (verificarPermiso("GESTIONAR_INVENTARIO")) {
            cargarVista("/views/productos/inventario.fxml");
        }
    }

    @FXML
    private void handleGestionarUsuarios(ActionEvent event) {
        if (verificarPermiso("GESTIONAR_USUARIOS")) {
            cargarVista("/views/usuarios/gestionar_usuarios.fxml");
        }
    }

    @FXML
    private void handleRoles(ActionEvent event) {
        if (verificarPermiso("GESTIONAR_USUARIOS")) {
            cargarVista("/views/usuarios/roles.fxml");
        }
    }

    @FXML
    private void handleValidarTransferencia(ActionEvent event) {
        if (verificarPermiso("VALIDAR_OCR")) {
            cargarVista("/views/ocr/validar_transferencia.fxml");
        }
    }

    @FXML
    private void handleHistorialOCR(ActionEvent event) {
        if (verificarPermiso("VALIDAR_OCR")) {
            cargarVista("/views/ocr/historial_ocr.fxml");
        }
    }

    @FXML
    private void handleReporteVentas(ActionEvent event) {
        if (verificarPermiso("VER_REPORTES")) {
            cargarVista("/views/reportes/reporte_ventas.fxml");
        }
    }

    @FXML
    private void handleReporteProductos(ActionEvent event) {
        if (verificarPermiso("VER_REPORTES")) {
            cargarVista("/views/reportes/reporte_productos.fxml");
        }
    }

    @FXML
    private void handleReporteStock(ActionEvent event) {
        if (verificarPermiso("VER_REPORTES")) {
            cargarVista("/views/reportes/reporte_stock.fxml");
        }
    }

    @FXML
    private void handleCambiarPassword(ActionEvent event) {
        abrirDialogoCambiarPassword();
    }

    @FXML
    private void handleConfiguracion(ActionEvent event) {
        if (verificarPermiso("GESTIONAR_SISTEMA")) {
            cargarVista("/views/sistema/configuracion.fxml");
        }
    }

    @FXML
    private void handleAcercaDe(ActionEvent event) {
        mostrarAcercaDe();
    }

    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        if (AlertUtils.mostrarConfirmacion("Cerrar Sesión",
                "¿Está seguro que desea cerrar la sesión?")) {
            cerrarSesion();
        }
    }

    // === MÉTODOS AUXILIARES ===

    private void cargarVista(String rutaFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Parent vista = loader.load();
            mainContainer.setCenter(vista);

            sessionManager.registrarActividad("Navegación a: " + rutaFxml);
        } catch (IOException e) {
            AlertUtils.mostrarError("Error de Navegación",
                    "No se pudo cargar la vista: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean verificarPermiso(String permiso) {
        if (!sessionManager.tienePermiso(permiso)) {
            AlertUtils.mostrarOperacionNoPermitida(permiso);
            return false;
        }
        return true;
    }

    private void abrirDialogoCambiarPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dialogs/cambiar_password.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Cambiar Contraseña");
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.initOwner(lblUsuario.getScene().getWindow());
            dialogStage.showAndWait();

        } catch (IOException e) {
            AlertUtils.mostrarError("Error", "No se pudo abrir el diálogo de cambio de contraseña");
        }
    }

    private void mostrarAcercaDe() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de Sistema POS");
        alert.setHeaderText("Sistema de Punto de Venta con OCR");
        alert.setContentText(
                "Versión: 1.0.0\n" +
                        "Desarrollado con JavaFX\n" +
                        "Base de datos: MySQL\n" +
                        "OCR: Tesseract\n\n" +
                        "Sistema completo para gestión de ventas,\n" +
                        "productos, usuarios y validación de transferencias."
        );
        alert.showAndWait();
    }

    private void cerrarSesionPorExpiracion() {
        Platform.runLater(() -> {
            AlertUtils.mostrarAdvertencia("Sesión Expirada",
                    "Su sesión ha expirado por inactividad. Será redirigido al login.");
            cerrarSesion();
        });
    }

    private void cerrarSesion() {
        // Detener timer
        if (timerReloj != null) {
            timerReloj.cancel();
        }

        // Cerrar sesión
        sessionManager.cerrarSesion();

        // Volver al login
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Sistema POS - Login");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

            // Cerrar ventana principal
            Stage currentStage = (Stage) lblUsuario.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            AlertUtils.mostrarError("Error", "No se pudo volver al login: " + e.getMessage());
            System.exit(0);
        }
    }
}