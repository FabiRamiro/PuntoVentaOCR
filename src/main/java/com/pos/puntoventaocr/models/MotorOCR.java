package com.pos.puntoventaocr.models;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MotorOCR {
    private Tesseract tesseract;
    private Map<String, Pattern> patronesBancos;

    public MotorOCR() {
        inicializarTesseract();
        inicializarPatronesBancos();
    }

    private void inicializarTesseract() {
        tesseract = new Tesseract();
        try {
            // CONFIGURACIÓN CORREGIDA: Usar la ubicación del sistema
            tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
            tesseract.setLanguage("spa"); // Español
            tesseract.setPageSegMode(1);
            tesseract.setOcrEngineMode(1);

            // Configuraciones adicionales para mejor reconocimiento
            tesseract.setVariable("tessedit_char_whitelist", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzÁÉÍÓÚáéíóúÑñ.,:-/$%() ");
            tesseract.setVariable("preserve_interword_spaces", "1");

            System.out.println("✓ Tesseract inicializado correctamente con idioma español");

        } catch (Exception e) {
            System.err.println("❌ Error inicializando Tesseract: " + e.getMessage());
            try {
                // Respaldo: intentar con inglés y ruta del sistema
                tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
                tesseract.setLanguage("eng");
                System.out.println("⚠ Usando inglés como respaldo");
            } catch (Exception e2) {
                System.err.println("❌ Error crítico en configuración de Tesseract: " + e2.getMessage());
                throw new RuntimeException("No se puede inicializar Tesseract", e2);
            }
        }
    }

    private void inicializarPatronesBancos() {
        patronesBancos = new HashMap<>();

        // Patrones para diferentes bancos mexicanos
        patronesBancos.put("BBVA", Pattern.compile("(?i)bbva|bancomer"));
        patronesBancos.put("BANAMEX", Pattern.compile("(?i)banamex|citibanamex"));
        patronesBancos.put("SANTANDER", Pattern.compile("(?i)santander"));
        patronesBancos.put("BANORTE", Pattern.compile("(?i)banorte"));
        patronesBancos.put("HSBC", Pattern.compile("(?i)hsbc"));
        patronesBancos.put("SCOTIABANK", Pattern.compile("(?i)scotiabank"));
        patronesBancos.put("INBURSA", Pattern.compile("(?i)inbursa"));
        patronesBancos.put("AZTECA", Pattern.compile("(?i)azteca"));
        patronesBancos.put("NU", Pattern.compile("(?i)nu\\s*bank|nubank"));
        patronesBancos.put("SPEI", Pattern.compile("(?i)spei"));
    }

    public ComprobanteOCR procesarComprobante(String rutaImagen) {
        ComprobanteOCR comprobante = new ComprobanteOCR();
        comprobante.setImagenOriginal(rutaImagen);

        try {
            // Extraer texto de la imagen
            String textoExtraido = tesseract.doOCR(new File(rutaImagen));

            if (textoExtraido == null || textoExtraido.trim().isEmpty()) {
                comprobante.marcarErrorProcesamiento("No se pudo extraer texto de la imagen");
                return comprobante;
            }

            // Procesar y extraer datos específicos
            extraerDatos(textoExtraido, comprobante);
            comprobante.setDatosExtraidos(textoExtraido);

            return comprobante;

        } catch (TesseractException e) {
            comprobante.marcarErrorProcesamiento("Error OCR: " + e.getMessage());
            return comprobante;
        } catch (Exception e) {
            comprobante.marcarErrorProcesamiento("Error procesando imagen: " + e.getMessage());
            return comprobante;
        }
    }

    private void extraerDatos(String texto, ComprobanteOCR comprobante) {
        // Normalizar texto para búsqueda
        String textoNormalizado = texto.toLowerCase().replaceAll("\\s+", " ");

        // Detectar si es un comprobante de Nu Bank y usar lógica especializada
        if (esComprobanteNuBank(texto)) {
            System.out.println("🔍 Detectado comprobante de Nu Bank - usando extracción especializada");
            extraerDatosNuBank(texto, comprobante);
        } else {
            // Lógica estándar para otros bancos
            extraerDatosEstandar(texto, comprobante);
        }
    }

    private boolean esComprobanteNuBank(String texto) {
        String textoLower = texto.toLowerCase();
        return textoLower.contains("nu bank") ||
               textoLower.contains("nubank") ||
               (textoLower.contains("cuenta destino") &&
                textoLower.contains("cuenta de origen") &&
                textoLower.contains("clave de rastreo"));
    }

    private void extraerDatosNuBank(String texto, ComprobanteOCR comprobante) {
        String[] lineas = texto.split("\\n");
        boolean enSeccionOrigen = false;

        // Para Nu Bank, el banco emisor es siempre NU
        comprobante.setBancoEmisor("NU");
        System.out.println("✓ Banco emisor establecido como NU");

        // Buscar monto en líneas que contengan símbolos de peso
        BigDecimal monto = extraerMontoNuBank(texto);
        if (monto != null) {
            comprobante.setMontoDetectado(monto);
            System.out.println("✓ Monto Nu Bank extraído: " + monto);
        }

        // Buscar cuenta de origen después del separador
        String cuentaOrigen = extraerCuentaOrigenNuBank(texto);
        if (cuentaOrigen != null) {
            comprobante.setCuentaRemitente(cuentaOrigen);
            System.out.println("✓ Cuenta origen Nu Bank extraída: " + cuentaOrigen);
        }

        // Extraer otros datos con métodos estándar
        String referencia = extraerReferencia(texto);
        if (referencia != null) {
            comprobante.setReferenciaOperacion(referencia);
        }

        LocalDate fecha = extraerFecha(texto);
        if (fecha != null) {
            comprobante.setFechaTransferencia(fecha);
        }

        String beneficiario = extraerBeneficiario(texto);
        if (beneficiario != null) {
            comprobante.setNombreBeneficiario(beneficiario);
        }
    }

    private BigDecimal extraerMontoNuBank(String texto) {
        // Patrones específicos para Nu Bank - buscar montos en formato de peso mexicano
        Pattern[] patronesNu = {
            // Montos con símbolo de peso
            Pattern.compile("\\$\\s*(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?)"),
            // Montos cerca de "monto" o palabras similares
            Pattern.compile("(?i)(?:monto|importe|cantidad)[:\\s]*\\$?\\s*(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?)"),
            // Buscar líneas que contengan solo números con formato de dinero
            Pattern.compile("^\\s*\\$?\\s*(\\d{1,3}(?:,\\d{3})*\\.\\d{2})\\s*$", Pattern.MULTILINE),
            // Patrones más amplios para capturar montos
            Pattern.compile("(\\d{1,3}(?:,\\d{3})*\\.\\d{2})\\s*(?:MXN|pesos?|$)")
        };

        String[] lineas = texto.split("\\n");

        // Buscar en líneas que no contengan información de cuenta destino
        for (String linea : lineas) {
            String lineaLower = linea.toLowerCase();

            // Saltar líneas que parecen ser información de destino
            if (lineaLower.contains("cuenta destino") ||
                lineaLower.contains("beneficiario") ||
                lineaLower.contains("tarjeta de debito")) {
                continue;
            }

            for (Pattern patron : patronesNu) {
                Matcher matcher = patron.matcher(linea);
                if (matcher.find()) {
                    try {
                        String montoStr = matcher.group(1).replace(",", "");
                        BigDecimal monto = new BigDecimal(montoStr);
                        // Validar que el monto sea razonable (mayor a 1 peso)
                        if (monto.compareTo(BigDecimal.ONE) > 0) {
                            System.out.println("💰 Monto Nu Bank encontrado en línea: '" + linea.trim() + "' -> " + monto);
                            return monto;
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
        }

        System.out.println("⚠️ No se pudo extraer monto de Nu Bank");
        return null;
    }

    private String extraerCuentaOrigenNuBank(String texto) {
        String[] lineas = texto.split("\\n");
        boolean encontroSeparador = false;

        // Buscar el separador "cuenta de origen" o similar
        for (int i = 0; i < lineas.length; i++) {
            String lineaLower = lineas[i].toLowerCase();

            // Detectar el separador que indica el inicio de la sección de origen
            if (lineaLower.contains("cuenta de origen") ||
                lineaLower.contains("cuenta origen") ||
                (lineaLower.contains("origen") && lineaLower.length() < 20)) {
                encontroSeparador = true;
                System.out.println("🔍 Separador encontrado en línea: " + lineas[i].trim());

                // Buscar cuenta en las siguientes líneas después del separador
                for (int j = i + 1; j < Math.min(i + 5, lineas.length); j++) {
                    String cuentaEncontrada = buscarCuentaEnLinea(lineas[j]);
                    if (cuentaEncontrada != null) {
                        System.out.println("✓ Cuenta origen encontrada después del separador: " + cuentaEncontrada);
                        return cuentaEncontrada;
                    }
                }
                break;
            }
        }

        // Si no se encontró separador, buscar patrones específicos de Nu Bank
        if (!encontroSeparador) {
            System.out.println("⚠️ No se encontró separador, buscando patrones alternativos...");

            for (String linea : lineas) {
                String lineaLower = linea.toLowerCase();

                // Buscar líneas que contengan "nu" y números de cuenta
                if (lineaLower.contains("nu") || lineaLower.contains("nubank")) {
                    String cuentaEncontrada = buscarCuentaEnLinea(linea);
                    if (cuentaEncontrada != null) {
                        System.out.println("✓ Cuenta origen encontrada en línea Nu Bank: " + cuentaEncontrada);
                        return cuentaEncontrada;
                    }
                }
            }
        }

        System.out.println("❌ No se pudo extraer cuenta de origen de Nu Bank");
        return null;
    }

    private String buscarCuentaEnLinea(String linea) {
        // Patrones para encontrar números de cuenta en una línea específica
        Pattern[] patronesCuenta = {
            Pattern.compile("([0-9]{4}[*\\s]*[0-9]{4})"), // Formato enmascarado típico
            Pattern.compile("([0-9]{4}\\s*[*]+\\s*[0-9]{4})"), // Con asteriscos
            Pattern.compile("([0-9]{16,20})"), // Cuenta completa
            Pattern.compile("([0-9]{10,18})"), // Números largos
            Pattern.compile("([0-9]{4}[\\s*-]+[0-9]{4})") // Con guiones o espacios
        };

        for (Pattern patron : patronesCuenta) {
            Matcher matcher = patron.matcher(linea);
            if (matcher.find()) {
                String cuenta = matcher.group(1).replaceAll("[\\s*-]+", "");
                if (cuenta.length() >= 8) { // Validar longitud mínima
                    return cuenta;
                }
            }
        }
        return null;
    }

    private void extraerDatosEstandar(String texto, ComprobanteOCR comprobante) {
        // Lógica original para otros bancos
        String banco = extraerBanco(texto);
        if (banco != null) {
            comprobante.setBancoEmisor(banco);
        }

        BigDecimal monto = extraerMonto(texto);
        if (monto != null) {
            comprobante.setMontoDetectado(monto);
        }

        String referencia = extraerReferencia(texto);
        if (referencia != null) {
            comprobante.setReferenciaOperacion(referencia);
        }

        LocalDate fecha = extraerFecha(texto);
        if (fecha != null) {
            comprobante.setFechaTransferencia(fecha);
        }

        String cuenta = extraerCuentaRemitente(texto);
        if (cuenta != null) {
            comprobante.setCuentaRemitente(cuenta);
        }

        String beneficiario = extraerBeneficiario(texto);
        if (beneficiario != null) {
            comprobante.setNombreBeneficiario(beneficiario);
        }
    }

    private String extraerBanco(String texto) {
        // CORRECCIÓN: Buscar específicamente el banco EMISOR (origen), no destino
        String[] lineas = texto.split("\\n");

        // Buscar patrones que indiquen banco emisor/origen
        for (String linea : lineas) {
            String lineaNormalizada = linea.toLowerCase();

            // Priorizar líneas que contengan "origen", "emisor", "desde", etc.
            if (lineaNormalizada.contains("origen") ||
                lineaNormalizada.contains("emisor") ||
                lineaNormalizada.contains("desde") ||
                lineaNormalizada.contains("remitente")) {

                // Buscar banco en esta línea prioritaria
                for (Map.Entry<String, Pattern> entry : patronesBancos.entrySet()) {
                    Matcher matcher = entry.getValue().matcher(linea);
                    if (matcher.find()) {
                        System.out.println("Banco emisor encontrado en línea de origen: " + entry.getKey());
                        return entry.getKey();
                    }
                }
            }
        }

        // Si no se encuentra en líneas específicas, buscar en todo el texto
        // pero evitar líneas que contengan "destino", "beneficiario", "hacia"
        for (String linea : lineas) {
            String lineaNormalizada = linea.toLowerCase();

            // Saltar líneas que claramente son del banco destino
            if (lineaNormalizada.contains("destino") ||
                lineaNormalizada.contains("beneficiario") ||
                lineaNormalizada.contains("hacia") ||
                lineaNormalizada.contains("recibe")) {
                continue;
            }

            // Buscar banco en líneas generales
            for (Map.Entry<String, Pattern> entry : patronesBancos.entrySet()) {
                Matcher matcher = entry.getValue().matcher(linea);
                if (matcher.find()) {
                    System.out.println("Banco emisor encontrado en línea general: " + entry.getKey());
                    return entry.getKey();
                }
            }
        }

        System.out.println("No se pudo identificar banco emisor");
        return null;
    }

    private BigDecimal extraerMonto(String texto) {
        // Patrones para montos en diferentes formatos
        Pattern[] patronesMontos = {
            Pattern.compile("\\$\\s*(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?)"),
            Pattern.compile("(?i)importe[:\\s]*\\$?\\s*(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?)"),
            Pattern.compile("(?i)monto[:\\s]*\\$?\\s*(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?)"),
            Pattern.compile("(?i)cantidad[:\\s]*\\$?\\s*(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?)"),
            Pattern.compile("(\\d{1,3}(?:,\\d{3})*\\.\\d{2})\\s*(?:MXN|pesos?)")
        };

        for (Pattern patron : patronesMontos) {
            Matcher matcher = patron.matcher(texto);
            if (matcher.find()) {
                try {
                    String montoStr = matcher.group(1).replace(",", "");
                    return new BigDecimal(montoStr);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return null;
    }

    private String extraerReferencia(String texto) {
        // Patrones para referencias bancarias
        Pattern[] patronesReferencia = {
            Pattern.compile("(?i)referencia[:\\s]*([A-Z0-9]{6,20})"),
            Pattern.compile("(?i)ref[:\\s]*([A-Z0-9]{6,20})"),
            Pattern.compile("(?i)clave[:\\s]*([A-Z0-9]{6,20})"),
            Pattern.compile("(?i)folio[:\\s]*([A-Z0-9]{6,20})"),
            Pattern.compile("([A-Z0-9]{10,18})") // Patrón genérico para códigos largos
        };

        for (Pattern patron : patronesReferencia) {
            Matcher matcher = patron.matcher(texto);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private LocalDate extraerFecha(String texto) {
        // Patrones para fechas en diferentes formatos
        Pattern[] patronesFechas = {
            Pattern.compile("(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})"),
            Pattern.compile("(\\d{4})[/-](\\d{1,2})[/-](\\d{1,2})"),
            Pattern.compile("(?i)(\\d{1,2})\\s+de\\s+(\\w+)\\s+de\\s+(\\d{4})"),
            Pattern.compile("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4})")
        };

        for (Pattern patron : patronesFechas) {
            Matcher matcher = patron.matcher(texto);
            if (matcher.find()) {
                try {
                    if (patron.pattern().contains("de")) {
                        // Formato con nombre del mes
                        int dia = Integer.parseInt(matcher.group(1));
                        String mesNombre = matcher.group(2).toLowerCase();
                        int año = Integer.parseInt(matcher.group(3));
                        int mes = convertirMesNombreANumero(mesNombre);
                        if (mes > 0) {
                            return LocalDate.of(año, mes, dia);
                        }
                    } else {
                        // Formato numérico
                        if (matcher.group(3).length() == 4) { // año al final
                            int dia = Integer.parseInt(matcher.group(1));
                            int mes = Integer.parseInt(matcher.group(2));
                            int año = Integer.parseInt(matcher.group(3));
                            return LocalDate.of(año, mes, dia);
                        } else { // año al principio
                            int año = Integer.parseInt(matcher.group(1));
                            int mes = Integer.parseInt(matcher.group(2));
                            int dia = Integer.parseInt(matcher.group(3));
                            return LocalDate.of(año, mes, dia);
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return null;
    }

    private int convertirMesNombreANumero(String mesNombre) {
        String[] meses = {"enero", "febrero", "marzo", "abril", "mayo", "junio",
                         "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};

        for (int i = 0; i < meses.length; i++) {
            if (meses[i].startsWith(mesNombre)) {
                return i + 1;
            }
        }
        return 0;
    }

    private String extraerCuentaRemitente(String texto) {
        // MEJORADO: Patrones más amplios para números de cuenta remitente
        String[] lineas = texto.split("\\n");

        // Buscar en líneas que contengan palabras clave de origen/remitente
        for (String linea : lineas) {
            String lineaNormalizada = linea.toLowerCase();

            if (lineaNormalizada.contains("origen") ||
                lineaNormalizada.contains("remitente") ||
                lineaNormalizada.contains("desde") ||
                lineaNormalizada.contains("emisor")) {

                // Buscar patrones de cuenta en esta línea prioritaria
                Pattern[] patronesCuentaPrioritarios = {
                    Pattern.compile("([0-9]{4}[*\\s]+[0-9]{4})"), // Formato enmascarado
                    Pattern.compile("([0-9]{16,20})"), // Cuenta completa
                    Pattern.compile("([0-9]{4}\\s*[*]+\\s*[0-9]{4})"), // Con espacios
                    Pattern.compile("([0-9]{10,18})") // Números largos
                };

                for (Pattern patron : patronesCuentaPrioritarios) {
                    Matcher matcher = patron.matcher(linea);
                    if (matcher.find()) {
                        String cuenta = matcher.group(1).replaceAll("\\s+", "");
                        System.out.println("Cuenta remitente encontrada en línea de origen: " + cuenta);
                        return cuenta;
                    }
                }
            }
        }

        // Buscar en todo el texto con patrones generales
        Pattern[] patronesCuenta = {
            Pattern.compile("(?i)cuenta\\s*origen[:\\s]*([0-9]{4}[*\\s]+[0-9]{4})"),
            Pattern.compile("(?i)cuenta\\s*remitente[:\\s]*([0-9]{4}[*\\s]+[0-9]{4})"),
            Pattern.compile("(?i)tarjeta[:\\s]*([0-9]{4}[*\\s]+[0-9]{4})"),
            Pattern.compile("([0-9]{4}[*]+[0-9]{4})"), // Patrón simple enmascarado
            Pattern.compile("([0-9]{4}\\s+[*]+\\s+[0-9]{4})") // Con espacios
        };

        for (Pattern patron : patronesCuenta) {
            Matcher matcher = patron.matcher(texto);
            if (matcher.find()) {
                String cuenta = matcher.group(1).replaceAll("\\s+", "");
                System.out.println("Cuenta remitente encontrada con patrón general: " + cuenta);
                return cuenta;
            }
        }

        System.out.println("No se pudo extraer cuenta remitente");
        return null;
    }

    private String extraerBeneficiario(String texto) {
        // Patrones para nombres de beneficiarios
        Pattern[] patronesBeneficiario = {
            Pattern.compile("(?i)beneficiario[:\\s]*([A-Z\\s]{10,50})"),
            Pattern.compile("(?i)a favor de[:\\s]*([A-Z\\s]{10,50})"),
            Pattern.compile("(?i)destinatario[:\\s]*([A-Z\\s]{10,50})")
        };

        for (Pattern patron : patronesBeneficiario) {
            Matcher matcher = patron.matcher(texto);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }
        return null;
    }

    public boolean verificarDisponibilidad() {
        try {
            // Verificar que Tesseract esté disponible
            tesseract.doOCR(new java.awt.image.BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
