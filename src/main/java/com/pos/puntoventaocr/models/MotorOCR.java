package com.pos.puntoventaocr.models;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class MotorOCR {
    private static MotorOCR instance;
    private Tesseract tesseract;
    private Map<String, String> configuracion;
    private String idioma;
    private int precision;
    private List<String> formatosPermitidos;
    private Map<String, Pattern> patronesBancos;
    private Map<String, BancoExtractor> extractoresBanco;

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
        this.idioma = "spa";
        this.precision = 80;
        this.formatosPermitidos = Arrays.asList("JPG", "JPEG", "PNG", "PDF");

        // Inicializar Tesseract REAL
        inicializarTesseract();
        inicializarPatronesBancos();
        inicializarExtractoresBanco();

        configuracion.put("tesseract.path", "/usr/bin/tesseract");
        configuracion.put("temp.dir", System.getProperty("java.io.tmpdir"));
        configuracion.put("output.format", "text");
    }

    private void inicializarTesseract() {
        tesseract = new Tesseract();
        try {
            tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
            tesseract.setLanguage("spa");
            tesseract.setPageSegMode(1);
            tesseract.setOcrEngineMode(1);
            tesseract.setVariable("tesseract_char_whitelist", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzÁÉÍÓÚáéíóúÑñ.,:-/$%() *");
            tesseract.setVariable("preserve_interword_spaces", "1");
            System.out.println("✓ Tesseract inicializado correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error inicializando Tesseract: " + e.getMessage());
        }
    }

    private void inicializarPatronesBancos() {
        patronesBancos = new HashMap<>();
        patronesBancos.put("BBVA", Pattern.compile("(?i)bbva|bancomer"));
        patronesBancos.put("NU", Pattern.compile("(?i)(^|\\s)nu(\\s|$|\\b)"));
        patronesBancos.put("SANTANDER", Pattern.compile("(?i)santander"));
        patronesBancos.put("BANAMEX", Pattern.compile("(?i)banamex|citibanamex"));
        patronesBancos.put("BANORTE", Pattern.compile("(?i)banorte"));
    }

    private void inicializarExtractoresBanco() {
        extractoresBanco = new HashMap<>();
        extractoresBanco.put("BBVA", new BBVAExtractor());
        extractoresBanco.put("NU", new NUExtractor());
        extractoresBanco.put("GENERICO", new GenericoExtractor());
    }

    public String procesar(String rutaImagen) {
        try {
            if (!validarFormato(rutaImagen)) {
                throw new IllegalArgumentException("Formato no soportado");
            }

            // USAR TESSERACT REAL en lugar de simulación
            String textoExtraido = tesseract.doOCR(new File(rutaImagen));
            
            System.out.println("🔍 TEXTO REAL EXTRAÍDO:");
            System.out.println("================================");
            System.out.println(textoExtraido);
            System.out.println("================================");

            return textoExtraido;

        } catch (Exception e) {
            System.err.println("Error en procesamiento OCR: " + e.getMessage());
            return null;
        }
    }

    public String extraerTexto(BufferedImage imagen) {
        try {
            // USAR TESSERACT REAL con BufferedImage
            return tesseract.doOCR(imagen);
        } catch (TesseractException e) {
            System.err.println("❌ Error Tesseract: " + e.getMessage());
            return null;
        }
    }

    public Map<String, Object> detectarCampos(String textoExtraido) {
        Map<String, Object> camposDetectados = new HashMap<>();

        if (textoExtraido == null || textoExtraido.trim().isEmpty()) {
            System.err.println("❌ Texto extraído está vacío o nulo");
            return camposDetectados;
        }

        System.out.println("🔍 INICIANDO DETECCIÓN DE CAMPOS...");
        System.out.println("📄 Texto a procesar: " + textoExtraido.substring(0, Math.min(200, textoExtraido.length())) + "...");

        // 1. Detectar banco primero
        String banco = extraerBanco(textoExtraido);
        if (banco != null) {
            camposDetectados.put("bancoEmisor", banco);
            System.out.println("✅ Banco detectado: " + banco);
        } else {
            System.out.println("❌ No se detectó banco");
        }

        // 2. Usar extractor especializado
        BancoExtractor extractor = extractoresBanco.getOrDefault(banco, extractoresBanco.get("GENERICO"));
        
        System.out.println("🏦 Usando extractor para: " + (banco != null ? banco : "GENERICO"));

        // 3. Extraer datos con métodos especializados CON MAS DEBUGGING
        System.out.println("💰 Intentando extraer MONTO...");
        BigDecimal monto = extractor.extraerMonto(textoExtraido);
        if (monto != null) {
            camposDetectados.put("montoDetectado", monto);
            System.out.println("✅ Monto extraído: " + monto);
        } else {
            System.out.println("❌ No se pudo extraer monto");
        }

        System.out.println("🔢 Intentando extraer REFERENCIA...");
        String referencia = extractor.extraerReferencia(textoExtraido);
        if (referencia != null) {
            camposDetectados.put("referenciaOperacion", referencia);
            System.out.println("✅ Referencia extraída: " + referencia);
        } else {
            System.out.println("❌ No se pudo extraer referencia");
        }

        System.out.println("📅 Intentando extraer FECHA...");
        LocalDate fecha = extractor.extraerFecha(textoExtraido);
        if (fecha != null) {
            camposDetectados.put("fechaTransferencia", fecha.atStartOfDay());
            System.out.println("✅ Fecha extraída: " + fecha);
        } else {
            System.out.println("❌ No se pudo extraer fecha");
        }

        System.out.println("🏧 Intentando extraer CUENTA REMITENTE...");
        String cuenta = extractor.extraerCuentaRemitente(textoExtraido);
        if (cuenta != null) {
            camposDetectados.put("cuentaRemitente", cuenta);
            System.out.println("✅ Cuenta extraída: " + cuenta);
        } else {
            System.out.println("❌ No se pudo extraer cuenta");
        }

        System.out.println("👤 Intentando extraer BENEFICIARIO...");
        String beneficiario = extractor.extraerBeneficiario(textoExtraido);
        if (beneficiario != null) {
            camposDetectados.put("nombreBeneficiario", beneficiario);
            System.out.println("✅ Beneficiario extraído: " + beneficiario);
        } else {
            System.out.println("❌ No se pudo extraer beneficiario");
        }

        System.out.println("📋 RESUMEN DE CAMPOS DETECTADOS:");
        for (Map.Entry<String, Object> entry : camposDetectados.entrySet()) {
            System.out.println("   - " + entry.getKey() + ": " + entry.getValue());
        }

        return camposDetectados;
    }

    private String extraerBanco(String texto) {
        System.out.println("🏦 Detectando banco en texto...");
        for (Map.Entry<String, Pattern> entry : patronesBancos.entrySet()) {
            Matcher matcher = entry.getValue().matcher(texto);
            if (matcher.find()) {
                System.out.println("✓ Banco detectado: " + entry.getKey() + " (coincidencia: " + matcher.group() + ")");
                return entry.getKey();
            }
        }
        System.out.println("⚠️ Banco no identificado, usando genérico");
        return "GENERICO";
    }

    // =================== INTERFACES Y EXTRACTORES ===================

    private interface BancoExtractor {
        BigDecimal extraerMonto(String texto);
        String extraerReferencia(String texto);
        LocalDate extraerFecha(String texto);
        String extraerCuentaRemitente(String texto);
        String extraerBeneficiario(String texto);
    }

    // =================== EXTRACTOR BBVA MEJORADO ===================
    private class BBVAExtractor implements BancoExtractor {
        
        @Override
        public BigDecimal extraerMonto(String texto) {
            System.out.println("🔎 Extrayendo monto BBVA...");
            
            // PATRONES MAS FLEXIBLES PARA BBVA
            List<Pattern> patronesMonto = Arrays.asList(
                // Patrones específicos de BBVA
                Pattern.compile("(?i)importe\\s+transferido[\\s\\n]*:?[\\s\\n]*\\$?\\s*([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i)importe[\\s\\n]*:?[\\s\\n]*\\$?\\s*([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i)monto[\\s\\n]*:?[\\s\\n]*\\$?\\s*([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i)cantidad[\\s\\n]*:?[\\s\\n]*\\$?\\s*([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE),
                // Patrones genéricos para montos
                Pattern.compile("\\$\\s*([0-9,]+\\.[0-9]{2})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\$\\s*([0-9,]+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("([0-9,]+\\.[0-9]{2})(?=\\s|$)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE)
            );

            return extraerMontoConPatrones(texto, patronesMonto, new String[]{"importe transferido", "importe", "monto", "cantidad"});
        }

        @Override
        public String extraerReferencia(String texto) {
            System.out.println("🔎 Extrayendo referencia BBVA...");
            
            List<Pattern> patronesRef = Arrays.asList(
                // Patrones específicos de BBVA
                Pattern.compile("(?i)clave\\s+de\\s+rastreo[\\s\\n]*:?[\\s\\n]*([A-Z0-9]{10,35})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i)folio[\\s\\n]*:?[\\s\\n]*([A-Z0-9]{10,35})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i)referencia[\\s\\n]*:?[\\s\\n]*([A-Z0-9]{10,35})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i)número\\s+de\\s+referencia[\\s\\n]*:?[\\s\\n]*([A-Z0-9]{10,35})", Pattern.CASE_INSENSITIVE),
                // Patrones genéricos para códigos
                Pattern.compile("([A-Z0-9]{15,35})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("MBAN([A-Z0-9]{10,30})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("([0-9]{10,20})", Pattern.CASE_INSENSITIVE)
            );

            return extraerTextoConPatrones(texto, patronesRef, new String[]{"clave de rastreo", "folio", "referencia", "número de referencia"});
        }

        @Override
        public LocalDate extraerFecha(String texto) {
            System.out.println("🔎 Extrayendo fecha BBVA...");
            
            List<Pattern> patronesFecha = Arrays.asList(
                // Patrones con "Fecha"
                Pattern.compile("(?i)fecha[\\s\\n]*:?[\\s\\n]*(\\d{1,2}\\s+[a-záéíóú]+\\s+\\d{4})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i)fecha[\\s\\n]*:?[\\s\\n]*(\\d{1,2}[-/]\\d{1,2}[-/]\\d{4})", Pattern.CASE_INSENSITIVE),
                // Patrones directos de fecha
                Pattern.compile("(\\d{1,2}\\s+(?:enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)\\s+\\d{4})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(\\d{1,2}[-/]\\d{1,2}[-/]\\d{4})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(\\d{4}[-/]\\d{1,2}[-/]\\d{1,2})", Pattern.CASE_INSENSITIVE)
            );

            return extraerFechaConPatrones(texto, patronesFecha);
        }

        @Override
        public String extraerCuentaRemitente(String texto) {
            System.out.println("🔎 Extrayendo cuenta BBVA...");
            
            List<Pattern> patronesCuenta = Arrays.asList(
                // Patrones específicos de BBVA
                Pattern.compile("(?i)cuenta\\s+de\\s+origen[\\s\\n]*:?[\\s\\n]*[•*\\-]*(\\d{4})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i)origen[\\s\\n]*:?[\\s\\n]*[•*\\-]*(\\d{4})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i)cuenta[\\s\\n]*:?[\\s\\n]*[•*\\-]*(\\d{4})", Pattern.CASE_INSENSITIVE),
                // Patrones genéricos
                Pattern.compile("[•*\\-]+(\\d{4})(?=\\s|$)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("-(\\d{4})(?=\\s|$)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\*+(\\d{4})(?=\\s|$)", Pattern.CASE_INSENSITIVE)
            );

            return extraerTextoConPatrones(texto, patronesCuenta, new String[]{"cuenta de origen", "origen", "cuenta"});
        }

        @Override
        public String extraerBeneficiario(String texto) {
            System.out.println("🔎 Extrayendo beneficiario BBVA...");

            List<Pattern> patronesBenef = Arrays.asList(
                    // ✅ ARREGLO: Patrón más específico que pare en la siguiente sección
                    Pattern.compile("(?i)nombre\\s+del\\s+beneficiario[\\s\\n]*:?[\\s\\n]*([A-ZÁÉÍÓÚÑ][a-záéíóúñ\\s]+?)(?=\\s*Nombre\\s+del\\s+banco|$)", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("(?i)beneficiario[\\s\\n]*:?[\\s\\n]*([A-ZÁÉÍÓÚÑ][a-záéíóúñ\\s]+?)(?=\\s*Nombre\\s+del\\s+banco|$)", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("(?i)nombre[\\s\\n]*:?[\\s\\n]*([A-ZÁÉÍÓÚÑ][a-záéíóúñ\\s]+?)(?=\\s*Nombre\\s+del\\s+banco|$)", Pattern.CASE_INSENSITIVE),
                    // Patrón para nombres propios (solo el nombre, sin texto adicional)
                    Pattern.compile("([A-ZÁÉÍÓÚÑ][a-záéíóúñ]+\\s+[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+\\s+[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+\\s+[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+)")
            );

            String resultado = extraerTextoConPatrones(texto, patronesBenef, new String[]{"nombre del beneficiario", "beneficiario", "nombre"});

            // ✅ ARREGLO: Limpiar el resultado
            if (resultado != null) {
                resultado = resultado.trim();
                // Remover saltos de línea y texto adicional
                resultado = resultado.replaceAll("[\\r\\n]+", " ");
                resultado = resultado.replaceAll("\\s+", " ");
                // Si hay más de 4 palabras, tomar solo las primeras 4 (nombre completo típico)
                String[] palabras = resultado.split("\\s+");
                if (palabras.length > 4) {
                    resultado = String.join(" ", Arrays.copyOf(palabras, 4));
                }
            }

            return resultado;
        }
    }

    // =================== EXTRACTOR GENÉRICO MEJORADO ===================
    private class GenericoExtractor implements BancoExtractor {
        
        @Override
        public BigDecimal extraerMonto(String texto) {
            System.out.println("🔎 Extrayendo monto GENÉRICO...");
            
            List<Pattern> patronesMonto = Arrays.asList(
                // Patrones con etiquetas
                Pattern.compile("(?i)(monto|importe|cantidad|total)[\\s\\n]*:?[\\s\\n]*\\$?\\s*([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE),
                // Patrones directos de dinero
                Pattern.compile("\\$\\s*([0-9,]+\\.[0-9]{2})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\$\\s*([0-9,]+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("([0-9,]+\\.[0-9]{2})(?=\\s|$)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE)
            );

            // Para el genérico, usar el grupo 2 si hay etiqueta, grupo 1 si es directo
            for (Pattern patron : patronesMonto) {
                Matcher matcher = patron.matcher(texto);
                if (matcher.find()) {
                    try {
                        String montoStr;
                        if (matcher.groupCount() >= 2 && matcher.group(2) != null) {
                            montoStr = matcher.group(2); // Tiene etiqueta
                        } else {
                            montoStr = matcher.group(1); // Es directo
                        }
                        
                        String montoLimpio = montoStr.replaceAll("[^0-9.]", "");
                        BigDecimal resultado = new BigDecimal(montoLimpio);
                        System.out.println("✓ Monto GENÉRICO detectado: " + resultado + " (texto original: " + montoStr + ")");
                        return resultado;
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Error parseando monto: " + e.getMessage());
                        continue;
                    }
                }
            }
            return null;
        }

        @Override
        public String extraerReferencia(String texto) {
            System.out.println("🔎 Extrayendo referencia GENÉRICA...");
            
            List<Pattern> patronesRef = Arrays.asList(
                // Con etiquetas
                Pattern.compile("(?i)(referencia|folio|clave)[\\s\\n]*:?[\\s\\n]*([A-Z0-9]{10,35})", Pattern.CASE_INSENSITIVE),
                // Directos
                Pattern.compile("([A-Z0-9]{15,35})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("([0-9]{10,20})", Pattern.CASE_INSENSITIVE)
            );

            return extraerTextoConPatrones(texto, patronesRef, new String[]{"referencia", "folio", "clave"});
        }

        @Override
        public LocalDate extraerFecha(String texto) {
            System.out.println("🔎 Extrayendo fecha GENÉRICA...");
            
            List<Pattern> patronesFecha = Arrays.asList(
                Pattern.compile("(\\d{1,2}\\s+[a-z]+\\s+\\d{4})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(\\d{1,2}[-/]\\d{1,2}[-/]\\d{4})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(\\d{4}[-/]\\d{1,2}[-/]\\d{1,2})", Pattern.CASE_INSENSITIVE)
            );
            
            return extraerFechaConPatrones(texto, patronesFecha);
        }

        @Override
        public String extraerCuentaRemitente(String texto) {
            System.out.println("🔎 Extrayendo cuenta GENÉRICA...");
            
            List<Pattern> patronesCuenta = Arrays.asList(
                Pattern.compile("(?i)cuenta[\\s\\n]*:?[\\s\\n]*[•*\\-]*(\\d{4})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("[•*\\-]+(\\d{4})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\*+(\\d{4})", Pattern.CASE_INSENSITIVE)
            );
            
            return extraerTextoConPatrones(texto, patronesCuenta, new String[]{"cuenta"});
        }

        @Override
        public String extraerBeneficiario(String texto) {
            System.out.println("🔎 Extrayendo beneficiario GENÉRICO...");
            
            List<Pattern> patronesBenef = Arrays.asList(
                Pattern.compile("(?i)beneficiario[\\s\\n:]*([A-Z][a-z\\s]+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i)nombre[\\s\\n:]*([A-Z][a-z\\s]+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("([A-ZÁÉÍÓÚÑ][a-záéíóúñ]+\\s+[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+)")
            );
            
            return extraerTextoConPatrones(texto, patronesBenef, new String[]{"beneficiario", "nombre"});
        }
    }

    // =================== EXTRACTOR NU ===================
    private class NUExtractor implements BancoExtractor {
        
        @Override
        public BigDecimal extraerMonto(String texto) {
            System.out.println("🔎 Extrayendo monto NU...");
            
            List<Pattern> patronesMonto = Arrays.asList(
                Pattern.compile("(?i)monto[\\s\\n]*:?[\\s\\n]*\\$?\\s*([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\$\\s*([0-9,]+\\.[0-9]{2})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("([0-9,]+\\.[0-9]{2})(?=\\s|$)", Pattern.CASE_INSENSITIVE)
            );

            return extraerMontoConPatrones(texto, patronesMonto, new String[]{"monto", "importe", "cantidad"});
        }

        @Override
        public String extraerReferencia(String texto) {
            System.out.println("🔎 Extrayendo referencia NU...");
            
            List<Pattern> patronesRef = Arrays.asList(
                Pattern.compile("(?i)clave\\s+de\\s+rastreo[\\s\\n]*:?[\\s\\n]*([A-Z0-9]{15,35})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(NU[A-Z0-9]{15,30})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("([A-Z0-9]{20,35})", Pattern.CASE_INSENSITIVE)
            );

            return extraerTextoConPatrones(texto, patronesRef, new String[]{"clave de rastreo", "número de referencia", "referencia"});
        }

        @Override
        public LocalDate extraerFecha(String texto) {
            System.out.println("🔎 Extrayendo fecha NU...");
            
            List<Pattern> patronesFecha = Arrays.asList(
                Pattern.compile("(?i)autorización\\s+(\\d{1,2}\\s+[A-Z]{3}\\s+\\d{4})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(\\d{1,2}\\s+(?:ENE|FEB|MAR|ABR|MAY|JUN|JUL|AGO|SEP|OCT|NOV|DIC)\\s+\\d{4})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(\\d{1,2}\\s+[A-Z]{3}\\s+\\d{4})", Pattern.CASE_INSENSITIVE)
            );

            return extraerFechaConPatrones(texto, patronesFecha);
        }

        @Override
        public String extraerCuentaRemitente(String texto) {
            System.out.println("🔎 Extrayendo cuenta NU...");
            
            List<Pattern> patronesCuenta = Arrays.asList(
                Pattern.compile("(?i)clabe[\\s\\n]*:?[\\s\\n]*\\*+(\\d{4})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\*+(\\d{4})(?=\\s|$)", Pattern.CASE_INSENSITIVE)
            );

            return extraerTextoConPatrones(texto, patronesCuenta, new String[]{"clabe", "tarjeta de débito", "cuenta"});
        }

        @Override
        public String extraerBeneficiario(String texto) {
            System.out.println("🔎 Extrayendo beneficiario NU...");
            
            List<Pattern> patronesBenef = Arrays.asList(
                Pattern.compile("(?i)nombre[\\s\\n]*:?[\\s\\n]*([A-ZÁÉÍÓÚÑ][a-záéíóúñ\\s]+?)(?=\\s*Dato\\s+no\\s+verificado|$)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("([A-ZÁÉÍÓÚÑ][a-záéíóúñ]+\\s+[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+\\s+[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+)")
            );

            String beneficiario = extraerTextoConPatrones(texto, patronesBenef, new String[]{"nombre", "beneficiario"});
            
            if (beneficiario != null) {
                beneficiario = beneficiario.replace("Dato no verificado por Nu", "").trim();
                beneficiario = beneficiario.replaceAll("\\s+", " ").trim();
            }
            
            return beneficiario;
        }
    }

    // =================== MÉTODOS UTILITARIOS MEJORADOS ===================

    private BigDecimal extraerMontoConPatrones(String texto, List<Pattern> patrones, String[] etiquetas) {
        System.out.println("💰 Probando " + patrones.size() + " patrones de monto...");
        
        for (int i = 0; i < patrones.size(); i++) {
            Pattern patron = patrones.get(i);
            System.out.println("   Patrón " + (i+1) + ": " + patron.pattern());
            
            Matcher matcher = patron.matcher(texto);
            while (matcher.find()) {
                try {
                    String montoStr = matcher.group(1);
                    System.out.println("   🎯 Coincidencia encontrada: '" + matcher.group() + "' -> monto: '" + montoStr + "'");
                    
                    String montoLimpio = montoStr.replaceAll("[^0-9.]", "");
                    if (montoLimpio.isEmpty()) {
                        System.out.println("   ❌ Monto vacío después de limpieza");
                        continue;
                    }
                    
                    BigDecimal resultado = new BigDecimal(montoLimpio);
                    System.out.println("   ✅ Monto válido detectado: " + resultado);
                    return resultado;
                } catch (NumberFormatException e) {
                    System.out.println("   ❌ Error parseando monto: " + e.getMessage());
                    continue;
                }
            }
        }
        System.out.println("   ❌ No se encontró monto con ningún patrón");
        return null;
    }

    private String extraerTextoConPatrones(String texto, List<Pattern> patrones, String[] etiquetas) {
        System.out.println("🔤 Probando " + patrones.size() + " patrones de texto...");
        
        for (int i = 0; i < patrones.size(); i++) {
            Pattern patron = patrones.get(i);
            System.out.println("   Patrón " + (i+1) + ": " + patron.pattern());
            
            Matcher matcher = patron.matcher(texto);
            while (matcher.find()) {
                String resultado = matcher.group(1);
                if (resultado != null && !resultado.trim().isEmpty()) {
                    System.out.println("   🎯 Coincidencia encontrada: '" + matcher.group() + "' -> resultado: '" + resultado + "'");
                    return resultado.trim();
                }
            }
        }
        System.out.println("   ❌ No se encontró texto con ningún patrón");
        return null;
    }

    private LocalDate extraerFechaConPatrones(String texto, List<Pattern> patrones) {
        System.out.println("📅 Probando " + patrones.size() + " patrones de fecha...");

        for (int i = 0; i < patrones.size(); i++) {
            Pattern patron = patrones.get(i);
            System.out.println("   Patrón " + (i+1) + ": " + patron.pattern());

            Matcher matcher = patron.matcher(texto);
            while (matcher.find()) {
                try {
                    String fechaStr = matcher.group(1).trim();
                    System.out.println("   🎯 Coincidencia encontrada: '" + matcher.group() + "' -> fecha: '" + fechaStr + "'");

                    // ✅ ARREGLO: NO duplicar los reemplazos de meses
                    Map<String, String> mesesCompletos = new HashMap<>();
                    mesesCompletos.put("enero", "enero");
                    mesesCompletos.put("febrero", "febrero");
                    mesesCompletos.put("marzo", "marzo");
                    mesesCompletos.put("abril", "abril");
                    mesesCompletos.put("mayo", "mayo");
                    mesesCompletos.put("junio", "junio");
                    mesesCompletos.put("julio", "julio");
                    mesesCompletos.put("agosto", "agosto");
                    mesesCompletos.put("septiembre", "septiembre");
                    mesesCompletos.put("octubre", "octubre");
                    mesesCompletos.put("noviembre", "noviembre");
                    mesesCompletos.put("diciembre", "diciembre");

                    // Solo reemplazar abreviaciones, NO los nombres completos
                    Map<String, String> mesesAbrev = new HashMap<>();
                    mesesAbrev.put("AGO", "agosto");
                    mesesAbrev.put("ENE", "enero");
                    mesesAbrev.put("FEB", "febrero");
                    mesesAbrev.put("MAR", "marzo");
                    mesesAbrev.put("ABR", "abril");
                    mesesAbrev.put("MAY", "mayo");
                    mesesAbrev.put("JUN", "junio");
                    mesesAbrev.put("JUL", "julio");
                    mesesAbrev.put("SEP", "septiembre");
                    mesesAbrev.put("OCT", "octubre");
                    mesesAbrev.put("NOV", "noviembre");
                    mesesAbrev.put("DIC", "diciembre");

                    // ✅ ARREGLO: Solo reemplazar si es abreviación (3 letras exactas)
                    for (Map.Entry<String, String> entry : mesesAbrev.entrySet()) {
                        fechaStr = fechaStr.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
                    }

                    System.out.println("   📝 Fecha después de reemplazos: '" + fechaStr + "'");

                    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .appendPattern("[d MMMM yyyy]")
                        .appendPattern("[d MMM yyyy]")
                        .appendPattern("[d-M-yyyy]")
                        .appendPattern("[d/M/yyyy]")
                        .appendPattern("[yyyy-M-d]")
                        .appendPattern("[yyyy/M/d]")
                        .parseCaseInsensitive()
                        .toFormatter(new Locale("es", "ES"));

                    LocalDate fecha = LocalDate.parse(fechaStr, formatter);
                    System.out.println("   ✅ Fecha válida detectada: " + fecha);
                    return fecha;
                } catch (Exception e) {
                    System.out.println("   ❌ Error parseando fecha: " + e.getMessage());
                }
            }
        }
        System.out.println("   ❌ No se encontró fecha con ningún patrón");
        return null;
    }

    // =================== MÉTODOS PÚBLICOS PARA COMPATIBILIDAD ===================

    public BufferedImage mejorarImagen(String rutaImagen) throws Exception {
        return ImageIO.read(new File(rutaImagen));
    }

    public boolean validarFormato(String rutaArchivo) {
        if (rutaArchivo == null) return false;
        String extension = rutaArchivo.substring(rutaArchivo.lastIndexOf('.') + 1).toUpperCase();
        return formatosPermitidos.contains(extension);
    }

    public double obtenerConfianza() {
        return precision / 100.0;
    }

    // ✅ MÉTODO CORRECTO QUE ARREGLA EL ERROR DEL CONSTRUCTOR
    public ComprobanteOCR procesarComprobante(String rutaImagen) {
        try {
            // ✅ USAR CONSTRUCTOR CORRECTO: new ComprobanteOCR() sin parámetros
            ComprobanteOCR comprobante = new ComprobanteOCR();
            comprobante.setImagenOriginal(rutaImagen);

            String textoExtraido = procesar(rutaImagen);
            if (textoExtraido == null) {
                comprobante.setEstadoValidacion("ERROR");
                comprobante.setObservaciones("Error al procesar la imagen");
                return comprobante;
            }

            Map<String, Object> campos = detectarCampos(textoExtraido);

            if (campos.containsKey("bancoEmisor")) {
                comprobante.setBancoEmisor((String) campos.get("bancoEmisor"));
            }

            if (campos.containsKey("montoDetectado")) {
                comprobante.setMontoDetectado((BigDecimal) campos.get("montoDetectado"));
            }

            if (campos.containsKey("fechaTransferencia")) {
                Object fecha = campos.get("fechaTransferencia");
                // ✅ CONVERSIÓN CORRECTA DE LocalDateTime A LocalDate
                if (fecha instanceof LocalDateTime) {
                    comprobante.setFechaTransferencia(((LocalDateTime) fecha).toLocalDate());
                }
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

            comprobante.setDatosExtraidos(convertirCamposAJson(campos));

            // ✅ SIMPLIFICAR LA VALIDACIÓN - QUITAR MÉTODOS QUE NO EXISTEN
            if (comprobante.getMontoDetectado() != null && comprobante.getReferenciaOperacion() != null) {
                comprobante.setEstadoValidacion("PENDIENTE");
            } else {
                comprobante.setEstadoValidacion("RECHAZADO");
                comprobante.setObservaciones("Datos insuficientes detectados");
            }

            return comprobante;

        } catch (Exception e) {
            System.err.println("Error al procesar comprobante: " + e.getMessage());
            ComprobanteOCR comprobante = new ComprobanteOCR();
            comprobante.setImagenOriginal(rutaImagen);
            comprobante.setEstadoValidacion("ERROR");
            comprobante.setObservaciones("Error técnico: " + e.getMessage());
            return comprobante;
        }
    }

    private String convertirCamposAJson(Map<String, Object> campos) {
        StringBuilder json = new StringBuilder("{");
        boolean primero = true;

        for (Map.Entry<String, Object> entry : campos.entrySet()) {
            if (!primero) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue() != null ? entry.getValue().toString() : "").append("\"");
            primero = false;
        }

        json.append("}");
        return json.toString();
    }

    // Getters y Setters
    public Map<String, String> getConfiguracion() { return configuracion; }
    public void setConfiguracion(Map<String, String> configuracion) { this.configuracion = configuracion; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    public int getPrecision() { return precision; }
    public void setPrecision(int precision) { this.precision = precision; }
    public List<String> getFormatosPermitidos() { return formatosPermitidos; }
    public void setFormatosPermitidos(List<String> formatosPermitidos) { this.formatosPermitidos = formatosPermitidos; }
}
