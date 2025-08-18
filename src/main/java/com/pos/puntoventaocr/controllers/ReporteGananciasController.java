package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.VentaDAO;
import com.pos.puntoventaocr.models.Venta;
import com.pos.puntoventaocr.models.ResumenGanancia;
import com.pos.puntoventaocr.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReporteGananciasController {

    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private Button btnGenerar;
    @FXML private Button btnExportar;

    // Resumen de ganancias
    @FXML private Label lblTotalVentas;
    @FXML private Label lblIngresosBrutos;
    @FXML private Label lblCostoMercancia;
    @FXML private Label lblGananciaNeta;
    @FXML private Label lblMargenGanancia;
    @FXML private Label lblGananciaDiaria;

    @FXML private TableView<ResumenGanancia> tablaGanancias;
    @FXML private TableColumn<ResumenGanancia, String> colFecha;
    @FXML private TableColumn<ResumenGanancia, Integer> colVentas;
    @FXML private TableColumn<ResumenGanancia, Double> colIngresos;
    @FXML private TableColumn<ResumenGanancia, Double> colCostos;
    @FXML private TableColumn<ResumenGanancia, Double> colGanancias;
    @FXML private TableColumn<ResumenGanancia, Double> colMargen;

    private VentaDAO ventaDAO;
    private ObservableList<ResumenGanancia> listaGanancias;

    public void initialize() {
        ventaDAO = new VentaDAO();
        listaGanancias = FXCollections.observableArrayList();

        configurarTabla();
        configurarFiltros();
        limpiarResumen();
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colVentas.setCellValueFactory(new PropertyValueFactory<>("ventas"));
        colIngresos.setCellValueFactory(new PropertyValueFactory<>("ingresos"));
        colCostos.setCellValueFactory(new PropertyValueFactory<>("costos"));
        colGanancias.setCellValueFactory(new PropertyValueFactory<>("ganancias"));
        colMargen.setCellValueFactory(new PropertyValueFactory<>("margen"));

        // Formatear columnas de dinero
        colIngresos.setCellFactory(column -> new TableCell<ResumenGanancia, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + String.format("%.2f", item));
                }
            }
        });

        colCostos.setCellFactory(column -> new TableCell<ResumenGanancia, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + String.format("%.2f", item));
                }
            }
        });

        colGanancias.setCellFactory(column -> new TableCell<ResumenGanancia, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + String.format("%.2f", item));
                    if (item < 0) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        colMargen.setCellFactory(column -> new TableCell<ResumenGanancia, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", item));
                    if (item < 20) {
                        setStyle("-fx-text-fill: red;");
                    } else if (item > 40) {
                        setStyle("-fx-text-fill: green;");
                    } else {
                        setStyle("-fx-text-fill: orange;");
                    }
                }
            }
        });

        tablaGanancias.setItems(listaGanancias);
    }

    private void configurarFiltros() {
        // Configurar fechas por defecto (mes actual)
        LocalDate ahora = LocalDate.now();
        dpFechaDesde.setValue(ahora.withDayOfMonth(1));
        dpFechaHasta.setValue(ahora);

        // Configurar métodos de pago
        cmbMetodoPago.getItems().addAll("Todos", "EFECTIVO", "TARJETA", "TRANSFERENCIA");
        cmbMetodoPago.setValue("Todos");
    }

    @FXML
    private void generarReporte() {
        try {
            LocalDate fechaDesde = dpFechaDesde.getValue();
            LocalDate fechaHasta = dpFechaHasta.getValue();
            String metodoPago = cmbMetodoPago.getValue();

            if (fechaDesde == null || fechaHasta == null) {
                AlertUtils.showWarning("Advertencia", "Seleccione el rango de fechas");
                return;
            }

            if (fechaDesde.isAfter(fechaHasta)) {
                AlertUtils.showWarning("Advertencia", "La fecha desde no puede ser mayor a la fecha hasta");
                return;
            }

            // Obtener ventas del período
            List<Venta> ventas = ventaDAO.obtenerPorRangoFechas(fechaDesde, fechaHasta);

            // Filtrar por método de pago si es necesario
            if (!"Todos".equals(metodoPago)) {
                ventas = ventas.stream()
                    .filter(v -> metodoPago.equals(v.getMetodoPago()))
                    .collect(java.util.stream.Collectors.toList());
            }

            // Calcular resumen de ganancias
            calcularResumenGanancias(ventas, fechaDesde, fechaHasta);
            actualizarResumenGeneral();

            AlertUtils.showInfo("Reporte Generado", "Reporte de ganancias generado exitosamente");

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al generar reporte: " + e.getMessage());
        }
    }

    @FXML
    private void exportarPDF() {
        try {
            if (listaGanancias.isEmpty()) {
                AlertUtils.showWarning("Advertencia", "Genere el reporte antes de exportar");
                return;
            }

            // Aquí iría la lógica de exportación a PDF
            AlertUtils.showInfo("Exportación", "Funcionalidad de exportación a PDF en desarrollo");

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al exportar: " + e.getMessage());
        }
    }

    @FXML
    private void exportarReporte() {
        try {
            if (listaGanancias.isEmpty()) {
                AlertUtils.showWarning("Advertencia", "Genere el reporte antes de exportar");
                return;
            }

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Guardar Reporte de Ganancias");
            fileChooser.setInitialFileName("reporte_ganancias_" +
                dpFechaDesde.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "_" +
                dpFechaHasta.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
            );

            java.io.File archivo = fileChooser.showSaveDialog(btnExportar.getScene().getWindow());
            if (archivo != null) {
                exportarAPDF(archivo);
                AlertUtils.showInfo("Exportación", "Reporte exportado exitosamente a: " + archivo.getName());
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al exportar: " + e.getMessage());
        }
    }

    private void exportarAPDF(java.io.File archivo) throws Exception {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
        com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(archivo));

        document.open();

        // Título
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("REPORTE DE GANANCIAS", titleFont);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(title);

        // Período
        com.itextpdf.text.Font dateFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12);
        com.itextpdf.text.Paragraph period = new com.itextpdf.text.Paragraph("Período: " +
            dpFechaDesde.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " +
            dpFechaHasta.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), dateFont);
        period.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(period);
        document.add(new com.itextpdf.text.Paragraph(" "));

        // Resumen general
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
        document.add(new com.itextpdf.text.Paragraph("RESUMEN GENERAL", headerFont));
        document.add(new com.itextpdf.text.Paragraph("Total de ventas: " + lblTotalVentas.getText(), dateFont));
        document.add(new com.itextpdf.text.Paragraph("Ingresos brutos: " + lblIngresosBrutos.getText(), dateFont));
        document.add(new com.itextpdf.text.Paragraph("Costo de mercancía: " + lblCostoMercancia.getText(), dateFont));
        document.add(new com.itextpdf.text.Paragraph("Ganancia neta: " + lblGananciaNeta.getText(), dateFont));
        document.add(new com.itextpdf.text.Paragraph("Margen de ganancia: " + lblMargenGanancia.getText(), dateFont));
        document.add(new com.itextpdf.text.Paragraph("Ganancia diaria promedio: " + lblGananciaDiaria.getText(), dateFont));
        document.add(new com.itextpdf.text.Paragraph(" "));

        // Tabla de ganancias diarias
        document.add(new com.itextpdf.text.Paragraph("DETALLE DIARIO", headerFont));
        com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{15, 15, 20, 20, 20, 10});

        // Encabezados
        String[] headers = {"Fecha", "Ventas", "Ingresos", "Costos", "Ganancias", "Margen"};
        for (String header : headers) {
            com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, headerFont));
            cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        // Datos
        com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9);
        for (ResumenGanancia item : listaGanancias) {
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getFecha(), dataFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.valueOf(item.getVentas()), dataFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.format("$%.2f", item.getIngresos()), dataFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.format("$%.2f", item.getCostos()), dataFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.format("$%.2f", item.getGanancias()), dataFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.format("%.1f%%", item.getMargen()), dataFont)));
        }

        document.add(table);
        document.close();
    }

    private void calcularResumenGanancias(List<Venta> ventas, LocalDate fechaDesde, LocalDate fechaHasta) {
        listaGanancias.clear();

        // Agrupar ventas por fecha
        java.util.Map<LocalDate, List<Venta>> ventasPorFecha = ventas.stream()
            .collect(java.util.stream.Collectors.groupingBy(v -> v.getFecha().toLocalDate()));

        // Crear resumen para cada día en el rango
        LocalDate fechaActual = fechaDesde;
        while (!fechaActual.isAfter(fechaHasta)) {
            List<Venta> ventasDelDia = ventasPorFecha.getOrDefault(fechaActual, java.util.Collections.emptyList());

            int cantidadVentas = ventasDelDia.size();
            double ingresosBrutos = ventasDelDia.stream()
                .mapToDouble(v -> v.getTotal().doubleValue())
                .sum();

            // Calcular costos aproximados (70% del precio de venta como estimación)
            double costoMercancia = ingresosBrutos * 0.7;

            if (cantidadVentas > 0 || ingresosBrutos > 0) {
                ResumenGanancia resumen = new ResumenGanancia(fechaActual, cantidadVentas, ingresosBrutos, costoMercancia);
                listaGanancias.add(resumen);
            }

            fechaActual = fechaActual.plusDays(1);
        }
    }

    private void actualizarResumenGeneral() {
        int totalVentas = listaGanancias.stream()
            .mapToInt(ResumenGanancia::getVentas)
            .sum();

        double totalIngresos = listaGanancias.stream()
            .mapToDouble(ResumenGanancia::getIngresos)
            .sum();

        double totalCostos = listaGanancias.stream()
            .mapToDouble(ResumenGanancia::getCostos)
            .sum();

        double gananciaTotal = totalIngresos - totalCostos;
        double margenPromedio = totalIngresos > 0 ? (gananciaTotal / totalIngresos) * 100 : 0;

        long diasEnPeriodo = ChronoUnit.DAYS.between(dpFechaDesde.getValue(), dpFechaHasta.getValue()) + 1;
        double gananciaDiaria = diasEnPeriodo > 0 ? gananciaTotal / diasEnPeriodo : 0;

        // Actualizar labels
        lblTotalVentas.setText(String.valueOf(totalVentas));
        lblIngresosBrutos.setText(String.format("$%.2f", totalIngresos));
        lblCostoMercancia.setText(String.format("$%.2f", totalCostos));
        lblGananciaNeta.setText(String.format("$%.2f", gananciaTotal));
        lblMargenGanancia.setText(String.format("%.1f%%", margenPromedio));
        lblGananciaDiaria.setText(String.format("$%.2f", gananciaDiaria));

        // Aplicar colores según performance
        if (gananciaTotal > 0) {
            lblGananciaNeta.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            lblGananciaNeta.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }

        if (margenPromedio > 30) {
            lblMargenGanancia.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else if (margenPromedio > 15) {
            lblMargenGanancia.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        } else {
            lblMargenGanancia.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    private void limpiarResumen() {
        lblTotalVentas.setText("0");
        lblIngresosBrutos.setText("$0.00");
        lblCostoMercancia.setText("$0.00");
        lblGananciaNeta.setText("$0.00");
        lblMargenGanancia.setText("0.0%");
        lblGananciaDiaria.setText("$0.00");

        listaGanancias.clear();
    }
}
