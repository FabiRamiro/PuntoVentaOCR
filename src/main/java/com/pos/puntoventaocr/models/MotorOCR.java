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

    // Patrones para extraer datos
    private static final Pattern PATRON_MONTO = Pattern.compile("\\$?([0-9,]+\\.?[0-9]*)");
    private static final Pattern PATRON_FECHA = Pattern.compile("(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})");
    private static final Pattern PATRON_REFERENCIA = Pattern.compile("([A-Z0-9]{8,20})");
    private static final Pattern PATRON_BANCO = Pattern.compile("(BBVA|BANAMEX|SANTANDER|HSBC|BANORTE|SCOTIABANK|INBURSA)");

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

        // Detectar banco
        String banco = detectarBanco(textoExtraido);
        if (banco != null) {
            camposDetectados.put("bancoEmisor", banco);
        }

        // Detectar monto
        BigDecimal monto = detectarMonto(textoExtraido);
        if (monto != null) {
            camposDetectados.put("montoDetectado", monto);
        }

        // Detectar fecha
        LocalDateTime fecha = detectarFecha(textoExtraido);
        if (fecha != null) {
            camposDetectados.put("fechaTransferencia", fecha);
        }

        // Detectar referencia
        String referencia = detectarReferencia(textoExtraido);
        if (referencia != null) {
            camposDetectados.put("referenciaOperacion", referencia);
        }

        // Detectar cuenta remitente
        String cuentaRemitente = detectarCuentaRemitente(textoExtraido);
        if (cuentaRemitente != null) {
            camposDetectados.put("cuentaRemitente", cuentaRemitente);
        }

        // Detectar beneficiario
        String beneficiario = detectarBeneficiario(textoExtraido);
        if (beneficiario != null) {
            camposDetectados.put("nombreBeneficiario", beneficiario);
        }

        return camposDetectados;
    }

    private String detectarBanco(String texto) {
        Matcher matcher = PATRON_BANCO.matcher(texto.toUpperCase());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private BigDecimal detectarMonto(String texto) {
        Matcher matcher = PATRON_MONTO.matcher(texto);
        while (matcher.find()) {
            try {
                String montoStr = matcher.group(1).replace(",", "");
                BigDecimal monto = new BigDecimal(montoStr);
                // Filtrar montos muy pequeños o muy grandes
                if (monto.compareTo(BigDecimal.ONE) > 0 && monto.compareTo(new BigDecimal("1000000")) < 0) {
                    return monto;
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }
        return null;
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
        Matcher matcher = PATRON_REFERENCIA.matcher(texto);
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