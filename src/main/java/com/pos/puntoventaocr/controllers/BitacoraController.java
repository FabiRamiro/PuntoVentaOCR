package com.pos.puntoventaocr.controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.pos.puntoventaocr.dao.BitacoraDAO;
import com.pos.puntoventaocr.models.Bitacora;
import com.pos.puntoventaocr.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BitacoraController {

    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private ComboBox<String> cmbAccion;
    @FXML private ComboBox<String> cmbModulo;
    @FXML private javafx.scene.control.TextField txtUsuario;
    @FXML private Button btnFiltrar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnExportar;

    @FXML private TableView<Bitacora> tablaBitacora;
    @FXML private TableColumn<Bitacora, Integer> colId;
    @FXML private TableColumn<Bitacora, String> colFecha;
    @FXML private TableColumn<Bitacora, String> colUsuario;
    @FXML private TableColumn<Bitacora, String> colAccion;
    @FXML private TableColumn<Bitacora, String> colModulo;
    @FXML private TableColumn<Bitacora, String> colIP;

    @FXML private TextArea txtDetalles;
    @FXML private Label lblTotalRegistros;
    @FXML private Label lblEstado;

    private BitacoraDAO bitacoraDAO;
    private ObservableList<Bitacora> listaBitacora;

    public void initialize() {
        bitacoraDAO = new BitacoraDAO();
        listaBitacora = FXCollections.observableArrayList();

        configurarTabla();
        configurarFiltros();
        cargarDatos();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idBitacora"));
        colFecha.setCellValueFactory(cellData -> {
            LocalDateTime fecha = cellData.getValue().getFechaHora();
            return new javafx.beans.property.SimpleStringProperty(
                fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            );
        });
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colAccion.setCellValueFactory(new PropertyValueFactory<>("accion"));
        colModulo.setCellValueFactory(new PropertyValueFactory<>("modulo"));
        colIP.setCellValueFactory(new PropertyValueFactory<>("direccionIp"));

        tablaBitacora.setItems(listaBitacora);

        // Listener para mostrar detalles
        tablaBitacora.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    mostrarDetalles(newSelection);
                }
            });
    }

    private void configurarFiltros() {
        // Configurar fechas por defecto (último mes)
        dpFechaFin.setValue(LocalDate.now());
        dpFechaInicio.setValue(LocalDate.now().minusMonths(1));

        // Configurar opciones de filtros
        cmbAccion.getItems().addAll(
            "Todas", "LOGIN", "LOGOUT", "CREATE", "UPDATE", "DELETE",
            "BACKUP", "RESTORE", "EXPORT", "IMPORT"
        );
        cmbAccion.setValue("Todas");

        cmbModulo.getItems().addAll(
            "Todos", "USUARIOS", "PRODUCTOS", "VENTAS", "INVENTARIO",
            "REPORTES", "SISTEMA", "CONFIGURACION"
        );
        cmbModulo.setValue("Todos");
    }

    @FXML
    private void filtrarRegistros() {
        try {
            lblEstado.setText("Filtrando...");

            LocalDate fechaInicio = dpFechaInicio.getValue();
            LocalDate fechaFin = dpFechaFin.getValue();
            String accion = "Todas".equals(cmbAccion.getValue()) ? null : cmbAccion.getValue();
            String modulo = "Todos".equals(cmbModulo.getValue()) ? null : cmbModulo.getValue();
            String usuario = txtUsuario.getText().trim().isEmpty() ? null : txtUsuario.getText().trim();

            List<Bitacora> registros = bitacoraDAO.obtenerConFiltros(
                fechaInicio, fechaFin, accion, modulo, usuario
            );

            listaBitacora.clear();
            listaBitacora.addAll(registros);

            lblTotalRegistros.setText("Total registros: " + registros.size());
            lblEstado.setText("Filtrado completado");

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al filtrar registros: " + e.getMessage());
            lblEstado.setText("Error en filtrado");
        }
    }

    @FXML
    private void limpiarFiltros() {
        dpFechaInicio.setValue(LocalDate.now().minusMonths(1));
        dpFechaFin.setValue(LocalDate.now());
        cmbAccion.setValue("Todas");
        cmbModulo.setValue("Todos");
        txtUsuario.clear();
        txtDetalles.clear();
        cargarDatos();
    }

    @FXML
    private void exportarRegistros() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Guardar Reporte como");
            fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"),
                new javafx.stage.FileChooser.ExtensionFilter("Todos los archivos", "*.*")
            );

            File archivo = fileChooser.showSaveDialog(btnExportar.getScene().getWindow());
            if (archivo != null) {
                // Crear documento PDF
                Document document = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(document, new FileOutputStream(archivo));
                document.open();

                // Título del reporte
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                Paragraph title = new Paragraph("REPORTE DE BITÁCORA DE ACCIONES", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph(" "));

                // Información de filtros
                Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                document.add(new Paragraph("Fecha de generación: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), infoFont));
                document.add(new Paragraph("Filtros aplicados:", infoFont));
                document.add(new Paragraph("  • Fecha inicio: " + (dpFechaInicio.getValue() != null ? dpFechaInicio.getValue() : "No aplicado"), infoFont));
                document.add(new Paragraph("  • Fecha fin: " + (dpFechaFin.getValue() != null ? dpFechaFin.getValue() : "No aplicado"), infoFont));
                document.add(new Paragraph("  • Acción: " + cmbAccion.getValue(), infoFont));
                document.add(new Paragraph("  • Módulo: " + cmbModulo.getValue(), infoFont));
                document.add(new Paragraph("  • Usuario: " + (txtUsuario.getText().trim().isEmpty() ? "Todos" : txtUsuario.getText().trim()), infoFont));
                document.add(new Paragraph("  • Total de registros: " + listaBitacora.size(), infoFont));
                document.add(new Paragraph(" "));

                // Crear tabla con registros
                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1f, 3f, 2f, 2f, 2f, 2f});

                // Encabezados de la tabla
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
                PdfPCell headerCell;

                headerCell = new PdfPCell(new Phrase("ID", headerFont));
                headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                headerCell.setPadding(5);
                table.addCell(headerCell);

                headerCell = new PdfPCell(new Phrase("Fecha/Hora", headerFont));
                headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                headerCell.setPadding(5);
                table.addCell(headerCell);

                headerCell = new PdfPCell(new Phrase("Usuario", headerFont));
                headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                headerCell.setPadding(5);
                table.addCell(headerCell);

                headerCell = new PdfPCell(new Phrase("Acción", headerFont));
                headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                headerCell.setPadding(5);
                table.addCell(headerCell);

                headerCell = new PdfPCell(new Phrase("Módulo", headerFont));
                headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                headerCell.setPadding(5);
                table.addCell(headerCell);

                headerCell = new PdfPCell(new Phrase("Dirección IP", headerFont));
                headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                headerCell.setPadding(5);
                table.addCell(headerCell);

                // Datos de la tabla
                Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
                for (Bitacora b : listaBitacora) {
                    table.addCell(new Phrase(String.valueOf(b.getIdBitacora()), dataFont));
                    table.addCell(new Phrase(b.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), dataFont));
                    table.addCell(new Phrase(b.getNombreUsuario() != null ? b.getNombreUsuario() : "N/A", dataFont));
                    table.addCell(new Phrase(b.getAccion(), dataFont));
                    table.addCell(new Phrase(b.getModulo(), dataFont));
                    table.addCell(new Phrase(b.getDireccionIp() != null ? b.getDireccionIp() : "N/A", dataFont));
                }

                document.add(table);

                // Pie de página
                document.add(new Paragraph(" "));
                Paragraph footer = new Paragraph("Sistema POS OCR - Reporte generado automáticamente", infoFont);
                footer.setAlignment(Element.ALIGN_CENTER);
                document.add(footer);

                document.close();

                AlertUtils.showInfo("Éxito", "Reporte exportado exitosamente a:\n" + archivo.getAbsolutePath());
                lblEstado.setText("Reporte exportado exitosamente");

            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al exportar reporte: " + e.getMessage());
            lblEstado.setText("Error al exportar");
            e.printStackTrace();
        }
    }

    private void cargarDatos() {
        try {
            lblEstado.setText("Cargando...");

            List<Bitacora> registros = bitacoraDAO.obtenerTodos();
            listaBitacora.clear();
            listaBitacora.addAll(registros);

            lblTotalRegistros.setText("Total registros: " + registros.size());
            lblEstado.setText("Listo");

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al cargar registros de bitácora: " + e.getMessage());
            lblEstado.setText("Error al cargar");
        }
    }

    private void mostrarDetalles(Bitacora bitacora) {
        StringBuilder detalles = new StringBuilder();
        detalles.append("ID: ").append(bitacora.getIdBitacora()).append("\n");
        detalles.append("Fecha/Hora: ").append(
            bitacora.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        ).append("\n");
        detalles.append("Usuario: ").append(bitacora.getNombreUsuario()).append("\n");
        detalles.append("Acción: ").append(bitacora.getAccion()).append("\n");
        detalles.append("Módulo: ").append(bitacora.getModulo()).append("\n");
        detalles.append("IP: ").append(bitacora.getDireccionIp()).append("\n");
        detalles.append("Detalles: ").append(
            bitacora.getDetalles() != null ? bitacora.getDetalles() : "Sin detalles"
        );

        txtDetalles.setText(detalles.toString());
    }
}
