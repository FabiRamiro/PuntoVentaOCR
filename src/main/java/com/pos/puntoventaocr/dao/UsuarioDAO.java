package com.pos.puntoventaocr.dao;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.models.Usuario;
import com.pos.puntoventaocr.models.Rol;
import com.pos.puntoventaocr.utils.PasswordUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // Autentica un usuario en el sistema
    public Usuario autenticar(String nombreUsuario, String password) {
        String sql = "SELECT u.*, r.nombre_rol, r.descripcion as rol_descripcion " +
                "FROM usuarios u " +
                "INNER JOIN roles r ON u.id_rol = r.id_rol " +
                "WHERE u.nombre_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombreUsuario);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Verificar si el usuario está bloqueado
                if (rs.getBoolean("bloqueado")) {
                    System.out.println("Usuario bloqueado: " + nombreUsuario);
                    return null;
                }

                // Verificar contraseña
                String hashedPassword = rs.getString("password");
                if (PasswordUtils.verificarPassword(password, hashedPassword)) {
                    // Login exitoso
                    Usuario usuario = mapearUsuario(rs);

                    // Resetear intentos fallidos
                    resetearIntentosFallidos(usuario.getIdUsuario());

                    return usuario;
                } else {
                    // Contraseña incorrecta - incrementar intentos fallidos
                    incrementarIntentosFallidos(nombreUsuario);
                    return null;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al autenticar usuario: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Incrementa los intentos fallidos de un usuario
    private void incrementarIntentosFallidos(String nombreUsuario) {
        String sql = "UPDATE usuarios SET intentos_fallidos = intentos_fallidos + 1, " +
                "bloqueado = CASE WHEN intentos_fallidos >= 2 THEN TRUE ELSE FALSE END, " +
                "estado = CASE WHEN intentos_fallidos >= 2 THEN 'BLOQUEADO' ELSE estado END " +
                "WHERE nombre_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombreUsuario);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al incrementar intentos fallidos: " + e.getMessage());
        }
    }

    // Resetea los intentos fallidos de un usuario
    private void resetearIntentosFallidos(int idUsuario) {
        String sql = "UPDATE usuarios SET intentos_fallidos = 0 WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al resetear intentos fallidos: " + e.getMessage());
        }
    }

    // Crear un nuevo usuario
    public boolean crear(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre_usuario, password, nombre, apellidos, " +
                "email, telefono, id_rol, estado, creado_por) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, usuario.getNombreUsuario());
            pstmt.setString(2, PasswordUtils.hashPassword(usuario.getPassword()));
            pstmt.setString(3, usuario.getNombre());
            pstmt.setString(4, usuario.getApellidos());
            pstmt.setString(5, usuario.getEmail());
            pstmt.setString(6, usuario.getTelefono());
            pstmt.setInt(7, usuario.getRol().getIdRol());
            pstmt.setString(8, usuario.getEstado());
            pstmt.setObject(9, usuario.getCreadoPor());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    usuario.setIdUsuario(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Actualizar un usuario existente
    public boolean actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombre = ?, apellidos = ?, email = ?, " +
                "telefono = ?, id_rol = ?, estado = ?, modificado_por = ? " +
                "WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getApellidos());
            pstmt.setString(3, usuario.getEmail());
            pstmt.setString(4, usuario.getTelefono());
            pstmt.setInt(5, usuario.getRol().getIdRol());
            pstmt.setString(6, usuario.getEstado());
            pstmt.setObject(7, usuario.getModificadoPor());
            pstmt.setInt(8, usuario.getIdUsuario());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Cambiar contraseña de un usuario
    public boolean cambiarPassword(int idUsuario, String nuevaPassword) {
        String sql = "UPDATE usuarios SET password = ? WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, PasswordUtils.hashPassword(nuevaPassword));
            pstmt.setInt(2, idUsuario);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al cambiar contraseña: " + e.getMessage());
        }

        return false;
    }

    // Buscar usuario por ID
    public Usuario buscarPorId(int idUsuario) {
        String sql = "SELECT u.*, r.nombre_rol, r.descripcion as rol_descripcion " +
                "FROM usuarios u " +
                "INNER JOIN roles r ON u.id_rol = r.id_rol " +
                "WHERE u.id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearUsuario(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por ID: " + e.getMessage());
        }

        return null;
    }

    // Listar todos los usuarios
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.*, r.nombre_rol, r.descripcion as rol_descripcion " +
                "FROM usuarios u " +
                "INNER JOIN roles r ON u.id_rol = r.id_rol " +
                "ORDER BY u.nombre_usuario";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }

        return usuarios;
    }

    // Bloquear usuario
    public boolean bloquearUsuario(int idUsuario) {
        String sql = "UPDATE usuarios SET bloqueado = TRUE, estado = 'BLOQUEADO' WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al bloquear usuario: " + e.getMessage());
        }

        return false;
    }

    // Desbloquear usuario
    public boolean desbloquearUsuario(int idUsuario) {
        String sql = "UPDATE usuarios SET bloqueado = FALSE, estado = 'ACTIVO', intentos_fallidos = 0 WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al desbloquear usuario: " + e.getMessage());
        }

        return false;
    }

    // Mapear ResultSet a objeto Usuario
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombreUsuario(rs.getString("nombre_usuario"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellidos(rs.getString("apellidos"));
        usuario.setEmail(rs.getString("email"));
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setEstado(rs.getString("estado"));
        usuario.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        usuario.setBloqueado(rs.getBoolean("bloqueado"));

        // Mapear el rol
        Rol rol = new Rol();
        rol.setIdRol(rs.getInt("id_rol"));
        rol.setNombreRol(rs.getString("nombre_rol"));
        rol.setDescripcion(rs.getString("rol_descripcion"));
        usuario.setRol(rol);

        // Fechas
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            usuario.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }

        Timestamp fechaModificacion = rs.getTimestamp("fecha_modificacion");
        if (fechaModificacion != null) {
            usuario.setFechaModificacion(fechaModificacion.toLocalDateTime());
        }

        return usuario;
    }
}