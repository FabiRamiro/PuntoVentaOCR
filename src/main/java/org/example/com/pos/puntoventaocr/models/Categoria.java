package com.pos.puntoventaocr.models;

import java.time.LocalDateTime;

public class Categoria {
    private int idCategoria;
    private String nombre;
    private String descripcion;
    private boolean estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Integer creadoPor;
    private Integer modificadoPor;

    // Constructor vacío
    public Categoria() {
        this.estado = true;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con parámetros
    public Categoria(String nombre, String descripcion) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Métodos de negocio
    public void activar() {
        this.estado = true;
    }

    public void desactivar() {
        this.estado = false;
    }

    public boolean isActiva() {
        return this.estado;
    }

    // Getters y Setters
    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
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
        return nombre;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Categoria categoria = (Categoria) obj;
        return idCategoria == categoria.idCategoria;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idCategoria);
    }
}