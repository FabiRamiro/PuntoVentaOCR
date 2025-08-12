package com.pos.puntoventaocr.models;

import java.time.LocalDateTime;

public class Usuario {
    private int idUsuario;
    private String nombreUsuario;
    private String password;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private Rol rol;
    private String estado;
    private int intentosFallidos;
    private boolean bloqueado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Integer creadoPor;
    private Integer modificadoPor;

    // Constructor vacío
    public Usuario() {}

    // Constructor con parámetros
    public Usuario(String nombreUsuario, String password, String nombre, String apellidos, Rol rol) {
        this.nombreUsuario = nombreUsuario;
        this.password = password;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.rol = rol;
        this.estado = "ACTIVO";
        this.intentosFallidos = 0;
        this.bloqueado = false;
    }

    // Métodos de negocio
    public void incrementarIntentosFallidos() {
        this.intentosFallidos++;
        if (this.intentosFallidos >= 3) {
            this.bloquearUsuario();
        }
    }

    public void resetearIntentosFallidos() {
        this.intentosFallidos = 0;
    }

    public void bloquearUsuario() {
        this.bloqueado = true;
        this.estado = "BLOQUEADO";
    }

    public void desbloquearUsuario() {
        this.bloqueado = false;
        this.estado = "ACTIVO";
        this.intentosFallidos = 0;
    }

    public boolean validarPermisos(String permiso) {
        if (rol != null) {
            return rol.tienePermiso(permiso);
        }
        return false;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    // Getters y Setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
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
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", rol=" + (rol != null ? rol.getNombreRol() : "null") +
                ", estado='" + estado + '\'' +
                '}';
    }
}