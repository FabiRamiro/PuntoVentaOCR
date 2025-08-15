package com.pos.puntoventaocr.models;

import java.time.LocalDateTime;

public class Inventario {
    private int idMovimiento;
    private Producto producto;
    private TipoMovimiento tipoMovimiento;
    private double cantidad;
    private double stockAnterior;
    private double stockNuevo;
    private LocalDateTime fecha;
    private Usuario usuario;
    private String referencia;
    private String observaciones;

    // Constructor vacío
    public Inventario() {
        this.fecha = LocalDateTime.now();
    }

    // Constructor con parámetros
    public Inventario(Producto producto, TipoMovimiento tipoMovimiento, double cantidad, Usuario usuario) {
        this();
        this.producto = producto;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.usuario = usuario;
        this.stockAnterior = producto != null ? producto.getCantidadStock() : 0;
        calcularStockNuevo();
    }

    // Constructor completo
    public Inventario(Producto producto, TipoMovimiento tipoMovimiento, double cantidad,
                      Usuario usuario, String referencia, String observaciones) {
        this(producto, tipoMovimiento, cantidad, usuario);
        this.referencia = referencia;
        this.observaciones = observaciones;
    }

    // Métodos de negocio
    private void calcularStockNuevo() {
        switch (tipoMovimiento) {
            case ENTRADA:
                this.stockNuevo = this.stockAnterior + this.cantidad;
                break;
            case SALIDA:
                this.stockNuevo = this.stockAnterior - this.cantidad;
                if (this.stockNuevo < 0) {
                    this.stockNuevo = 0;
                }
                break;
            case AJUSTE:
                // En ajuste, la cantidad representa el nuevo stock total
                this.stockNuevo = this.cantidad;
                break;
            case DEVOLUCION:
                this.stockNuevo = this.stockAnterior + this.cantidad;
                break;
            default:
                this.stockNuevo = this.stockAnterior;
        }
    }

    public boolean registrarEntrada(double cantidad, String referencia) {
        if (cantidad <= 0) {
            return false;
        }

        this.tipoMovimiento = TipoMovimiento.ENTRADA;
        this.cantidad = cantidad;
        this.referencia = referencia;
        calcularStockNuevo();

        // Actualizar stock del producto
        if (producto != null) {
            producto.setCantidadStock((int) stockNuevo);
        }

        return true;
    }

    public boolean registrarSalida(double cantidad, String referencia) {
        if (cantidad <= 0 || cantidad > stockAnterior) {
            return false;
        }

        this.tipoMovimiento = TipoMovimiento.SALIDA;
        this.cantidad = cantidad;
        this.referencia = referencia;
        calcularStockNuevo();

        // Actualizar stock del producto
        if (producto != null) {
            producto.setCantidadStock((int) stockNuevo);
        }

        return true;
    }

    public boolean ajustarStock(double nuevoStock, String motivo) {
        if (nuevoStock < 0) {
            return false;
        }

        this.tipoMovimiento = TipoMovimiento.AJUSTE;
        this.cantidad = nuevoStock;
        this.observaciones = motivo;
        calcularStockNuevo();

        // Actualizar stock del producto
        if (producto != null) {
            producto.setCantidadStock((int) stockNuevo);
        }

        return true;
    }

    public boolean validarExistencias() {
        if (producto == null) {
            return false;
        }

        // Validar que el stock calculado coincida con el stock actual del producto
        return Math.abs(stockNuevo - producto.getCantidadStock()) < 0.001;
    }

    public String getDescripcionMovimiento() {
        StringBuilder descripcion = new StringBuilder();

        switch (tipoMovimiento) {
            case ENTRADA:
                descripcion.append("Entrada de ").append(cantidad).append(" unidades");
                break;
            case SALIDA:
                descripcion.append("Salida de ").append(cantidad).append(" unidades");
                break;
            case AJUSTE:
                descripcion.append("Ajuste a ").append(cantidad).append(" unidades");
                break;
            case DEVOLUCION:
                descripcion.append("Devolución de ").append(cantidad).append(" unidades");
                break;
        }

        if (referencia != null && !referencia.trim().isEmpty()) {
            descripcion.append(" (Ref: ").append(referencia).append(")");
        }

        return descripcion.toString();
    }

    public double getDiferencia() {
        return stockNuevo - stockAnterior;
    }

    public boolean esMovimientoPositivo() {
        return getDiferencia() > 0;
    }

    public boolean esMovimientoNegativo() {
        return getDiferencia() < 0;
    }

    public boolean requiereAutorizacion() {
        // Los ajustes y movimientos grandes requieren autorización
        return tipoMovimiento == TipoMovimiento.AJUSTE ||
                Math.abs(getDiferencia()) > 100; // Más de 100 unidades
    }

    // Getters y Setters
    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
        if (producto != null) {
            this.stockAnterior = producto.getCantidadStock();
        }
    }

    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
        calcularStockNuevo();
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
        calcularStockNuevo();
    }

    public double getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(double stockAnterior) {
        this.stockAnterior = stockAnterior;
        calcularStockNuevo();
    }

    public double getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(double stockNuevo) {
        this.stockNuevo = stockNuevo;
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

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "Inventario{" +
                "producto=" + (producto != null ? producto.getNombre() : "null") +
                ", tipo=" + tipoMovimiento +
                ", cantidad=" + cantidad +
                ", stockAnterior=" + stockAnterior +
                ", stockNuevo=" + stockNuevo +
                ", fecha=" + fecha +
                '}';
    }

    // Enumeración TipoMovimiento
    public enum TipoMovimiento {
        ENTRADA("Entrada"),
        SALIDA("Salida"),
        AJUSTE("Ajuste"),
        DEVOLUCION("Devolución");

        private final String descripcion;

        TipoMovimiento(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }

        @Override
        public String toString() {
            return descripcion;
        }
    }
}