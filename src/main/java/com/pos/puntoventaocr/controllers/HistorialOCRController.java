package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.ComprobanteOCRDAO;
import com.pos.puntoventaocr.models.ComprobanteOCR;
import com.pos.puntoventaocr.models.ComprobanteOCR.EstadoOCR;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class HistorialOCRController implements Initializable {

    @FXML private TableView<ComprobanteOCR> tableHistorial;
    @FXML private TableColumn<ComprobanteOCR, Integer> colId;
    @FXML private TableColumn<ComprobanteOCR, String> colFecha;
    @FXML private TableColumn<ComprobanteOCR, String> colBanco;
    @FXML private TableColumn<ComprobanteOCR, BigDecimal> colMonto;
    @FXML private TableColumn<ComprobanteOCR, String> colReferencia;
    @FXML private TableColumn<ComprobanteOCR, String> colEstado;
    @FXML private TableColumn<ComprobanteOCR, String> colProcesadoPor;

    @FXML private DatePicker dateFechaInicio;
    @FXML private DatePicker dateFechaFin;
    @FXML private ComboBox<EstadoOCR> cmbEstado;
    @FXML private TextField txtBuscarReferencia;

    @FXML private Button btnBuscar;
    @FXML private Button btnExportarCSV;
    @FXML private Button btnExportarPDF;
    @FXML private Button btnVerDetalles;
    @FXML private Button btnVerImagen;
    @FXML private Button btnEliminar;

    @FXML private Label lblTotalRegistros;
    @FXML private Label lblValidados;
    @FXML private Label lblPendientes;
    @FXML private Label lblRechazados;

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
        configurarEventos();
        cargarHistorial();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idComprobante"));
        colFecha.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFechaProcesamiento()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        colBanco.setCellValueFactory(new PropertyValueFactory<>("bancoEmisor"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("montoDetectado"));
        colReferencia.setCellValueFactory(new PropertyValueFactory<>("referenciaOperacion"));
        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEstadoValidacion().name()));
        colProcesadoPor.setCellValueFactory(cellData -> {
            ComprobanteOCR comp = cellData.getValue();
            String procesadoPor = comp.getUsuarioValidador() != null 
                ? comp.getUsuarioValidador().getNombreCompleto() 
                : "Sistema";
            return new SimpleStringProperty(procesadoPor);
        });

        // Personalizar celdas de estado con colores
        colEstado.setCellFactory(column -> new TableCell<ComprobanteOCR, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "PENDIENTE":
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                            break;
                        case "VALIDADO":
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                            break;
                        case "RECHAZADO":
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        tableHistorial.setItems(listaComprobantes);
        tableHistorial.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    comprobanteSeleccionado = newSelection;
                    configurarBotones();
                });
    }

    private void configurarFiltros() {
        // Configurar ComboBox de estados
        cmbEstado.setItems(FXCollections.observableArrayList(EstadoOCR.values()));
        cmbEstado.getItems().add(0, null); // Opción "Todos"

        // Configurar fechas por defecto (último mes)
        dateFechaFin.setValue(LocalDate.now());
        dateFechaInicio.setValue(LocalDate.now().minusMonths(1));
    }

    private void configurarEventos() {
        // Configurar búsqueda automática al cambiar filtros
        dateFechaInicio.setOnAction(e -> aplicarFiltros());
        dateFechaFin.setOnAction(e -> aplicarFiltros());
        cmbEstado.setOnAction(e -> aplicarFiltros());
        
        // Búsqueda por referencia con enter
        txtBuscarReferencia.setOnAction(e -> aplicarFiltros());
    }

    private void configurarBotones() {
        boolean seleccionado = comprobanteSeleccionado != null;
        boolean tienePermiso = sessionManager.tienePermiso("administrar_ocr");

        btnVerDetalles.setDisable(!seleccionado);
        btnVerImagen.setDisable(!seleccionado);
        btnEliminar.setDisable(!seleccionado || !tienePermiso);
    }

    @FXML
    private void handleBuscar(ActionEvent event) {
        aplicarFiltros();
    }

    @FXML
    private void handleExportarCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar historial OCR");
        fileChooser.setInitialFileName("historial_ocr_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));

        File archivo = fileChooser.showSaveDialog(btnExportarCSV.getScene().getWindow());
        if (archivo != null) {
            exportarCSV(archivo);
        }
    }

    @FXML
    private void handleExportarPDF(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar historial OCR a PDF");
        fileChooser.setInitialFileName("historial_ocr_" + LocalDate.now() + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));

        File archivo = fileChooser.showSaveDialog(btnExportarPDF.getScene().getWindow());
        if (archivo != null) {
            exportarPDF(archivo);
        }
    }

    @FXML
    private void handleVerDetalles(ActionEvent event) {
        if (comprobanteSeleccionado != null) {
            mostrarDetallesComprobante(comprobanteSeleccionado);
        }
    }

    @FXML
    private void handleVerImagen(ActionEvent event) {
        if (comprobanteSeleccionado != null && comprobanteSeleccionado.getImagenOriginal() != null) {
            // Abrir ventana para mostrar la imagen
            // Implementación de visor de imágenes
            AlertUtils.mostrarInformacion("Ver Imagen", 
                "Imagen: " + comprobanteSeleccionado.getImagenOriginal());
        } else {
            AlertUtils.mostrarAdvertencia("Sin imagen", "Este comprobante no tiene imagen asociada");
        }
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        if (comprobanteSeleccionado != null) {
            if (AlertUtils.mostrarConfirmacion("Confirmar eliminación",
                    "¿Está seguro que desea eliminar este comprobante?\n" +
                            "Esta acción no se puede deshacer.")) {

                try {
                    if (comprobanteDAO.eliminar(comprobanteSeleccionado.getIdComprobante())) {
                        AlertUtils.mostrarExito("Éxito", "Comprobante eliminado correctamente");
                        sessionManager.registrarActividad("Comprobante OCR eliminado: ID " + 
                                comprobanteSeleccionado.getIdComprobante());
                        cargarHistorial();
                    } else {
                        AlertUtils.mostrarError("Error", "No se pudo eliminar el comprobante");
                    }
                } catch (Exception e) {
                    AlertUtils.mostrarError("Error", "Error al eliminar: " + e.getMessage());
                }
            }
        }
    }

    private void aplicarFiltros() {
        try {
            List<ComprobanteOCR> comprobantes;

            // Aplicar filtros
            LocalDate fechaInicio = dateFechaInicio.getValue();
            LocalDate fechaFin = dateFechaFin.getValue();
            EstadoOCR estadoFiltro = cmbEstado.getValue();
            String referenciaFiltro = txtBuscarReferencia.getText().trim();

            if (!referenciaFiltro.isEmpty()) {
                // Búsqueda específica por referencia
                ComprobanteOCR comprobante = comprobanteDAO.buscarPorReferencia(referenciaFiltro);
                comprobantes = comprobante != null ? List.of(comprobante) : List.of();
            } else {
                // Búsqueda general con filtros
                comprobantes = comprobanteDAO.buscarConFiltros(fechaInicio, fechaFin, estadoFiltro);
            }

            listaComprobantes.setAll(comprobantes);
            actualizarEstadisticas(comprobantes);

        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "Error al aplicar filtros: " + e.getMessage());
        }
    }

    private void cargarHistorial() {
        try {
            List<ComprobanteOCR> comprobantes = comprobanteDAO.listarTodos();
            listaComprobantes.setAll(comprobantes);
            actualizarEstadisticas(comprobantes);
        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "Error al cargar historial: " + e.getMessage());
        }
    }

    private void actualizarEstadisticas(List<ComprobanteOCR> comprobantes) {
        int total = comprobantes.size();
        long validados = comprobantes.stream().mapToLong(c -> 
            c.getEstadoValidacion() == EstadoOCR.VALIDADO ? 1 : 0).sum();
        long pendientes = comprobantes.stream().mapToLong(c -> 
            c.getEstadoValidacion() == EstadoOCR.PENDIENTE ? 1 : 0).sum();
        long rechazados = comprobantes.stream().mapToLong(c -> 
            c.getEstadoValidacion() == EstadoOCR.RECHAZADO ? 1 : 0).sum();

        lblTotalRegistros.setText(String.valueOf(total));
        lblValidados.setText(String.valueOf(validados));
        lblPendientes.setText(String.valueOf(pendientes));
        lblRechazados.setText(String.valueOf(rechazados));
    }

    private void exportarCSV(File archivo) {
        try {
            // Implementar exportación CSV
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Fecha,Banco,Monto,Referencia,Estado,Procesado Por\n");

            for (ComprobanteOCR comp : listaComprobantes) {
                csv.append(comp.getIdComprobante()).append(",");
                csv.append(comp.getFechaProcesamiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append(",");
                csv.append(comp.getBancoEmisor() != null ? comp.getBancoEmisor() : "").append(",");
                csv.append(comp.getMontoDetectado() != null ? comp.getMontoDetectado() : "").append(",");
                csv.append(comp.getReferenciaOperacion() != null ? comp.getReferenciaOperacion() : "").append(",");
                csv.append(comp.getEstadoValidacion().name()).append(",");
                csv.append(comp.getUsuarioValidador() != null ? comp.getUsuarioValidador().getNombreCompleto() : "Sistema");
                csv.append("\n");
            }

            java.nio.file.Files.write(archivo.toPath(), csv.toString().getBytes());
            AlertUtils.mostrarExito("Exportación exitosa", "Archivo CSV generado: " + archivo.getAbsolutePath());

        } catch (Exception e) {
            AlertUtils.mostrarError("Error al exportar", "No se pudo generar el archivo CSV: " + e.getMessage());
        }
    }

    private void exportarPDF(File archivo) {
        try {
            PDFGenerator pdfGenerator = new PDFGenerator();
            
            // Crear reporte de historial OCR
            pdfGenerator.generarReporteHistorialOCR(
                archivo.getAbsolutePath(),
                listaComprobantes,
                dateFechaInicio.getValue(),
                dateFechaFin.getValue(),
                cmbEstado.getValue()
            );
            
            AlertUtils.mostrarExito("Exportación Exitosa", 
                          "Reporte PDF generado: " + archivo.getAbsolutePath());
                          
        } catch (Exception e) {
            AlertUtils.mostrarError("Error al generar PDF", "Error al generar PDF: " + e.getMessage());
        }
    }

    private void mostrarDetallesComprobante(ComprobanteOCR comprobante) {
        StringBuilder detalles = new StringBuilder();
        detalles.append("=== DETALLES DEL COMPROBANTE ===\n\n");
        detalles.append("ID: ").append(comprobante.getIdComprobante()).append("\n");
        detalles.append("Fecha de procesamiento: ").append(
            comprobante.getFechaProcesamiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        ).append("\n");
        detalles.append("Banco emisor: ").append(comprobante.getBancoEmisor() != null ? comprobante.getBancoEmisor() : "N/A").append("\n");
        detalles.append("Monto: $").append(comprobante.getMontoDetectado() != null ? comprobante.getMontoDetectado() : "N/A").append("\n");
        detalles.append("Referencia: ").append(comprobante.getReferenciaOperacion() != null ? comprobante.getReferenciaOperacion() : "N/A").append("\n");
        detalles.append("Estado: ").append(comprobante.getEstadoValidacion().name()).append("\n");
        
        if (comprobante.getUsuarioValidador() != null) {
            detalles.append("Procesado por: ").append(comprobante.getUsuarioValidador().getNombreCompleto()).append("\n");
        }
        
        if (comprobante.getObservaciones() != null && !comprobante.getObservaciones().trim().isEmpty()) {
            detalles.append("Observaciones: ").append(comprobante.getObservaciones()).append("\n");
        }

        AlertUtils.mostrarInformacion("Detalles del Comprobante", detalles.toString());
    }
}