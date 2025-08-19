package com.pos.puntoventaocr.models;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
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
        if (textoExtraido == null || textoExtraido.trim().isEmpty()) {
            return new HashMap<>();
        }

        // Usar el nuevo sistema de procesamiento con estrategia "default"
        Map<String, Object> datosExtraidos = procesarTextoExtraido(textoExtraido, "default");
        
        // Mapear los datos al formato esperado por el código existente
        Map<String, Object> camposDetectados = new HashMap<>();
        
        // Mapear campos con nombres compatibles
        if (datosExtraidos.containsKey("banco")) {
            camposDetectados.put("bancoEmisor", datosExtraidos.get("banco"));
        }
        
        if (datosExtraidos.containsKey("monto")) {
            try {
                String montoStr = datosExtraidos.get("monto").toString();
                BigDecimal monto = new BigDecimal(montoStr);
                camposDetectados.put("montoDetectado", monto);
            } catch (NumberFormatException e) {
                // Si hay error, usar el método original como fallback
                BigDecimal monto = detectarMonto(textoExtraido);
                if (monto != null) {
                    camposDetectados.put("montoDetectado", monto);
                }
            }
        }
        
        if (datosExtraidos.containsKey("fecha")) {
            try {
                String fechaStr = datosExtraidos.get("fecha").toString();
                LocalDateTime fecha = LocalDateTime.parse(fechaStr);
                camposDetectados.put("fechaTransferencia", fecha);
            } catch (Exception e) {
                // Si hay error, usar el método original como fallback
                LocalDateTime fecha = detectarFecha(textoExtraido);
                if (fecha != null) {
                    camposDetectados.put("fechaTransferencia", fecha);
                }
            }
        }
        
        if (datosExtraidos.containsKey("referencia")) {
            camposDetectados.put("referenciaOperacion", datosExtraidos.get("referencia"));
        }
        
        if (datosExtraidos.containsKey("cuenta")) {
            camposDetectados.put("cuentaRemitente", datosExtraidos.get("cuenta"));
        }
        
        if (datosExtraidos.containsKey("beneficiario")) {
            camposDetectados.put("nombreBeneficiario", datosExtraidos.get("beneficiario"));
        }
        
        // Si no se extrajo nada con el nuevo sistema, usar el sistema original como fallback
        if (camposDetectados.isEmpty()) {
            // Fallback al sistema original
            String banco = detectarBanco(textoExtraido);
            if (banco != null) {
                camposDetectados.put("bancoEmisor", banco);
            }

            BigDecimal monto = detectarMonto(textoExtraido);
            if (monto != null) {
                camposDetectados.put("montoDetectado", monto);
            }

            LocalDateTime fecha = detectarFecha(textoExtraido);
            if (fecha != null) {
                camposDetectados.put("fechaTransferencia", fecha);
            }

            String referencia = detectarReferencia(textoExtraido);
            if (referencia != null) {
                camposDetectados.put("referenciaOperacion", referencia);
            }

            String cuentaRemitente = detectarCuentaRemitente(textoExtraido);
            if (cuentaRemitente != null) {
                camposDetectados.put("cuentaRemitente", cuentaRemitente);
            }

            String beneficiario = detectarBeneficiario(textoExtraido);
            if (beneficiario != null) {
                camposDetectados.put("nombreBeneficiario", beneficiario);
            }
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

    /**
     * Detecta el tipo de banco basado en el texto extraído
     */
    private String detectarTipoBanco(String texto) {
        String textoUpper = texto.toUpperCase();
        
        if (textoUpper.contains("BBVA")) {
            return "BBVA";
        } else if (textoUpper.contains("NU ") || textoUpper.contains("NUBANK")) {
            return "NU";
        } else if (textoUpper.contains("BANAMEX")) {
            return "BANAMEX";
        } else if (textoUpper.contains("SANTANDER")) {
            return "SANTANDER";
        } else if (textoUpper.contains("HSBC")) {
            return "HSBC";
        } else if (textoUpper.contains("BANORTE")) {
            return "BANORTE";
        } else if (textoUpper.contains("SCOTIABANK")) {
            return "SCOTIABANK";
        } else if (textoUpper.contains("INBURSA")) {
            return "INBURSA";
        }
        
        return "GENERICO";
    }

    /**
     * Procesa el texto extraído usando detección de banco y enrutamiento
     */
    private Map<String, Object> procesarTextoExtraido(String textoCompleto, String estrategia) {
        Map<String, Object> datosExtraidos = new HashMap<>();
        datosExtraidos.put("texto_completo", textoCompleto);
        datosExtraidos.put("estrategia_usada", estrategia);

        // Detectar tipo de banco
        String tipoBanco = detectarTipoBanco(textoCompleto);
        datosExtraidos.put("tipo_banco_detectado", tipoBanco);

        // Procesar según el banco detectado
        Map<String, Object> datosEspecificos;
        switch (tipoBanco) {
            case "BBVA":
                datosEspecificos = procesarComprobanteBBVA(textoCompleto);
                break;
            case "NU":
                datosEspecificos = procesarComprobanteNU(textoCompleto);
                break;
            default:
                datosEspecificos = procesarComprobanteGenerico(textoCompleto);
                break;
        }
        
        // Validar datos antes de combinar
        if (!validarDatosExtraidos(datosEspecificos)) {
            System.out.println("⚠️ Advertencia: Datos extraídos insuficientes para " + tipoBanco);
        }
        
        // Combinar datos
        datosExtraidos.putAll(datosEspecificos);
        
        // Agregar información de debug si está habilitada
        agregarInfoDebug(datosExtraidos, tipoBanco, textoCompleto);
        
        // Calcular confianza mejorada
        double confianza = calcularConfianzaMejorada(textoCompleto, tipoBanco, datosEspecificos);
        datosExtraidos.put("confianza", confianza);

        return datosExtraidos;
    }

    /**
     * Procesa comprobantes de BBVA - implementación básica para ejemplo
     */
    private Map<String, Object> procesarComprobanteBBVA(String texto) {
        // Por ahora, usar el procesamiento genérico 
        // En el futuro se puede especializar para BBVA
        return procesarComprobanteGenerico(texto);
    }

    /**
     * Procesa comprobantes de NU - implementación básica para ejemplo
     */
    private Map<String, Object> procesarComprobanteNU(String texto) {
        // Por ahora, usar el procesamiento genérico
        // En el futuro se puede especializar para NU
        return procesarComprobanteGenerico(texto);
    }

    /**
     * Procesa comprobantes genéricos cuando no se detecta un banco específico
     * Utiliza los patrones originales más mejorados
     */
    private Map<String, Object> procesarComprobanteGenerico(String texto) {
        Map<String, Object> datos = new HashMap<>();
        String textoLimpio = limpiarTexto(texto);
        
        // Extraer banco usando el método original mejorado
        String banco = detectarBanco(textoLimpio.toUpperCase());
        if (banco != null) {
            datos.put("banco", banco);
        }

        // Extraer monto usando patrón mejorado
        BigDecimal monto = extraerMontoMejorado(textoLimpio);
        if (monto != null) {
            datos.put("monto", monto.toString());
        }

        // Extraer fecha usando patrón original
        LocalDateTime fecha = detectarFecha(textoLimpio.toUpperCase());
        if (fecha != null) {
            datos.put("fecha", fecha.toString());
        }

        // Extraer referencia usando patrón original
        String referencia = detectarReferencia(textoLimpio.toUpperCase());
        if (referencia != null) {
            datos.put("referencia", referencia);
        }

        // Extraer cuenta usando patrón original
        String cuenta = detectarCuentaRemitente(textoLimpio.toUpperCase());
        if (cuenta != null) {
            datos.put("cuenta", cuenta);
        }

        // Extraer beneficiario usando patrón original
        String beneficiario = detectarBeneficiario(textoLimpio.toUpperCase());
        if (beneficiario != null) {
            datos.put("beneficiario", beneficiario);
        }

        return datos;
    }

    /**
     * Patrón de monto mejorado que combina múltiples variaciones
     */
    private BigDecimal extraerMontoMejorado(String texto) {
        // Patrones múltiples para detectar montos
        Pattern[] patronesMontos = {
            // Patrón original
            Pattern.compile("(?:MONTO|IMPORTE|CANTIDAD|TOTAL)[^\\d]*\\$?([0-9,]+(?:\\.[0-9]{2})?)|\\$([0-9,]+(?:\\.[0-9]{2})?)"),
            // Patrón para montos con peso mexicano
            Pattern.compile("\\$\\s*([0-9,]+(?:\\.[0-9]{2})?)"),
            // Patrón para números después de palabras clave
            Pattern.compile("(?:TRANSFERENCIA|DEPOSITO|RETIRO|PAGO).*?\\$?([0-9,]+(?:\\.[0-9]{2})?)"),
            // Patrón para formato numérico simple
            Pattern.compile("([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{2})?)"),
            // Patrón específico para "Importe transferido"
            Pattern.compile("(?:Importe\\s+transferido|IMPORTE\\s+TRANSFERIDO)\\s*\\$?([0-9,]+(?:\\.[0-9]{2})?)"),
            // Patrón para "Monto" seguido de cantidad
            Pattern.compile("(?:Monto|MONTO)\\s*\\$?([0-9,]+(?:\\.[0-9]{2})?)"),
        };
        
        BigDecimal montoMayor = null;
        
        for (Pattern patron : patronesMontos) {
            java.util.regex.Matcher matcher = patron.matcher(texto);
            while (matcher.find()) {
                // Intentar con cada grupo capturado
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String montoStr = matcher.group(i);
                    if (montoStr != null && !montoStr.trim().isEmpty()) {
                        try {
                            montoStr = montoStr.replace(",", "").replace(" ", "").trim();
                            BigDecimal monto = new BigDecimal(montoStr);
                            
                            // Validar que sea un monto razonable (entre $1 y $1,000,000)
                            if (monto.compareTo(BigDecimal.ONE) >= 0 && 
                                monto.compareTo(BigDecimal.valueOf(1000000)) <= 0) {
                                
                                if (montoMayor == null || monto.compareTo(montoMayor) > 0) {
                                    montoMayor = monto;
                                }
                            }
                        } catch (NumberFormatException e) {
                            // Continuar con siguiente coincidencia
                        }
                    }
                }
            }
        }
        
        return montoMayor;
    }

    /**
     * Método para limpiar texto mejorado
     */
    private String limpiarTexto(String texto) {
        return texto
                .replaceAll("\\s+", " ")  // Múltiples espacios a uno
                .replaceAll("[^\\w\\s$.,/:-áéíóúñÁÉÍÓÚÑüÜ()]", " ")  // Eliminar caracteres especiales, incluir ü
                .replaceAll("\\b\\d{1,2}\\s*[:]\\s*\\d{2}\\b", " ")  // Limpiar horas que interfieren
                .trim();
    }

    /**
     * Valida que los datos extraídos tengan sentido
     */
    private boolean validarDatosExtraidos(Map<String, Object> datos) {
        // Validar que al menos tengamos monto o referencia
        boolean tieneMonto = datos.containsKey("monto") && 
                            datos.get("monto") != null && 
                            !datos.get("monto").toString().trim().isEmpty();
                            
        boolean tieneReferencia = datos.containsKey("referencia") && 
                                 datos.get("referencia") != null && 
                                 !datos.get("referencia").toString().trim().isEmpty();
        
        return tieneMonto || tieneReferencia;
    }

    /**
     * Agrega información de debugging para desarrollo
     */
    private void agregarInfoDebug(Map<String, Object> datos, String tipoBanco, String texto) {
        if (Boolean.getBoolean("ocr.debug.enabled")) {
            datos.put("debug_tipo_banco", tipoBanco);
            datos.put("debug_longitud_texto", texto.length());
            datos.put("debug_contiene_peso", texto.contains("$"));
            datos.put("debug_contiene_spei", texto.toUpperCase().contains("SPEI"));
            datos.put("debug_timestamp", System.currentTimeMillis());
        }
    }

    /**
     * Calcula la confianza mejorada basada en el texto y datos extraídos
     */
    private double calcularConfianzaMejorada(String textoCompleto, String tipoBanco, Map<String, Object> datosEspecificos) {
        double confianzaBase = obtenerConfianza();
        
        // Ajustar confianza basada en datos extraídos
        int puntaje = 0;
        int maxPuntaje = 0;
        
        // Verificar campos principales
        if (datosEspecificos.containsKey("monto") && datosEspecificos.get("monto") != null) {
            puntaje += 30;
        }
        maxPuntaje += 30;
        
        if (datosEspecificos.containsKey("referencia") && datosEspecificos.get("referencia") != null) {
            puntaje += 25;
        }
        maxPuntaje += 25;
        
        if (datosEspecificos.containsKey("banco") && datosEspecificos.get("banco") != null) {
            puntaje += 20;
        }
        maxPuntaje += 20;
        
        if (datosEspecificos.containsKey("fecha") && datosEspecificos.get("fecha") != null) {
            puntaje += 15;
        }
        maxPuntaje += 15;
        
        if (datosEspecificos.containsKey("cuenta") && datosEspecificos.get("cuenta") != null) {
            puntaje += 10;
        }
        maxPuntaje += 10;
        
        double confianzaDatos = maxPuntaje > 0 ? (double) puntaje / maxPuntaje : 0.0;
        
        // Combinar confianza base con confianza de datos extraídos
        return (confianzaBase + confianzaDatos) / 2.0;
    }

    /**
     * Activa modo debug para desarrollo
     * Agregar esta propiedad del sistema: -Docr.debug.enabled=true
     */
    private void logDebugInfo(String tipoBanco, Map<String, Object> datos, String texto) {
        if (Boolean.getBoolean("ocr.debug.enabled")) {
            System.out.println("=== DEBUG OCR ===");
            System.out.println("Banco detectado: " + tipoBanco);
            System.out.println("Datos extraídos: " + datos.size() + " campos");
            System.out.println("Confianza: " + datos.get("confianza") + "%");
            System.out.println("Texto (primeros 100 chars): " + 
                              (texto.length() > 100 ? texto.substring(0, 100) + "..." : texto));
            System.out.println("================");
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