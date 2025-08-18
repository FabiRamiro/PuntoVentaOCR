package com.pos.puntoventaocr.models;

import java.math.BigDecimal;

public class DetalleVenta {
    private int idDetalle;
    private Venta venta;
    private Producto producto;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private BigDecimal descuento;

    // Constructor vacío
    public DetalleVenta() {
        this.cantidad = BigDecimal.ONE;
        this.descuento = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }

    // Constructor con parámetros básicos
    public DetalleVenta(Producto producto, BigDecimal cantidad) {
        this();
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecioVenta();
        calcularSubtotal();
    }

    // Constructor completo
    public DetalleVenta(Venta venta, Producto producto, BigDecimal cantidad, BigDecimal precioUnitario) {
        this();
        this.venta = venta;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }

    // Métodos de negocio
    public void calcularSubtotal() {
        if (cantidad != null && precioUnitario != null) {
            BigDecimal total = cantidad.multiply(precioUnitario);
            this.subtotal = total.subtract(descuento);
        }
    }

    public void aplicarDescuento(BigDecimal descuento) {
        this.descuento = descuento != null ? descuento : BigDecimal.ZERO;
        calcularSubtotal();
    }

    public BigDecimal calcularTotalSinDescuento() {
        if (cantidad != null && precioUnitario != null) {
            return cantidad.multiply(precioUnitario);
        }
        return BigDecimal.ZERO;
    }

    public boolean tieneDescuento() {
        return descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0;
    }

    // Getters y Setters
    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
        if (producto != null && this.precioUnitario == null) {
            this.precioUnitario = producto.getPrecioVenta();
        }
        calcularSubtotal();
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
        calcularSubtotal();
    }

    @Override
    public String toString() {
        return String.format("DetalleVenta{producto='%s', cantidad=%.2f, precio=%.2f, subtotal=%.2f}",
                producto != null ? producto.getNombre() : "Sin producto",
                cantidad, precioUnitario, subtotal);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DetalleVenta detalle = (DetalleVenta) obj;
        return idDetalle == detalle.idDetalle;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idDetalle);
    }
}
