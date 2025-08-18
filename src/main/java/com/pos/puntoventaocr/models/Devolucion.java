package com.pos.puntoventaocr.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Devolucion {
    private int idDevolucion;
    private Venta ventaOriginal;
    private String numeroDevolucion;
    private LocalDateTime fechaDevolucion;
    private String motivo;
    private BigDecimal montoTotal;
    private String estado; // PENDIENTE, APROBADA, RECHAZADA
    private Usuario autorizadoPor;
    private Usuario procesadoPor;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaProcesamiento;

    // Constructores
    public Devolucion() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }

    public Devolucion(Venta ventaOriginal, String motivo, BigDecimal montoTotal, Usuario procesadoPor) {
        this();
        this.ventaOriginal = ventaOriginal;
        this.motivo = motivo;
        this.montoTotal = montoTotal;
        this.procesadoPor = procesadoPor;
        this.numeroDevolucion = generarNumeroDevolucion();
    }

    private String generarNumeroDevolucion() {
        return "DEV-" + System.currentTimeMillis();
    }

    // Getters y Setters
    public int getIdDevolucion() { return idDevolucion; }
    public void setIdDevolucion(int idDevolucion) { this.idDevolucion = idDevolucion; }

    public Venta getVentaOriginal() { return ventaOriginal; }
    public void setVentaOriginal(Venta ventaOriginal) { this.ventaOriginal = ventaOriginal; }

    public String getNumeroDevolucion() { return numeroDevolucion; }
    public void setNumeroDevolucion(String numeroDevolucion) { this.numeroDevolucion = numeroDevolucion; }

    public LocalDateTime getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDateTime fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Usuario getAutorizadoPor() { return autorizadoPor; }
    public void setAutorizadoPor(Usuario autorizadoPor) { this.autorizadoPor = autorizadoPor; }

    public Usuario getProcesadoPor() { return procesadoPor; }
    public void setProcesadoPor(Usuario procesadoPor) { this.procesadoPor = procesadoPor; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaProcesamiento() { return fechaProcesamiento; }
    public void setFechaProcesamiento(LocalDateTime fechaProcesamiento) { this.fechaProcesamiento = fechaProcesamiento; }

    // Métodos de negocio
    public void aprobar(Usuario autorizadoPor, String observaciones) {
        this.estado = "APROBADA";
        this.autorizadoPor = autorizadoPor;
        this.observaciones = observaciones;
        this.fechaProcesamiento = LocalDateTime.now();
        this.fechaDevolucion = LocalDateTime.now();
    }

    public void rechazar(Usuario autorizadoPor, String observaciones) {
        this.estado = "RECHAZADA";
        this.autorizadoPor = autorizadoPor;
        this.observaciones = observaciones;
        this.fechaProcesamiento = LocalDateTime.now();
    }

    public boolean puedeSerProcesada() {
        return "PENDIENTE".equals(this.estado);
    }

    @Override
    public String toString() {
        return "Devolucion{" +
               "numeroDevolucion='" + numeroDevolucion + '\'' +
               ", estado='" + estado + '\'' +
               ", montoTotal=" + montoTotal +
               '}';
    }
}
