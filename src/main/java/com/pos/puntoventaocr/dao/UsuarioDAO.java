package com.pos.puntoventaocr.dao;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.models.Rol;
import com.pos.puntoventaocr.models.Usuario;
import com.pos.puntoventaocr.utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public boolean autenticar(String nombreUsuario, String password) {
        String sql = "SELECT u.*, r.nombre_rol, r.descripcion " +
                    "FROM usuarios u " +
                    "INNER JOIN roles r ON u.id_rol = r.id_rol " +
                    "WHERE u.nombre_usuario = ? AND u.estado = 'ACTIVO' AND u.bloqueado = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            System.out.println("🔍 DEBUG: Intentando autenticar usuario: " + nombreUsuario);

            if (conn == null) {
                System.err.println("❌ DEBUG: Conexión a BD es null");
                return false;
            }

            stmt.setString(1, nombreUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("✅ DEBUG: Usuario encontrado en BD");
                System.out.println("🔍 DEBUG: Estado: " + rs.getString("estado"));
                System.out.println("🔍 DEBUG: Bloqueado: " + rs.getBoolean("bloqueado"));
                System.out.println("🔍 DEBUG: Intentos fallidos: " + rs.getInt("intentos_fallidos"));
                System.out.println("🔍 DEBUG: Rol: " + rs.getString("nombre_rol"));

                String passwordHash = rs.getString("password");

                // Verificar contraseña
                if (PasswordUtils.verificarPassword(password, passwordHash)) {
                    System.out.println("✅ DEBUG: Password correcto");
                    resetearIntentosFallidos(nombreUsuario);
                    actualizarUltimoAcceso(nombreUsuario);
                    return true;
                } else {
                    System.out.println("❌ DEBUG: Password incorrecto");
                    incrementarIntentosFallidos(nombreUsuario);
                    return false;
                }
            } else {
                System.out.println("❌ DEBUG: Usuario no encontrado");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ DEBUG: Error en autenticación: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Usuario obtenerPorNombreUsuario(String nombreUsuario) {
        String sql = "SELECT u.*, r.nombre_rol, r.descripcion as rol_descripcion " +
                    "FROM usuarios u " +
                    "INNER JOIN roles r ON u.id_rol = r.id_rol " +
                    "WHERE u.nombre_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombreUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearUsuario(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo usuario: " + e.getMessage());
            e.printStackTrace(); // Agregar stack trace para debug
        }

        return null;
    }

    public List<Usuario> obtenerTodos() {
        String sql = "SELECT u.*, r.nombre_rol, r.descripcion as rol_descripcion, " +
                    "s.ultimo_acceso " +
                    "FROM usuarios u " +
                    "INNER JOIN roles r ON u.id_rol = r.id_rol " +
                    "LEFT JOIN (" +
                    "   SELECT id_usuario, MAX(fecha_inicio) as ultimo_acceso " +
                    "   FROM sesiones " +
                    "   GROUP BY id_usuario" +
                    ") s ON u.id_usuario = s.id_usuario " +
                    "ORDER BY u.nombre, u.apellidos";

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo usuarios: " + e.getMessage());
            e.printStackTrace();
        }

        return usuarios;
    }

    public boolean crear(Usuario usuario) {
        // Obtener ID del rol antes de la transacción principal
        int idRol = obtenerIdRolPorNombre(usuario.getNombreRol());
        if (idRol == 0) {
            System.err.println("Error: Rol no encontrado: " + usuario.getNombreRol());
            return false;
        }

        String sql = "INSERT INTO usuarios (nombre, apellidos, nombre_usuario, email, password, " +
                    "id_rol, estado, creado_por) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setString(3, usuario.getNombreUsuario());
            stmt.setString(4, usuario.getEmail());
            stmt.setString(5, PasswordUtils.hashPassword(usuario.getPassword()));
            stmt.setInt(6, idRol);
            stmt.setString(7, usuario.getEstado());
            stmt.setObject(8, usuario.getCreadoPor());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    usuario.setIdUsuario(keys.getInt(1));
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error creando usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Usuario usuario) {
        // Obtener ID del rol antes de la transacción principal
        int idRol = obtenerIdRolPorNombre(usuario.getNombreRol());
        if (idRol == 0) {
            System.err.println("Error: Rol no encontrado: " + usuario.getNombreRol());
            return false;
        }

        String sql = "UPDATE usuarios SET nombre = ?, apellidos = ?, nombre_usuario = ?, email = ?, " +
                    "id_rol = ?, estado = ?, modificado_por = ?, fecha_modificacion = CURRENT_TIMESTAMP " +
                    "WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setString(3, usuario.getNombreUsuario());
            stmt.setString(4, usuario.getEmail());
            stmt.setInt(5, idRol);
            stmt.setString(6, usuario.getEstado());
            stmt.setObject(7, usuario.getModificadoPor());
            stmt.setInt(8, usuario.getIdUsuario());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int idUsuario) {
        String sql = "UPDATE usuarios SET estado = 'INACTIVO', fecha_modificacion = CURRENT_TIMESTAMP " +
                    "WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error eliminando usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean resetearPassword(int idUsuario, String nuevaPassword) {
        String sql = "UPDATE usuarios SET password = ?, fecha_modificacion = CURRENT_TIMESTAMP " +
                    "WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, PasswordUtils.hashPassword(nuevaPassword));
            stmt.setInt(2, idUsuario);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error reseteando password: " + e.getMessage());
            return false;
        }
    }

    public boolean verificarPassword(int idUsuario, String password) {
        String sql = "SELECT password FROM usuarios WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String passwordHash = rs.getString("password");
                return PasswordUtils.verificarPassword(password, passwordHash);
            }

        } catch (SQLException e) {
            System.err.println("Error verificando password: " + e.getMessage());
        }

        return false;
    }

    public boolean cambiarPassword(int idUsuario, String nuevaPassword) {
        return resetearPassword(idUsuario, nuevaPassword);
    }

    private void desbloquearUsuarioAdmin() {
        String sql = "UPDATE usuarios SET bloqueado = FALSE, intentos_fallidos = 0 WHERE nombre_usuario = 'admin'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.executeUpdate();
            System.out.println("🔧 DEBUG: Admin desbloqueado");

        } catch (SQLException e) {
            System.err.println("Error desbloqueando admin: " + e.getMessage());
        }
    }

    private void resetearIntentosFallidos(String nombreUsuario) {
        String sql = "UPDATE usuarios SET intentos_fallidos = 0, bloqueado = FALSE WHERE nombre_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombreUsuario);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error reseteando intentos fallidos: " + e.getMessage());
        }
    }

    private void incrementarIntentosFallidos(String nombreUsuario) {
        String sql = "UPDATE usuarios SET intentos_fallidos = intentos_fallidos + 1, " +
                    "bloqueado = CASE WHEN intentos_fallidos >= 2 THEN TRUE ELSE FALSE END " +
                    "WHERE nombre_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombreUsuario);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error incrementando intentos fallidos: " + e.getMessage());
        }
    }

    private void actualizarUltimoAcceso(String nombreUsuario) {
        // Actualizar último acceso en la tabla usuarios
        String sqlUsuario = "UPDATE usuarios SET ultimo_acceso = CURRENT_TIMESTAMP WHERE nombre_usuario = ?";

        // Registrar nueva sesión en la tabla sesiones
        String sqlSesion = "INSERT INTO sesiones (id_usuario, fecha_inicio) " +
                          "SELECT id_usuario, CURRENT_TIMESTAMP FROM usuarios WHERE nombre_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Actualizar último acceso del usuario
            try (PreparedStatement stmtUsuario = conn.prepareStatement(sqlUsuario)) {
                stmtUsuario.setString(1, nombreUsuario);
                int filasUsuario = stmtUsuario.executeUpdate();

                if (filasUsuario > 0) {
                    System.out.println("✅ DEBUG: Último acceso actualizado para usuario: " + nombreUsuario);
                } else {
                    System.err.println("❌ ERROR: No se pudo actualizar último acceso para: " + nombreUsuario);
                }
            }

            // Registrar sesión
            try (PreparedStatement stmtSesion = conn.prepareStatement(sqlSesion)) {
                stmtSesion.setString(1, nombreUsuario);
                int filasSesion = stmtSesion.executeUpdate();

                if (filasSesion > 0) {
                    System.out.println("✅ DEBUG: Sesión registrada para usuario: " + nombreUsuario);
                } else {
                    System.err.println("❌ ERROR: No se pudo registrar sesión para: " + nombreUsuario);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ ERROR actualizando último acceso: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int obtenerIdRolPorNombre(String nombreRol) {
        String sql = "SELECT id_rol FROM roles WHERE nombre_rol = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombreRol);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int idRol = rs.getInt("id_rol");
                System.out.println("🔍 DEBUG: Rol encontrado - " + nombreRol + " = ID " + idRol);
                return idRol;
            } else {
                System.err.println("❌ ERROR: Rol no encontrado: " + nombreRol);
                // Intentar mapear roles alternativos
                switch (nombreRol.toUpperCase()) {
                    case "ADMINISTRADOR":
                        return 1;
                    case "GERENTE":
                    case "SUPERVISOR":
                        return 2;
                    case "CAJERO":
                    case "VENDEDOR":
                        return 3;
                    default:
                        System.err.println("❌ ERROR: Rol no mapeado, usando CAJERO por defecto");
                        return 3; // Default CAJERO en lugar de VENDEDOR
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ ERROR obteniendo ID de rol: " + e.getMessage());
            e.printStackTrace();
        }

        return 3; // Default CAJERO
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();

        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellidos"));
        usuario.setNombreUsuario(rs.getString("nombre_usuario"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPasswordHash(rs.getString("password"));
        usuario.setEstado(rs.getString("estado"));
        usuario.setBloqueado(rs.getBoolean("bloqueado"));
        usuario.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        usuario.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        usuario.setFechaModificacion(rs.getTimestamp("fecha_modificacion"));
        usuario.setCreadoPor(rs.getObject("creado_por", Integer.class));
        usuario.setModificadoPor(rs.getObject("modificado_por", Integer.class));

        // Mapear último acceso si existe
        Timestamp ultimoAcceso = rs.getTimestamp("ultimo_acceso");
        if (ultimoAcceso != null) {
            usuario.setUltimoAcceso(ultimoAcceso);
        }

        // Mapear rol
        Rol rol = new Rol();
        rol.setIdRol(rs.getInt("id_rol"));
        rol.setNombreRol(rs.getString("nombre_rol"));
        rol.setDescripcion(rs.getString("rol_descripcion"));
        usuario.setRol(rol);

        return usuario;
    }
}
