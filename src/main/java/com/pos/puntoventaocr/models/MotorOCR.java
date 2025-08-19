package com.pos.puntoventaocr.models;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class MotorOCR {
    private static MotorOCR instance;
    private Map<String, String> configuracion;
    private String idioma;
    private int precision;
    private List<String> formatosPermitidos;

    // Patrones mejorados para extraer datos
    private static final Pattern PATRON_MONTO = Pattern.compile("(?:MONTO|TOTAL|IMPORTE|CANTIDAD)[\\s:]*\\$?([0-9,]+\\.?[0-9]*)");
    private static final Pattern PATRON_MONTO_SIMPLE = Pattern.compile("\\$([0-9,]+\\.?[0-9]*)");
    private static final Pattern PATRON_FECHA = Pattern.compile("(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})");
    private static final Pattern PATRON_REFERENCIA = Pattern.compile("(?:REF|REFERENCIA|FOLIO)[\\s:]*([A-Z0-9]{6,20})");
    private static final Pattern PATRON_REFERENCIA_SIMPLE = Pattern.compile("([A-Z0-9]{8,20})");
    private static final Pattern PATRON_BANCO = Pattern.compile("(BBVA|BANAMEX|SANTANDER|HSBC|BANORTE|SCOTIABANK|INBURSA|NU)");
    
    // Patrones específicos para BBVA
    private static final Pattern PATRON_BBVA_FOLIO = Pattern.compile("FOLIO[\\s:]*([0-9]{6,12})");
    private static final Pattern PATRON_BBVA_MONTO = Pattern.compile("TRANSFERENCIA[\\s\\S]*?\\$([0-9,]+\\.?[0-9]*)");
    
    // Patrones específicos para Nu México
    private static final Pattern PATRON_NU_REFERENCIA = Pattern.compile("(?:Clave|ID)[\\s:]*([A-Z0-9]{6,15})");
    private static final Pattern PATRON_NU_MONTO = Pattern.compile("(?:Enviaste|Pagaste)[\\s\\S]*?\\$([0-9,]+\\.?[0-9]*)");
    private static final Pattern PATRON_NU_BENEFICIARIO = Pattern.compile("(?:Para|A)[\\s:]*([A-ZÁÉÍÓÚ\\s]{3,50})");

    // Constructor privado para Singleton
    private MotorOCR() {
        inicializar();
    }

    // Método para obtener la instancia única
    public static synchronized MotorOCR getInstance() {
        if (instance == null) {
            instance = new MotorOCR();
        }
        return instance;
    }

    public void inicializar() {
        this.configuracion = new HashMap<>();
        this.idioma = "spa"; // Español
        this.precision = 80; // Precisión del 80%
        this.formatosPermitidos = Arrays.asList("JPG", "JPEG", "PNG", "PDF");

        // Configuración por defecto
        configuracion.put("tesseract.path", "/usr/bin/tesseract"); // Ajustar según instalación
        configuracion.put("temp.dir", System.getProperty("java.io.tmpdir"));
        configuracion.put("output.format", "text");
    }

    public String procesar(String rutaImagen) {
        try {
            // Validar formato
            if (!validarFormato(rutaImagen)) {
                throw new IllegalArgumentException("Formato de imagen no soportado");
            }

            // Mejorar imagen si es necesario
            BufferedImage imagenMejorada = mejorarImagen(rutaImagen);

            // Simular procesamiento OCR (en implementación real usarías Tesseract)
            String textoExtraido = extraerTexto(imagenMejorada);

            return textoExtraido;

        } catch (Exception e) {
            System.err.println("Error en procesamiento OCR: " + e.getMessage());
            return null;
        }
    }

    public String extraerTexto(BufferedImage imagen) {
        // SIMULACIÓN - En implementación real integrarías con Tesseract
        // Esta es una simulación para propósitos de demostración
        return "BBVA MÉXICO\n" +
                "COMPROBANTE DE TRANSFERENCIA\n" +
                "FECHA: 15/08/2025 14:30:25\n" +
                "MONTO: $1,250.00\n" +
                "REFERENCIA: ABC123DEF456\n" +
                "CUENTA DESTINO: ****1234\n" +
                "BENEFICIARIO: COMERCIO XYZ SA\n" +
                "CONCEPTO: PAGO VENTA";
    }

    public Map<String, Object> detectarCampos(String textoExtraido) {
        Map<String, Object> camposDetectados = new HashMap<>();

        if (textoExtraido == null || textoExtraido.trim().isEmpty()) {
            return camposDetectados;
        }

        // Detectar banco primero
        String banco = detectarBanco(textoExtraido);
        if (banco != null) {
            camposDetectados.put("bancoEmisor", banco);
            
            // Procesar según el banco específico
            if ("BBVA".equals(banco)) {
                procesarComprobanteBBVA(textoExtraido, camposDetectados);
            } else if ("NU".equals(banco)) {
                procesarComprobanteNU(textoExtraido, camposDetectados);
            } else {
                // Procesamiento genérico para otros bancos
                procesarComprobanteGenerico(textoExtraido, camposDetectados);
            }
        } else {
            // Si no detecta banco, usar procesamiento genérico
            procesarComprobanteGenerico(textoExtraido, camposDetectados);
        }

        return camposDetectados;
    }

    private String detectarBanco(String texto) {
        Matcher matcher = PATRON_BANCO.matcher(texto.toUpperCase());
        if (matcher.find()) {
            String banco = matcher.group(1);
            // Normalizar nombres de bancos
            if ("NU".equals(banco)) {
                return "NU MEXICO";
            }
            return banco;
        }
        
        // Detectar Nu México con patrones específicos
        if (texto.toUpperCase().contains("NU MÉXICO") || 
            texto.toUpperCase().contains("NU MEXICO") ||
            texto.toUpperCase().contains("NUBANK")) {
            return "NU MEXICO";
        }
        
        return null;
    }

    private BigDecimal detectarMonto(String texto) {
        BigDecimal monto = null;
        
        // Intentar patrón con contexto primero
        Matcher matcher = PATRON_MONTO.matcher(texto.toUpperCase());
        if (matcher.find()) {
            try {
                String montoStr = matcher.group(1).replace(",", "");
                monto = new BigDecimal(montoStr);
            } catch (NumberFormatException e) {
                // Continuar con siguiente patrón
            }
        }
        
        // Si no funciona, intentar patrón simple
        if (monto == null) {
            matcher = PATRON_MONTO_SIMPLE.matcher(texto);
            while (matcher.find()) {
                try {
                    String montoStr = matcher.group(1).replace(",", "");
                    BigDecimal montoCandidate = new BigDecimal(montoStr);
                    // Filtrar montos razonables
                    if (montoCandidate.compareTo(BigDecimal.ONE) > 0 && 
                        montoCandidate.compareTo(new BigDecimal("1000000")) < 0) {
                        if (monto == null || montoCandidate.compareTo(monto) > 0) {
                            monto = montoCandidate;
                        }
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        
        return monto;
    }

    private LocalDateTime detectarFecha(String texto) {
        Matcher matcher = PATRON_FECHA.matcher(texto);
        if (matcher.find()) {
            String fechaStr = matcher.group(1);
            try {
                // Intentar diferentes formatos de fecha
                String[] formatos = {"dd/MM/yyyy", "dd-MM-yyyy", "dd/MM/yy", "dd-MM-yy"};

                for (String formato : formatos) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formato);
                        return LocalDateTime.parse(fechaStr + " 00:00:00",
                                DateTimeFormatter.ofPattern(formato + " HH:mm:ss"));
                    } catch (DateTimeParseException e) {
                        continue;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al parsear fecha: " + e.getMessage());
            }
        }
        return null;
    }

    private String detectarReferencia(String texto) {
        // Intentar patrón con contexto primero
        Matcher matcher = PATRON_REFERENCIA.matcher(texto.toUpperCase());
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Si no funciona, intentar patrón simple
        matcher = PATRON_REFERENCIA_SIMPLE.matcher(texto.toUpperCase());
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }

    private String detectarCuentaRemitente(String texto) {
        // Buscar patrones de cuenta (ej: ****1234, XXXX1234)
        Pattern patronCuenta = Pattern.compile("(\\*{4}\\d{4}|X{4}\\d{4}|\\d{4}-\\d{4}-\\d{4}-\\d{4})");
        Matcher matcher = patronCuenta.matcher(texto);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String detectarBeneficiario(String texto) {
        // Buscar líneas que contengan "BENEFICIARIO" seguido de un nombre
        String[] lineas = texto.split("\\n");
        for (String linea : lineas) {
            if (linea.toUpperCase().contains("BENEFICIARIO")) {
                String[] partes = linea.split(":");
                if (partes.length > 1) {
                    return partes[1].trim();
                }
            }
        }
        return null;
    }

    // Procesamiento específico para BBVA
    private void procesarComprobanteBBVA(String texto, Map<String, Object> campos) {
        // Buscar folio específico de BBVA
        Matcher folioMatcher = PATRON_BBVA_FOLIO.matcher(texto.toUpperCase());
        if (folioMatcher.find()) {
            campos.put("referenciaOperacion", folioMatcher.group(1));
        }
        
        // Buscar monto específico de BBVA
        Matcher montoMatcher = PATRON_BBVA_MONTO.matcher(texto.toUpperCase());
        if (montoMatcher.find()) {
            try {
                String montoStr = montoMatcher.group(1).replace(",", "");
                BigDecimal monto = new BigDecimal(montoStr);
                campos.put("montoDetectado", monto);
            } catch (NumberFormatException e) {
                // Usar detección genérica como fallback
                BigDecimal monto = detectarMonto(texto);
                if (monto != null) {
                    campos.put("montoDetectado", monto);
                }
            }
        } else {
            // Usar detección genérica
            BigDecimal monto = detectarMonto(texto);
            if (monto != null) {
                campos.put("montoDetectado", monto);
            }
        }
        
        // Aplicar detección genérica para el resto de campos
        procesarCamposComunes(texto, campos);
    }

    // Procesamiento específico para Nu México
    private void procesarComprobanteNU(String texto, Map<String, Object> campos) {
        // Buscar referencia específica de Nu
        Matcher refMatcher = PATRON_NU_REFERENCIA.matcher(texto);
        if (refMatcher.find()) {
            campos.put("referenciaOperacion", refMatcher.group(1));
        }
        
        // Buscar monto específico de Nu
        Matcher montoMatcher = PATRON_NU_MONTO.matcher(texto);
        if (montoMatcher.find()) {
            try {
                String montoStr = montoMatcher.group(1).replace(",", "");
                BigDecimal monto = new BigDecimal(montoStr);
                campos.put("montoDetectado", monto);
            } catch (NumberFormatException e) {
                // Usar detección genérica como fallback
                BigDecimal monto = detectarMonto(texto);
                if (monto != null) {
                    campos.put("montoDetectado", monto);
                }
            }
        } else {
            // Usar detección genérica
            BigDecimal monto = detectarMonto(texto);
            if (monto != null) {
                campos.put("montoDetectado", monto);
            }
        }
        
        // Buscar beneficiario específico de Nu
        Matcher beneficiarioMatcher = PATRON_NU_BENEFICIARIO.matcher(texto);
        if (beneficiarioMatcher.find()) {
            campos.put("nombreBeneficiario", beneficiarioMatcher.group(1).trim());
        }
        
        // Aplicar detección genérica para el resto de campos
        procesarCamposComunes(texto, campos);
    }

    // Procesamiento genérico para bancos sin patrones específicos
    private void procesarComprobanteGenerico(String texto, Map<String, Object> campos) {
        procesarCamposComunes(texto, campos);
    }

    // Procesamiento común para todos los tipos de comprobantes
    private void procesarCamposComunes(String texto, Map<String, Object> campos) {
        // Detectar monto si no se ha detectado
        if (!campos.containsKey("montoDetectado")) {
            BigDecimal monto = detectarMonto(texto);
            if (monto != null) {
                campos.put("montoDetectado", monto);
            }
        }

        // Detectar fecha
        LocalDateTime fecha = detectarFecha(texto);
        if (fecha != null) {
            campos.put("fechaTransferencia", fecha);
        }

        // Detectar referencia si no se ha detectado
        if (!campos.containsKey("referenciaOperacion")) {
            String referencia = detectarReferencia(texto);
            if (referencia != null) {
                campos.put("referenciaOperacion", referencia);
            }
        }

        // Detectar cuenta remitente
        String cuentaRemitente = detectarCuentaRemitente(texto);
        if (cuentaRemitente != null) {
            campos.put("cuentaRemitente", cuentaRemitente);
        }

        // Detectar beneficiario si no se ha detectado
        if (!campos.containsKey("nombreBeneficiario")) {
            String beneficiario = detectarBeneficiario(texto);
            if (beneficiario != null) {
                campos.put("nombreBeneficiario", beneficiario);
            }
        }
    }

    public BufferedImage mejorarImagen(String rutaImagen) throws Exception {
        BufferedImage imagen = ImageIO.read(new File(rutaImagen));

        // Aquí implementarías mejoras de imagen como:
        // - Ajuste de contraste
        // - Reducción de ruido
        // - Corrección de orientación
        // - Filtros de nitidez

        // Por ahora retornamos la imagen original
        return imagen;
    }

    public boolean validarFormato(String rutaArchivo) {
        if (rutaArchivo == null) {
            return false;
        }

        String extension = rutaArchivo.substring(rutaArchivo.lastIndexOf('.') + 1).toUpperCase();
        return formatosPermitidos.contains(extension);
    }

    public double obtenerConfianza() {
        // En implementación real, esto vendría de Tesseract
        // Por ahora retornamos un valor simulado basado en la precisión configurada
        return precision / 100.0;
    }

    // Método principal para procesar un comprobante completo
    public ComprobanteOCR procesarComprobante(String rutaImagen, Venta venta) {
        try {
            ComprobanteOCR comprobante = new ComprobanteOCR(venta, rutaImagen);

            // Procesar OCR
            String textoExtraido = procesar(rutaImagen);
            if (textoExtraido == null) {
                comprobante.setEstadoValidacion(ComprobanteOCR.EstadoOCR.ERROR_PROCESAMIENTO);
                comprobante.setObservaciones("Error al procesar la imagen");
                return comprobante;
            }

            // Extraer campos
            Map<String, Object> campos = detectarCampos(textoExtraido);

            // Mapear campos al comprobante
            if (campos.containsKey("bancoEmisor")) {
                comprobante.setBancoEmisor((String) campos.get("bancoEmisor"));
            }

            if (campos.containsKey("montoDetectado")) {
                comprobante.setMontoDetectado((BigDecimal) campos.get("montoDetectado"));
            }

            if (campos.containsKey("fechaTransferencia")) {
                comprobante.setFechaTransferencia((LocalDateTime) campos.get("fechaTransferencia"));
            }

            if (campos.containsKey("referenciaOperacion")) {
                comprobante.setReferenciaOperacion((String) campos.get("referenciaOperacion"));
            }

            if (campos.containsKey("cuentaRemitente")) {
                comprobante.setCuentaRemitente((String) campos.get("cuentaRemitente"));
            }

            if (campos.containsKey("nombreBeneficiario")) {
                comprobante.setNombreBeneficiario((String) campos.get("nombreBeneficiario"));
            }

            // Guardar datos extraídos como JSON
            comprobante.setDatosExtraidos(convertirCamposAJson(campos));

            // Validar datos automáticamente
            if (comprobante.validarDatos() && comprobante.validarContraVenta()) {
                comprobante.setEstadoValidacion(ComprobanteOCR.EstadoOCR.PENDIENTE);
            } else {
                comprobante.setEstadoValidacion(ComprobanteOCR.EstadoOCR.RECHAZADO);
                comprobante.setObservaciones("Datos no coinciden con la venta");
            }

            return comprobante;

        } catch (Exception e) {
            System.err.println("Error al procesar comprobante: " + e.getMessage());
            ComprobanteOCR comprobante = new ComprobanteOCR(venta, rutaImagen);
            comprobante.setEstadoValidacion(ComprobanteOCR.EstadoOCR.ERROR_PROCESAMIENTO);
            comprobante.setObservaciones("Error técnico: " + e.getMessage());
            return comprobante;
        }
    }

    private String convertirCamposAJson(Map<String, Object> campos) {
        // Implementación simple de conversión a JSON
        StringBuilder json = new StringBuilder("{");
        boolean primero = true;

        for (Map.Entry<String, Object> entry : campos.entrySet()) {
            if (!primero) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue().toString()).append("\"");
            primero = false;
        }

        json.append("}");
        return json.toString();
    }

    // Getters y Setters
    public Map<String, String> getConfiguracion() {
        return configuracion;
    }

    public void setConfiguracion(Map<String, String> configuracion) {
        this.configuracion = configuracion;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public List<String> getFormatosPermitidos() {
        return formatosPermitidos;
    }

    public void setFormatosPermitidos(List<String> formatosPermitidos) {
        this.formatosPermitidos = formatosPermitidos;
    }
}