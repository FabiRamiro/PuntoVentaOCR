package com.pos.puntoventaocr.models;

import java.math.BigDecimal;

public class DetalleVenta {
    private int idDetalle;
    private Venta venta;
    private Producto producto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private BigDecimal descuento;

    // Constructor vacío
    public DetalleVenta() {
        this.descuento = BigDecimal.ZERO;
    }

    // Constructor con parámetros
    public DetalleVenta(Producto producto, int cantidad) {
        this();
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecioVenta();
        calcularSubtotal();
    }

    public DetalleVenta(Producto producto, int cantidad, BigDecimal precioUnitario) {
        this();
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }

    // Métodos de negocio
    public void calcularSubtotal() {
        BigDecimal total = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        this.subtotal = total.subtract(descuento);
    }

    public void aplicarDescuento(BigDecimal descuento) {
        this.descuento = descuento;
        calcularSubtotal();
    }

    public BigDecimal getSubtotalSinDescuento() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public double getPorcentajeDescuento() {
        if (getSubtotalSinDescuento().compareTo(BigDecimal.ZERO) > 0) {
            return descuento.divide(getSubtotalSinDescuento(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }
        return 0.0;
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
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
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
        return producto.getNombre() + " x" + cantidad + " = $" + subtotal;
    }
}