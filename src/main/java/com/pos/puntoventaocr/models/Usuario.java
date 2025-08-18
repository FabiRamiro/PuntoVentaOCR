package com.pos.puntoventaocr.models;

import java.time.LocalDateTime;
import java.sql.Timestamp;

public class Usuario {
    private int id;
    private int idUsuario;
    private String nombreUsuario;
    private String password;
    private String passwordHash;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private Rol rol;
    private String estado;
    private int intentosFallidos;
    private boolean bloqueado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Timestamp ultimoAcceso;
    private Integer creadoPor;
    private Integer modificadoPor;

    // Constructor vacío
    public Usuario() {
        this.estado = "ACTIVO";
        this.intentosFallidos = 0;
        this.bloqueado = false;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }

    // Constructor con parámetros básicos
    public Usuario(String nombreUsuario, String password, String nombre, String apellido, Rol rol) {
        this();
        this.nombreUsuario = nombreUsuario;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rol = rol;
    }

    // Constructor completo
    public Usuario(int idUsuario, String nombreUsuario, String password, String nombre,
                   String apellido, String email, String telefono, Rol rol, String estado) {
        this();
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.rol = rol;
        this.estado = estado;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        this.idUsuario = id; // Para compatibilidad
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
        this.id = idUsuario; // Para compatibilidad
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
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

    public String getNombreRol() {
        return rol != null ? rol.getNombreRol() : "VENDEDOR";
    }

    public void setNombreRol(String nombreRol) {
        if (rol == null) {
            rol = new Rol();
        }
        rol.setNombreRol(nombreRol);
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

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion.toLocalDateTime() : null;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public void setFechaModificacion(Timestamp fechaModificacion) {
        this.fechaModificacion = fechaModificacion != null ? fechaModificacion.toLocalDateTime() : null;
    }

    public Timestamp getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(Timestamp ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
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

    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public boolean isActivo() {
        return "ACTIVO".equalsIgnoreCase(estado);
    }

    public boolean isAdministrador() {
        return rol != null && "ADMINISTRADOR".equalsIgnoreCase(rol.getNombreRol());
    }

    public boolean isVendedor() {
        return rol != null && "VENDEDOR".equalsIgnoreCase(rol.getNombreRol());
    }

    public boolean isSupervisor() {
        return rol != null && "SUPERVISOR".equalsIgnoreCase(rol.getNombreRol());
    }

    // Métodos adicionales para compatibilidad con pruebas
    public boolean puedeAcceder() {
        return isActivo() && !isBloqueado();
    }

    public boolean estaActivo() {
        return isActivo();
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", email='" + email + '\'' +
                ", rol=" + (rol != null ? rol.getNombreRol() : "Sin rol") +
                ", estado='" + estado + '\'' +
                ", bloqueado=" + bloqueado +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Usuario usuario = (Usuario) obj;
        return idUsuario == usuario.idUsuario;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idUsuario);
    }
}