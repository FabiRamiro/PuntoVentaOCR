package com.pos.puntoventaocr.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {
    private int idVenta;
    private String numeroVenta;
    private LocalDateTime fechaVenta;
    private Usuario usuario;
    private String metodoPago;
    private BigDecimal subtotal;
    private BigDecimal impuestos;
    private BigDecimal total;
    private String estado;
    private String observaciones;
    private LocalDateTime fechaAnulacion;
    private String motivoAnulacion;
    private Integer anuladoPor;
    private List<DetalleVenta> detalles;
    private String referenciaTransferencia;

    // Constructor vacío
    public Venta() {
        this.detalles = new ArrayList<>();
        this.fechaVenta = LocalDateTime.now();
        this.estado = "COMPLETADA";
        this.subtotal = BigDecimal.ZERO;
        this.impuestos = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.numeroVenta = generarNumeroVenta();
    }

    // Constructor con parámetros
    public Venta(Usuario usuario, String metodoPago) {
        this();
        this.usuario = usuario;
        this.metodoPago = metodoPago;
    }

    // Métodos de negocio
    private String generarNumeroVenta() {
        return "V" + System.currentTimeMillis();
    }

    public void agregarDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
        calcularTotales();
    }

    public void eliminarDetalle(DetalleVenta detalle) {
        detalles.remove(detalle);
        calcularTotales();
    }

    public void calcularTotales() {
        subtotal = BigDecimal.ZERO;

        for (DetalleVenta detalle : detalles) {
            subtotal = subtotal.add(detalle.getSubtotal());
        }

        // Calcular IVA (16%)
        impuestos = subtotal.multiply(BigDecimal.valueOf(0.16));
        total = subtotal.add(impuestos);
    }

    public void anular(String motivo, Integer usuarioAnula) {
        this.estado = "ANULADA";
        this.motivoAnulacion = motivo;
        this.fechaAnulacion = LocalDateTime.now();
        this.anuladoPor = usuarioAnula;
    }

    public boolean puedeAnularse() {
        return "COMPLETADA".equals(estado) &&
                fechaVenta.isAfter(LocalDateTime.now().minusDays(1)); // Solo el mismo día
    }

    public boolean esTransferencia() {
        return "TRANSFERENCIA".equals(metodoPago);
    }

    public boolean tieneTransferenciaValidada() {
        return esTransferencia() && referenciaTransferencia != null && !referenciaTransferencia.isEmpty();
    }

    public int getCantidadArticulos() {
        return detalles.stream().mapToInt(DetalleVenta::getCantidad).sum();
    }

    // Getters y Setters
    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public String getNumeroVenta() {
        return numeroVenta;
    }

    public void setNumeroVenta(String numeroVenta) {
        this.numeroVenta = numeroVenta;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(BigDecimal impuestos) {
        this.impuestos = impuestos;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(LocalDateTime fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }

    public Integer getAnuladoPor() {
        return anuladoPor;
    }

    public void setAnuladoPor(Integer anuladoPor) {
        this.anuladoPor = anuladoPor;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

    public String getReferenciaTransferencia() {
        return referenciaTransferencia;
    }

    public void setReferenciaTransferencia(String referenciaTransferencia) {
        this.referenciaTransferencia = referenciaTransferencia;
    }

    @Override
    public String toString() {
        return "Venta #" + numeroVenta + " - $" + total + " (" + estado + ")";
    }
}