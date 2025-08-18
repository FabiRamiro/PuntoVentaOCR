package com.pos.puntoventaocr.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.sql.Timestamp;

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
    private String codigoInterno;
    private String marca;
    private String estado;
    private int stockMinimo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Integer creadoPor;
    private Integer modificadoPor;

    // Constructor vacío
    public Producto() {
        this.estado = "ACTIVO";
        this.stockMinimo = 5;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
        this.precioCompra = BigDecimal.ZERO;
        this.precioVenta = BigDecimal.ZERO;
        this.unidadMedida = "PIEZA";
    }

    // Constructor con parámetros básicos
    public Producto(String nombre, BigDecimal precioVenta, int cantidadStock, Categoria categoria) {
        this();
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.cantidadStock = cantidadStock;
        this.categoria = categoria;
    }

    // Constructor completo
    public Producto(String nombre, String descripcionCorta, BigDecimal precioCompra,
                   BigDecimal precioVenta, int cantidadStock, String unidadMedida,
                   Categoria categoria, String codigoBarras, String marca) {
        this();
        this.nombre = nombre;
        this.descripcionCorta = descripcionCorta;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.cantidadStock = cantidadStock;
        this.unidadMedida = unidadMedida;
        this.categoria = categoria;
        this.codigoBarras = codigoBarras;
        this.marca = marca;
    }

    // Métodos de negocio
    public boolean hasBajoStock() {
        return cantidadStock <= stockMinimo && cantidadStock > 0;
    }

    public boolean estaAgotado() {
        return cantidadStock <= 0;
    }

    public boolean estaActivo() {
        return "ACTIVO".equals(this.estado);
    }

    public void reducirStock(int cantidad) {
        if (cantidad > 0 && cantidad <= cantidadStock) {
            this.cantidadStock -= cantidad;
            this.fechaModificacion = LocalDateTime.now();

            // Auto-deshabilitar si el stock llega a 0 (opcional)
            if (this.cantidadStock == 0 && !"AGOTADO".equals(this.estado)) {
                this.estado = "AGOTADO";
            }
        } else {
            throw new IllegalArgumentException("Cantidad inválida o stock insuficiente");
        }
    }

    public void aumentarStock(int cantidad) {
        if (cantidad > 0) {
            this.cantidadStock += cantidad;
            this.fechaModificacion = LocalDateTime.now();

            // Reactivar producto si tenía stock agotado
            if ("AGOTADO".equals(this.estado) && this.cantidadStock > 0) {
                this.estado = "ACTIVO";
            }
        } else {
            throw new IllegalArgumentException("Cantidad debe ser mayor a 0");
        }
    }

    public BigDecimal calcularMargenGanancia() {
        if (precioCompra != null && precioVenta != null &&
            precioCompra.compareTo(BigDecimal.ZERO) > 0) {
            return precioVenta.subtract(precioCompra);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calcularPorcentajeMargen() {
        if (precioCompra != null && precioCompra.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal margen = calcularMargenGanancia();
            return margen.divide(precioCompra, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
        }
        return BigDecimal.ZERO;
    }

    public String getEstadoDisplay() {
        if (estaAgotado()) {
            return "AGOTADO";
        } else if (hasBajoStock()) {
            return "STOCK BAJO";
        } else if (estaActivo()) {
            return "DISPONIBLE";
        } else {
            return estado;
        }
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
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getDescripcionCorta() {
        return descripcionCorta;
    }

    public void setDescripcionCorta(String descripcionCorta) {
        this.descripcionCorta = descripcionCorta;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getDescripcionLarga() {
        return descripcionLarga;
    }

    public void setDescripcionLarga(String descripcionLarga) {
        this.descripcionLarga = descripcionLarga;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
        this.fechaModificacion = LocalDateTime.now();
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
        this.fechaModificacion = LocalDateTime.now();
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
        this.fechaModificacion = LocalDateTime.now();
    }

    public int getCantidadStock() {
        return cantidadStock;
    }

    public void setCantidadStock(int cantidadStock) {
        this.cantidadStock = cantidadStock;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
        this.fechaModificacion = LocalDateTime.now();
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getCodigoInterno() {
        return codigoInterno;
    }

    public void setCodigoInterno(String codigoInterno) {
        this.codigoInterno = codigoInterno;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
        this.fechaModificacion = LocalDateTime.now();
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
        this.fechaModificacion = LocalDateTime.now();
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setFechaCreacion(Timestamp timestamp) {
        this.fechaCreacion = timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public void setFechaModificacion(Timestamp timestamp) {
        this.fechaModificacion = timestamp != null ? timestamp.toLocalDateTime() : null;
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

    // Métodos adicionales que necesitan los controladores
    public String getCodigo() {
        return codigoInterno != null ? codigoInterno : codigoBarras;
    }

    public int getStock() {
        return cantidadStock;
    }

    public void setStock(int stock) {
        this.cantidadStock = stock;
    }

    public String getNombreCategoria() {
        return categoria != null ? categoria.getNombre() : "";
    }

    public double getPrecioCompraDouble() {
        return precioCompra != null ? precioCompra.doubleValue() : 0.0;
    }

    public double getPrecioVentaDouble() {
        return precioVenta != null ? precioVenta.doubleValue() : 0.0;
    }

    // Métodos de utilidad
    public boolean tieneStock() {
        return cantidadStock > 0;
    }

    public boolean esBajoStock() {
        return cantidadStock <= stockMinimo;
    }

    public boolean esStockCritico() {
        return cantidadStock == 0;
    }

    public double calcularValorInventario() {
        return cantidadStock * (precioCompra != null ? precioCompra.doubleValue() : 0.0);
    }

    @Override
    public String toString() {
        return "Producto{" +
                "idProducto=" + idProducto +
                ", nombre='" + nombre + '\'' +
                ", codigo='" + getCodigo() + '\'' +
                ", precio=" + precioVenta +
                ", stock=" + cantidadStock +
                '}';
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