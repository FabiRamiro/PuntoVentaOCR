package com.pos.puntoventaocr.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Rol {
    private int idRol;
    private String nombreRol;
    private String descripcion;
    private List<String> permisos;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    // Constructor vacío
    public Rol() {
        this.permisos = new ArrayList<>();
        this.estado = true;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con parámetros
    public Rol(int idRol, String nombreRol, String descripcion) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.descripcion = descripcion;
        this.permisos = new ArrayList<>();
        this.estado = true;
        this.fechaCreacion = LocalDateTime.now();
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
                // Removido VALIDAR_OCR y VER_REPORTES_BASICOS
                break;

            default:
                // Sin permisos por defecto
                break;
        }
    }

    // Verificar si el rol tiene un permiso específico
    public boolean tienePermiso(String permiso) {
        return permisos.contains(permiso.toUpperCase());
    }

    // Agregar un permiso al rol
    public void agregarPermiso(String permiso) {
        if (!permisos.contains(permiso.toUpperCase())) {
            permisos.add(permiso.toUpperCase());
        }
    }

    // Remover un permiso del rol
    public void removerPermiso(String permiso) {
        permisos.remove(permiso.toUpperCase());
    }

    // Métodos específicos para cada tipo de permiso
    public boolean isPermisoVentas() {
        return tienePermiso("REALIZAR_VENTAS");
    }

    public void setPermisoVentas(boolean permiso) {
        if (permiso) {
            agregarPermiso("REALIZAR_VENTAS");
        } else {
            removerPermiso("REALIZAR_VENTAS");
        }
    }

    public boolean isPermisoProductos() {
        return tienePermiso("GESTIONAR_PRODUCTOS");
    }

    public void setPermisoProductos(boolean permiso) {
        if (permiso) {
            agregarPermiso("GESTIONAR_PRODUCTOS");
        } else {
            removerPermiso("GESTIONAR_PRODUCTOS");
        }
    }

    public boolean isPermisoInventario() {
        return tienePermiso("GESTIONAR_INVENTARIO");
    }

    public void setPermisoInventario(boolean permiso) {
        if (permiso) {
            agregarPermiso("GESTIONAR_INVENTARIO");
        } else {
            removerPermiso("GESTIONAR_INVENTARIO");
        }
    }

    public boolean isPermisoReportes() {
        return tienePermiso("VER_REPORTES");
    }

    public void setPermisoReportes(boolean permiso) {
        if (permiso) {
            agregarPermiso("VER_REPORTES");
        } else {
            removerPermiso("VER_REPORTES");
        }
    }

    public boolean isPermisoUsuarios() {
        return tienePermiso("GESTIONAR_USUARIOS");
    }

    public void setPermisoUsuarios(boolean permiso) {
        if (permiso) {
            agregarPermiso("GESTIONAR_USUARIOS");
        } else {
            removerPermiso("GESTIONAR_USUARIOS");
        }
    }

    public boolean isPermisoConfiguracion() {
        return tienePermiso("GESTIONAR_SISTEMA");
    }

    public void setPermisoConfiguracion(boolean permiso) {
        if (permiso) {
            agregarPermiso("GESTIONAR_SISTEMA");
        } else {
            removerPermiso("GESTIONAR_SISTEMA");
        }
    }

    public boolean isPermisoOCR() {
        return tienePermiso("VALIDAR_OCR");
    }

    public void setPermisoOCR(boolean permiso) {
        if (permiso) {
            agregarPermiso("VALIDAR_OCR");
        } else {
            removerPermiso("VALIDAR_OCR");
        }
    }

    public boolean isPermisoDevoluciones() {
        return tienePermiso("ANULAR_VENTAS");
    }

    public void setPermisoDevoluciones(boolean permiso) {
        if (permiso) {
            agregarPermiso("ANULAR_VENTAS");
        } else {
            removerPermiso("ANULAR_VENTAS");
        }
    }

    public boolean isActivo() {
        return estado;
    }

    public void setActivo(boolean activo) {
        this.estado = activo;
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
        inicializarPermisos(); // Reinicializar permisos cuando cambie el nombre del rol
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<String> getPermisos() {
        return new ArrayList<>(permisos); // Devolver una copia para evitar modificaciones externas
    }

    public void setPermisos(List<String> permisos) {
        this.permisos = new ArrayList<>(permisos);
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