package com.pos.puntoventaocr.dao;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.models.Rol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RolDAO {

    public boolean guardar(Rol rol) {
        String sql = "INSERT INTO roles (nombre_rol, descripcion, permisos, activo) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, rol.getNombreRol());
            stmt.setString(2, rol.getDescripcion());
            stmt.setString(3, convertirPermisosAString(rol.getPermisos()));
            stmt.setBoolean(4, rol.isActivo());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    rol.setIdRol(keys.getInt(1));
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error guardando rol: " + e.getMessage());
            return false;
        }
    }

    public boolean crear(Rol rol) {
        return guardar(rol);
    }

    public boolean actualizar(Rol rol) {
        String sql = "UPDATE roles SET nombre_rol = ?, descripcion = ?, permisos = ?, activo = ? WHERE id_rol = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rol.getNombreRol());
            stmt.setString(2, rol.getDescripcion());
            stmt.setString(3, convertirPermisosAString(rol.getPermisos()));
            stmt.setBoolean(4, rol.isActivo());
            stmt.setInt(5, rol.getIdRol());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando rol: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int idRol) {
        String sql = "UPDATE roles SET activo = false WHERE id_rol = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRol);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error eliminando rol: " + e.getMessage());
            return false;
        }
    }

    public List<Rol> obtenerTodos() {
        String sql = "SELECT * FROM roles ORDER BY nombre_rol";
        List<Rol> roles = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                roles.add(mapearRol(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo roles: " + e.getMessage());
        }

        return roles;
    }

    public List<Rol> obtenerActivos() {
        String sql = "SELECT * FROM roles WHERE activo = true ORDER BY nombre_rol";
        List<Rol> roles = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                roles.add(mapearRol(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo roles activos: " + e.getMessage());
        }

        return roles;
    }

    public Rol obtenerPorId(int idRol) {
        String sql = "SELECT * FROM roles WHERE id_rol = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRol);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearRol(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo rol por ID: " + e.getMessage());
        }

        return null;
    }

    public Rol obtenerPorNombre(String nombreRol) {
        String sql = "SELECT * FROM roles WHERE nombre_rol = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombreRol);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearRol(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo rol por nombre: " + e.getMessage());
        }

        return null;
    }

    private Rol mapearRol(ResultSet rs) throws SQLException {
        Rol rol = new Rol();

        rol.setIdRol(rs.getInt("id_rol"));
        rol.setNombreRol(rs.getString("nombre_rol"));
        rol.setDescripcion(rs.getString("descripcion"));
        rol.setPermisos(convertirStringAPermisos(rs.getString("permisos")));
        rol.setActivo(rs.getBoolean("activo"));

        return rol;
    }

    private String convertirPermisosAString(List<String> permisos) {
        if (permisos == null || permisos.isEmpty()) {
            return "";
        }
        return String.join(",", permisos);
    }

    private List<String> convertirStringAPermisos(String permisosStr) {
        List<String> permisos = new ArrayList<>();
        if (permisosStr != null && !permisosStr.trim().isEmpty()) {
            String[] permisosArray = permisosStr.split(",");
            for (String permiso : permisosArray) {
                String permisoLimpio = permiso.trim();
                if (!permisoLimpio.isEmpty()) {
                    permisos.add(permisoLimpio);
                }
            }
        }
        return permisos;
    }
}
