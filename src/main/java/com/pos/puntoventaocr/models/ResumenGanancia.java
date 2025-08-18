package com.pos.puntoventaocr.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ResumenGanancia {
    private String fecha;
    private LocalDate fechaLocal;
    private Integer cantidadVentas;
    private Double ingresosBrutos;
    private Double costoMercancia;
    private Double gananciaNetar;
    private Double margenGanancia;

    // Constructor vacío
    public ResumenGanancia() {}

    // Constructor completo
    public ResumenGanancia(LocalDate fecha, Integer cantidadVentas, Double ingresosBrutos, Double costoMercancia) {
        this.fechaLocal = fecha;
        this.fecha = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        this.cantidadVentas = cantidadVentas;
        this.ingresosBrutos = ingresosBrutos;
        this.costoMercancia = costoMercancia;
        this.gananciaNetar = ingresosBrutos - costoMercancia;

        // Calcular margen de ganancia como porcentaje
        if (ingresosBrutos > 0) {
            this.margenGanancia = (this.gananciaNetar / ingresosBrutos) * 100;
            this.margenGanancia = BigDecimal.valueOf(this.margenGanancia)
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
        } else {
            this.margenGanancia = 0.0;
        }
    }

    // Getters y Setters
    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public LocalDate getFechaLocal() {
        return fechaLocal;
    }

    public void setFechaLocal(LocalDate fechaLocal) {
        this.fechaLocal = fechaLocal;
        if (fechaLocal != null) {
            this.fecha = fechaLocal.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    public Integer getVentas() {
        return cantidadVentas;
    }

    public void setVentas(Integer cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }

    public Double getIngresos() {
        return ingresosBrutos;
    }

    public void setIngresos(Double ingresosBrutos) {
        this.ingresosBrutos = ingresosBrutos;
    }

    public Double getCostos() {
        return costoMercancia;
    }

    public void setCostos(Double costoMercancia) {
        this.costoMercancia = costoMercancia;
    }

    public Double getGanancias() {
        return gananciaNetar;
    }

    public void setGanancias(Double gananciaNetar) {
        this.gananciaNetar = gananciaNetar;
    }

    public Double getMargen() {
        return margenGanancia;
    }

    public void setMargen(Double margenGanancia) {
        this.margenGanancia = margenGanancia;
    }

    // Métodos de utilidad
    public String getIngresosBrutosFormateado() {
        return String.format("$%.2f", ingresosBrutos);
    }

    public String getCostoMercanciaFormateado() {
        return String.format("$%.2f", costoMercancia);
    }

    public String getGananciaNeutraFormateada() {
        return String.format("$%.2f", gananciaNetar);
    }

    public String getMargenGananciaFormateado() {
        return String.format("%.2f%%", margenGanancia);
    }

    @Override
    public String toString() {
        return "ResumenGanancia{" +
                "fecha='" + fecha + '\'' +
                ", cantidadVentas=" + cantidadVentas +
                ", ingresosBrutos=" + ingresosBrutos +
                ", costoMercancia=" + costoMercancia +
                ", gananciaNetar=" + gananciaNetar +
                ", margenGanancia=" + margenGanancia +
                '}';
    }
}
