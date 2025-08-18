package com.pos.puntoventaocr.test;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.dao.UsuarioDAO;
import com.pos.puntoventaocr.models.Usuario;
import com.pos.puntoventaocr.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestAutenticacion {
    public static void main(String[] args) {
        System.out.println("=== DIAGNÓSTICO DE AUTENTICACIÓN ===");
        System.out.println();

        // Verificar conexión
        System.out.println("1. Verificando conexión a base de datos...");
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.out.println("❌ No hay conexión a la base de datos");
            return;
        }
        System.out.println("✅ Conexión exitosa");
        System.out.println();

        // Verificar si existe la tabla usuarios
        System.out.println("2. Verificando tabla usuarios...");
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM usuarios");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("✅ Tabla usuarios existe con " + count + " registros");
            }
        } catch (Exception e) {
            System.out.println("❌ Error accediendo tabla usuarios: " + e.getMessage());
            return;
        }
        System.out.println();

        // Verificar si existe el usuario admin
        System.out.println("3. Verificando usuario 'admin'...");
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT u.*, r.nombre_rol FROM usuarios u " +
                "LEFT JOIN roles r ON u.id_rol = r.id_rol " +
                "WHERE u.nombre_usuario = 'admin'"
            );
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("✅ Usuario 'admin' encontrado:");
                System.out.println("   - ID: " + rs.getInt("id_usuario"));
                System.out.println("   - Nombre: " + rs.getString("nombre"));
                System.out.println("   - Estado: " + rs.getString("estado"));
                System.out.println("   - Bloqueado: " + rs.getBoolean("bloqueado"));
                System.out.println("   - Intentos fallidos: " + rs.getInt("intentos_fallidos"));
                System.out.println("   - Rol: " + rs.getString("nombre_rol"));
                System.out.println("   - Hash password: " + rs.getString("password"));

                // Verificar el hash
                String hashDB = rs.getString("password");
                boolean hashValido = PasswordUtils.verificarPassword("admin123", hashDB);
                System.out.println("   - Verificación password: " + (hashValido ? "✅ CORRECTO" : "❌ INCORRECTO"));

            } else {
                System.out.println("❌ Usuario 'admin' NO encontrado en la base de datos");
                System.out.println("💡 Necesita ejecutar el script SQL para crear el usuario");
                return;
            }
        } catch (Exception e) {
            System.out.println("❌ Error verificando usuario: " + e.getMessage());
            return;
        }
        System.out.println();

        // Probar autenticación con DAO
        System.out.println("4. Probando autenticación con UsuarioDAO...");
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        // Probar con credenciales correctas
        boolean auth1 = usuarioDAO.autenticar("admin", "admin123");
        System.out.println("   - Autenticación 'admin'/'admin123': " + (auth1 ? "✅ EXITOSA" : "❌ FALLÓ"));

        // Probar con credenciales incorrectas
        boolean auth2 = usuarioDAO.autenticar("admin", "wrong");
        System.out.println("   - Autenticación 'admin'/'wrong': " + (auth2 ? "❌ NO DEBERÍA FUNCIONAR" : "✅ CORRECTAMENTE RECHAZADA"));

        // Obtener usuario completo
        System.out.println();
        System.out.println("5. Obteniendo datos completos del usuario...");
        Usuario usuario = usuarioDAO.obtenerPorNombreUsuario("admin");
        if (usuario != null) {
            System.out.println("✅ Usuario obtenido correctamente:");
            System.out.println("   - Nombre completo: " + usuario.getNombreCompleto());
            System.out.println("   - Puede acceder: " + usuario.puedeAcceder());
            System.out.println("   - Está activo: " + usuario.estaActivo());
            System.out.println("   - Rol: " + (usuario.getRol() != null ? usuario.getRol().getNombreRol() : "SIN ROL"));
        } else {
            System.out.println("❌ No se pudo obtener el usuario");
        }

        System.out.println();
        System.out.println("=== FIN DEL DIAGNÓSTICO ===");

        // Cerrar conexión
        DatabaseConnection.closeConnection();
    }
}
