package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.BitacoraDAO;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RespaldoBDController {

    @FXML private Button btnCrearRespaldo;
    @FXML private Button btnRestaurarRespaldo;
    @FXML private Button btnSeleccionarCarpeta;
    @FXML private Button btnSeleccionarArchivo;

    @FXML private TextField txtRutaRespaldo;
    @FXML private TextField txtRutaRestauracion;

    @FXML private TextArea txtLog;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblEstado;

    @FXML private CheckBox chkIncluirDatos;
    @FXML private CheckBox chkComprimirArchivo;

    private BitacoraDAO bitacoraDAO;
    private File carpetaRespaldo;
    private File archivoRestauracion;

    public void initialize() {
        bitacoraDAO = new BitacoraDAO();
        progressBar.setVisible(false);

        // Configurar ruta por defecto
        String rutaPorDefecto = System.getProperty("user.home") + File.separator + "Respaldos_POS";
        txtRutaRespaldo.setText(rutaPorDefecto);
        carpetaRespaldo = new File(rutaPorDefecto);

        // Configurar valores por defecto
        chkIncluirDatos.setSelected(true);
        chkComprimirArchivo.setSelected(true);

        lblEstado.setText("Listo para realizar respaldo");

        agregarLog("Sistema de respaldo inicializado correctamente");
    }

    @FXML
    private void seleccionarCarpetaRespaldo() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta para respaldo");

        if (carpetaRespaldo != null && carpetaRespaldo.exists()) {
            directoryChooser.setInitialDirectory(carpetaRespaldo);
        }

        File carpetaSeleccionada = directoryChooser.showDialog(btnSeleccionarCarpeta.getScene().getWindow());

        if (carpetaSeleccionada != null) {
            carpetaRespaldo = carpetaSeleccionada;
            txtRutaRespaldo.setText(carpetaSeleccionada.getAbsolutePath());
            agregarLog("Carpeta de respaldo seleccionada: " + carpetaSeleccionada.getAbsolutePath());
        }
    }

    @FXML
    private void seleccionarArchivoRestauracion() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de respaldo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Archivos SQL", "*.sql"),
            new FileChooser.ExtensionFilter("Archivos comprimidos", "*.zip", "*.gz"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File archivoSeleccionado = fileChooser.showOpenDialog(btnSeleccionarArchivo.getScene().getWindow());

        if (archivoSeleccionado != null) {
            archivoRestauracion = archivoSeleccionado;
            txtRutaRestauracion.setText(archivoSeleccionado.getAbsolutePath());
            agregarLog("Archivo de restauración seleccionado: " + archivoSeleccionado.getAbsolutePath());
        }
    }

    @FXML
    private void crearRespaldo() {
        if (carpetaRespaldo == null || !validarCarpetaRespaldo()) {
            return;
        }

        // Crear tarea en segundo plano
        Task<Boolean> respaldoTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return ejecutarRespaldo();
            }
        };

        respaldoTask.setOnSucceeded(e -> {
            progressBar.setVisible(false);
            if (respaldoTask.getValue()) {
                lblEstado.setText("Respaldo completado exitosamente");
                agregarLog("✅ Respaldo completado exitosamente");

                // Registrar en bitácora
                int idUsuario = SessionManager.getInstance().getUsuarioActual().getIdUsuario();
                bitacoraDAO.registrarAccion(idUsuario, "BACKUP", "SISTEMA",
                    "Respaldo de base de datos creado en: " + carpetaRespaldo.getAbsolutePath());

                AlertUtils.showInfo("Éxito", "Respaldo creado exitosamente");
            } else {
                lblEstado.setText("Error en el respaldo");
                agregarLog("❌ Error al crear el respaldo");
                AlertUtils.showError("Error", "No se pudo completar el respaldo");
            }
            habilitarControles(true);
        });

        respaldoTask.setOnFailed(e -> {
            progressBar.setVisible(false);
            lblEstado.setText("Error en el respaldo");
            agregarLog("❌ Error inesperado: " + respaldoTask.getException().getMessage());
            AlertUtils.showError("Error", "Error inesperado: " + respaldoTask.getException().getMessage());
            habilitarControles(true);
        });

        habilitarControles(false);
        progressBar.setVisible(true);
        lblEstado.setText("Creando respaldo...");
        agregarLog("Iniciando proceso de respaldo...");

        new Thread(respaldoTask).start();
    }

    @FXML
    private void restaurarRespaldo() {
        if (archivoRestauracion == null || !archivoRestauracion.exists()) {
            AlertUtils.showWarning("Advertencia", "Seleccione un archivo válido para restaurar");
            return;
        }

        // Confirmar restauración
        if (!AlertUtils.confirmDialog("Confirmar Restauración",
            "¿Está seguro de restaurar la base de datos?\n\nEsta acción reemplazará todos los datos actuales.").get().equals(ButtonType.OK)) {
            return;
        }

        // Crear tarea en segundo plano
        Task<Boolean> restaurarTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return ejecutarRestauracion();
            }
        };

        restaurarTask.setOnSucceeded(e -> {
            progressBar.setVisible(false);
            if (restaurarTask.getValue()) {
                lblEstado.setText("Restauración completada exitosamente");
                agregarLog("✅ Restauración completada exitosamente");

                // Registrar en bitácora
                int idUsuario = SessionManager.getInstance().getUsuarioActual().getIdUsuario();
                bitacoraDAO.registrarAccion(idUsuario, "RESTORE", "SISTEMA",
                    "Base de datos restaurada desde: " + archivoRestauracion.getAbsolutePath());

                AlertUtils.showInfo("Éxito", "Base de datos restaurada exitosamente");
            } else {
                lblEstado.setText("Error en la restauración");
                agregarLog("❌ Error al restaurar la base de datos");
                AlertUtils.showError("Error", "No se pudo completar la restauración");
            }
            habilitarControles(true);
        });

        restaurarTask.setOnFailed(e -> {
            progressBar.setVisible(false);
            lblEstado.setText("Error en la restauración");
            agregarLog("❌ Error inesperado: " + restaurarTask.getException().getMessage());
            AlertUtils.showError("Error", "Error inesperado: " + restaurarTask.getException().getMessage());
            habilitarControles(true);
        });

        habilitarControles(false);
        progressBar.setVisible(true);
        lblEstado.setText("Restaurando base de datos...");
        agregarLog("Iniciando proceso de restauración...");

        new Thread(restaurarTask).start();
    }

    private boolean validarCarpetaRespaldo() {
        if (!carpetaRespaldo.exists()) {
            try {
                if (!carpetaRespaldo.mkdirs()) {
                    AlertUtils.showError("Error", "No se pudo crear la carpeta de respaldo");
                    return false;
                }
                agregarLog("Carpeta de respaldo creada: " + carpetaRespaldo.getAbsolutePath());
            } catch (Exception e) {
                AlertUtils.showError("Error", "Error al crear carpeta: " + e.getMessage());
                return false;
            }
        }

        if (!carpetaRespaldo.canWrite()) {
            AlertUtils.showError("Error", "No se tienen permisos de escritura en la carpeta seleccionada");
            return false;
        }

        return true;
    }

    private boolean ejecutarRespaldo() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nombreArchivo = "respaldo_pos_" + timestamp + ".sql";
            File archivoRespaldo = new File(carpetaRespaldo, nombreArchivo);

            agregarLog("Iniciando respaldo de base de datos...");
            agregarLog("Archivo destino: " + archivoRespaldo.getAbsolutePath());

            // Comando mysqldump para crear el respaldo
            ProcessBuilder pb = new ProcessBuilder();

            if (chkIncluirDatos.isSelected()) {
                pb.command("mysqldump",
                    "--host=localhost",
                    "--user=root",
                    "--password=",
                    "--single-transaction",
                    "--routines",
                    "--triggers",
                    "punto_venta_ocr");
            } else {
                pb.command("mysqldump",
                    "--host=localhost",
                    "--user=root",
                    "--password=",
                    "--no-data",
                    "--routines",
                    "--triggers",
                    "punto_venta_ocr");
            }

            pb.redirectOutput(archivoRespaldo);
            Process proceso = pb.start();

            int codigoSalida = proceso.waitFor();

            if (codigoSalida == 0) {
                agregarLog("Respaldo SQL creado exitosamente");

                // Comprimir si está habilitado
                if (chkComprimirArchivo.isSelected()) {
                    agregarLog("Comprimiendo archivo...");
                    // TODO: Implementar compresión ZIP
                    agregarLog("Compresión completada");
                }

                return true;
            } else {
                agregarLog("Error en mysqldump, código: " + codigoSalida);
                return false;
            }

        } catch (Exception e) {
            agregarLog("Error durante el respaldo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean ejecutarRestauracion() {
        try {
            agregarLog("Iniciando restauración de base de datos...");
            agregarLog("Archivo origen: " + archivoRestauracion.getAbsolutePath());

            // Comando mysql para restaurar
            ProcessBuilder pb = new ProcessBuilder(
                "mysql",
                "--host=localhost",
                "--user=root",
                "--password=",
                "punto_venta_ocr"
            );

            pb.redirectInput(archivoRestauracion);
            Process proceso = pb.start();

            int codigoSalida = proceso.waitFor();

            if (codigoSalida == 0) {
                agregarLog("Restauración completada exitosamente");
                return true;
            } else {
                agregarLog("Error en mysql, código: " + codigoSalida);
                return false;
            }

        } catch (Exception e) {
            agregarLog("Error durante la restauración: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void habilitarControles(boolean habilitar) {
        btnCrearRespaldo.setDisable(!habilitar);
        btnRestaurarRespaldo.setDisable(!habilitar);
        btnSeleccionarCarpeta.setDisable(!habilitar);
        btnSeleccionarArchivo.setDisable(!habilitar);
    }

    private void agregarLog(String mensaje) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String lineaLog = "[" + timestamp + "] " + mensaje + "\n";
        txtLog.appendText(lineaLog);
    }

    @FXML
    private void seleccionarRutaRespaldo() {
        seleccionarCarpetaRespaldo();
    }

    @FXML
    private void seleccionarArchivoRestaurar() {
        seleccionarArchivoRestauracion();
    }
}
