package com.pos.puntoventaocr;

import com.pos.puntoventaocr.controllers.HistorialOCRController;
import com.pos.puntoventaocr.controllers.ValidarOCRController;
import com.pos.puntoventaocr.dao.ComprobanteOCRDAO;
import com.pos.puntoventaocr.models.ComprobanteOCR;
import com.pos.puntoventaocr.utils.PDFGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test para validar que las mejoras del sistema OCR funcionan correctamente
 */
public class OCRImprovementsTest {

    @Test
    public void testComprobanteOCRDAOMethodsExist() {
        // Verificar que los métodos necesarios existen en ComprobanteOCRDAO
        ComprobanteOCRDAO dao = new ComprobanteOCRDAO();
        
        // Estos métodos deben existir y no lanzar excepciones por sintaxis
        assertNotNull(dao, "ComprobanteOCRDAO debe poder instanciarse");
        
        // Verificar que los métodos agregados existen
        try {
            List<ComprobanteOCR> result = dao.buscarConFiltros(null, null, null);
            assertNotNull(result, "buscarConFiltros debe retornar una lista no nula");
        } catch (Exception e) {
            // Es esperado que falle por falta de BD, pero no por sintaxis
            assertTrue(e.getMessage().contains("Connection") || e.getMessage().contains("database"), 
                "Error debe ser de conexión a BD, no de sintaxis");
        }
    }

    @Test
    public void testPDFGeneratorExists() {
        // Verificar que PDFGenerator se puede instanciar
        PDFGenerator pdfGenerator = new PDFGenerator();
        assertNotNull(pdfGenerator, "PDFGenerator debe poder instanciarse");
    }

    @Test
    public void testPDFGeneratorCanGenerateReport() {
        PDFGenerator pdfGenerator = new PDFGenerator();
        List<ComprobanteOCR> comprobantes = new ArrayList<>();
        
        // Test con lista vacía
        try {
            pdfGenerator.generarReporteHistorialOCR(
                "/tmp/test_report.pdf",
                comprobantes,
                null,
                null,
                null
            );
            // Si llega aquí, la estructura del método es correcta
            assertTrue(true, "El método de generación PDF funciona sin errores de sintaxis");
        } catch (Exception e) {
            // Verificar que no es un error de sintaxis sino de archivo/permisos
            assertTrue(e.getMessage().contains("Permission") || 
                      e.getMessage().contains("No such file") ||
                      e.getMessage().contains("FileNotFoundException") ||
                      e.getMessage().contains("DocumentException"),
                "Error debe ser de archivo/permisos, no de sintaxis: " + e.getMessage());
        }
    }

    @Test
    public void testControllersCanBeInstantiated() {
        // Verificar que los controladores se pueden instanciar
        // Nota: No llamamos initialize() porque requiere contexto JavaFX
        
        HistorialOCRController historialController = new HistorialOCRController();
        assertNotNull(historialController, "HistorialOCRController debe poder instanciarse");

        ValidarOCRController validarController = new ValidarOCRController();
        assertNotNull(validarController, "ValidarOCRController debe poder instanciarse");
    }

    @Test
    public void testComprobanteOCRHasCorrectEnums() {
        // Verificar que los enums necesarios existen
        ComprobanteOCR.EstadoOCR[] estados = ComprobanteOCR.EstadoOCR.values();
        
        assertTrue(estados.length >= 3, "Debe haber al menos 3 estados OCR");
        
        boolean hasValidado = false, hasPendiente = false, hasRechazado = false;
        
        for (ComprobanteOCR.EstadoOCR estado : estados) {
            if (estado.name().equals("VALIDADO")) hasValidado = true;
            if (estado.name().equals("PENDIENTE")) hasPendiente = true;
            if (estado.name().equals("RECHAZADO")) hasRechazado = true;
        }
        
        assertTrue(hasValidado, "Debe existir estado VALIDADO");
        assertTrue(hasPendiente, "Debe existir estado PENDIENTE");
        assertTrue(hasRechazado, "Debe existir estado RECHAZADO");
    }
}