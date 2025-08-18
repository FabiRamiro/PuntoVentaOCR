package com.pos.puntoventaocr.test;

import com.pos.puntoventaocr.config.DatabaseConnection;
import java.sql.Connection;

public class TestConexion {
    public static void main(String[] args) {
        System.out.println("=== DIAGNÓSTICO DE CONEXIÓN A BASE DE DATOS ===");
        System.out.println();

        // Mostrar información de conexión
        System.out.println("Información de conexión:");
        System.out.println(DatabaseConnection.getConnectionInfo());
        System.out.println();

        // Probar conexión
        System.out.println("Iniciando prueba de conexión...");
        Connection conn = DatabaseConnection.getConnection();

        if (conn != null) {
            System.out.println("✓ CONEXIÓN EXITOSA");

            // Probar consulta básica
            if (DatabaseConnection.testConnection()) {
                System.out.println("✓ CONSULTA DE PRUEBA EXITOSA");
            } else {
                System.out.println("✗ ERROR EN CONSULTA DE PRUEBA");
            }
        } else {
            System.out.println("✗ CONEXIÓN FALLÓ");
        }

        System.out.println();
        System.out.println("=== FIN DEL DIAGNÓSTICO ===");
    }
}
