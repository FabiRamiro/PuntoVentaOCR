package com.pos.puntoventaocr.utils;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.pos.puntoventaocr.models.ComprobanteOCR;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFGenerator {

    public void generarReporteHistorialOCR(String rutaArchivo, ObservableList<ComprobanteOCR> comprobantes, 
                                         LocalDate fechaInicio, LocalDate fechaFin, String filtroEstado) 
            throws IOException {
        
        try (PdfWriter writer = new PdfWriter(rutaArchivo);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Título del reporte
            Paragraph titulo = new Paragraph("REPORTE HISTORIAL OCR")
                    .setFontSize(18)
                    .setBold();
            document.add(titulo);

            // Información del reporte
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fechaInicioStr = fechaInicio != null ? fechaInicio.format(formatter) : "N/A";
            String fechaFinStr = fechaFin != null ? fechaFin.format(formatter) : "N/A";
            
            Paragraph info = new Paragraph(String.format(
                    "Período: %s - %s | Estado: %s | Total registros: %d", 
                    fechaInicioStr, fechaFinStr, filtroEstado, comprobantes.size()))
                    .setFontSize(10)
                    .setMarginBottom(20);
            document.add(info);

            // Resumen estadístico
            agregarResumenEstadistico(document, comprobantes);

            // Tabla de comprobantes
            if (!comprobantes.isEmpty()) {
                agregarTablaComprobantes(document, comprobantes);
            } else {
                Paragraph sinDatos = new Paragraph("No se encontraron comprobantes con los filtros especificados.")
                        .setMarginTop(20);
                document.add(sinDatos);
            }

            // Pie de página
            Paragraph piePagina = new Paragraph(String.format(
                    "Reporte generado el %s", 
                    LocalDate.now().format(formatter)))
                    .setFontSize(8)
                    .setMarginTop(20);
            document.add(piePagina);
        }
    }

    private void agregarResumenEstadistico(Document document, List<ComprobanteOCR> comprobantes) {
        // Calcular estadísticas
        int total = comprobantes.size();
        long validados = comprobantes.stream()
                .mapToLong(c -> ComprobanteOCR.EstadoOCR.VALIDADO.equals(c.getEstadoValidacion()) ? 1 : 0)
                .sum();
        long rechazados = comprobantes.stream()
                .mapToLong(c -> ComprobanteOCR.EstadoOCR.RECHAZADO.equals(c.getEstadoValidacion()) ? 1 : 0)
                .sum();
        long pendientes = comprobantes.stream()
                .mapToLong(c -> ComprobanteOCR.EstadoOCR.PENDIENTE.equals(c.getEstadoValidacion()) ? 1 : 0)
                .sum();
        long errores = comprobantes.stream()
                .mapToLong(c -> ComprobanteOCR.EstadoOCR.ERROR_PROCESAMIENTO.equals(c.getEstadoValidacion()) ? 1 : 0)
                .sum();

        BigDecimal montoTotal = comprobantes.stream()
                .filter(c -> c.getMontoDetectado() != null)
                .map(ComprobanteOCR::getMontoDetectado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Crear tabla de resumen
        Table resumenTable = new Table(6)
                .setMarginBottom(20);

        // Encabezados
        resumenTable.addHeaderCell(createCell("Total", true));
        resumenTable.addHeaderCell(createCell("Validados", true));
        resumenTable.addHeaderCell(createCell("Rechazados", true));
        resumenTable.addHeaderCell(createCell("Pendientes", true));
        resumenTable.addHeaderCell(createCell("Errores", true));
        resumenTable.addHeaderCell(createCell("Monto Total", true));

        // Datos
        resumenTable.addCell(createCell(String.valueOf(total), false));
        resumenTable.addCell(createCell(String.valueOf(validados), false));
        resumenTable.addCell(createCell(String.valueOf(rechazados), false));
        resumenTable.addCell(createCell(String.valueOf(pendientes), false));
        resumenTable.addCell(createCell(String.valueOf(errores), false));
        resumenTable.addCell(createCell("$" + montoTotal.toString(), false));

        document.add(new Paragraph("RESUMEN ESTADÍSTICO").setBold().setMarginBottom(10));
        document.add(resumenTable);
    }

    private void agregarTablaComprobantes(Document document, List<ComprobanteOCR> comprobantes) {
        // Crear tabla con 7 columnas
        Table table = new Table(7);

        // Encabezados
        table.addHeaderCell(createCell("ID", true));
        table.addHeaderCell(createCell("Fecha", true));
        table.addHeaderCell(createCell("Banco", true));
        table.addHeaderCell(createCell("Monto", true));
        table.addHeaderCell(createCell("Referencia", true));
        table.addHeaderCell(createCell("Estado", true));
        table.addHeaderCell(createCell("Observaciones", true));

        // Datos
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (ComprobanteOCR comprobante : comprobantes) {
            table.addCell(createCell(String.valueOf(comprobante.getIdComprobante()), false));
            
            String fecha = comprobante.getFechaProcesamiento() != null ? 
                    comprobante.getFechaProcesamiento().format(formatter) : "N/A";
            table.addCell(createCell(fecha, false));
            
            table.addCell(createCell(comprobante.getBancoEmisor() != null ? 
                    comprobante.getBancoEmisor() : "N/A", false));
            
            String monto = comprobante.getMontoDetectado() != null ? 
                    "$" + comprobante.getMontoDetectado().toString() : "N/A";
            table.addCell(createCell(monto, false));
            
            table.addCell(createCell(comprobante.getReferenciaOperacion() != null ? 
                    comprobante.getReferenciaOperacion() : "N/A", false));
            
            table.addCell(createCell(comprobante.getEstadoValidacion() != null ? 
                    comprobante.getEstadoValidacion().toString() : "N/A", false));
            
            String observaciones = comprobante.getObservaciones() != null ? 
                    comprobante.getObservaciones() : "";
            if (observaciones.length() > 50) {
                observaciones = observaciones.substring(0, 47) + "...";
            }
            table.addCell(createCell(observaciones, false));
        }

        document.add(new Paragraph("DETALLE DE COMPROBANTES").setBold().setMarginBottom(10).setMarginTop(20));
        document.add(table);
    }

    private Cell createCell(String content, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(content).setFontSize(isHeader ? 10 : 9));
        
        if (isHeader) {
            cell.setBold();
        }
        
        return cell;
    }
}