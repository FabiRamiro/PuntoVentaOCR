package com.pos.puntoventaocr.dao;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.models.Categoria;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    // Crear nueva categoría
    public boolean crear(Categoria categoria) {
        String sql = "INSERT INTO categorias (nombre, descripcion, estado, creado_por) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, categoria.getNombre());
            pstmt.setString(2, categoria.getDescripcion());
            pstmt.setBoolean(3, categoria.isEstado());
            pstmt.setObject(4, categoria.getCreadoPor());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    categoria.setIdCategoria(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al crear categoría: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Actualizar categoría existente
    public boolean actualizar(Categoria categoria) {
        String sql = "UPDATE categorias SET nombre = ?, descripcion = ?, estado = ?, " +
                "fecha_modificacion = CURRENT_TIMESTAMP, modificado_por = ? WHERE id_categoria = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoria.getNombre());
            pstmt.setString(2, categoria.getDescripcion());
            pstmt.setBoolean(3, categoria.isEstado());
            pstmt.setObject(4, categoria.getModificadoPor());
            pstmt.setInt(5, categoria.getIdCategoria());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar categoría: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Buscar categoría por ID
    public Categoria buscarPorId(int idCategoria) {
        String sql = "SELECT * FROM categorias WHERE id_categoria = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCategoria);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearCategoria(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar categoría por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Buscar categoría por nombre
    public Categoria buscarPorNombre(String nombre) {
        String sql = "SELECT * FROM categorias WHERE nombre = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearCategoria(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar categoría por nombre: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Listar todas las categorías
    public List<Categoria> listarTodas() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM categorias ORDER BY nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categorias.add(mapearCategoria(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar categorías: " + e.getMessage());
            e.printStackTrace();
        }

        return categorias;
    }

    // Listar solo categorías activas
    public List<Categoria> listarActivas() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM categorias WHERE estado = TRUE ORDER BY nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categorias.add(mapearCategoria(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar categorías activas: " + e.getMessage());
            e.printStackTrace();
        }

        return categorias;
    }

    // Eliminar categoría (soft delete)
    public boolean eliminar(int idCategoria) {
        String sql = "UPDATE categorias SET estado = FALSE WHERE id_categoria = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCategoria);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar categoría: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Verificar si la categoría existe
    public boolean existe(String nombre) {
        return buscarPorNombre(nombre) != null;
    }

    // Verificar si la categoría está siendo usada por productos
    public boolean estaEnUso(int idCategoria) {
        String sql = "SELECT COUNT(*) FROM productos WHERE id_categoria = ? AND estado = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCategoria);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar uso de categoría: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Contar productos por categoría
    public int contarProductosPorCategoria(int idCategoria) {
        String sql = "SELECT COUNT(*) FROM productos WHERE id_categoria = ? AND estado = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCategoria);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error al contar productos por categoría: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    // Mapear ResultSet a objeto Categoria
    private Categoria mapearCategoria(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(rs.getInt("id_categoria"));
        categoria.setNombre(rs.getString("nombre"));
        categoria.setDescripcion(rs.getString("descripcion"));
        categoria.setEstado(rs.getBoolean("estado"));

        // Fechas
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            categoria.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }

        Timestamp fechaModificacion = rs.getTimestamp("fecha_modificacion");
        if (fechaModificacion != null) {
            categoria.setFechaModificacion(fechaModificacion.toLocalDateTime());
        }

        // IDs de usuario
        Object creadoPor = rs.getObject("creado_por");
        if (creadoPor != null) {
            categoria.setCreadoPor((Integer) creadoPor);
        }

        Object modificadoPor = rs.getObject("modificado_por");
        if (modificadoPor != null) {
            categoria.setModificadoPor((Integer) modificadoPor);
        }

        return categoria;
    }
}