package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.ComprobanteOCRDAO;
import com.pos.puntoventaocr.dao.VentaDAO;
import com.pos.puntoventaocr.dao.BitacoraDAO;
import com.pos.puntoventaocr.models.ComprobanteOCR;
import com.pos.puntoventaocr.models.MotorOCR;
import com.pos.puntoventaocr.models.Venta;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ValidarOCRController {

    @FXML private TextField txtRutaArchivo;
    @FXML private Button btnSeleccionarArchivo;
    @FXML private Button btnProcesarOCR;
    @FXML private ProgressIndicator progressOCR;
    @FXML private Label lblEstadoProceso;
    @FXML private ImageView imgComprobante;

    // Controles de zoom para la imagen
    @FXML private Button btnZoomIn;
    @FXML private Button btnZoomOut;
    @FXML private Button btnZoomReset;
    @FXML private Button btnVerCompleta;

    // Formulario de datos extraídos
    @FXML private TextField txtBancoEmisor;
    @FXML private TextField txtReferencia;
    @FXML private TextField txtMonto;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtCuentaRemitente;
    @FXML private TextField txtBeneficiario;

    @FXML private Button btnCorregirDatos;
    @FXML private Button btnLimpiarFormulario;

    // Panel de validación
    @FXML private Label lblValidacionMonto;
    @FXML private Label txtValidacionMonto;
    @FXML private Label lblValidacionFecha;
    @FXML private Label txtValidacionFecha;
    @FXML private Label lblValidacionReferencia;
    @FXML private Label txtValidacionReferencia;

    // Búsqueda de venta
    @FXML private TextField txtBuscarVenta;
    @FXML private Button btnBuscarVenta;
    @FXML private Label lblVentaEncontrada;

    @FXML private TextArea txtObservaciones;
    @FXML private Button btnValidar;
    @FXML private Button btnRechazar;

    // Estado
    @FXML private Label lblEstadoGeneral;
    @FXML private Label lblTiempoProcesamientoOCR;

    private ComprobanteOCRDAO comprobanteDAO;
    private VentaDAO ventaDAO;
    private BitacoraDAO bitacoraDAO;
    private SessionManager sessionManager;
    private MotorOCR motorOCR;
    private ComprobanteOCR comprobanteActual;
    private Venta ventaAsociada;
    private boolean comprobanteValidado = false; // Variable para rastrear si el comprobante fue validado

    public void initialize() {
        comprobanteDAO = new ComprobanteOCRDAO();
        ventaDAO = new VentaDAO();
        bitacoraDAO = new BitacoraDAO();
        sessionManager = SessionManager.getInstance();
        motorOCR = MotorOCR.getInstance(); // ✅ CAMBIO: Usar getInstance() en lugar del constructor

        configurarEventos();
        resetearFormulario();
    }

    private void configurarEventos() {
        // Habilitar procesamiento solo si hay archivo
        txtRutaArchivo.textProperty().addListener((obs, oldText, newText) -> {
            btnProcesarOCR.setDisable(newText.trim().isEmpty());
        });

        // Validación automática cuando cambian los datos - MEJORADO
        txtMonto.textProperty().addListener((obs, oldText, newText) -> validarDatosAutomatico());
        dpFecha.valueProperty().addListener((obs, oldDate, newDate) -> validarDatosAutomatico());
        txtReferencia.textProperty().addListener((obs, oldText, newText) -> validarDatosAutomatico());

        // NUEVOS LISTENERS para todos los campos editables
        txtBancoEmisor.textProperty().addListener((obs, oldText, newText) -> validarDatosAutomatico());
        txtCuentaRemitente.textProperty().addListener((obs, oldText, newText) -> validarDatosAutomatico());
        txtBeneficiario.textProperty().addListener((obs, oldText, newText) -> validarDatosAutomatico());
        txtBuscarVenta.textProperty().addListener((obs, oldText, newText) -> {
            // Si se cambia la venta manualmente, limpiar la asociación
            if (!newText.trim().isEmpty() && ventaAsociada != null &&
                !newText.equals(ventaAsociada.getNumeroVenta())) {
                ventaAsociada = null;
                lblVentaEncontrada.setText("Busque la venta nuevamente");
                lblVentaEncontrada.setStyle("-fx-text-fill: orange;");
            }
        });
    }

    @FXML
    private void seleccionarArchivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Comprobante");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("PDF", "*.pdf"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File archivo = fileChooser.showOpenDialog(btnSeleccionarArchivo.getScene().getWindow());
        if (archivo != null) {
            txtRutaArchivo.setText(archivo.getAbsolutePath());
            cargarVistaPrevia(archivo);
        }
    }

    private void cargarVistaPrevia(File archivo) {
        try {
            if (archivo.getName().toLowerCase().endsWith(".pdf")) {
                // Para PDF mostrar ícono
                lblEstadoProceso.setText("Archivo PDF seleccionado");
            } else {
                // Para imágenes mostrar vista previa
                Image imagen = new Image(archivo.toURI().toString());
                imgComprobante.setImage(imagen);
                lblEstadoProceso.setText("Imagen cargada correctamente");
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error cargando vista previa: " + e.getMessage());
        }
    }

    @FXML
    private void procesarOCR() {
        String rutaArchivo = txtRutaArchivo.getText().trim();
        if (rutaArchivo.isEmpty()) {
            AlertUtils.showWarning("Archivo", "Seleccione un archivo para procesar");
            return;
        }

        btnProcesarOCR.setDisable(true);
        progressOCR.setVisible(true);
        lblEstadoProceso.setText("Procesando con OCR...");

        // Procesar en hilo separado
        Thread ocrThread = new Thread(() -> {
            long inicioTiempo = System.currentTimeMillis();

            try {
                // ✅ CAMBIO: Crear el comprobante manualmente y procesarlo
                comprobanteActual = procesarComprobanteManual(rutaArchivo);

                javafx.application.Platform.runLater(() -> {
                    long tiempoTotal = System.currentTimeMillis() - inicioTiempo;
                    lblTiempoProcesamientoOCR.setText("Procesamiento: " + tiempoTotal + "ms");

                    if (comprobanteActual.getObservaciones() != null && comprobanteActual.getObservaciones().contains("ERROR")) {
                        // Registrar en bitácora - procesamiento fallido
                        Integer idUsuario = SessionManager.getInstance().getCurrentUserId();
                        if (idUsuario != null && idUsuario > 0) {
                            bitacoraDAO.registrarProcesamientoOCR(idUsuario,
                                new File(rutaArchivo).getName(), 0, "FALLIDO");
                        }

                        AlertUtils.showError("Error OCR", "No se pudo procesar el comprobante: " +
                                           comprobanteActual.getObservaciones());
                        resetearFormulario();
                    } else {
                        // Registrar en bitácora - procesamiento exitoso
                        Integer idUsuario = SessionManager.getInstance().getCurrentUserId();
                        if (idUsuario != null && idUsuario > 0) {
                            int productosDetectados = comprobanteActual.getMontoDetectado() != null ? 1 : 0;
                            bitacoraDAO.registrarProcesamientoOCR(idUsuario,
                                    new File(rutaArchivo).getName(), productosDetectados, "EXITOSO");
                        }

                        // ✅ ACTIVAR FLAG ANTES DE MOSTRAR DATOS
                        actualizandoDesdeDatos = true;
                        mostrarDatosExtraidos();
                        // ✅ DESACTIVAR FLAG DESPUÉS DE UN DELAY
                        javafx.application.Platform.runLater(() -> {
                            actualizandoDesdeDatos = false;
                            System.out.println("🔓 Flag de actualización desactivado - validación automática habilitada");
                        });

                        lblEstadoProceso.setText("OCR completado exitosamente");
                    }

                    btnProcesarOCR.setDisable(false);
                    progressOCR.setVisible(false);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    AlertUtils.showError("Error", "Error procesando OCR: " + e.getMessage());
                    resetearFormulario();
                    btnProcesarOCR.setDisable(false);
                    progressOCR.setVisible(false);
                });
            }
        });

        ocrThread.setDaemon(true);
        ocrThread.start();
    }

    // ✅ NUEVO MÉTODO: Procesar comprobante manualmente
    private ComprobanteOCR procesarComprobanteManual(String rutaArchivo) {
        try {
            // Crear comprobante vacío
            ComprobanteOCR comprobante = new ComprobanteOCR();
            comprobante.setImagenOriginal(rutaArchivo);

            // Procesar con OCR
            String textoExtraido = motorOCR.procesar(rutaArchivo);
            if (textoExtraido == null || textoExtraido.trim().isEmpty()) {
                comprobante.setObservaciones("ERROR: No se pudo extraer texto de la imagen");
                return comprobante;
            }

            // Detectar campos
            Map<String, Object> campos = motorOCR.detectarCampos(textoExtraido);

            // ✅ ARREGLO: Log para debug antes de asignar
            System.out.println("🔧 ASIGNANDO CAMPOS AL COMPROBANTE:");

            // Asignar campos detectados
            if (campos.containsKey("bancoEmisor")) {
                String banco = (String) campos.get("bancoEmisor");
                comprobante.setBancoEmisor(banco);
                System.out.println("   ✅ Banco asignado: " + banco);
            }

            if (campos.containsKey("montoDetectado")) {
                BigDecimal monto = (BigDecimal) campos.get("montoDetectado");
                comprobante.setMontoDetectado(monto);
                System.out.println("   ✅ Monto asignado: " + monto);
            }

            if (campos.containsKey("fechaTransferencia")) {
                Object fecha = campos.get("fechaTransferencia");
                if (fecha instanceof java.time.LocalDateTime) {
                    LocalDate fechaLocal = ((java.time.LocalDateTime) fecha).toLocalDate();
                    comprobante.setFechaTransferencia(fechaLocal);
                    System.out.println("   ✅ Fecha asignada: " + fechaLocal);
                }
            }

            if (campos.containsKey("referenciaOperacion")) {
                String referencia = (String) campos.get("referenciaOperacion");
                comprobante.setReferenciaOperacion(referencia);
                System.out.println("   ✅ Referencia asignada: " + referencia);
            }

            if (campos.containsKey("cuentaRemitente")) {
                String cuenta = (String) campos.get("cuentaRemitente");
                comprobante.setCuentaRemitente(cuenta);
                System.out.println("   ✅ Cuenta asignada: " + cuenta);
            }

            if (campos.containsKey("nombreBeneficiario")) {
                String beneficiario = (String) campos.get("nombreBeneficiario");
                comprobante.setNombreBeneficiario(beneficiario);
                System.out.println("   ✅ Beneficiario asignado: " + beneficiario);
            }

            // Guardar datos extraídos como JSON
            comprobante.setDatosExtraidos(convertirCamposAJson(campos));

            // ✅ VERIFICACIÓN FINAL
            System.out.println("🎯 COMPROBANTE FINAL:");
            System.out.println("   - Banco: " + comprobante.getBancoEmisor());
            System.out.println("   - Monto: " + comprobante.getMontoDetectado());
            System.out.println("   - Referencia: " + comprobante.getReferenciaOperacion());
            System.out.println("   - Fecha: " + comprobante.getFechaTransferencia());
            System.out.println("   - Cuenta: " + comprobante.getCuentaRemitente());
            System.out.println("   - Beneficiario: " + comprobante.getNombreBeneficiario());

            return comprobante;

        } catch (Exception e) {
            System.err.println("Error procesando comprobante: " + e.getMessage());
            e.printStackTrace();
            ComprobanteOCR comprobante = new ComprobanteOCR();
            comprobante.setImagenOriginal(rutaArchivo);
            comprobante.setObservaciones("ERROR: " + e.getMessage());
            return comprobante;
        }
    }

    private void mostrarDatosExtraidos() {
        if (comprobanteActual == null) {
            System.out.println("❌ comprobanteActual es null en mostrarDatosExtraidos()");
            return;
        }

        // ✅ ARREGLO: DESACTIVAR TEMPORALMENTE LOS LISTENERS PARA EVITAR SOBRESCRIBIR
        System.out.println("🔧 DESACTIVANDO LISTENERS TEMPORALMENTE...");

        // ✅ Log para debug
        System.out.println("🖥️ MOSTRANDO DATOS EN INTERFAZ:");
        System.out.println("   - Banco: " + comprobanteActual.getBancoEmisor());
        System.out.println("   - Monto: " + comprobanteActual.getMontoDetectado());
        System.out.println("   - Referencia: " + comprobanteActual.getReferenciaOperacion());
        System.out.println("   - Fecha: " + comprobanteActual.getFechaTransferencia());
        System.out.println("   - Cuenta: " + comprobanteActual.getCuentaRemitente());
        System.out.println("   - Beneficiario: " + comprobanteActual.getNombreBeneficiario());

        // ✅ BLOQUEAR TEMPORALMENTE LA VALIDACIÓN AUTOMÁTICA
        boolean bloqueadoTemp = true;

        // ✅ VERIFICAR QUE LOS CAMPOS NO SEAN NULL ANTES DE ASIGNAR
        if (comprobanteActual.getBancoEmisor() != null) {
            txtBancoEmisor.setText(comprobanteActual.getBancoEmisor());
            System.out.println("   ✅ Banco mostrado en interfaz: " + comprobanteActual.getBancoEmisor());
        } else {
            txtBancoEmisor.setText("");
            System.out.println("   ⚠️ Banco es null");
        }

        if (comprobanteActual.getReferenciaOperacion() != null) {
            txtReferencia.setText(comprobanteActual.getReferenciaOperacion());
            System.out.println("   ✅ Referencia mostrada en interfaz: " + comprobanteActual.getReferenciaOperacion());
        } else {
            txtReferencia.setText("");
            System.out.println("   ⚠️ Referencia es null");
        }

        if (comprobanteActual.getMontoDetectado() != null) {
            txtMonto.setText(comprobanteActual.getMontoDetectado().toString());
            System.out.println("   ✅ Monto mostrado en interfaz: " + comprobanteActual.getMontoDetectado());
        } else {
            txtMonto.setText("");
            System.out.println("   ⚠️ Monto es null");
        }

        if (comprobanteActual.getFechaTransferencia() != null) {
            dpFecha.setValue(comprobanteActual.getFechaTransferencia());
            System.out.println("   ✅ Fecha mostrada en interfaz: " + comprobanteActual.getFechaTransferencia());
        } else {
            dpFecha.setValue(null);
            System.out.println("   ⚠️ Fecha es null");
        }

        if (comprobanteActual.getCuentaRemitente() != null) {
            txtCuentaRemitente.setText(comprobanteActual.getCuentaRemitente());
            System.out.println("   ✅ Cuenta mostrada en interfaz: " + comprobanteActual.getCuentaRemitente());
        } else {
            txtCuentaRemitente.setText("");
            System.out.println("   ⚠️ Cuenta es null");
        }

        if (comprobanteActual.getNombreBeneficiario() != null) {
            txtBeneficiario.setText(comprobanteActual.getNombreBeneficiario());
            System.out.println("   ✅ Beneficiario mostrado en interfaz: " + comprobanteActual.getNombreBeneficiario());
        } else {
            txtBeneficiario.setText("");
            System.out.println("   ⚠️ Beneficiario es null");
        }

        // ✅ REACTIVAR LISTENERS Y VALIDAR DESPUÉS DE MOSTRAR DATOS
        bloqueadoTemp = false;
        System.out.println("🔧 REACTIVANDO LISTENERS...");

        // ✅ AHORA SÍ VALIDAR CON LOS DATOS YA MOSTRADOS
        javafx.application.Platform.runLater(() -> {
            System.out.println("⏰ EJECUTANDO VALIDACIÓN DESPUÉS DE MOSTRAR DATOS...");
            validarDatosAutomatico();
        });

        // NUEVA FUNCIONALIDAD: Búsqueda automática de ventas candidatas
        // Solo si no hay venta ya asociada (cuando se abre desde ventas, ya viene asociada)
        if (ventaAsociada == null) {
            buscarVentasCandidatas();
        }
    }

    // ✅ NUEVO MÉTODO: Convertir campos a JSON
    private String convertirCamposAJson(Map<String, Object> campos) {
        StringBuilder json = new StringBuilder("{");
        boolean primero = true;

        for (Map.Entry<String, Object> entry : campos.entrySet()) {
            if (!primero) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue() != null ? entry.getValue().toString() : "").append("\"");
            primero = false;
        }

        json.append("}");
        return json.toString();
    }

    private boolean actualizandoDesdeDatos = false; // Flag para evitar loops

    private void validarDatosAutomatico() {
        if (comprobanteActual == null) return;

        // ✅ EVITAR SOBRESCRIBIR DURANTE LA CARGA INICIAL
        if (actualizandoDesdeDatos) {
            System.out.println("⏸️ Validación pausada - actualizando desde datos extraídos");
            return;
        }

        System.out.println("🔍 INICIANDO VALIDACIÓN AUTOMÁTICA...");

        // Actualizar datos del comprobante con los valores del formulario
        actualizarComprobanteDesdeFormulario();

        // Obtener cuenta destino del sistema desde configuración
        String cuentaDestinoSistema = new com.pos.puntoventaocr.dao.ConfiguracionDAO().getCuentaDestinoSistema();
        String beneficiarioSistema = new com.pos.puntoventaocr.dao.ConfiguracionDAO().getNombreBeneficiarioSistema();

        // VALIDACIÓN ESPECÍFICA: Verificar que sea exactamente para esta venta
        if (ventaAsociada != null) {
            int puntajeCoincidencia = comprobanteActual.getPuntajeCoincidencia();
            boolean esEspecifico = comprobanteActual.esEspecificoParaVenta();

            // Mostrar información de coincidencia específica
            lblEstadoGeneral.setText(String.format("📊 Coincidencia con venta %s: %d%% %s",
                    ventaAsociada.getNumeroVenta(),
                    puntajeCoincidencia,
                    esEspecifico ? "(ESPECÍFICO)" : "(GENÉRICO)"));

            if (puntajeCoincidencia >= 80) {
                lblEstadoGeneral.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else if (puntajeCoincidencia >= 60) {
                lblEstadoGeneral.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            } else {
                lblEstadoGeneral.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        }

        // Validar monto CON MAYOR PRECISIÓN
        if (ventaAsociada != null && comprobanteActual.coincideMontoConVenta()) {
            lblValidacionMonto.setText("✓");
            lblValidacionMonto.setStyle("-fx-text-fill: green;");

            BigDecimal diferencia = comprobanteActual.getMontoDetectado().subtract(ventaAsociada.getTotal()).abs();
            if (diferencia.compareTo(BigDecimal.ZERO) == 0) {
                txtValidacionMonto.setText("✓ Monto EXACTO: $" + ventaAsociada.getTotal());
            } else {
                txtValidacionMonto.setText("✓ Monto coincide (dif: $" + diferencia + ")");
            }
        } else if (ventaAsociada != null) {
            lblValidacionMonto.setText("✗");
            lblValidacionMonto.setStyle("-fx-text-fill: red;");

            if (comprobanteActual.getMontoDetectado() != null) {
                BigDecimal diferencia = comprobanteActual.getMontoDetectado().subtract(ventaAsociada.getTotal()).abs();
                txtValidacionMonto.setText("✗ Monto NO coincide - Esperado: $" + ventaAsociada.getTotal() +
                                          ", Detectado: $" + comprobanteActual.getMontoDetectado() +
                                          " (dif: $" + diferencia + ")");
            } else {
                txtValidacionMonto.setText("✗ No se detectó monto en el comprobante");
            }
        } else {
            lblValidacionMonto.setText("⏳");
            lblValidacionMonto.setStyle("-fx-text-fill: orange;");
            txtValidacionMonto.setText("Esperando venta asociada");
        }

        // Validar fecha CON MAYOR PRECISIÓN
        if (ventaAsociada != null && comprobanteActual.esFechaValida()) {
            lblValidacionFecha.setText("✓");
            lblValidacionFecha.setStyle("-fx-text-fill: green;");

            LocalDate fechaVenta = ventaAsociada.getFecha().toLocalDate();
            LocalDate fechaTransf = comprobanteActual.getFechaTransferencia();

            // CORRECCIÓN: Verificar que fechaTransf no sea null antes de usar equals()
            if (fechaTransf != null && fechaTransf.equals(fechaVenta)) {
                txtValidacionFecha.setText("✓ Fecha EXACTA: " + fechaVenta);
            } else if (fechaTransf != null) {
                txtValidacionFecha.setText("✓ Fecha válida: " + fechaTransf + " (venta: " + fechaVenta + ")");
            } else {
                txtValidacionFecha.setText("✓ Fecha no detectada (considerada válida)");
            }
        } else if (ventaAsociada != null) {
            LocalDate fechaVenta = ventaAsociada.getFecha().toLocalDate();
            LocalDate fechaTransf = comprobanteActual.getFechaTransferencia();

            // Si no hay fecha detectada pero tenemos venta, considerarlo válido (fecha opcional)
            if (fechaTransf == null) {
                lblValidacionFecha.setText("⚠");
                lblValidacionFecha.setStyle("-fx-text-fill: orange;");
                txtValidacionFecha.setText("⚠ No se detectó fecha (considerada válida)");
            } else {
                lblValidacionFecha.setText("✗");
                lblValidacionFecha.setStyle("-fx-text-fill: red;");
                txtValidacionFecha.setText("✗ Fecha NO válida - Venta: " + fechaVenta +
                                          ", Transferencia: " + fechaTransf);
            }
        } else {
            lblValidacionFecha.setText("⏳");
            lblValidacionFecha.setStyle("-fx-text-fill: orange;");
            txtValidacionFecha.setText("Esperando venta asociada");
        }

        // Validar referencia
        boolean referenciaValida = comprobanteActual.getReferenciaOperacion() != null &&
                                 !comprobanteActual.getReferenciaOperacion().trim().isEmpty() &&
                                 !comprobanteDAO.existeReferencia(comprobanteActual.getReferenciaOperacion());

        if (referenciaValida) {
            lblValidacionReferencia.setText("✓");
            lblValidacionReferencia.setStyle("-fx-text-fill: green;");
            txtValidacionReferencia.setText("✓ Referencia única: " + comprobanteActual.getReferenciaOperacion());
        } else {
            lblValidacionReferencia.setText("✗");
            lblValidacionReferencia.setStyle("-fx-text-fill: red;");

            if (comprobanteActual.getReferenciaOperacion() == null || comprobanteActual.getReferenciaOperacion().trim().isEmpty()) {
                txtValidacionReferencia.setText("✗ No se detectó referencia");
            } else if (comprobanteDAO.existeReferencia(comprobanteActual.getReferenciaOperacion())) {
                txtValidacionReferencia.setText("✗ Referencia YA EXISTE: " + comprobanteActual.getReferenciaOperacion());
            } else {
                txtValidacionReferencia.setText("✗ Referencia inválida");
            }
        }

        // Validar cuenta destino (beneficiario) CON MAYOR PRECISIÓN - AHORA OPCIONAL
        boolean cuentaDestinoValida = true; // Por defecto válida (beneficiario opcional)
        if (comprobanteActual.getNombreBeneficiario() != null && !beneficiarioSistema.isEmpty()) {
            String beneficiarioDetectado = comprobanteActual.getNombreBeneficiario().toUpperCase().trim();
            String beneficiarioEsperado = beneficiarioSistema.toUpperCase().trim();

            // Validación más estricta del beneficiario
            double similitud = calcularSimilitudTexto(beneficiarioDetectado, beneficiarioEsperado);
            cuentaDestinoValida = similitud > 0.7; // Reducir umbral para ser más tolerante

            if (cuentaDestinoValida) {
                txtBeneficiario.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
                lblEstadoProceso.setText(String.format("✓ Beneficiario verificado (%.0f%% similitud): %s",
                    similitud * 100, beneficiarioDetectado));
            } else {
                txtBeneficiario.setStyle("-fx-border-color: orange; -fx-border-width: 2px;");
                lblEstadoProceso.setText(String.format("⚠ Beneficiario NO coincide perfectamente (%.0f%% similitud) - Esperado: %s, Detectado: %s",
                    similitud * 100, beneficiarioEsperado, beneficiarioDetectado));
                // No marcar como inválido, solo como advertencia
                cuentaDestinoValida = true; // BENEFICIARIO OPCIONAL
            }
        } else {
            txtBeneficiario.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px;");
            lblEstadoProceso.setText("ℹ️ Beneficiario no detectado o no configurado (OPCIONAL)");
        }

        // VALIDACIÓN FINAL: Solo permitir validación si ES ESPECÍFICO para esta venta
        boolean todasValidaciones = lblValidacionMonto.getText().equals("✓") &&
                                   lblValidacionFecha.getText().equals("✓") &&
                                   lblValidacionReferencia.getText().equals("✓") &&
                                   cuentaDestinoValida;

        // VERIFICACIÓN ADICIONAL: Debe ser específico para la venta actual
        boolean esEspecificoParaVenta = ventaAsociada != null && comprobanteActual.esEspecificoParaVenta();

        if (todasValidaciones && esEspecificoParaVenta) {
            lblEstadoGeneral.setText("✅ COMPROBANTE ESPECÍFICO - Listo para validar venta " + ventaAsociada.getNumeroVenta());
            lblEstadoGeneral.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-background-color: #e8f5e8;");
        } else if (todasValidaciones) {
            lblEstadoGeneral.setText("⚠️ Validaciones correctas pero comprobante NO específico para esta venta");
            lblEstadoGeneral.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        } else {
            lblEstadoGeneral.setText("❌ Faltan validaciones por completar");
            lblEstadoGeneral.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }

        // Solo habilitar validación si ES ESPECÍFICO para la venta
        btnValidar.setDisable(!(todasValidaciones && esEspecificoParaVenta));
        btnRechazar.setDisable(false);
    }

    // ✅ NUEVO MÉTODO: Actualización segura que no sobrescribe datos existentes
    private void actualizarComprobanteDesdeFormularioSeguro() {
        if (comprobanteActual == null) {
            comprobanteActual = new ComprobanteOCR();
            comprobanteActual.setImagenOriginal(txtRutaArchivo.getText());
        }

        System.out.println("🔄 ACTUALIZANDO COMPROBANTE DESDE FORMULARIO (MODO SEGURO):");

        // ✅ SOLO ACTUALIZAR SI EL CAMPO TIENE DATOS Y EL COMPROBANTE NO TIENE DATOS AÚN
        String bancoForm = txtBancoEmisor.getText().trim();
        if (!bancoForm.isEmpty() && (comprobanteActual.getBancoEmisor() == null || comprobanteActual.getBancoEmisor().isEmpty())) {
            comprobanteActual.setBancoEmisor(bancoForm);
            System.out.println("   🔄 Banco actualizado desde formulario: " + bancoForm);
        }

        String referenciaForm = txtReferencia.getText().trim();
        if (!referenciaForm.isEmpty() && (comprobanteActual.getReferenciaOperacion() == null || comprobanteActual.getReferenciaOperacion().isEmpty())) {
            comprobanteActual.setReferenciaOperacion(referenciaForm);
            System.out.println("   🔄 Referencia actualizada desde formulario: " + referenciaForm);
        }

        // Actualizar monto con validación mejorada
        try {
            String montoTexto = txtMonto.getText().trim();
            if (!montoTexto.isEmpty() && comprobanteActual.getMontoDetectado() == null) {
                // Limpiar el texto de monto (quitar $, comas, espacios)
                montoTexto = montoTexto.replaceAll("[^0-9.]", "");
                BigDecimal monto = new BigDecimal(montoTexto);
                comprobanteActual.setMontoDetectado(monto);
                System.out.println("   🔄 Monto actualizado desde formulario: " + monto);
            }
        } catch (NumberFormatException e) {
            System.err.println("Formato de monto inválido: " + txtMonto.getText());
        }

        // Actualizar fecha SOLO si no hay fecha en el comprobante
        if (dpFecha.getValue() != null && comprobanteActual.getFechaTransferencia() == null) {
            comprobanteActual.setFechaTransferencia(dpFecha.getValue());
            System.out.println("   🔄 Fecha actualizada desde formulario: " + dpFecha.getValue());
        }

        // Actualizar otros campos SOLO si están vacíos en el comprobante
        String cuentaForm = txtCuentaRemitente.getText().trim();
        if (!cuentaForm.isEmpty() && (comprobanteActual.getCuentaRemitente() == null || comprobanteActual.getCuentaRemitente().isEmpty())) {
            comprobanteActual.setCuentaRemitente(cuentaForm);
            System.out.println("   🔄 Cuenta actualizada desde formulario: " + cuentaForm);
        }

        String beneficiarioForm = txtBeneficiario.getText().trim();
        if (!beneficiarioForm.isEmpty() && (comprobanteActual.getNombreBeneficiario() == null || comprobanteActual.getNombreBeneficiario().isEmpty())) {
            comprobanteActual.setNombreBeneficiario(beneficiarioForm);
            System.out.println("   🔄 Beneficiario actualizado desde formulario: " + beneficiarioForm);
        }

        // Asociar venta si existe
        if (ventaAsociada != null) {
            comprobanteActual.setVenta(ventaAsociada);
        }

        // Log para debugging
        System.out.println("📋 ESTADO FINAL DEL COMPROBANTE:");
        System.out.println("   - Monto: " + comprobanteActual.getMontoDetectado());
        System.out.println("   - Referencia: " + comprobanteActual.getReferenciaOperacion());
        System.out.println("   - Fecha: " + comprobanteActual.getFechaTransferencia());
        System.out.println("   - Venta asociada: " + (ventaAsociada != null ? ventaAsociada.getNumeroVenta() : "ninguna"));
    }

    /**
     * Calcula la similitud entre dos textos usando el algoritmo de Levenshtein
     * Retorna un valor entre 0 y 1, donde 1 es idéntico
     */
    private double calcularSimilitudTexto(String texto1, String texto2) {
        if (texto1 == null || texto2 == null) return 0.0;
        if (texto1.equals(texto2)) return 1.0;

        int maxLength = Math.max(texto1.length(), texto2.length());
        if (maxLength == 0) return 1.0;

        return (maxLength - calcularDistanciaLevenshtein(texto1, texto2)) / (double) maxLength;
    }

    /**
     * Calcula la distancia de Levenshtein entre dos cadenas
     */
    private int calcularDistanciaLevenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1),
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    );
                }
            }
        }

        return dp[a.length()][b.length()];
    }

    /**
     * Busca automáticamente ventas candidatas basándose en el monto detectado
     */
    private void buscarVentasCandidatas() {
        if (comprobanteActual == null || comprobanteActual.getMontoDetectado() == null) {
            return;
        }

        try {
            // Buscar ventas recientes (últimos 7 días) que coincidan con el monto
            List<Venta> ventasRecientes = ventaDAO.obtenerPorRangoFechas(
                LocalDate.now().minusDays(7),
                LocalDate.now()
            );

            // Filtrar ventas que coincidan con el monto (con tolerancia de $1)
            BigDecimal montoDetectado = comprobanteActual.getMontoDetectado();
            BigDecimal tolerancia = new BigDecimal("1.00");

            List<Venta> ventasCandidatas = ventasRecientes.stream()
                .filter(venta -> {
                    BigDecimal diferencia = venta.getTotal().subtract(montoDetectado).abs();
                    return diferencia.compareTo(tolerancia) <= 0;
                })
                .limit(5) // Máximo 5 candidatas
                .toList();

            if (!ventasCandidatas.isEmpty()) {
                // Si hay una sola candidata que coincide exactamente, seleccionarla automáticamente
                if (ventasCandidatas.size() == 1 &&
                    ventasCandidatas.get(0).getTotal().compareTo(montoDetectado) == 0) {

                    ventaAsociada = ventasCandidatas.get(0);
                    txtBuscarVenta.setText(ventaAsociada.getNumeroVenta());
                    lblVentaEncontrada.setText("✓ Venta encontrada automáticamente: " +
                        ventaAsociada.getNumeroVenta() + " - $" + ventaAsociada.getTotal());
                    lblVentaEncontrada.setStyle("-fx-text-fill: green;");

                    comprobanteActual.setVenta(ventaAsociada);

                } else {
                    // Mostrar sugerencias si hay múltiples candidatas
                    StringBuilder sugerencias = new StringBuilder("💡 Ventas candidatas encontradas: ");
                    for (Venta venta : ventasCandidatas) {
                        sugerencias.append(venta.getNumeroVenta()).append(" ($").append(venta.getTotal()).append(") ");
                    }
                    lblVentaEncontrada.setText(sugerencias.toString());
                    lblVentaEncontrada.setStyle("-fx-text-fill: orange;");
                }
            } else {
                lblVentaEncontrada.setText("⚠ No se encontraron ventas con monto similar: $" + montoDetectado);
                lblVentaEncontrada.setStyle("-fx-text-fill: red;");
            }

        } catch (Exception e) {
            System.err.println("Error buscando ventas candidatas: " + e.getMessage());
            lblVentaEncontrada.setText("Error buscando ventas candidatas");
            lblVentaEncontrada.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void buscarVenta() {
        String numeroVenta = txtBuscarVenta.getText().trim();
        if (numeroVenta.isEmpty()) {
            AlertUtils.showWarning("Búsqueda", "Ingrese el número de venta");
            return;
        }

        try {
            // Buscar por número de venta en los últimos 30 días
            List<Venta> ventasRecientes = ventaDAO.obtenerPorRangoFechas(
                LocalDate.now().minusDays(30),
                LocalDate.now()
            );

            ventaAsociada = ventasRecientes.stream()
                .filter(v -> v.getNumeroVenta().equals(numeroVenta))
                .findFirst()
                .orElse(null);

            if (ventaAsociada != null) {
                lblVentaEncontrada.setText("Venta encontrada: $" + ventaAsociada.getTotal() +
                                         " - " + ventaAsociada.getFecha().toLocalDate());
                lblVentaEncontrada.setStyle("-fx-text-fill: green;");

                if (comprobanteActual != null) {
                    comprobanteActual.setVenta(ventaAsociada);
                    validarDatosAutomatico();
                }
            } else {
                lblVentaEncontrada.setText("Venta no encontrada");
                lblVentaEncontrada.setStyle("-fx-text-fill: red;");
                ventaAsociada = null;
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error buscando venta: " + e.getMessage());
        }
    }

    @FXML
    private void validarComprobante() {
        if (comprobanteActual == null || ventaAsociada == null) {
            AlertUtils.showWarning("Validación", "Complete el procesamiento OCR y asocie una venta");
            return;
        }

        Optional<ButtonType> resultado = AlertUtils.showConfirmation("Confirmar", "¿Está seguro de validar este comprobante?");
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                comprobanteActual.validar(sessionManager.getUsuarioActual(), txtObservaciones.getText());

                if (comprobanteDAO.guardar(comprobanteActual)) {
                    comprobanteValidado = true; // Marcar que el comprobante fue validado exitosamente

                    // Registrar en bitácora
                    Integer idUsuario = SessionManager.getInstance().getCurrentUserId();
                    if (idUsuario != null && idUsuario > 0) {
                        bitacoraDAO.registrarValidacionOCR(idUsuario,
                            new File(txtRutaArchivo.getText()).getName(), "VALIDADO");
                    }

                    AlertUtils.showInfo("Éxito", "Comprobante validado correctamente");
                    resetearFormulario();
                } else {
                    AlertUtils.showError("Error", "No se pudo guardar la validación");
                }
            } catch (Exception e) {
                AlertUtils.showError("Error", "Error validando comprobante: " + e.getMessage());
            }
        }
    }

    @FXML
    private void rechazarComprobante() {
        if (comprobanteActual == null) {
            AlertUtils.showWarning("Rechazo", "No hay comprobante para rechazar");
            return;
        }

        String motivo = txtObservaciones.getText().trim();
        if (motivo.isEmpty()) {
            AlertUtils.showWarning("Motivo", "Ingrese el motivo del rechazo en observaciones");
            return;
        }

        Optional<ButtonType> resultado = AlertUtils.showConfirmation("Confirmar", "¿Está seguro de rechazar este comprobante?");
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                comprobanteActual.rechazar(sessionManager.getUsuarioActual(), motivo);

                if (comprobanteDAO.guardar(comprobanteActual)) {
                    // Registrar en bitácora
                    Integer idUsuario = SessionManager.getInstance().getCurrentUserId();
                    if (idUsuario != null && idUsuario > 0) {
                        bitacoraDAO.registrarValidacionOCR(idUsuario,
                            new File(txtRutaArchivo.getText()).getName(), "RECHAZADO - " + motivo);
                    }

                    AlertUtils.showInfo("Comprobante Rechazado", "El comprobante ha sido rechazado");
                    resetearFormulario();
                } else {
                    AlertUtils.showError("Error", "No se pudo guardar el rechazo");
                }
            } catch (Exception e) {
                AlertUtils.showError("Error", "Error rechazando comprobante: " + e.getMessage());
            }
        }
    }

    @FXML
    private void corregirDatos() {
        // Permitir edición manual de todos los campos
        txtBancoEmisor.setEditable(true);
        txtReferencia.setEditable(true);
        txtMonto.setEditable(true);
        dpFecha.setEditable(true);
        txtCuentaRemitente.setEditable(true);
        txtBeneficiario.setEditable(true);

        AlertUtils.showInfo("Corrección", "Ahora puede editar manualmente los datos extraídos");
    }

    @FXML
    private void limpiarFormulario() {
        resetearFormulario();
    }

    private void resetearFormulario() {
        txtRutaArchivo.clear();
        txtBancoEmisor.clear();
        txtReferencia.clear();
        txtMonto.clear();
        dpFecha.setValue(null);
        txtCuentaRemitente.clear();
        txtBeneficiario.clear();
        txtBuscarVenta.clear();
        txtObservaciones.clear();

        lblVentaEncontrada.setText("");
        lblEstadoProceso.setText("");
        lblTiempoProcesamientoOCR.setText("");

        imgComprobante.setImage(null);

        lblValidacionMonto.setText("⏳");
        txtValidacionMonto.setText("Esperando procesamiento...");
        lblValidacionFecha.setText("⏳");
        txtValidacionFecha.setText("Esperando procesamiento...");
        lblValidacionReferencia.setText("⏳");
        txtValidacionReferencia.setText("Esperando procesamiento...");

        btnValidar.setDisable(true);
        btnRechazar.setDisable(true);
        btnProcesarOCR.setDisable(true);

        comprobanteActual = null;
        ventaAsociada = null;

        lblEstadoGeneral.setText("Listo para procesar comprobantes");
    }

    private void actualizarComprobanteDesdeFormulario() {
        // CORRECCIÓN: Crear comprobante si no existe (para edición manual)
        if (comprobanteActual == null) {
            comprobanteActual = new ComprobanteOCR();
            comprobanteActual.setImagenOriginal(txtRutaArchivo.getText());
        }

        // Actualizar todos los campos desde el formulario
        comprobanteActual.setBancoEmisor(txtBancoEmisor.getText().trim());
        comprobanteActual.setReferenciaOperacion(txtReferencia.getText().trim());

        // Actualizar monto con validación mejorada
        try {
            String montoTexto = txtMonto.getText().trim();
            if (!montoTexto.isEmpty()) {
                // Limpiar el texto de monto (quitar $, comas, espacios)
                montoTexto = montoTexto.replaceAll("[^0-9.]", "");
                BigDecimal monto = new BigDecimal(montoTexto);
                comprobanteActual.setMontoDetectado(monto);
            } else {
                comprobanteActual.setMontoDetectado(null);
            }
        } catch (NumberFormatException e) {
            // Si no es válido, mantener el valor anterior o null
            System.err.println("Formato de monto inválido: " + txtMonto.getText());
        }

        // Actualizar fecha
        comprobanteActual.setFechaTransferencia(dpFecha.getValue());

        // Actualizar otros campos
        comprobanteActual.setCuentaRemitente(txtCuentaRemitente.getText().trim());
        comprobanteActual.setNombreBeneficiario(txtBeneficiario.getText().trim());

        // Asociar venta si existe
        if (ventaAsociada != null) {
            comprobanteActual.setVenta(ventaAsociada);
        }

        // Log para debugging
        System.out.println("Comprobante actualizado desde formulario:");
        System.out.println("- Monto: " + comprobanteActual.getMontoDetectado());
        System.out.println("- Referencia: " + comprobanteActual.getReferenciaOperacion());
        System.out.println("- Fecha: " + comprobanteActual.getFechaTransferencia());
        System.out.println("- Venta asociada: " + (ventaAsociada != null ? ventaAsociada.getNumeroVenta() : "ninguna"));
    }

    public boolean isComprobanteValidado() {
        return comprobanteValidado;
    }

    /**
     * Inicializa el módulo OCR con una venta específica para validación
     */
    public void inicializarConVenta(Venta venta) {
        if (venta != null) {
            ventaAsociada = venta;
            txtBuscarVenta.setText(venta.getNumeroVenta());
            lblVentaEncontrada.setText("✓ Venta encontrada: " + venta.getNumeroVenta() +
                " - Total: $" + venta.getTotal());
            lblVentaEncontrada.setStyle("-fx-text-fill: green;");
        }
    }

    // Métodos de zoom para la vista previa de la imagen
    private double zoomFactor = 1.0;
    private final double ZOOM_INCREMENT = 0.25;
    private final double MIN_ZOOM = 0.25;
    private final double MAX_ZOOM = 3.0;

    @FXML
    private void aumentarZoom() {
        if (imgComprobante.getImage() != null && zoomFactor < MAX_ZOOM) {
            zoomFactor += ZOOM_INCREMENT;
            aplicarZoom();
        }
    }

    @FXML
    private void reducirZoom() {
        if (imgComprobante.getImage() != null && zoomFactor > MIN_ZOOM) {
            zoomFactor -= ZOOM_INCREMENT;
            aplicarZoom();
        }
    }

    @FXML
    private void resetearZoom() {
        if (imgComprobante.getImage() != null) {
            zoomFactor = 1.0;
            aplicarZoom();
        }
    }

    @FXML
    private void verImagenCompleta() {
        if (imgComprobante.getImage() == null) {
            AlertUtils.showWarning("Sin imagen", "No hay imagen cargada para mostrar");
            return;
        }

        try {
            // Crear una nueva ventana para mostrar la imagen completa
            javafx.stage.Stage imageStage = new javafx.stage.Stage();
            imageStage.setTitle("Imagen Completa - Comprobante");

            // Crear ImageView para la imagen completa
            ImageView fullImageView = new ImageView(imgComprobante.getImage());
            fullImageView.setPreserveRatio(true);
            fullImageView.setSmooth(true);

            // Crear ScrollPane para permitir scroll si la imagen es muy grande
            ScrollPane scrollPane = new ScrollPane(fullImageView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setPannable(true);

            // Ajustar el tamaño de la ventana según la imagen
            double imageWidth = imgComprobante.getImage().getWidth();
            double imageHeight = imgComprobante.getImage().getHeight();

            // Limitar el tamaño máximo de la ventana
            double maxWidth = 1200;
            double maxHeight = 800;

            double windowWidth = Math.min(imageWidth + 20, maxWidth);
            double windowHeight = Math.min(imageHeight + 60, maxHeight);

            javafx.scene.Scene scene = new javafx.scene.Scene(scrollPane, windowWidth, windowHeight);
            imageStage.setScene(scene);
            imageStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            imageStage.initOwner(btnVerCompleta.getScene().getWindow());

            // Permitir redimensionar
            imageStage.setResizable(true);

            imageStage.show();

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error mostrando imagen completa: " + e.getMessage());
        }
    }

    private void aplicarZoom() {
        if (imgComprobante.getImage() != null) {
            // Obtener dimensiones originales de la imagen
            double originalWidth = imgComprobante.getImage().getWidth();
            double originalHeight = imgComprobante.getImage().getHeight();

            // Aplicar zoom manteniendo la proporción
            imgComprobante.setFitWidth(originalWidth * zoomFactor);
            imgComprobante.setFitHeight(originalHeight * zoomFactor);

            // Actualizar estado del zoom en la interfaz
            lblEstadoProceso.setText(String.format("Zoom: %.0f%%", zoomFactor * 100));
        }
    }
}
