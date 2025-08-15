package com.pos.puntoventaocr.models;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

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
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Integer creadoPor;
    private Integer modificadoPor;

    // Constructor vacío
    public Cliente() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaCreacion = LocalDateTime.now();
        this.credito = BigDecimal.ZERO;
        this.estado = true;
    }

    // Constructor con parámetros básicos
    public Cliente(String nombre, String apellidos, String telefono) {
        this();
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
    }

    // Constructor completo
    public Cliente(String nombre, String apellidos, String rfc, String telefono, String email, String direccion) {
        this();
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.rfc = rfc;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    // Métodos de negocio
    public boolean registrar() {
        // Validar datos mínimos
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        if (telefono == null || telefono.trim().isEmpty()) {
            return false;
        }

        // Validar RFC si se proporciona
        if (rfc != null && !rfc.trim().isEmpty()) {
            if (!validarRFC(rfc)) {
                return false;
            }
        }

        // Validar email si se proporciona
        if (email != null && !email.trim().isEmpty()) {
            if (!validarEmail(email)) {
                return false;
            }
        }

        return true;
    }

    public boolean actualizar() {
        this.fechaModificacion = LocalDateTime.now();
        return registrar(); // Usa las mismas validaciones
    }

    public boolean validarCredito(BigDecimal monto) {
        if (credito == null) {
            return false;
        }
        return credito.compareTo(monto) >= 0;
    }

    public void aplicarCredito(BigDecimal monto) {
        if (credito != null && monto != null) {
            this.credito = this.credito.add(monto);
        }
    }

    public void descontarCredito(BigDecimal monto) {
        if (credito != null && monto != null) {
            this.credito = this.credito.subtract(monto);
            if (this.credito.compareTo(BigDecimal.ZERO) < 0) {
                this.credito = BigDecimal.ZERO;
            }
        }
    }

    public String getNombreCompleto() {
        StringBuilder nombreCompleto = new StringBuilder();
        if (nombre != null) {
            nombreCompleto.append(nombre);
        }
        if (apellidos != null && !apellidos.trim().isEmpty()) {
            if (nombreCompleto.length() > 0) {
                nombreCompleto.append(" ");
            }
            nombreCompleto.append(apellidos);
        }
        return nombreCompleto.toString();
    }

    public void activar() {
        this.estado = true;
    }

    public void desactivar() {
        this.estado = false;
    }

    public boolean isActivo() {
        return this.estado;
    }

    // Métodos de validación
    private boolean validarRFC(String rfc) {
        // Validación básica de RFC mexicano
        if (rfc == null || rfc.trim().isEmpty()) {
            return false;
        }

        String rfcLimpio = rfc.trim().toUpperCase();

        // RFC Persona Física: 4 letras + 6 dígitos + 3 caracteres alfanuméricos
        // RFC Persona Moral: 3 letras + 6 dígitos + 3 caracteres alfanuméricos
        return rfcLimpio.matches("^[A-Z]{3,4}\\d{6}[A-Z0-9]{3}$");
    }

    private boolean validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // Validación básica de email
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }

    private boolean validarTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return false;
        }

        // Remover espacios y caracteres especiales
        String telefonoLimpio = telefono.replaceAll("[\\s\\-\\(\\)]", "");

        // Validar que tenga 10 dígitos (formato mexicano)
        return telefonoLimpio.matches("^\\d{10}$");
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
        return getNombreCompleto() + " (" + telefono + ")";
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