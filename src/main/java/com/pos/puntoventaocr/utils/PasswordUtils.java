package com.pos.puntoventaocr.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    // Hashea una contraseña usando BCrypt
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Verifica una contraseña contra su hash
    public static boolean verificarPassword(String password, String hashedPassword) {
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            System.err.println("Error al verificar contraseña: " + e.getMessage());
            return false;
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

        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * caracteres.length());
            password.append(caracteres.charAt(index));
        }

        return password.toString();
    }
}