package com.pos.puntoventaocr.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Cliente {
    private int idCliente;
    private String nombre;
    private String apellidos;
    private String rfc;
    private String telefono;
    private String email;
    private String direccion;
    private LocalDateTime fechaRegistro;
    private BigDecimal credito;
    private boolean estado;

    // Constructor vacío
    public Cliente() {
        this.fechaRegistro = LocalDateTime.now();
        this.credito = BigDecimal.ZERO;
        this.estado = true;
    }

    // Constructor con parámetros básicos
    public Cliente(String nombre, String apellidos) {
        this();
        this.nombre = nombre;
        this.apellidos = apellidos;
    }

    // Constructor completo
    public Cliente(String nombre, String apellidos, String rfc, String telefono, String email) {
        this();
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.rfc = rfc;
        this.telefono = telefono;
        this.email = email;
    }

    // Métodos de negocio
    public String getNombreCompleto() {
        return this.nombre + " " + (this.apellidos != null ? this.apellidos : "");
    }

    public boolean estaActivo() {
        return this.estado;
    }

    public void activar() {
        this.estado = true;
    }

    public void desactivar() {
        this.estado = false;
    }

    public boolean tieneCredito() {
        return credito != null && credito.compareTo(BigDecimal.ZERO) > 0;
    }

    public void agregarCredito(BigDecimal monto) {
        if (monto != null && monto.compareTo(BigDecimal.ZERO) > 0) {
            this.credito = this.credito.add(monto);
        }
    }

    public void reducirCredito(BigDecimal monto) {
        if (monto != null && monto.compareTo(BigDecimal.ZERO) > 0 &&
            this.credito.compareTo(monto) >= 0) {
            this.credito = this.credito.subtract(monto);
        }
    }

    // Getters y Setters
    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
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

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public BigDecimal getCredito() {
        return credito;
    }

    public void setCredito(BigDecimal credito) {
        this.credito = credito;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cliente cliente = (Cliente) obj;
        return idCliente == cliente.idCliente;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idCliente);
    }
}
