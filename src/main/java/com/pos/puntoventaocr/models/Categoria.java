package com.pos.puntoventaocr.models;

import java.time.LocalDateTime;
import java.sql.Timestamp;

public class Categoria {
    private int idCategoria;
    private String nombre;
    private String descripcion;
    private Integer categoriaPadre;
    private String icono;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Integer creadoPor;
    private Integer modificadoPor;
    private int cantidadProductos;

    // Constructor vacío
    public Categoria() {
        this.estado = "ACTIVO";
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }

    // Constructor con parámetros
    public Categoria(String nombre, String descripcion) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Constructor completo
    public Categoria(int idCategoria, String nombre, String descripcion, String estado) {
        this();
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    // Alias para compatibilidad con TableView
    public int getId() {
        return idCategoria;
    }

    public void setId(int id) {
        this.idCategoria = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        this.fechaModificacion = LocalDateTime.now();
    }

    public Integer getCategoriaPadre() {
        return categoriaPadre;
    }

    public void setCategoriaPadre(Integer categoriaPadre) {
        this.categoriaPadre = categoriaPadre;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public int getCantidadProductos() {
        return cantidadProductos;
    }

    public void setCantidadProductos(int cantidadProductos) {
        this.cantidadProductos = cantidadProductos;
    }

    // Métodos de negocio
    public void activar() {
        this.estado = "ACTIVO";
        this.fechaModificacion = LocalDateTime.now();
    }

    public void desactivar() {
        this.estado = "INACTIVO";
        this.fechaModificacion = LocalDateTime.now();
    }

    public boolean isActivo() {
        return "ACTIVO".equals(estado);
    }

    public boolean isInactivo() {
        return "INACTIVO".equals(estado);
    }

    // Método para obtener el estado como boolean
    public boolean getActivo() {
        return isActivo();
    }

    public void setActivo(boolean activo) {
        this.estado = activo ? "ACTIVO" : "INACTIVO";
        this.fechaModificacion = LocalDateTime.now();
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