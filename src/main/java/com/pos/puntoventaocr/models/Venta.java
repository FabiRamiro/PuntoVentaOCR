package com.pos.puntoventaocr.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {
    private int idVenta;
    private String numeroVenta;
    private LocalDateTime fecha;
    private Usuario usuario;
    private Cliente cliente;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;
    private String metodoPago;
    private String estado;
    private String motivoAnulacion;
    private List<DetalleVenta> detalles;
    private LocalDateTime fechaCreacion;
    private boolean comprobanteTransferenciaSubido;

    // Constructor vacío
    public Venta() {
        this.detalles = new ArrayList<>();
        this.fecha = LocalDateTime.now();
        this.fechaCreacion = LocalDateTime.now();
        this.estado = "COMPLETADA";
        this.subtotal = BigDecimal.ZERO;
        this.iva = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.comprobanteTransferenciaSubido = false;
    }

    // Constructor con parámetros básicos
    public Venta(String numeroVenta, Usuario usuario) {
        this();
        this.numeroVenta = numeroVenta;
        this.usuario = usuario;
    }

    // Constructor completo
    public Venta(String numeroVenta, Usuario usuario, Cliente cliente, String metodoPago) {
        this();
        this.numeroVenta = numeroVenta;
        this.usuario = usuario;
        this.cliente = cliente;
        this.metodoPago = metodoPago;
    }

    // Métodos de negocio
    public void agregarDetalle(DetalleVenta detalle) {
        this.detalles.add(detalle);
        calcularTotales();
    }

    public void removerDetalle(DetalleVenta detalle) {
        this.detalles.remove(detalle);
        calcularTotales();
    }

    public void calcularTotales() {
        this.subtotal = BigDecimal.ZERO;

        for (DetalleVenta detalle : detalles) {
            this.subtotal = this.subtotal.add(detalle.getSubtotal());
        }

        // Calcular IVA (16%)
        this.iva = this.subtotal.multiply(new BigDecimal("0.16"));

        // Calcular total
        this.total = this.subtotal.add(this.iva);
    }

    public boolean puedeAnularse() {
        return "COMPLETADA".equals(this.estado);
    }

    public void anular(String motivo) {
        if (puedeAnularse()) {
            this.estado = "ANULADA";
            this.motivoAnulacion = motivo;
        }
    }

    public int getCantidadArticulos() {
        return detalles.stream().mapToInt(detalle -> detalle.getCantidad().intValue()).sum();
    }

    public String generarResumenVenta() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Venta: ").append(numeroVenta).append("\n");
        resumen.append("Fecha: ").append(fecha.toString()).append("\n");
        resumen.append("Usuario: ").append(usuario.getNombreCompleto()).append("\n");
        if (cliente != null) {
            resumen.append("Cliente: ").append(cliente.getNombreCompleto()).append("\n");
        }
        resumen.append("Método de pago: ").append(metodoPago).append("\n");
        resumen.append("Productos: ").append(detalles.size()).append("\n");
        resumen.append("Total: $").append(total).append("\n");
        resumen.append("Estado: ").append(estado);
        return resumen.toString();
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

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
        calcularTotales();
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isComprobanteTransferenciaSubido() {
        return comprobanteTransferenciaSubido;
    }

    public void setComprobanteTransferenciaSubido(boolean comprobanteTransferenciaSubido) {
        this.comprobanteTransferenciaSubido = comprobanteTransferenciaSubido;
    }

    // Métodos adicionales que necesitan los controladores
    public String getFechaVenta() {
        return fecha != null ? fecha.toLocalDate().toString() : "";
    }

    public String getNombreUsuario() {
        return usuario != null ? usuario.getNombreCompleto() : "";
    }

    public String getNombreCliente() {
        return cliente != null ? cliente.getNombre() : "Cliente General";
    }

    public double getTotalDouble() {
        return total != null ? total.doubleValue() : 0.0;
    }

    public double getSubtotalDouble() {
        return subtotal != null ? subtotal.doubleValue() : 0.0;
    }

    public double getIvaDouble() {
        return iva != null ? iva.doubleValue() : 0.0;
    }

    @Override
    public String toString() {
        return "Venta{" +
                "numeroVenta='" + numeroVenta + '\'' +
                ", fecha=" + fecha +
                ", total=" + total +
                ", estado='" + estado + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Venta venta = (Venta) obj;
        return idVenta == venta.idVenta;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idVenta);
    }
}
