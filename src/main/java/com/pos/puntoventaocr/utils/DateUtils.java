package com.pos.puntoventaocr.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    
    private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public static String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) {
            return "-";
        }
        return fecha.format(FECHA_FORMATO);
    }
    
    public static String formatearFechaHora(LocalDateTime fecha) {
        if (fecha == null) {
            return "-";
        }
        return fecha.format(FECHA_HORA_FORMATO);
    }
}