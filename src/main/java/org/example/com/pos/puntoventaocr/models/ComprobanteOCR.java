package com.pos.puntoventaocr.models;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class ComprobanteOCR {
    private int idComprobante;
    private Venta venta;
    private String imagenOriginal;
    private String imagenProcesada;
    private String bancoEmisor;
    private String cuentaRemitente;
    private BigDecimal montoDetectado;
    private LocalDateTime fechaTransferencia;
    private String referenciaOperacion;
    private String nombreBeneficiario;
    private EstadoOCR estadoValidacion;
    private String datosExtraidos; // JSON
    private LocalDateTime fechaProcesamiento;
    private Usuario usuarioValidador;
    private String observaciones;

    // Constructor vacío
    public ComprobanteOCR() {
        this.estadoValidacion = EstadoOCR.PENDIENTE;
        this.fechaProcesamiento = LocalDateTime.now();
        this.montoDetectado = BigDecimal.ZERO;
    }

    // Constructor con parámetros
    public ComprobanteOCR(Venta venta, String imagenOriginal) {
        this();
        this.venta = venta;
        this.imagenOriginal = imagenOriginal;
    }

    // Métodos de negocio
    public boolean cargarImagen(String rutaImagen) {
        try {
            this.imagenOriginal = rutaImagen;
            return true;
        } catch (Exception e) {
            System.err.println("Error al cargar imagen: " + e.getMessage());
            return false;
        }
    }

    public boolean validarDatos() {
        StringBuilder errores = new StringBuilder();

        if (bancoEmisor == null || bancoEmisor.trim().isEmpty()) {
            errores.append("Banco emisor requerido. ");
        }

        if (montoDetectado == null || montoDetectado.compareTo(BigDecimal.ZERO) <= 0) {
            errores.append("Monto inválido. ");
        }

        if (referenciaOperacion == null || referenciaOperacion.trim().isEmpty()) {
            errores.append("Referencia de operación requerida. ");
        }

        return errores.length() == 0;
    }

    public boolean validarContraVenta() {
        if (venta == null) {
            return false;
        }

        // Validar que el monto coincida
        if (montoDetectado.compareTo(venta.getTotal()) != 0) {
            return false;
        }

        // Validar que la transferencia sea del mismo día
        if (fechaTransferencia != null && venta.getFechaVenta() != null) {
            return fechaTransferencia.toLocalDate().equals(venta.getFechaVenta().toLocalDate());
        }

        return true;
    }

    public void aprobarComprobante(Usuario usuario) {
        this.estadoValidacion = EstadoOCR.VALIDADO;
        this.usuarioValidador = usuario;

        if (venta != null) {
            venta.setReferenciaTransferencia(this.referenciaOperacion);
        }
    }

    public void rechazarComprobante(Usuario usuario, String motivo) {
        this.estadoValidacion = EstadoOCR.RECHAZADO;
        this.usuarioValidador = usuario;
        this.observaciones = motivo;
    }

    public boolean verificarReferenciaDuplicada() {
        // Esta lógica debería implementarse en el DAO
        // Por ahora retornamos false como placeholder
        return false;
    }

    public boolean esPendiente() {
        return EstadoOCR.PENDIENTE.equals(estadoValidacion);
    }

    public boolean estaValidado() {
        return EstadoOCR.VALIDADO.equals(estadoValidacion);
    }

    public boolean estaRechazado() {
        return EstadoOCR.RECHAZADO.equals(estadoValidacion);
    }

    // Getters y Setters
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

    public LocalDateTime getFechaTransferencia() {
        return fechaTransferencia;
    }

    public void setFechaTransferencia(LocalDateTime fechaTransferencia) {
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

    public EstadoOCR getEstadoValidacion() {
        return estadoValidacion;
    }

    public void setEstadoValidacion(EstadoOCR estadoValidacion) {
        this.estadoValidacion = estadoValidacion;
    }

    public String getDatosExtraidos() {
        return datosExtraidos;
    }

    public void setDatosExtraidos(String datosExtraidos) {
        this.datosExtraidos = datosExtraidos;
    }

    public LocalDateTime getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    public void setFechaProcesamiento(LocalDateTime fechaProcesamiento) {
        this.fechaProcesamiento = fechaProcesamiento;
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

    @Override
    public String toString() {
        return "ComprobanteOCR{" +
                "idComprobante=" + idComprobante +
                ", referenciaOperacion='" + referenciaOperacion + '\'' +
                ", montoDetectado=" + montoDetectado +
                ", estadoValidacion=" + estadoValidacion +
                '}';
    }

    // Enumeración EstadoOCR
    public enum EstadoOCR {
        VALIDADO,
        PENDIENTE,
        RECHAZADO,
        ERROR_PROCESAMIENTO
    }
}