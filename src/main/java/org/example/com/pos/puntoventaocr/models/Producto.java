package com.pos.puntoventaocr.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Producto {
    private int idProducto;
    private String nombre;
    private String descripcionCorta;
    private String descripcionLarga;
    private String rutaImagen;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private int cantidadStock;
    private String unidadMedida;
    private Categoria categoria;
    private String codigoBarras;
    private boolean estado;
    private int stockMinimo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Integer creadoPor;
    private Integer modificadoPor;

    // Constructor vacío
    public Producto() {
        this.estado = true;
        this.stockMinimo = 5;
        this.fechaCreacion = LocalDateTime.now();
        this.precioCompra = BigDecimal.ZERO;
        this.precioVenta = BigDecimal.ZERO;
    }

    // Constructor con parámetros básicos
    public Producto(String nombre, BigDecimal precioVenta, int cantidadStock, Categoria categoria) {
        this();
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.cantidadStock = cantidadStock;
        this.categoria = categoria;
    }

    // Métodos de negocio
    public boolean hasBajoStock() {
        return cantidadStock <= stockMinimo;
    }

    public boolean estaAgotado() {
        return cantidadStock <= 0;
    }

    public void reducirStock(int cantidad) {
        if (cantidadStock >= cantidad) {
            this.cantidadStock -= cantidad;
        } else {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + cantidadStock);
        }
    }

    public void aumentarStock(int cantidad) {
        if (cantidad > 0) {
            this.cantidadStock += cantidad;
        }
    }

    public BigDecimal calcularGanancia() {
        return precioVenta.subtract(precioCompra);
    }

    public double calcularMargenGanancia() {
        if (precioCompra.compareTo(BigDecimal.ZERO) > 0) {
            return calcularGanancia().divide(precioCompra, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }
        return 0.0;
    }

    public void activar() {
        this.estado = true;
    }

    public void desactivar() {
        this.estado = false;
    }

    // Getters y Setters
    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcionCorta() {
        return descripcionCorta;
    }

    public void setDescripcionCorta(String descripcionCorta) {
        this.descripcionCorta = descripcionCorta;
    }

    public String getDescripcionLarga() {
        return descripcionLarga;
    }

    public void setDescripcionLarga(String descripcionLarga) {
        this.descripcionLarga = descripcionLarga;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public int getCantidadStock() {
        return cantidadStock;
    }

    public void setCantidadStock(int cantidadStock) {
        this.cantidadStock = cantidadStock;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Integer getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(Integer creadoPor) {
        this.creadoPor = creadoPor;
    }

    public Integer getModificadoPor() {
        return modificadoPor;
    }

    public void setModificadoPor(Integer modificadoPor) {
        this.modificadoPor = modificadoPor;
    }

    @Override
    public String toString() {
        return nombre + " - $" + precioVenta + " (Stock: " + cantidadStock + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Producto producto = (Producto) obj;
        return idProducto == producto.idProducto;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idProducto);
    }
}