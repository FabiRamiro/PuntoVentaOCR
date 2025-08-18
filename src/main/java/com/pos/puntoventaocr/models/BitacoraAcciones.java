package com.pos.puntoventaocr.models;

import java.time.LocalDateTime;

public class BitacoraAcciones {
    private int idBitacora;
    private int idUsuario;
    private String accion;
    private String modulo;
    private String descripcion;
    private String datosAnteriores;
    private String datosNuevos;
    private String ipAddress;
    private LocalDateTime fecha;
    private String resultado;

    // Campos adicionales para mostrar información del usuario
    private String nombreUsuario;
    private String nombreCompleto;

    // Constructor vacío
    public BitacoraAcciones() {
    }

    // Constructor con parámetros principales
    public BitacoraAcciones(int idUsuario, String accion, String modulo, String descripcion,
                           String datosAnteriores, String datosNuevos, String ipAddress, String resultado) {
        this.idUsuario = idUsuario;
        this.accion = accion;
        this.modulo = modulo;
        this.descripcion = descripcion;
        this.datosAnteriores = datosAnteriores;
        this.datosNuevos = datosNuevos;
        this.ipAddress = ipAddress;
        this.resultado = resultado;
        this.fecha = LocalDateTime.now();
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDatosAnteriores() {
        return datosAnteriores;
    }

    public void setDatosAnteriores(String datosAnteriores) {
        this.datosAnteriores = datosAnteriores;
    }

    public String getDatosNuevos() {
        return datosNuevos;
    }

    public void setDatosNuevos(String datosNuevos) {
        this.datosNuevos = datosNuevos;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
}
