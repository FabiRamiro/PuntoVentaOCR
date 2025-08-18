package com.pos.puntoventaocr.models;

import java.time.LocalDateTime;

public class Bitacora {
    private int idBitacora;
    private int idUsuario;
    private String nombreUsuario;
    private String accion;
    private String modulo;
    private String detalles;
    private String direccionIp;
    private LocalDateTime fechaHora;

    // Constructor vacío
    public Bitacora() {
        this.fechaHora = LocalDateTime.now();
    }

    // Constructor con parámetros
    public Bitacora(int idUsuario, String accion, String modulo, String detalles, String direccionIp) {
        this();
        this.idUsuario = idUsuario;
        this.accion = accion;
        this.modulo = modulo;
        this.detalles = detalles;
        this.direccionIp = direccionIp;
    }

    // Getters y Setters
    public int getIdBitacora() {
        return idBitacora;
    }

    public void setIdBitacora(int idBitacora) {
        this.idBitacora = idBitacora;
    }

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

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getDetalles() {
        return detalles;
    }

    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    @Override
    public String toString() {
        return "Bitacora{" +
                "idBitacora=" + idBitacora +
                ", idUsuario=" + idUsuario +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", accion='" + accion + '\'' +
                ", modulo='" + modulo + '\'' +
                ", direccionIp='" + direccionIp + '\'' +
                ", fechaHora=" + fechaHora +
                '}';
    }
}
