package com.pos.puntoventaocr.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.pos.puntoventaocr.models.ComprobanteOCR;
import com.pos.puntoventaocr.models.ComprobanteOCR.EstadoOCR;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFGenerator {

    public void generarReporteHistorialOCR(String rutaArchivo, 
                                           List<ComprobanteOCR> comprobantes,
                                           LocalDate fechaInicio, 
                                           LocalDate fechaFin,
                                           EstadoOCR estadoFiltro) throws FileNotFoundException {
        
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo));
            document.open();

            // Título
            agregarTitulo(document, "HISTORIAL DE PROCESAMIENTO OCR");

            // Período
            String periodo = (fechaInicio != null ? fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Inicio") +
                    " - " + (fechaFin != null ? fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Fin");
            agregarParrafo(document, "Período: " + periodo);
            
            if (estadoFiltro != null) {
                agregarParrafo(document, "Estado filtrado: " + estadoFiltro.name());
            }
            
            agregarParrafo(document, "Generado el: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            document.add(new Paragraph("\n"));

            // Resumen estadístico
            long totalComprobantes = comprobantes.size();
            long validados = comprobantes.stream().mapToLong(c -> c.getEstadoValidacion() == EstadoOCR.VALIDADO ? 1 : 0).sum();
            long pendientes = comprobantes.stream().mapToLong(c -> c.getEstadoValidacion() == EstadoOCR.PENDIENTE ? 1 : 0).sum();
            long rechazados = comprobantes.stream().mapToLong(c -> c.getEstadoValidacion() == EstadoOCR.RECHAZADO ? 1 : 0).sum();
            
            BigDecimal montoTotal = comprobantes.stream()
                    .filter(c -> c.getEstadoValidacion() == EstadoOCR.VALIDADO)
                    .map(c -> c.getMontoDetectado() != null ? c.getMontoDetectado() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            agregarSeccion(document, "RESUMEN ESTADÍSTICO");
            PdfPTable resumenTable = new PdfPTable(5);
            resumenTable.setWidthPercentage(100);
            
            resumenTable.addCell(crearCeldaEncabezado("Total"));
            resumenTable.addCell(crearCeldaEncabezado("Validados"));
            resumenTable.addCell(crearCeldaEncabezado("Pendientes"));
            resumenTable.addCell(crearCeldaEncabezado("Rechazados"));
            resumenTable.addCell(crearCeldaEncabezado("Monto Total"));

            resumenTable.addCell(String.valueOf(totalComprobantes));
            resumenTable.addCell(String.valueOf(validados));
            resumenTable.addCell(String.valueOf(pendientes));
            resumenTable.addCell(String.valueOf(rechazados));
            resumenTable.addCell(String.format("$%.2f", montoTotal));

            document.add(resumenTable);

            // Detalle de comprobantes
            agregarSeccion(document, "DETALLE DE COMPROBANTES");
            PdfPTable comprobantesTable = new PdfPTable(7);
            comprobantesTable.setWidthPercentage(100);
            float[] columnWidths = {1f, 2f, 1.5f, 1.5f, 2f, 1.5f, 1.5f};
            comprobantesTable.setWidths(columnWidths);

            comprobantesTable.addCell(crearCeldaEncabezado("ID"));
            comprobantesTable.addCell(crearCeldaEncabezado("Fecha Proceso"));
            comprobantesTable.addCell(crearCeldaEncabezado("Banco"));
            comprobantesTable.addCell(crearCeldaEncabezado("Monto"));
            comprobantesTable.addCell(crearCeldaEncabezado("Referencia"));
            comprobantesTable.addCell(crearCeldaEncabezado("Estado"));
            comprobantesTable.addCell(crearCeldaEncabezado("Procesado Por"));

            for (ComprobanteOCR comp : comprobantes) {
                comprobantesTable.addCell(String.valueOf(comp.getIdComprobante()));
                comprobantesTable.addCell(formatearFechaHora(comp.getFechaProcesamiento()));
                comprobantesTable.addCell(comp.getBancoEmisor() != null ? comp.getBancoEmisor() : "-");
                comprobantesTable.addCell(comp.getMontoDetectado() != null ? "$" + comp.getMontoDetectado() : "-");
                comprobantesTable.addCell(comp.getReferenciaOperacion() != null ? comp.getReferenciaOperacion() : "-");
                comprobantesTable.addCell(comp.getEstadoValidacion().name());
                comprobantesTable.addCell(comp.getUsuarioValidador() != null ? comp.getUsuarioValidador().getNombreCompleto() : "Sistema");
            }

            document.add(comprobantesTable);

            // Pie de página
            document.add(new Paragraph("\n\n"));
            agregarParrafo(document, "Sistema de Punto de Venta con OCR v1.0.0");
            agregarParrafo(document, "Reporte generado automáticamente");

        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar PDF", e);
        } finally {
            document.close();
        }
    }

    private void agregarTitulo(Document document, String titulo) throws DocumentException {
        Font font = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph p = new Paragraph(titulo, font);
        p.setAlignment(Element.ALIGN_CENTER);
        document.add(p);
        document.add(new Paragraph("\n"));
    }

    private void agregarSeccion(Document document, String seccion) throws DocumentException {
        document.add(new Paragraph("\n"));
        Font font = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Paragraph p = new Paragraph(seccion, font);
        document.add(p);
        document.add(new Paragraph("\n"));
    }

    private void agregarParrafo(Document document, String texto) throws DocumentException {
        Font font = new Font(Font.FontFamily.HELVETICA, 10);
        Paragraph p = new Paragraph(texto, font);
        document.add(p);
    }

    private PdfPCell crearCeldaEncabezado(String texto) {
        Font font = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        return cell;
    }

    private String formatearFechaHora(java.time.LocalDateTime fecha) {
        if (fecha == null) return "-";
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}