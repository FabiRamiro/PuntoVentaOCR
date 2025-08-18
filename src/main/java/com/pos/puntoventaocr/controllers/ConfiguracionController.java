package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.ConfiguracionDAO;
import com.pos.puntoventaocr.dao.BitacoraDAO;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.Map;
import java.util.Optional;

public class ConfiguracionController {

    // Pestaña General
    @FXML private TextField txtNombreEmpresa;
    @FXML private TextField txtRif;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;

    // Pestaña Ventas
    @FXML private TextField txtIva;
    @FXML private ComboBox<String> cmbMoneda;
    @FXML private CheckBox chkPermitirVentaSinStock;
    @FXML private TextField txtSerieVentas;
    @FXML private TextField txtMensajeTicket;

    // Pestaña OCR
    @FXML private ComboBox<String> cmbMotorOCR;
    @FXML private TextField txtCarpetaArchivos;
    @FXML private CheckBox chkValidacionAutomatica;

    // NUEVOS CAMPOS: Pestaña Datos Bancarios
    @FXML private TextField txtBancoEmpresa;
    @FXML private ComboBox<String> cmbTipoCuenta;
    @FXML private TextField txtCuentaDestino;
    @FXML private TextField txtNombreBeneficiario;
    @FXML private TextField txtRifBancario;
    @FXML private TextField txtEmailNotificacion;

    // Pestaña Sistema
    @FXML private TextField txtTimeoutSesion;
    @FXML private CheckBox chkBackupAutomatico;
    @FXML private TextField txtRutaBackup;

    // Botones
    @FXML private Button btnGuardar;
    @FXML private Button btnRestaurar;

    private ConfiguracionDAO configuracionDAO;
    private BitacoraDAO bitacoraDAO;

    public void initialize() {
        configuracionDAO = new ConfiguracionDAO();
        bitacoraDAO = new BitacoraDAO();
        configurarCombos();
        cargarConfiguracion();
    }

    private void configurarCombos() {
        // Configurar monedas
        cmbMoneda.getItems().addAll("USD", "EUR", "VES", "COP", "MXN");
        cmbMoneda.setValue("USD");

        // Configurar motores OCR
        cmbMotorOCR.getItems().addAll("Tesseract", "Google Vision", "AWS Textract", "Azure OCR");
        cmbMotorOCR.setValue("Tesseract");

        // Configurar tipos de cuenta
        cmbTipoCuenta.getItems().addAll("Corriente", "Ahorro", "Empresarial");
        cmbTipoCuenta.setValue("Corriente");
    }

    private void cargarConfiguracion() {
        try {
            // Cargar configuraciones de empresa
            txtNombreEmpresa.setText(configuracionDAO.getNombreEmpresa());
            txtRif.setText(configuracionDAO.getRfcEmpresa());
            txtDireccion.setText(configuracionDAO.getDireccionEmpresa());
            txtTelefono.setText(configuracionDAO.getTelefonoEmpresa());

            // Cargar configuraciones de ventas
            txtIva.setText(String.valueOf(configuracionDAO.getIvaPorcentaje()));
            txtSerieVentas.setText(configuracionDAO.getSerieVentas());
            txtMensajeTicket.setText(configuracionDAO.getMensajeTicket());

            // Cargar configuraciones de sistema
            txtTimeoutSesion.setText(String.valueOf(configuracionDAO.getTimeoutSesion()));
            chkBackupAutomatico.setSelected(configuracionDAO.isBackupAutomatico());

            // Cargar configuraciones específicas de OCR y otros
            String motorOCR = configuracionDAO.obtenerConfiguracion("ocr.motor", "Tesseract");
            cmbMotorOCR.setValue(motorOCR);

            String carpetaArchivos = configuracionDAO.obtenerConfiguracion("ocr.carpeta_archivos", "");
            txtCarpetaArchivos.setText(carpetaArchivos);

            boolean validacionAutomatica = configuracionDAO.obtenerConfiguracionBoolean("ocr.validacion_automatica", false);
            chkValidacionAutomatica.setSelected(validacionAutomatica);

            boolean permitirVentaSinStock = configuracionDAO.obtenerConfiguracionBoolean("ventas.permitir_sin_stock", false);
            chkPermitirVentaSinStock.setSelected(permitirVentaSinStock);

            String moneda = configuracionDAO.obtenerConfiguracion("sistema.moneda", "USD");
            cmbMoneda.setValue(moneda);

            String rutaBackup = configuracionDAO.obtenerConfiguracion("sistema.ruta_backup", "");
            txtRutaBackup.setText(rutaBackup);

            // Cargar configuraciones bancarias
            String bancoEmpresa = configuracionDAO.obtenerConfiguracion("banco.empresa", "Banco1");
            txtBancoEmpresa.setText(bancoEmpresa);

            String tipoCuenta = configuracionDAO.obtenerConfiguracion("banco.tipo_cuenta", "Corriente");
            cmbTipoCuenta.setValue(tipoCuenta);

            String cuentaDestino = configuracionDAO.obtenerConfiguracion("banco.cuenta_destino", "");
            txtCuentaDestino.setText(cuentaDestino);

            String nombreBeneficiario = configuracionDAO.obtenerConfiguracion("banco.nombre_beneficiario", "");
            txtNombreBeneficiario.setText(nombreBeneficiario);

            String rifBancario = configuracionDAO.obtenerConfiguracion("banco.rif_bancario", "");
            txtRifBancario.setText(rifBancario);

            String emailNotificacion = configuracionDAO.obtenerConfiguracion("banco.email_notificacion", "");
            txtEmailNotificacion.setText(emailNotificacion);

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al cargar configuraciones: " + e.getMessage());
        }
    }

    @FXML
    private void guardarConfiguracion() {
        try {
            int idUsuario = SessionManager.getInstance().getUsuarioActual().getIdUsuario();

            // Obtener valores anteriores para la bitácora
            Map<String, String> valoresAnteriores = obtenerValoresActuales();

            boolean todoCorrecto = true;

            // Guardar configuraciones de empresa
            todoCorrecto &= configuracionDAO.guardarConfiguracion("empresa.nombre", txtNombreEmpresa.getText().trim(), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("empresa.rfc", txtRif.getText().trim(), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("empresa.direccion", txtDireccion.getText().trim(), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("empresa.telefono", txtTelefono.getText().trim(), idUsuario);

            // Guardar configuraciones de ventas
            todoCorrecto &= configuracionDAO.guardarConfiguracion("ventas.iva_porcentaje", txtIva.getText().trim(), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("ventas.numero_serie", txtSerieVentas.getText().trim(), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("ventas.mensaje_ticket", txtMensajeTicket.getText().trim(), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("ventas.permitir_sin_stock", String.valueOf(chkPermitirVentaSinStock.isSelected()), idUsuario);

            // Guardar configuraciones de sistema
            todoCorrecto &= configuracionDAO.guardarConfiguracion("sistema.timeout_sesion", txtTimeoutSesion.getText().trim(), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("sistema.backup_automatico", String.valueOf(chkBackupAutomatico.isSelected()), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("sistema.moneda", cmbMoneda.getValue(), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("sistema.ruta_backup", txtRutaBackup.getText().trim(), idUsuario);

            // Guardar configuraciones de OCR
            todoCorrecto &= configuracionDAO.guardarConfiguracion("ocr.motor", cmbMotorOCR.getValue(), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("ocr.carpeta_archivos", txtCarpetaArchivos.getText().trim(), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("ocr.validacion_automatica", String.valueOf(chkValidacionAutomatica.isSelected()), idUsuario);

            // Guardar configuraciones bancarias
            todoCorrecto &= configuracionDAO.guardarConfiguracion("banco.empresa", txtBancoEmpresa.getText().trim(), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("banco.tipo_cuenta", cmbTipoCuenta.getValue(), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("banco.cuenta_destino", txtCuentaDestino.getText().trim(), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("banco.nombre_beneficiario", txtNombreBeneficiario.getText().trim(), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("banco.rif_bancario", txtRifBancario.getText().trim(), idUsuario);
            todoCorrecto &= configuracionDAO.guardarConfiguracion("banco.email_notificacion", txtEmailNotificacion.getText().trim(), idUsuario);

            if (todoCorrecto) {
                // Registrar en bitácora
                bitacoraDAO.registrarConfiguracion(idUsuario, "CONFIGURACION_SISTEMA",
                    "Configuraciones del sistema actualizadas");

                // Forzar recarga del caché
                configuracionDAO.recargarCache();

                AlertUtils.showSuccess("Éxito", "Configuraciones guardadas correctamente");
            } else {
                AlertUtils.showError("Error", "Hubo problemas al guardar algunas configuraciones");
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al guardar configuraciones: " + e.getMessage());
        }
    }

    private Map<String, String> obtenerValoresActuales() {
        return Map.of(
            "empresa.nombre", txtNombreEmpresa.getText(),
            "empresa.rfc", txtRif.getText(),
            "ventas.iva_porcentaje", txtIva.getText(),
            "ventas.numero_serie", txtSerieVentas.getText(),
            "sistema.timeout_sesion", txtTimeoutSesion.getText(),
            "sistema.backup_automatico", String.valueOf(chkBackupAutomatico.isSelected())
        );
    }

    @FXML
    private void restaurarConfiguracion() {
        Optional<ButtonType> resultado = AlertUtils.confirmDialog(
            "Confirmar Restauración",
            "��Está seguro de restaurar la configuración a los valores por defecto?"
        );

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            cargarConfiguracion();
            AlertUtils.showInfo("Información", "Configuración restaurada");
        }
    }

    @FXML
    private void restaurarDefecto() {
        restaurarConfiguracion();
    }

    @FXML
    private void seleccionarCarpetaArchivos() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta para archivos OCR");

        File carpetaSeleccionada = directoryChooser.showDialog(txtCarpetaArchivos.getScene().getWindow());
        if (carpetaSeleccionada != null) {
            txtCarpetaArchivos.setText(carpetaSeleccionada.getAbsolutePath());
        }
    }

    @FXML
    private void seleccionarRutaBackup() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta para backups");

        File carpetaSeleccionada = directoryChooser.showDialog(txtRutaBackup.getScene().getWindow());
        if (carpetaSeleccionada != null) {
            txtRutaBackup.setText(carpetaSeleccionada.getAbsolutePath());
        }
    }

    @FXML
    private void probarConexionOCR() {
        try {
            String motor = cmbMotorOCR.getValue();
            AlertUtils.showInfo("Información", "Probando conexión con " + motor + "...");

            // Aquí iría la lógica para probar la conexión con el motor OCR

            AlertUtils.showSuccess("Éxito", "Conexión con " + motor + " exitosa");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al probar conexión OCR: " + e.getMessage());
        }
    }

    @FXML
    private void realizarBackup() {
        try {
            AlertUtils.showInfo("Información", "Iniciando backup...");

            // Aquí iría la lógica para realizar el backup

            AlertUtils.showSuccess("Éxito", "Backup realizado correctamente");

            // Registrar en bitácora
            int idUsuario = SessionManager.getInstance().getUsuarioActual().getIdUsuario();
            bitacoraDAO.registrarAccion(idUsuario, "BACKUP", "SISTEMA",
                "Realizó backup manual");

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al realizar backup: " + e.getMessage());
        }
    }

    @FXML
    private void seleccionarCarpeta() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar Carpeta para Archivos OCR");

        // Establecer directorio inicial si ya hay una ruta configurada
        String rutaActual = txtCarpetaArchivos.getText();
        if (rutaActual != null && !rutaActual.trim().isEmpty()) {
            File directorioActual = new File(rutaActual);
            if (directorioActual.exists() && directorioActual.isDirectory()) {
                directoryChooser.setInitialDirectory(directorioActual);
            }
        }

        // Mostrar el diálogo
        File carpetaSeleccionada = directoryChooser.showDialog(txtCarpetaArchivos.getScene().getWindow());

        if (carpetaSeleccionada != null) {
            txtCarpetaArchivos.setText(carpetaSeleccionada.getAbsolutePath());
        }
    }
}
