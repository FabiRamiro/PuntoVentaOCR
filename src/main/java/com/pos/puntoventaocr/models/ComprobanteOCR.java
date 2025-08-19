package com.pos.puntoventaocr.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ComprobanteOCR {
    private int idComprobante;
    private Venta venta;
    private String imagenOriginal;
    private String imagenProcesada;
    private String bancoEmisor;
    private String cuentaRemitente;
    private BigDecimal montoDetectado;
    private LocalDate fechaTransferencia;
    private String referenciaOperacion;
    private String nombreBeneficiario;
    private String estadoValidacion;
    private String datosExtraidos;
    private Usuario usuarioValidador;
    private String observaciones;
    private LocalDateTime fechaProcesamiento;
    private LocalDateTime fechaValidacion;

    // Constructor vacío
    public ComprobanteOCR() {
        this.fechaProcesamiento = LocalDateTime.now();
        this.estadoValidacion = "PENDIENTE";
        this.montoDetectado = BigDecimal.ZERO;
    }

    // Constructor con parámetros básicos
    public ComprobanteOCR(String imagenOriginal, String bancoEmisor, BigDecimal montoDetectado) {
        this();
        this.imagenOriginal = imagenOriginal;
        this.bancoEmisor = bancoEmisor;
        this.montoDetectado = montoDetectado;
    }

    // Métodos de negocio
    public boolean estaPendiente() {
        return "PENDIENTE".equals(this.estadoValidacion);
    }

    public boolean estaValidado() {
        return "VALIDADO".equals(this.estadoValidacion);
    }

    public boolean estaRechazado() {
        return "RECHAZADO".equals(this.estadoValidacion);
    }

    public void validar(Usuario usuario, String observaciones) {
        this.estadoValidacion = "VALIDADO";
        this.usuarioValidador = usuario;
        this.observaciones = observaciones;
        this.fechaValidacion = LocalDateTime.now();
    }

    public void rechazar(Usuario usuario, String motivo) {
        this.estadoValidacion = "RECHAZADO";
        this.usuarioValidador = usuario;
        this.observaciones = motivo;
        this.fechaValidacion = LocalDateTime.now();
    }

    // ✅ MÉTODOS DE VALIDACIÓN CORREGIDOS
    public boolean coincideMontoConVenta() {
        if (this.venta == null || this.montoDetectado == null) {
            return false;
        }

        // Validación MEJORADA del monto para manejar diferencias de decimales
        BigDecimal montoVenta = this.venta.getTotal();
        BigDecimal montoDetectado = this.montoDetectado;

        // Normalizar ambos montos a 2 decimales para comparación
        montoVenta = montoVenta.setScale(2, BigDecimal.ROUND_HALF_UP);
        montoDetectado = montoDetectado.setScale(2, BigDecimal.ROUND_HALF_UP);

        // Comparar con una tolerancia muy pequeña para diferencias de redondeo
        BigDecimal diferencia = montoDetectado.subtract(montoVenta).abs();
        boolean montoCoincide = diferencia.compareTo(new BigDecimal("0.01")) <= 0;

        // Log para debugging
        System.out.println("Validación monto - Venta: " + montoVenta + ", Detectado: " + montoDetectado +
                          ", Diferencia: " + diferencia + ", Coincide: " + montoCoincide);

        return montoCoincide;
    }

    public boolean esFechaValida() {
        // CORRECCIÓN: La fecha ahora es OPCIONAL
        if (this.venta == null) {
            return false;
        }

        // Si no se detectó fecha en el comprobante, se considera válido
        if (this.fechaTransferencia == null) {
            System.out.println("Fecha no detectada en comprobante - Se considera válida (fecha opcional)");
            return true; // FECHA OPCIONAL
        }

        // Si hay fecha, validar que sea coherente con la venta
        LocalDate fechaVenta = this.venta.getFecha().toLocalDate();
        LocalDate fechaTransferencia = this.fechaTransferencia;

        // La transferencia debe ser el mismo día de la venta o máximo 1 día después
        boolean fechaValida = !fechaTransferencia.isBefore(fechaVenta) &&
                             !fechaTransferencia.isAfter(fechaVenta.plusDays(1));

        System.out.println("Validación fecha - Venta: " + fechaVenta + ", Transferencia: " + fechaTransferencia +
                          ", Válida: " + fechaValida);

        return fechaValida;
    }

    /**
     * Validación específica para verificar que el comprobante corresponde exactamente a la venta
     * @return true si el comprobante es específico para esta venta
     */
    public boolean esEspecificoParaVenta() {
        if (this.venta == null) {
            return false;
        }

        // Verificaciones específicas:
        // 1. Monto debe coincidir exactamente
        boolean montoEspecifico = coincideMontoConVenta();

        // 2. Fecha debe estar en el rango correcto
        boolean fechaEspecifica = esFechaValida();

        // 3. La venta debe ser por transferencia
        boolean esTransferencia = "TRANSFERENCIA".equals(this.venta.getMetodoPago());

        // 4. La venta debe estar completada
        boolean ventaCompletada = "COMPLETADA".equals(this.venta.getEstado());

        return montoEspecifico && fechaEspecifica && esTransferencia && ventaCompletada;
    }

    /**
     * Obtiene un puntaje de coincidencia con la venta (0-100)
     * @return puntaje de 0 a 100 indicando qué tan bien coincide el comprobante con la venta
     */
    public int getPuntajeCoincidencia() {
        if (this.venta == null) {
            return 0;
        }

        int puntaje = 0;

        // Monto (40 puntos máximo)
        if (this.montoDetectado != null) {
            BigDecimal diferencia = this.montoDetectado.subtract(this.venta.getTotal()).abs();
            if (diferencia.compareTo(BigDecimal.ZERO) == 0) {
                puntaje += 40; // Coincidencia exacta
            } else if (diferencia.compareTo(new BigDecimal("0.01")) <= 0) {
                puntaje += 35; // Diferencia mínima de redondeo
            } else if (diferencia.compareTo(new BigDecimal("1.00")) <= 0) {
                puntaje += 20; // Diferencia pequeña
            } else if (diferencia.compareTo(new BigDecimal("10.00")) <= 0) {
                puntaje += 10; // Diferencia moderada
            }
        }

        // Fecha (30 puntos máximo)
        if (this.fechaTransferencia != null) {
            LocalDate fechaVenta = this.venta.getFecha().toLocalDate();
            if (this.fechaTransferencia.equals(fechaVenta)) {
                puntaje += 30; // Mismo día
            } else if (this.fechaTransferencia.equals(fechaVenta.plusDays(1))) {
                puntaje += 25; // Día siguiente
            } else if (!this.fechaTransferencia.isBefore(fechaVenta.minusDays(1)) &&
                      !this.fechaTransferencia.isAfter(fechaVenta.plusDays(2))) {
                puntaje += 15; // Dentro de rango razonable
            }
        }

        // Método de pago (20 puntos máximo)
        if ("TRANSFERENCIA".equals(this.venta.getMetodoPago())) {
            puntaje += 20;
        }

        // Referencia válida (10 puntos máximo)
        if (referenciaEsUnica()) {
            puntaje += 10;
        }

        return Math.min(puntaje, 100);
    }

    /**
     * Verifica si la referencia de operación es única y válida
     * @return true si la referencia cumple con los criterios de validez
     */
    public boolean referenciaEsUnica() {
        return this.referenciaOperacion != null &&
               !this.referenciaOperacion.trim().isEmpty() &&
               this.referenciaOperacion.length() >= 6; // Mínimo 6 caracteres para ser válida
    }

    // Método para marcar errores de procesamiento OCR
    public void marcarErrorProcesamiento(String mensajeError) {
        this.estadoValidacion = "ERROR";
        this.observaciones = mensajeError;
        this.fechaValidacion = LocalDateTime.now();
    }

    // Método para verificar si hay errores de procesamiento
    public boolean tieneErrorProcesamiento() {
        return "ERROR".equals(this.estadoValidacion) ||
               (this.observaciones != null && this.observaciones.contains("ERROR"));
    }

    // =================== GETTERS Y SETTERS ===================
    
    public int getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(int idComprobante) {
        this.idComprobante = idComprobante;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public String getImagenOriginal() {
        return imagenOriginal;
    }

    public void setImagenOriginal(String imagenOriginal) {
        this.imagenOriginal = imagenOriginal;
    }

    public String getImagenProcesada() {
        return imagenProcesada;
    }

    public void setImagenProcesada(String imagenProcesada) {
        this.imagenProcesada = imagenProcesada;
    }

    public String getBancoEmisor() {
        return bancoEmisor;
    }

    public void setBancoEmisor(String bancoEmisor) {
        this.bancoEmisor = bancoEmisor;
    }

    public String getCuentaRemitente() {
        return cuentaRemitente;
    }

    public void setCuentaRemitente(String cuentaRemitente) {
        this.cuentaRemitente = cuentaRemitente;
    }

    public BigDecimal getMontoDetectado() {
        return montoDetectado;
    }

    public void setMontoDetectado(BigDecimal montoDetectado) {
        this.montoDetectado = montoDetectado;
    }

    public LocalDate getFechaTransferencia() {
        return fechaTransferencia;
    }

    public void setFechaTransferencia(LocalDate fechaTransferencia) {
        this.fechaTransferencia = fechaTransferencia;
    }

    public String getReferenciaOperacion() {
        return referenciaOperacion;
    }

    public void setReferenciaOperacion(String referenciaOperacion) {
        this.referenciaOperacion = referenciaOperacion;
    }

    public String getNombreBeneficiario() {
        return nombreBeneficiario;
    }

    public void setNombreBeneficiario(String nombreBeneficiario) {
        this.nombreBeneficiario = nombreBeneficiario;
    }

    public String getEstadoValidacion() {
        return estadoValidacion;
    }

    public void setEstadoValidacion(String estadoValidacion) {
        this.estadoValidacion = estadoValidacion;
    }

    public String getDatosExtraidos() {
        return datosExtraidos;
    }

    public void setDatosExtraidos(String datosExtraidos) {
        if (datosExtraidos == null || datosExtraidos.trim().isEmpty()) {
            this.datosExtraidos = "{}";
            return;
        }

        try {
            // Sanitizar el texto para convertirlo en JSON válido
            this.datosExtraidos = sanitizarTextoParaJSON(datosExtraidos);
        } catch (Exception e) {
            System.err.println("Error sanitizando datos extraídos: " + e.getMessage());
            // En caso de error, guardar como objeto JSON simple
            this.datosExtraidos = "{\"texto_raw\": \"Error procesando texto\", \"error\": \"" +
                                 escapeJSON(e.getMessage()) + "\"}";
        }
    }

    private String sanitizarTextoParaJSON(String textoRaw) {
        // Limpiar el texto de caracteres problemáticos
        String textoLimpio = textoRaw
            .replace("\\", "\\\\")  // Escapar backslashes
            .replace("\"", "\\\"")  // Escapar comillas dobles
            .replace("\n", "\\n")   // Escapar saltos de línea
            .replace("\r", "\\r")   // Escapar retornos de carro
            .replace("\t", "\\t")   // Escapar tabs
            .replace("\b", "\\b")   // Escapar backspaces
            .replace("\f", "\\f");  // Escapar form feeds

        // Remover caracteres de control que no son válidos en JSON
        textoLimpio = textoLimpio.replaceAll("[\u0000-\u001F\u007F-\u009F]", "");

        // Crear un objeto JSON simple con el texto sanitizado
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"texto_extraido\": \"").append(textoLimpio).append("\",");
        jsonBuilder.append("\"fecha_procesamiento\": \"").append(LocalDateTime.now().toString()).append("\",");
        jsonBuilder.append("\"longitud_texto\": ").append(textoRaw.length()).append(",");

        // Agregar información adicional si está disponible
        if (this.bancoEmisor != null) {
            jsonBuilder.append("\"banco_detectado\": \"").append(escapeJSON(this.bancoEmisor)).append("\",");
        }
        if (this.montoDetectado != null) {
            jsonBuilder.append("\"monto_detectado\": ").append(this.montoDetectado).append(",");
        }
        if (this.referenciaOperacion != null) {
            jsonBuilder.append("\"referencia_detectada\": \"").append(escapeJSON(this.referenciaOperacion)).append("\",");
        }

        // Remover la última coma y cerrar el JSON
        String json = jsonBuilder.toString();
        if (json.endsWith(",")) {
            json = json.substring(0, json.length() - 1);
        }
        json += "}";

        return json;
    }

    private String escapeJSON(String value) {
        if (value == null) return "";
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    public Usuario getUsuarioValidador() {
        return usuarioValidador;
    }

    public void setUsuarioValidador(Usuario usuarioValidador) {
        this.usuarioValidador = usuarioValidador;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    public void setFechaProcesamiento(LocalDateTime fechaProcesamiento) {
        this.fechaProcesamiento = fechaProcesamiento;
    }

    public LocalDateTime getFechaValidacion() {
        return fechaValidacion;
    }

    public void setFechaValidacion(LocalDateTime fechaValidacion) {
        this.fechaValidacion = fechaValidacion;
    }

    // Métodos adicionales que necesitan los controladores
    public String getEstado() {
        return estadoValidacion;
    }

    public void setEstado(String estado) {
        this.estadoValidacion = estado;
    }

    @Override
    public String toString() {
        return "ComprobanteOCR{" +
                "idComprobante=" + idComprobante +
                ", bancoEmisor='" + bancoEmisor + '\'' +
                ", montoDetectado=" + montoDetectado +
                ", fechaTransferencia=" + fechaTransferencia +
                ", referenciaOperacion='" + referenciaOperacion + '\'' +
                ", estadoValidacion='" + estadoValidacion + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ComprobanteOCR that = (ComprobanteOCR) obj;
        return idComprobante == that.idComprobante;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idComprobante);
    }
}
