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
    private static final Pattern PATRON_BANCO = Pattern.compile("(BBVA|BANAMEX|SANTANDER|HSBC|BANORTE|SCOTIABANK|INBURSA|NU)", Pattern.CASE_INSENSITIVE);
    
    // Patrones específicos por banco
    private static final Pattern PATRON_FECHA_ESPANOL = Pattern.compile("(\\d{1,2})\\s+(enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)\\s+(\\d{4})", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATRON_FECHA_ABREV = Pattern.compile("(\\d{1,2})\\s+(ENE|FEB|MAR|ABR|MAY|JUN|JUL|AGO|SEP|OCT|NOV|DIC)\\s+(\\d{4})", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATRON_REFERENCIA_BBVA = Pattern.compile("(MBAN\\d{18,24})");
    private static final Pattern PATRON_REFERENCIA_NU = Pattern.compile("(NU[A-Z0-9]{24,30})");

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
            String banco = matcher.group(1).toUpperCase();
            // Normalizar nombres de banco
            if ("NU".equals(banco)) {
                return "NU";
            }
            return banco;
        }
        return null;
    }

    private BigDecimal detectarMonto(String texto) {
        String bancoDetectado = detectarBanco(texto);
        
        // Intentar extracción específica por banco primero
        if (bancoDetectado != null) {
            BigDecimal monto = detectarMontoPorBanco(texto, bancoDetectado);
            if (monto != null) {
                return monto;
            }
        }
        
        // Fallback al método genérico
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
    
    private BigDecimal detectarMontoPorBanco(String texto, String banco) {
        String etiquetaBuscar = null;
        
        switch (banco) {
            case "BBVA":
                etiquetaBuscar = "Importe transferido";
                break;
            case "NU":
                etiquetaBuscar = "Monto";
                break;
            default:
                return null;
        }
        
        String montoStr = buscarValorPorEtiqueta(texto, etiquetaBuscar);
        if (montoStr != null) {
            try {
                // Limpiar y convertir el monto
                montoStr = montoStr.replaceAll("[^0-9.,]", "").replace(",", "");
                return new BigDecimal(montoStr);
            } catch (NumberFormatException e) {
                // Si falla, continuar con método genérico
            }
        }
        
        return null;
    }

    private LocalDateTime detectarFecha(String texto) {
        String bancoDetectado = detectarBanco(texto);
        
        // Intentar detección específica por banco primero
        if (bancoDetectado != null) {
            LocalDateTime fecha = detectarFechaPorBanco(texto, bancoDetectado);
            if (fecha != null) {
                return fecha;
            }
        }
        
        // Intentar formatos en español
        LocalDateTime fechaEspanol = detectarFechaEspanol(texto);
        if (fechaEspanol != null) {
            return fechaEspanol;
        }
        
        // Fallback al método original
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
    
    private LocalDateTime detectarFechaPorBanco(String texto, String banco) {
        // Para ahora, usar el método genérico de fechas en español
        // Se puede especializar más si cada banco tiene formatos muy específicos
        return detectarFechaEspanol(texto);
    }
    
    private LocalDateTime detectarFechaEspanol(String texto) {
        // Mapa de meses en español
        Map<String, Integer> mesesEspanol = new HashMap<>();
        mesesEspanol.put("enero", 1); mesesEspanol.put("febrero", 2); mesesEspanol.put("marzo", 3);
        mesesEspanol.put("abril", 4); mesesEspanol.put("mayo", 5); mesesEspanol.put("junio", 6);
        mesesEspanol.put("julio", 7); mesesEspanol.put("agosto", 8); mesesEspanol.put("septiembre", 9);
        mesesEspanol.put("octubre", 10); mesesEspanol.put("noviembre", 11); mesesEspanol.put("diciembre", 12);
        
        // Mapa de abreviaciones
        Map<String, Integer> mesesAbrev = new HashMap<>();
        mesesAbrev.put("ENE", 1); mesesAbrev.put("FEB", 2); mesesAbrev.put("MAR", 3);
        mesesAbrev.put("ABR", 4); mesesAbrev.put("MAY", 5); mesesAbrev.put("JUN", 6);
        mesesAbrev.put("JUL", 7); mesesAbrev.put("AGO", 8); mesesAbrev.put("SEP", 9);
        mesesAbrev.put("OCT", 10); mesesAbrev.put("NOV", 11); mesesAbrev.put("DIC", 12);
        
        // Intentar formato completo
        Matcher matcher = PATRON_FECHA_ESPANOL.matcher(texto);
        if (matcher.find()) {
            try {
                int dia = Integer.parseInt(matcher.group(1));
                String mesStr = matcher.group(2).toLowerCase();
                int anio = Integer.parseInt(matcher.group(3));
                
                Integer mes = mesesEspanol.get(mesStr);
                if (mes != null) {
                    return LocalDateTime.of(anio, mes, dia, 0, 0);
                }
            } catch (Exception e) {
                // Continuar con otros formatos
            }
        }
        
        // Intentar formato abreviado
        matcher = PATRON_FECHA_ABREV.matcher(texto);
        if (matcher.find()) {
            try {
                int dia = Integer.parseInt(matcher.group(1));
                String mesStr = matcher.group(2).toUpperCase();
                int anio = Integer.parseInt(matcher.group(3));
                
                Integer mes = mesesAbrev.get(mesStr);
                if (mes != null) {
                    return LocalDateTime.of(anio, mes, dia, 0, 0);
                }
            } catch (Exception e) {
                // Continuar
            }
        }
        
        return null;
    }

    private String detectarReferencia(String texto) {
        String bancoDetectado = detectarBanco(texto);
        
        // Intentar detección específica por banco primero
        if (bancoDetectado != null) {
            String referencia = detectarReferenciaPorBanco(texto, bancoDetectado);
            if (referencia != null) {
                return referencia;
            }
        }
        
        // Fallback al método genérico
        Matcher matcher = PATRON_REFERENCIA.matcher(texto);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private String detectarReferenciaPorBanco(String texto, String banco) {
        String etiquetaBuscar = "Clave de rastreo";
        
        // Buscar por etiqueta primero
        String referencia = buscarValorPorEtiqueta(texto, etiquetaBuscar);
        if (referencia != null && !referencia.trim().isEmpty()) {
            return referencia.trim();
        }
        
        // Patrones específicos por banco
        Pattern patron = null;
        switch (banco) {
            case "BBVA":
                patron = PATRON_REFERENCIA_BBVA;
                break;
            case "NU":
                patron = PATRON_REFERENCIA_NU;
                break;
        }
        
        if (patron != null) {
            Matcher matcher = patron.matcher(texto);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        
        return null;
    }

    private String detectarCuentaRemitente(String texto) {
        String bancoDetectado = detectarBanco(texto);
        
        // Patrones específicos por banco
        if ("BBVA".equals(bancoDetectado)) {
            // BBVA usa formato "-3773"
            Pattern patronBBVA = Pattern.compile("-(\\d{4})");
            Matcher matcher = patronBBVA.matcher(texto);
            if (matcher.find()) {
                return matcher.group(0); // Incluir el guión
            }
        } else if ("NU".equals(bancoDetectado)) {
            // NU usa formato "***0606"
            Pattern patronNU = Pattern.compile("\\*{3}(\\d{4})");
            Matcher matcher = patronNU.matcher(texto);
            if (matcher.find()) {
                return matcher.group(0); // Incluir los asteriscos
            }
        }
        
        // Fallback a patrones genéricos
        Pattern patronCuenta = Pattern.compile("(\\*{3,4}\\d{4}|X{4}\\d{4}|\\d{4}-\\d{4}-\\d{4}-\\d{4}|-\\d{4})");
        Matcher matcher = patronCuenta.matcher(texto);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String detectarBeneficiario(String texto) {
        String bancoDetectado = detectarBanco(texto);
        
        // Buscar por etiqueta primero
        String beneficiario = buscarValorPorEtiqueta(texto, "Beneficiario");
        if (beneficiario != null && !beneficiario.trim().isEmpty()) {
            return beneficiario.trim();
        }
        
        // Fallback al método original
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
    
    /**
     * Busca un valor asociado a una etiqueta específica en el texto
     * @param texto El texto completo extraído del OCR
     * @param etiqueta La etiqueta a buscar (ej: "Monto", "Importe transferido", "Clave de rastreo")
     * @return El valor encontrado o null si no se encuentra
     */
    private String buscarValorPorEtiqueta(String texto, String etiqueta) {
        if (texto == null || etiqueta == null) {
            return null;
        }
        
        String[] lineas = texto.split("\\n");
        
        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i].trim();
            
            // Buscar la etiqueta en la línea actual
            if (linea.toUpperCase().contains(etiqueta.toUpperCase())) {
                // Caso 1: Etiqueta y valor en la misma línea separados por ":"
                if (linea.contains(":")) {
                    String[] partes = linea.split(":", 2);
                    if (partes.length > 1) {
                        String valor = partes[1].trim();
                        if (!valor.isEmpty()) {
                            return valor;
                        }
                    }
                }
                
                // Caso 2: Valor en la siguiente línea
                if (i + 1 < lineas.length) {
                    String siguienteLinea = lineas[i + 1].trim();
                    if (!siguienteLinea.isEmpty()) {
                        return siguienteLinea;
                    }
                }
                
                // Caso 3: Buscar el valor al final de la misma línea
                String lineaSinEtiqueta = linea.replaceFirst("(?i)" + Pattern.quote(etiqueta), "").trim();
                if (!lineaSinEtiqueta.isEmpty()) {
                    // Remover caracteres comunes como ":" al inicio
                    lineaSinEtiqueta = lineaSinEtiqueta.replaceFirst("^[:\\s]+", "");
                    if (!lineaSinEtiqueta.isEmpty()) {
                        return lineaSinEtiqueta;
                    }
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