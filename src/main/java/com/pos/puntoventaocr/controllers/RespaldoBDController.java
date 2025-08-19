package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.BitacoraDAO;
import com.pos.puntoventaocr.models.RespaldoInfo;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javafx.application.Platform;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RespaldoBDController {

    // Controles principales
    @FXML private Button btnCrearRespaldo;
    @FXML private Button btnSeleccionarRuta;
    @FXML private TextField txtRutaRespaldo;
    @FXML private TextArea txtLog;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblEstado;
    @FXML private CheckBox chkIncluirDatos;
    @FXML private CheckBox chkComprimirRespaldo;

    // Controles para restauración
    @FXML private TextField txtArchivoRestaurar;
    @FXML private Button btnSeleccionarArchivo;
    @FXML private CheckBox chkConfirmarRestauracion;
    @FXML private Button btnRestaurar;

    // Controles para respaldo automático
    @FXML private CheckBox chkRespaldoAutomatico;
    @FXML private ComboBox<String> cmbFrecuencia;
    @FXML private TextField txtHoraRespaldo;
    @FXML private Button btnGuardarConfiguracion;

    // Controles para historial
    @FXML private TableView<RespaldoInfo> tablaHistorial;
    @FXML private TableColumn<RespaldoInfo, String> colFecha;
    @FXML private TableColumn<RespaldoInfo, String> colArchivo;
    @FXML private TableColumn<RespaldoInfo, String> colTamaño;
    @FXML private TableColumn<RespaldoInfo, String> colTipo;
    @FXML private TableColumn<RespaldoInfo, String> colEstado;

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
        chkComprimirRespaldo.setSelected(true);

        // Verificar que los controles existen antes de usarlos
        if (chkConfirmarRestauracion != null) {
            chkConfirmarRestauracion.setSelected(false);
        }
        if (chkRespaldoAutomatico != null) {
            chkRespaldoAutomatico.setSelected(false);
        }

        lblEstado.setText("Listo para realizar respaldo");

        // Configurar ComboBox de frecuencia
        if (cmbFrecuencia != null) {
            cmbFrecuencia.getItems().addAll("Diario", "Semanal", "Mensual");
            cmbFrecuencia.setValue("Diario");
        }

        // Configurar tabla de historial
        if (tablaHistorial != null) {
            configurarTablaHistorial();
        }

        agregarLog("Sistema de respaldo inicializado correctamente");
    }

    @FXML
    private void seleccionarRutaRespaldo() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta para respaldo");

        if (carpetaRespaldo != null && carpetaRespaldo.exists()) {
            directoryChooser.setInitialDirectory(carpetaRespaldo);
        }

        // Usar el nombre correcto del botón
        File carpetaSeleccionada = directoryChooser.showDialog(btnSeleccionarRuta.getScene().getWindow());

        if (carpetaSeleccionada != null) {
            carpetaRespaldo = carpetaSeleccionada;
            txtRutaRespaldo.setText(carpetaSeleccionada.getAbsolutePath());
            agregarLog("Carpeta de respaldo seleccionada: " + carpetaSeleccionada.getAbsolutePath());
        }
    }

    @FXML
    private void seleccionarArchivoRestaurar() {
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
            txtArchivoRestaurar.setText(archivoSeleccionado.getAbsolutePath());
            agregarLog("Archivo de restauración seleccionado: " + archivoSeleccionado.getAbsolutePath());
        }
    }

    // Agregar campos para configuración de base de datos
    private static final String DB_HOST = "165.22.180.74"; // O puedes hacerlo configurable
    private static final String DB_PORT = "3306";
    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "c99011f0ea6a8886193d6ab93cc66ed83e7478b278e2456a";
    private static final String DB_NAME = "punto_venta";

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
    private void restaurarBaseDatos() {
        if (archivoRestauracion == null || !archivoRestauracion.exists()) {
            AlertUtils.showWarning("Advertencia", "Seleccione un archivo válido para restaurar");
            return;
        }

        // Verificar que el checkbox de confirmación esté marcado
        if (chkConfirmarRestauracion != null && !chkConfirmarRestauracion.isSelected()) {
            AlertUtils.showWarning("Advertencia",
                    "Debe confirmar que entiende las consecuencias de la restauración");
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

    @FXML
    private void guardarConfiguracionAutomatica() {
        if (chkRespaldoAutomatico == null || !chkRespaldoAutomatico.isSelected()) {
            AlertUtils.showInfo("Información", "Respaldo automático deshabilitado");
            agregarLog("Respaldo automático deshabilitado");
            return;
        }

        String frecuencia = cmbFrecuencia.getValue();
        String hora = txtHoraRespaldo.getText();

        if (frecuencia == null || frecuencia.isEmpty()) {
            AlertUtils.showWarning("Advertencia", "Seleccione una frecuencia para el respaldo automático");
            return;
        }

        if (hora == null || hora.isEmpty() || !hora.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            AlertUtils.showWarning("Advertencia", "Ingrese una hora válida en formato HH:mm (ej: 02:00)");
            return;
        }

        agregarLog("Configuración de respaldo automático guardada - Frecuencia: " + frecuencia + ", Hora: " + hora);
        AlertUtils.showInfo("Éxito", "Configuración de respaldo automático guardada correctamente");

        // Registrar en bitácora
        int idUsuario = SessionManager.getInstance().getUsuarioActual().getIdUsuario();
        bitacoraDAO.registrarAccion(idUsuario, "CONFIG", "SISTEMA",
                "Configuración de respaldo automático: " + frecuencia + " a las " + hora);
    }

    private void configurarTablaHistorial() {
        colFecha.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFecha()));
        colArchivo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNombreArchivo()));
        colTamaño.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTamaño()));
        colTipo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTipo()));
        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEstado()));
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

    private String encontrarMysqldump() {
        String[] posiblesRutas = {
                "mysqldump", // PATH del sistema
                "/opt/lampp/bin/mysqldump", // XAMPP Linux
                "/usr/bin/mysqldump",
                "/usr/local/bin/mysqldump",
                "/opt/mysql/bin/mysqldump",
                "/usr/local/mysql/bin/mysqldump"
        };

        for (String ruta : posiblesRutas) {
            try {
                ProcessBuilder pb = new ProcessBuilder(ruta, "--version");
                Process proceso = pb.start();
                int codigo = proceso.waitFor();
                if (codigo == 0) {
                    agregarLog("✅ Encontrado mysqldump en: " + ruta);
                    return ruta;
                }
            } catch (Exception e) {
                // Continuar con la siguiente ruta
            }
        }

        agregarLog("❌ No se encontró mysqldump en ninguna ubicación conocida");
        return null;
    }

    private String encontrarMysql() {
        String[] posiblesRutas = {
                "mysql", // PATH del sistema
                "/opt/lampp/bin/mysql", // XAMPP Linux
                "/usr/bin/mysql",
                "/usr/local/bin/mysql",
                "/opt/mysql/bin/mysql",
                "/usr/local/mysql/bin/mysql"
        };

        for (String ruta : posiblesRutas) {
            try {
                ProcessBuilder pb = new ProcessBuilder(ruta, "--version");
                Process proceso = pb.start();
                int codigo = proceso.waitFor();
                if (codigo == 0) {
                    agregarLog("✅ Encontrado mysql en: " + ruta);
                    return ruta;
                }
            } catch (Exception e) {
                // Continuar con la siguiente ruta
            }
        }

        agregarLog("❌ No se encontró mysql en ninguna ubicación conocida");
        return null;
    }

    private boolean ejecutarRespaldo() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nombreArchivo = "respaldo_pos_" + timestamp + ".sql";
            File archivoRespaldo = new File(carpetaRespaldo, nombreArchivo);

            agregarLog("Iniciando respaldo de base de datos...");
            agregarLog("Archivo destino: " + archivoRespaldo.getAbsolutePath());

            // Buscar mysqldump
            String mysqldumpPath = encontrarMysqldump();
            if (mysqldumpPath == null) {
                agregarLog("❌ Error: No se encontró mysqldump");
                return false;
            }

            ProcessBuilder pb = new ProcessBuilder();

            if (chkIncluirDatos.isSelected()) {
                pb.command(mysqldumpPath,
                        "--host=" + DB_HOST,
                        "--port=" + DB_PORT,
                        "--user=" + DB_USER,
                        "--password=" + DB_PASSWORD,
                        "--single-transaction",
                        "--routines",
                        "--triggers",
                        "--add-drop-database",
                        "--databases",
                        DB_NAME,
                        "--skip-lock-tables", // Evitar problemas con permisos
                        "--no-tablespaces"); // Evitar la advertencia de PROCESS privilege
            } else {
                pb.command(mysqldumpPath,
                        "--host=" + DB_HOST,
                        "--port=" + DB_PORT,
                        "--user=" + DB_USER,
                        "--password=" + DB_PASSWORD,
                        "--no-data",
                        "--routines",
                        "--triggers",
                        "--add-drop-database",
                        "--databases",
                        DB_NAME,
                        "--skip-lock-tables",
                        "--no-tablespaces");
            }

            pb.redirectOutput(archivoRespaldo);
            pb.redirectError(ProcessBuilder.Redirect.PIPE);

            agregarLog("🔧 Ejecutando comando mysqldump...");

            Process proceso = pb.start();

            // Leer errores/advertencias
            Thread errorReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(proceso.getErrorStream()))) {
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        // Filtrar advertencias conocidas que no son críticas
                        if (linea.contains("PROCESS privilege") ||
                                linea.contains("tablespaces")) {
                            agregarLog("💡 Info: " + linea);
                        } else if (linea.toLowerCase().contains("error")) {
                            agregarLog("❌ Error: " + linea);
                        } else {
                            agregarLog("⚠️ " + linea);
                        }
                    }
                } catch (Exception e) {
                    agregarLog("Error leyendo stderr: " + e.getMessage());
                }
            });
            errorReader.start();

            int codigoSalida = proceso.waitFor();
            errorReader.join(5000);

            if (codigoSalida == 0) {
                long tamaño = archivoRespaldo.length();

                // Verificar que el archivo no esté vacío
                if (tamaño == 0) {
                    agregarLog("❌ El archivo de respaldo está vacío");
                    archivoRespaldo.delete();
                    return false;
                }

                agregarLog("✅ Respaldo SQL creado exitosamente");
                agregarLog("📁 Tamaño del archivo: " + formatearTamaño(tamaño));

                // Validar contenido del archivo
                if (validarArchivoRespaldo(archivoRespaldo)) {
                    agregarLog("✅ Archivo de respaldo validado correctamente");
                } else {
                    agregarLog("⚠️ Advertencia: El archivo podría estar incompleto");
                }

                if (chkComprimirRespaldo.isSelected()) {
                    agregarLog("🗜️ Comprimiendo archivo...");
                    boolean comprimido = comprimirArchivo(archivoRespaldo);
                    if (comprimido) {
                        agregarLog("✅ Compresión completada");
                    } else {
                        agregarLog("⚠️ No se pudo comprimir el archivo, pero el respaldo es válido");
                    }
                }

                return true;
            } else {
                agregarLog("❌ Error en mysqldump, código de salida: " + codigoSalida);

                if (archivoRespaldo.exists() && archivoRespaldo.length() == 0) {
                    archivoRespaldo.delete();
                    agregarLog("🗑️ Archivo vacío eliminado");
                }

                return false;
            }

        } catch (Exception e) {
            agregarLog("❌ Error durante el respaldo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Método para validar que el archivo de respaldo contiene datos
    private boolean validarArchivoRespaldo(File archivo) {
        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(archivo))) {
            String linea;
            boolean encontroDatos = false;
            int lineasLeidas = 0;

            while ((linea = reader.readLine()) != null && lineasLeidas < 50) {
                if (linea.contains("CREATE DATABASE") ||
                        linea.contains("CREATE TABLE") ||
                        linea.contains("INSERT INTO")) {
                    encontroDatos = true;
                    break;
                }
                lineasLeidas++;
            }

            return encontroDatos;
        } catch (Exception e) {
            agregarLog("Error validando archivo: " + e.getMessage());
            return false;
        }
    }

    private boolean ejecutarRestauracion() {
        try {
            agregarLog("Iniciando restauración de base de datos...");
            agregarLog("📂 Archivo origen: " + archivoRestauracion.getAbsolutePath());

            // Verificar que el archivo existe y no está vacío
            if (!archivoRestauracion.exists() || archivoRestauracion.length() == 0) {
                agregarLog("❌ El archivo de respaldo no existe o está vacío");
                return false;
            }

            // Buscar mysql
            String mysqlPath = encontrarMysql();
            if (mysqlPath == null) {
                agregarLog("❌ Error: No se encontró mysql");
                agregarLog("💡 Sugerencia: Verifique que XAMPP esté instalado en /opt/lampp/");
                return false;
            }

            ProcessBuilder pb = new ProcessBuilder(
                    mysqlPath,
                    "--host=165.22.180.74",
                    "--port=3306",
                    "--user=admin",
                    "--password=c99011f0ea6a8886193d6ab93cc66ed83e7478b278e2456a",
                    "--default-character-set=utf8"
            );

            pb.redirectInput(archivoRestauracion);
            pb.redirectError(ProcessBuilder.Redirect.PIPE);

            agregarLog("🔧 Ejecutando comando de restauración...");

            Process proceso = pb.start();

            // Leer errores en tiempo real
            Thread errorReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(proceso.getErrorStream()))) {
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        // Filtrar advertencias menores
                        if (!linea.contains("Warning") || linea.contains("Error")) {
                            agregarLog("⚠️ " + linea);
                        }
                    }
                } catch (Exception e) {
                    agregarLog("Error leyendo stderr: " + e.getMessage());
                }
            });
            errorReader.start();

            int codigoSalida = proceso.waitFor();
            errorReader.join(5000);

            if (codigoSalida == 0) {
                agregarLog("✅ Restauración completada exitosamente");
                return true;
            } else {
                agregarLog("❌ Error en mysql, código de salida: " + codigoSalida);
                return false;
            }

        } catch (Exception e) {
            agregarLog("❌ Error durante la restauración: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Método auxiliar para formatear tamaño
    private String formatearTamaño(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    // Método para comprimir archivo (opcional)
    private boolean comprimirArchivo(File archivo) {
        try {
            // Usar gzip para comprimir
            ProcessBuilder pb = new ProcessBuilder("gzip", "-9", archivo.getAbsolutePath());
            Process proceso = pb.start();
            int codigo = proceso.waitFor();
            return codigo == 0;
        } catch (Exception e) {
            agregarLog("Error al comprimir: " + e.getMessage());
            return false;
        }
    }

    // Método mejorado para agregar log (thread-safe)
    private void agregarLog(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String lineaLog = "[" + timestamp + "] " + mensaje + "\n";

        // Ejecutar en el hilo de JavaFX
        Platform.runLater(() -> {
            if (txtLog != null) {
                txtLog.appendText(lineaLog);
                // Auto-scroll al final
                txtLog.setScrollTop(Double.MAX_VALUE);
            }
        });
    }

    private void habilitarControles(boolean habilitar) {
        btnCrearRespaldo.setDisable(!habilitar);
        if (btnRestaurar != null) {
            btnRestaurar.setDisable(!habilitar);
        }
        btnSeleccionarRuta.setDisable(!habilitar); // Nombre correcto
        if (btnSeleccionarArchivo != null) {
            btnSeleccionarArchivo.setDisable(!habilitar);
        }
    }
}