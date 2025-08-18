package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.BitacoraDAO;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import com.pos.puntoventaocr.models.Usuario;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PrincipalController {

    @FXML private BorderPane mainPane;
    @FXML private MenuBar menuBar;
    @FXML private Label lblUsuario;
    @FXML private Label lblRol;
    @FXML private Label lblFechaHora;
    @FXML private Label lblSesionInfo;

    // Menús principales
    @FXML private Menu menuVentas;
    @FXML private Menu menuProductos;
    @FXML private Menu menuOCR;
    @FXML private Menu menuReportes;
    @FXML private Menu menuAdministracion;
    @FXML private Menu menuSistema;

    private SessionManager sessionManager;
    private BitacoraDAO bitacoraDAO;
    private Timeline clockTimeline;

    public void initialize() {
        System.out.println("🔍 DEBUG: Inicializando PrincipalController...");

        sessionManager = SessionManager.getInstance();
        bitacoraDAO = new BitacoraDAO();

        // Configurar información de usuario
        configurarInterfazUsuario();

        // Configurar permisos de menús
        configurarPermisosMenus();

        // Iniciar reloj
        iniciarReloj();

        // Cargar pantalla de bienvenida por defecto
        cargarPantallaBienvenida();

        System.out.println("✅ DEBUG: PrincipalController inicializado correctamente");
    }

    private void configurarInterfazUsuario() {
        Usuario usuario = sessionManager.getUsuarioActual();
        if (usuario != null) {
            lblUsuario.setText(usuario.getNombreCompleto());
            lblRol.setText(usuario.getNombreRol());
            lblSesionInfo.setText("Sesión iniciada: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        } else {
            lblUsuario.setText("Usuario no identificado");
            lblRol.setText("Sin rol");
            lblSesionInfo.setText("Sesión no válida");
        }
    }

    private void configurarPermisosMenus() {
        Usuario usuario = sessionManager.getUsuarioActual();
        if (usuario == null) return;

        String rol = usuario.getNombreRol().toUpperCase();
        System.out.println("🔍 DEBUG: Configurando permisos para rol: " + rol);

        switch (rol) {
            case "ADMINISTRADOR":
                // Administrador tiene acceso a todo
                menuVentas.setDisable(false);
                menuProductos.setDisable(false);
                menuOCR.setDisable(false);
                menuReportes.setDisable(false);
                if (menuAdministracion != null) {
                    menuAdministracion.setDisable(false);
                }
                menuSistema.setDisable(false);
                break;

            case "GERENTE":
                // Gerente tiene acceso a todo excepto administración de usuarios y sistema
                menuVentas.setDisable(false);
                menuProductos.setDisable(false);
                menuOCR.setDisable(false);
                menuReportes.setDisable(false);
                if (menuAdministracion != null) {
                    menuAdministracion.setDisable(true); // No puede gestionar usuarios ni sistema
                }
                menuSistema.setDisable(false);
                break;

            case "CAJERO":
                // Cajero solo puede hacer ventas y ver productos (sin modificar)
                menuVentas.setDisable(false);
                menuProductos.setDisable(false); // Puede ver productos pero sin modificar
                menuOCR.setDisable(true); // Sin acceso a OCR
                menuReportes.setDisable(true); // Sin acceso a reportes
                if (menuAdministracion != null) {
                    menuAdministracion.setDisable(true); // Sin acceso a administración
                }
                menuSistema.setDisable(false); // Solo cambiar contraseña
                break;

            default:
                // Sin permisos por defecto
                menuVentas.setDisable(true);
                menuProductos.setDisable(true);
                menuOCR.setDisable(true);
                menuReportes.setDisable(true);
                if (menuAdministracion != null) {
                    menuAdministracion.setDisable(true);
                }
                menuSistema.setDisable(true);
                break;
        }

        System.out.println("✅ DEBUG: Permisos configurados para " + rol);
    }

    private void iniciarReloj() {
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            lblFechaHora.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        }));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    // ================== MÉTODOS DE MENÚ VENTAS ==================
    @FXML
    private void nuevaVenta() {
        cargarVista("/fxml/ventas/nueva_venta.fxml", "Nueva Venta");
    }

    @FXML
    private void historialVentas() {
        cargarVista("/fxml/ventas/historial_ventas.fxml", "Historial de Ventas");
    }

    @FXML
    private void gestionarDevoluciones() {
        cargarVista("/fxml/ventas/gestionar_devoluciones.fxml", "Gestionar Devoluciones");
    }

    // ================== MÉTODOS DE MENÚ PRODUCTOS ==================
    @FXML
    private void gestionarProductos() {
        cargarVista("/fxml/productos/gestionar_productos.fxml", "Gestionar Productos");
    }

    @FXML
    private void gestionarCategorias() {
        cargarVista("/fxml/productos/gestionar_categorias.fxml", "Gestionar Categorías");
    }

    @FXML
    private void alertasStock() {
        cargarVista("/fxml/productos/alertas_stock.fxml", "Alertas de Stock");
    }

    @FXML
    private void inventarioFisico() {
        cargarVista("/fxml/productos/inventario_fisico.fxml", "Inventario Físico");
    }

    // ================== MÉTODOS DE MENÚ OCR ==================
    @FXML
    private void validarOCR() {
        cargarVista("/fxml/ocr/validar_ocr.fxml", "Validar Comprobantes OCR");
    }

    @FXML
    private void historialOCR() {
        cargarVista("/fxml/ocr/historial_ocr.fxml", "Historial OCR");
    }

    @FXML
    private void configurarOCR() {
        AlertUtils.showInfo("Información", "Funcionalidad de configuración OCR en desarrollo");
    }

    // ================== MÉTODOS DE MENÚ REPORTES ==================
    @FXML
    private void reporteVentas() {
        cargarVista("/fxml/reportes/reporte_ventas.fxml", "Reporte de Ventas");
    }

    @FXML
    private void reporteProductos() {
        cargarVista("/fxml/reportes/reporte_productos.fxml", "Reporte de Productos");
    }

    @FXML
    private void reporteInventario() {
        cargarVista("/fxml/reportes/reporte_inventario.fxml", "Reporte de Inventario");
    }

    @FXML
    private void reporteGanancias() {
        cargarVista("/fxml/reportes/reporte_ganancias.fxml", "Reporte de Ganancias");
    }

    // ================== MÉTODOS DE MENÚ ADMINISTRACIÓN ==================
    @FXML
    private void gestionarUsuarios() {
        cargarVista("/fxml/admin/gestionar_usuarios.fxml", "Gestionar Usuarios");
    }

    @FXML
    private void configuracionSistema() {
        cargarVista("/fxml/admin/configuracion.fxml", "Configuración del Sistema");
    }

    @FXML
    private void bitacoraAcciones() {
        cargarVista("/fxml/admin/bitacora.fxml", "Bitácora de Acciones");
    }

    @FXML
    private void respaldoBD() {
        cargarVista("/fxml/admin/respaldo_bd.fxml", "Respaldo de Base de Datos");
    }

    // ================== MÉTODOS DE MENÚ SISTEMA ==================
    @FXML
    private void cambiarPassword() {
        cargarVista("/fxml/sistema/cambiar_password.fxml", "Cambiar Contraseña");
    }

    @FXML
    private void acercaDe() {
        AlertUtils.showInfo("Acerca de",
            "Sistema POS OCR v1.0\n\n" +
            "Sistema de Punto de Venta con reconocimiento OCR\n" +
            "para validación de comprobantes de transferencia.\n\n" +
            "© 2025 - Todos los derechos reservados");
    }

    @FXML
    private void cerrarSesion() {
        Optional<ButtonType> resultado = AlertUtils.confirmDialog(
            "Cerrar Sesión",
            "¿Está seguro de que desea cerrar la sesión?"
        );

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            // Registrar logout en bitácora
            Usuario usuario = sessionManager.getUsuarioActual();
            if (usuario != null) {
                bitacoraDAO.registrarLogout(usuario.getIdUsuario(), "127.0.0.1");
            }

            // Detener reloj
            if (clockTimeline != null) {
                clockTimeline.stop();
            }

            // Limpiar sesión
            sessionManager.limpiarSesion();

            // Volver a login
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Scene scene = new Scene(loader.load());
                Stage stage = (Stage) mainPane.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Sistema POS OCR - Login");
                stage.centerOnScreen();
            } catch (IOException e) {
                System.err.println("Error al cargar ventana de login: " + e.getMessage());
                Platform.exit();
            }
        }
    }

    // ================== MÉTODO AUXILIAR MEJORADO ==================
    private void cargarVista(String rutaFxml, String titulo) {
        try {
            System.out.println("🔍 DEBUG: Cargando vista en ventana principal: " + rutaFxml);

            // Cargar el FXML en el centro de la ventana principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Node contenido = loader.load();

            // Establecer el contenido en el centro del BorderPane principal
            mainPane.setCenter(contenido);

            // Actualizar el título de la ventana solo si la Scene está disponible
            try {
                if (mainPane.getScene() != null && mainPane.getScene().getWindow() != null) {
                    Stage stage = (Stage) mainPane.getScene().getWindow();
                    stage.setTitle(titulo + " - Sistema POS OCR");
                } else {
                    // Si la Scene no está disponible, programar la actualización del título para más tarde
                    Platform.runLater(() -> {
                        if (mainPane.getScene() != null && mainPane.getScene().getWindow() != null) {
                            Stage stage = (Stage) mainPane.getScene().getWindow();
                            stage.setTitle(titulo + " - Sistema POS OCR");
                        }
                    });
                }
            } catch (Exception titleException) {
                System.out.println("⚠️ WARNING: No se pudo actualizar el título de la ventana: " + titleException.getMessage());
                // No es crítico, continuamos sin actualizar el título
            }

            System.out.println("✅ DEBUG: Vista cargada exitosamente en ventana principal: " + titulo);

        } catch (IOException e) {
            System.err.println("❌ ERROR: No se pudo cargar la vista: " + rutaFxml);
            e.printStackTrace();
            AlertUtils.showError("Error",
                "No se pudo cargar la vista solicitada.\n" +
                "Vista: " + titulo + "\n" +
                "Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ ERROR inesperado al cargar vista: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Error", "Error inesperado: " + e.getMessage());
        }
    }

    // ================== MÉTODO CARGAR PANTALLA BIENVENIDA ==================
    private void cargarPantallaBienvenida() {
        // Cargar la vista de bienvenida (splash screen) al iniciar el sistema
        cargarVista("/fxml/bienvenida.fxml", "Bienvenida");
    }
}
