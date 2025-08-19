package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.ComprobanteOCRDAO;
import com.pos.puntoventaocr.models.ComprobanteOCR;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.PDFGenerator;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class HistorialOCRController implements Initializable {

    @FXML private DatePicker dateFechaInicio;
    @FXML private DatePicker dateFechaFin;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ComboBox<String> cmbBanco;
    @FXML private TextField txtBuscarReferencia;
    
    @FXML private TableView<ComprobanteOCR> tableComprobantes;
    @FXML private TableColumn<ComprobanteOCR, Integer> colId;
    @FXML private TableColumn<ComprobanteOCR, String> colFecha;
    @FXML private TableColumn<ComprobanteOCR, String> colBanco;
    @FXML private TableColumn<ComprobanteOCR, BigDecimal> colMonto;
    @FXML private TableColumn<ComprobanteOCR, String> colReferencia;
    @FXML private TableColumn<ComprobanteOCR, String> colEstado;
    @FXML private TableColumn<ComprobanteOCR, String> colObservaciones;
    
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnExportarPDF;
    @FXML private Button btnVerDetalle;
    @FXML private Button btnEliminar;
    
    @FXML private Label lblTotalComprobantes;
    @FXML private Label lblTotalValidados;
    @FXML private Label lblTotalRechazados;
    @FXML private Label lblTotalPendientes;

    private ComprobanteOCRDAO comprobanteDAO;
    private SessionManager sessionManager;
    private ObservableList<ComprobanteOCR> listaComprobantes;
    private ComprobanteOCR comprobanteSeleccionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comprobanteDAO = new ComprobanteOCRDAO();
        sessionManager = SessionManager.getInstance();
        listaComprobantes = FXCollections.observableArrayList();
        
        configurarTabla();
        configurarFiltros();
        cargarDatos();
        actualizarEstadisticas();
    }

    private void configurarTabla() {
        // Configurar columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("idComprobante"));
        colFecha.setCellValueFactory(cellData -> {
            LocalDateTime fecha = cellData.getValue().getFechaProcesamiento();
            if (fecha != null) {
                return new SimpleStringProperty(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
            return new SimpleStringProperty("");
        });
        colBanco.setCellValueFactory(new PropertyValueFactory<>("bancoEmisor"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("montoDetectado"));
        colReferencia.setCellValueFactory(new PropertyValueFactory<>("referenciaOperacion"));
        colEstado.setCellValueFactory(cellData -> {
            ComprobanteOCR.EstadoOCR estado = cellData.getValue().getEstadoValidacion();
            return new SimpleStringProperty(estado != null ? estado.toString() : "");
        });
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));

        // Configurar selección
        tableComprobantes.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    comprobanteSeleccionado = newSelection;
                    btnVerDetalle.setDisable(newSelection == null);
                    btnEliminar.setDisable(newSelection == null);
                });

        tableComprobantes.setItems(listaComprobantes);
    }

    private void configurarFiltros() {
        // Configurar ComboBox de estados
        cmbEstado.setItems(FXCollections.observableArrayList(
                "TODOS", "VALIDADO", "PENDIENTE", "RECHAZADO", "ERROR_PROCESAMIENTO"
        ));
        cmbEstado.setValue("TODOS");

        // Configurar ComboBox de bancos
        cmbBanco.setItems(FXCollections.observableArrayList(
                "TODOS", "BBVA", "BANAMEX", "SANTANDER", "HSBC", "BANORTE", "SCOTIABANK", "INBURSA", "NU"
        ));
        cmbBanco.setValue("TODOS");

        // Configurar fechas por defecto (último mes)
        dateFechaFin.setValue(LocalDate.now());
        dateFechaInicio.setValue(LocalDate.now().minusMonths(1));
    }

    private void cargarDatos() {
        try {
            List<ComprobanteOCR> comprobantes;
            
            // Aplicar filtros
            LocalDate fechaInicio = dateFechaInicio.getValue();
            LocalDate fechaFin = dateFechaFin.getValue();
            String estado = cmbEstado.getValue();
            String banco = cmbBanco.getValue();
            String referencia = txtBuscarReferencia.getText();

            if (referencia != null && !referencia.trim().isEmpty()) {
                // Buscar por referencia específica
                ComprobanteOCR comprobante = comprobanteDAO.buscarPorReferencia(referencia.trim());
                comprobantes = comprobante != null ? List.of(comprobante) : List.of();
            } else {
                // Buscar con filtros de fecha, estado y banco
                comprobantes = comprobanteDAO.buscarPorFiltros(
                        fechaInicio != null ? fechaInicio.atStartOfDay() : null,
                        fechaFin != null ? fechaFin.atTime(23, 59, 59) : null,
                        "TODOS".equals(estado) ? null : estado,
                        "TODOS".equals(banco) ? null : banco
                );
            }

            listaComprobantes.clear();
            listaComprobantes.addAll(comprobantes);
            
            actualizarEstadisticas();
            sessionManager.registrarActividad("Consulta historial OCR: " + comprobantes.size() + " registros");
            
        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "No se pudieron cargar los comprobantes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void actualizarEstadisticas() {
        int total = listaComprobantes.size();
        long validados = listaComprobantes.stream()
                .mapToLong(c -> ComprobanteOCR.EstadoOCR.VALIDADO.equals(c.getEstadoValidacion()) ? 1 : 0)
                .sum();
        long rechazados = listaComprobantes.stream()
                .mapToLong(c -> ComprobanteOCR.EstadoOCR.RECHAZADO.equals(c.getEstadoValidacion()) ? 1 : 0)
                .sum();
        long pendientes = listaComprobantes.stream()
                .mapToLong(c -> ComprobanteOCR.EstadoOCR.PENDIENTE.equals(c.getEstadoValidacion()) ? 1 : 0)
                .sum();

        lblTotalComprobantes.setText(String.valueOf(total));
        lblTotalValidados.setText(String.valueOf(validados));
        lblTotalRechazados.setText(String.valueOf(rechazados));
        lblTotalPendientes.setText(String.valueOf(pendientes));
    }

    @FXML
    private void handleBuscar(ActionEvent event) {
        cargarDatos();
    }

    @FXML
    private void handleLimpiar(ActionEvent event) {
        dateFechaInicio.setValue(LocalDate.now().minusMonths(1));
        dateFechaFin.setValue(LocalDate.now());
        cmbEstado.setValue("TODOS");
        cmbBanco.setValue("TODOS");
        txtBuscarReferencia.clear();
        cargarDatos();
    }

    @FXML
    private void handleExportarPDF(ActionEvent event) {
        if (listaComprobantes.isEmpty()) {
            AlertUtils.mostrarAdvertencia("Sin datos", "No hay comprobantes para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Historial OCR a PDF");
        fileChooser.setInitialFileName("historial_ocr_" + LocalDate.now().toString() + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
        );

        File archivo = fileChooser.showSaveDialog(btnExportarPDF.getScene().getWindow());
        if (archivo != null) {
            exportarPDF(archivo);
        }
    }

    private void exportarPDF(File archivo) {
        try {
            // Asegurar que el archivo tenga extensión .pdf
            String rutaCompleta = archivo.getAbsolutePath();
            if (!rutaCompleta.toLowerCase().endsWith(".pdf")) {
                rutaCompleta += ".pdf";
            }

            // Generar PDF
            PDFGenerator pdfGenerator = new PDFGenerator();
            pdfGenerator.generarReporteHistorialOCR(
                    rutaCompleta,
                    listaComprobantes,
                    dateFechaInicio.getValue(),
                    dateFechaFin.getValue(),
                    cmbEstado.getValue()
            );

            AlertUtils.mostrarInformacion("Éxito", 
                    "El reporte PDF se ha generado correctamente en:\n" + rutaCompleta);
                    
            sessionManager.registrarActividad("Exportación PDF historial OCR: " + rutaCompleta);

        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "No se pudo generar el PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVerDetalle(ActionEvent event) {
        if (comprobanteSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Selección requerida", 
                    "Por favor seleccione un comprobante para ver los detalles.");
            return;
        }

        // Mostrar detalles del comprobante en un diálogo
        mostrarDetalleComprobante(comprobanteSeleccionado);
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        if (comprobanteSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Selección requerida", 
                    "Por favor seleccione un comprobante para eliminar.");
            return;
        }

        if (!sessionManager.tienePermiso("ELIMINAR_COMPROBANTES")) {
            AlertUtils.mostrarOperacionNoPermitida("ELIMINAR_COMPROBANTES");
            return;
        }

        if (AlertUtils.mostrarConfirmacion("Confirmar eliminación",
                "¿Está seguro que desea eliminar el comprobante seleccionado?\n" +
                "Esta acción no se puede deshacer.")) {
            
            try {
                boolean eliminado = comprobanteDAO.eliminar(comprobanteSeleccionado.getIdComprobante());
                if (eliminado) {
                    listaComprobantes.remove(comprobanteSeleccionado);
                    actualizarEstadisticas();
                    AlertUtils.mostrarInformacion("Éxito", "El comprobante se ha eliminado correctamente.");
                    sessionManager.registrarActividad("Eliminación comprobante OCR: " + 
                            comprobanteSeleccionado.getIdComprobante());
                } else {
                    AlertUtils.mostrarError("Error", "No se pudo eliminar el comprobante.");
                }
            } catch (Exception e) {
                AlertUtils.mostrarError("Error", "Error al eliminar: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void mostrarDetalleComprobante(ComprobanteOCR comprobante) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle del Comprobante OCR");
        alert.setHeaderText("Comprobante ID: " + comprobante.getIdComprobante());
        
        StringBuilder detalles = new StringBuilder();
        detalles.append("Banco Emisor: ").append(comprobante.getBancoEmisor() != null ? comprobante.getBancoEmisor() : "N/A").append("\n");
        detalles.append("Monto Detectado: $").append(comprobante.getMontoDetectado() != null ? comprobante.getMontoDetectado() : "N/A").append("\n");
        detalles.append("Referencia: ").append(comprobante.getReferenciaOperacion() != null ? comprobante.getReferenciaOperacion() : "N/A").append("\n");
        detalles.append("Cuenta Remitente: ").append(comprobante.getCuentaRemitente() != null ? comprobante.getCuentaRemitente() : "N/A").append("\n");
        detalles.append("Beneficiario: ").append(comprobante.getNombreBeneficiario() != null ? comprobante.getNombreBeneficiario() : "N/A").append("\n");
        
        if (comprobante.getFechaTransferencia() != null) {
            detalles.append("Fecha Transferencia: ").append(
                    comprobante.getFechaTransferencia().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ).append("\n");
        }
        
        if (comprobante.getFechaProcesamiento() != null) {
            detalles.append("Fecha Procesamiento: ").append(
                    comprobante.getFechaProcesamiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ).append("\n");
        }
        
        detalles.append("Estado: ").append(comprobante.getEstadoValidacion()).append("\n");
        
        if (comprobante.getObservaciones() != null && !comprobante.getObservaciones().trim().isEmpty()) {
            detalles.append("Observaciones: ").append(comprobante.getObservaciones()).append("\n");
        }
        
        if (comprobante.getDatosExtraidos() != null && !comprobante.getDatosExtraidos().trim().isEmpty()) {
            detalles.append("\nDatos Extraídos (JSON):\n").append(comprobante.getDatosExtraidos());
        }

        alert.setContentText(detalles.toString());
        alert.getDialogPane().setPrefWidth(600);
        alert.showAndWait();
    }
}