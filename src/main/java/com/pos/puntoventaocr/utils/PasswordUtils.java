package com.pos.puntoventaocr.utils;

import org.mindrot.jbcrypt.BCrypt;
import java.security.SecureRandom;

public class PasswordUtils {

    // Hashea una contraseña usando BCrypt
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Verifica una contraseña contra su hash
    public static boolean verificarPassword(String password, String hashedPassword) {
        try {
            // Para compatibilidad con contraseñas en texto plano (modo debug)
            if (hashedPassword != null && hashedPassword.equals(password)) {
                System.out.println("🔧 DEBUG: Contraseña verificada en modo texto plano");
                return true;
            }

            // Verificación normal con BCrypt
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            System.err.println("Error al verificar contraseña: " + e.getMessage());
            // Fallback a comparación simple para contraseñas no hasheadas
            return hashedPassword != null && hashedPassword.equals(password);
        }
    }

    // Valida que una contraseña cumpla con los requisitos mínimos
    public static boolean validarFortaleza(String password) {
        // Mínimo 6 caracteres
        if (password == null || password.length() < 6) {
            return false;
        }

        // Debe contener al menos una letra y un número
        boolean tieneLetra = false;
        boolean tieneNumero = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                tieneLetra = true;
            }
            if (Character.isDigit(c)) {
                tieneNumero = true;
            }
            if (tieneLetra && tieneNumero) {
                return true;
            }
        }

        return false;
    }

    // Genera una contraseña temporal aleatoria
    public static String generarPasswordTemporal() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 8; i++) {
            password.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }

        return password.toString();
    }

    // Valida el formato de una contraseña y retorna mensajes de error
    public static String validarPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "La contraseña no puede estar vacía";
        }

        if (password.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres";
        }

        if (password.length() > 50) {
            return "La contraseña no puede tener más de 50 caracteres";
        }

        if (!validarFortaleza(password)) {
            return "La contraseña debe contener al menos una letra y un número";
        }

        return null; // null indica que la contraseña es válida
    }

    // Método adicional para calcular fortaleza (0-4) para UI
    public static int calcularFortaleza(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int puntaje = 0;

        if (password.length() >= 6) puntaje++;
        if (password.matches(".*[a-z].*")) puntaje++;
        if (password.matches(".*[A-Z].*")) puntaje++;
        if (password.matches(".*[0-9].*")) puntaje++;

        return puntaje;
    }

    // Método de utilidad para testing - NO USAR EN PRODUCCIÓN
    public static boolean verificarPasswordDebug(String password, String hashedPassword) {
        System.out.println("🔍 DEBUG PasswordUtils:");
        System.out.println("  - Password entrada: '" + password + "'");
        System.out.println("  - Hash almacenado: '" + hashedPassword + "'");

        boolean resultado = verificarPassword(password, hashedPassword);
        System.out.println("  - Resultado: " + resultado);

        return resultado;
    }
}
