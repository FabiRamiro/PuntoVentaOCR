
package com.pos.puntoventaocr.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RespaldoInfo {
    private String fecha;
    private String nombreArchivo;
    private String tamaño;
    private String tipo;
    private String estado;
    private String rutaCompleta;
    private LocalDateTime fechaCreacion;

    // Constructor vacío
    public RespaldoInfo() {}

    // Constructor completo
    public RespaldoInfo(String nombreArchivo, String rutaCompleta, String tipo, String estado, long tamañoBytes) {
        this.nombreArchivo = nombreArchivo;
        this.rutaCompleta = rutaCompleta;
        this.tipo = tipo;
        this.estado = estado;
        this.fechaCreacion = LocalDateTime.now();
        this.fecha = fechaCreacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        this.tamaño = formatearTamaño(tamañoBytes);
    }

    // Getters y Setters
    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getTamaño() {
        return tamaño;
    }

    public void setTamaño(String tamaño) {
        this.tamaño = tamaño;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRutaCompleta() {
        return rutaCompleta;
    }

    public void setRutaCompleta(String rutaCompleta) {
        this.rutaCompleta = rutaCompleta;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
        if (fechaCreacion != null) {
            this.fecha = fechaCreacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
    }

    // Método para formatear el tamaño en bytes a una representación legible
    private String formatearTamaño(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    @Override
    public String toString() {
        return "RespaldoInfo{" +
                "fecha='" + fecha + '\'' +
                ", nombreArchivo='" + nombreArchivo + '\'' +
                ", tamaño='" + tamaño + '\'' +
                ", tipo='" + tipo + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
