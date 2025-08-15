package com.pos.puntoventaocr.models;

import java.util.ArrayList;
import java.util.List;

public class Rol {
    private int idRol;
    private String nombreRol;
    private String descripcion;
    private List<String> permisos;
    private boolean estado;

    // Constructor vacío
    public Rol() {
        this.permisos = new ArrayList<>();
        this.estado = true;
        inicializarPermisos();
    }

    // Constructor con parámetros
    public Rol(int idRol, String nombreRol, String descripcion) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.descripcion = descripcion;
        this.permisos = new ArrayList<>();
        this.estado = true;
        inicializarPermisos();
    }

    // Inicializar permisos según el rol
    private void inicializarPermisos() {
        permisos.clear();

        switch (nombreRol.toUpperCase()) {
            case "ADMINISTRADOR":
                // Administrador tiene todos los permisos
                permisos.add("GESTIONAR_USUARIOS");
                permisos.add("GESTIONAR_PRODUCTOS");
                permisos.add("GESTIONAR_CATEGORIAS");
                permisos.add("REALIZAR_VENTAS");
                permisos.add("ANULAR_VENTAS");
                permisos.add("VER_REPORTES");
                permisos.add("GESTIONAR_SISTEMA");
                permisos.add("VALIDAR_OCR");
                permisos.add("GESTIONAR_INVENTARIO");
                permisos.add("BACKUP_SISTEMA");
                break;

            case "GERENTE":
                // Gerente tiene permisos intermedios
                permisos.add("GESTIONAR_PRODUCTOS");
                permisos.add("GESTIONAR_CATEGORIAS");
                permisos.add("REALIZAR_VENTAS");
                permisos.add("ANULAR_VENTAS");
                permisos.add("VER_REPORTES");
                permisos.add("VALIDAR_OCR");
                permisos.add("GESTIONAR_INVENTARIO");
                break;

            case "CAJERO":
                // Cajero tiene permisos básicos
                permisos.add("REALIZAR_VENTAS");
                permisos.add("CONSULTAR_PRODUCTOS");
                permisos.add("VER_REPORTES_BASICOS");
                break;
        }
    }

    // Métodos de negocio
    public void agregarPermiso(String permiso) {
        if (!permisos.contains(permiso)) {
            permisos.add(permiso);
        }
    }

    public void quitarPermiso(String permiso) {
        permisos.remove(permiso);
    }

    public boolean tienePermiso(String permiso) {
        return permisos.contains(permiso);
    }

    public List<String> listarPermisos() {
        return new ArrayList<>(permisos);
    }

    // Getters y Setters
    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
        inicializarPermisos(); // Reinicializar permisos si cambia el rol
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<String> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<String> permisos) {
        this.permisos = permisos;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return nombreRol;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Rol rol = (Rol) obj;
        return idRol == rol.idRol;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idRol);
    }
}