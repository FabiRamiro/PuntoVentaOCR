package com.pos.puntoventaocr.models;

import javafx.beans.property.*;

public class InventarioFisico {
    private StringProperty codigo;
    private StringProperty nombreProducto;
    private StringProperty categoria;
    private IntegerProperty stockSistema;
    private IntegerProperty stockFisico;
    private IntegerProperty diferencia;
    private StringProperty observaciones;

    // Constructor
    public InventarioFisico(String codigo, String nombreProducto, String categoria, int stockSistema) {
        this.codigo = new SimpleStringProperty(codigo);
        this.nombreProducto = new SimpleStringProperty(nombreProducto);
        this.categoria = new SimpleStringProperty(categoria);
        this.stockSistema = new SimpleIntegerProperty(stockSistema);
        this.stockFisico = new SimpleIntegerProperty();
        this.diferencia = new SimpleIntegerProperty(0);
        this.observaciones = new SimpleStringProperty("");
    }

    // Métodos para calcular la diferencia
    public void calcularDiferencia() {
        if (getStockFisico() != null) {
            setDiferencia(getStockFisico() - getStockSistema());
        } else {
            setDiferencia(0);
        }
    }

    // Getters y Setters para codigo
    public String getCodigo() {
        return codigo.get();
    }

    public StringProperty codigoProperty() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo.set(codigo);
    }

    // Getters y Setters para nombreProducto
    public String getNombreProducto() {
        return nombreProducto.get();
    }

    public StringProperty nombreProductoProperty() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto.set(nombreProducto);
    }

    // Getters y Setters para categoria
    public String getCategoria() {
        return categoria.get();
    }

    public StringProperty categoriaProperty() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria.set(categoria);
    }

    // Getters y Setters para stockSistema
    public int getStockSistema() {
        return stockSistema.get();
    }

    public IntegerProperty stockSistemaProperty() {
        return stockSistema;
    }

    public void setStockSistema(int stockSistema) {
        this.stockSistema.set(stockSistema);
    }

    // Getters y Setters para stockFisico
    public Integer getStockFisico() {
        return stockFisico.get() == 0 ? null : stockFisico.get();
    }

    public IntegerProperty stockFisicoProperty() {
        return stockFisico;
    }

    public void setStockFisico(Integer stockFisico) {
        if (stockFisico != null) {
            this.stockFisico.set(stockFisico);
        } else {
            this.stockFisico.set(0);
        }
        calcularDiferencia();
    }

    // Getters y Setters para diferencia
    public int getDiferencia() {
        return diferencia.get();
    }

    public IntegerProperty diferenciaProperty() {
        return diferencia;
    }

    public void setDiferencia(int diferencia) {
        this.diferencia.set(diferencia);
    }

    // Getters y Setters para observaciones
    public String getObservaciones() {
        return observaciones.get();
    }

    public StringProperty observacionesProperty() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones.set(observaciones != null ? observaciones : "");
    }

    // Métodos de utilidad
    public boolean tieneStockFisico() {
        return getStockFisico() != null;
    }

    public boolean tieneDiferencia() {
        return getDiferencia() != 0;
    }

    public String getEstadoConteo() {
        if (!tieneStockFisico()) {
            return "Pendiente";
        } else if (tieneDiferencia()) {
            return "Con diferencia";
        } else {
            return "Correcto";
        }
    }

    @Override
    public String toString() {
        return "InventarioFisico{" +
                "codigo='" + getCodigo() + '\'' +
                ", nombreProducto='" + getNombreProducto() + '\'' +
                ", categoria='" + getCategoria() + '\'' +
                ", stockSistema=" + getStockSistema() +
                ", stockFisico=" + getStockFisico() +
                ", diferencia=" + getDiferencia() +
                ", observaciones='" + getObservaciones() + '\'' +
                '}';
    }
}
