package com.pos.puntoventaocr.test;

import com.pos.puntoventaocr.utils.PasswordUtils;

public class GenerarHashPassword {
    public static void main(String[] args) {
        String password = "admin123";
        String hash = PasswordUtils.hashPassword(password);

        System.out.println("=== GENERADOR DE HASH PARA CONTRASEÑAS ===");
        System.out.println();
        System.out.println("Contraseña original: " + password);
        System.out.println("Hash generado: " + hash);
        System.out.println();
        System.out.println("SQL para insertar usuario administrador:");
        System.out.println("INSERT INTO usuarios (nombre_usuario, password, nombre, apellidos, email, id_rol)");
        System.out.println("VALUES ('admin', '" + hash + "', 'Administrador', 'Sistema', 'admin@pos.com', 1);");
        System.out.println();

        // Verificar que el hash funciona
        boolean verifica = PasswordUtils.verificarPassword(password, hash);
        System.out.println("Verificación del hash: " + (verifica ? "✓ CORRECTO" : "✗ ERROR"));

        // Verificar el hash del schema.sql
        String hashSchema = "$2a$10$DJeI4pGc6QqFY8XwZBxDyOKZTQzHvDDQzV6nmVmNjKxA5KYPyqfLm";
        boolean verificaSchema = PasswordUtils.verificarPassword(password, hashSchema);
        System.out.println("Hash del schema.sql: " + (verificaSchema ? "✓ CORRECTO" : "✗ ERROR"));
    }
}
