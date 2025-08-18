package com.pos.puntoventaocr.dao;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.models.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public boolean crear(Categoria categoria) {
        String sql = "INSERT INTO categorias (nombre, descripcion, categoria_padre, icono, estado, creado_por) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, categoria.getNombre());
            stmt.setString(2, categoria.getDescripcion());
            stmt.setObject(3, categoria.getCategoriaPadre());
            stmt.setString(4, categoria.getIcono());
            // Convertir estado String a Boolean
            stmt.setBoolean(5, "ACTIVO".equals(categoria.getEstado()));
            stmt.setObject(6, categoria.getCreadoPor());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    categoria.setIdCategoria(keys.getInt(1));
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error creando categoría: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Categoria categoria) {
        String sql = "UPDATE categorias SET nombre = ?, descripcion = ?, categoria_padre = ?, " +
                    "icono = ?, estado = ?, modificado_por = ?, fecha_modificacion = CURRENT_TIMESTAMP " +
                    "WHERE id_categoria = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoria.getNombre());
            stmt.setString(2, categoria.getDescripcion());
            stmt.setObject(3, categoria.getCategoriaPadre());
            stmt.setString(4, categoria.getIcono());
            // Convertir estado String a Boolean
            stmt.setBoolean(5, "ACTIVO".equals(categoria.getEstado()));
            stmt.setObject(6, categoria.getModificadoPor());
            stmt.setInt(7, categoria.getIdCategoria());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando categoría: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int idCategoria) {
        String sql = "DELETE FROM categorias WHERE id_categoria = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCategoria);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error eliminando categoría: " + e.getMessage());
            return false;
        }
    }

    public List<Categoria> obtenerTodas() {
        String sql = "SELECT c.*, " +
                    "(SELECT COUNT(*) FROM productos p WHERE p.id_categoria = c.id_categoria) as cantidad_productos " +
                    "FROM categorias c " +
                    "ORDER BY c.nombre";

        List<Categoria> categorias = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categorias.add(mapearCategoria(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo todas las categorías: " + e.getMessage());
        }

        return categorias;
    }

    public List<Categoria> obtenerActivas() {
        String sql = "SELECT c.*, " +
                    "(SELECT COUNT(*) FROM productos p WHERE p.id_categoria = c.id_categoria) as cantidad_productos " +
                    "FROM categorias c " +
                    "WHERE c.estado = TRUE " +
                    "ORDER BY c.nombre";

        List<Categoria> categorias = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categorias.add(mapearCategoria(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo categorías activas: " + e.getMessage());
        }

        return categorias;
    }

    public Categoria obtenerPorId(int idCategoria) {
        String sql = "SELECT c.*, " +
                    "(SELECT COUNT(*) FROM productos p WHERE p.id_categoria = c.id_categoria) as cantidad_productos " +
                    "FROM categorias c " +
                    "WHERE c.id_categoria = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCategoria);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearCategoria(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo categoría por ID: " + e.getMessage());
        }

        return null;
    }

    public List<Categoria> buscarPorNombre(String nombre) {
        String sql = "SELECT c.*, " +
                    "(SELECT COUNT(*) FROM productos p WHERE p.id_categoria = c.id_categoria) as cantidad_productos " +
                    "FROM categorias c " +
                    "WHERE c.nombre LIKE ? AND c.estado = TRUE " +
                    "ORDER BY c.nombre";

        List<Categoria> categorias = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nombre + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                categorias.add(mapearCategoria(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error buscando categorías por nombre: " + e.getMessage());
        }

        return categorias;
    }

    public boolean existeNombre(String nombre, int idCategoriaExcluir) {
        String sql = "SELECT COUNT(*) FROM categorias WHERE nombre = ? AND id_categoria != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setInt(2, idCategoriaExcluir);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando existencia de nombre: " + e.getMessage());
        }

        return false;
    }

    public boolean tieneProductos(int idCategoria) {
        String sql = "SELECT COUNT(*) FROM productos WHERE id_categoria = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCategoria);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando productos en categoría: " + e.getMessage());
        }

        return false;
    }

    private Categoria mapearCategoria(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria();

        categoria.setIdCategoria(rs.getInt("id_categoria"));
        categoria.setNombre(rs.getString("nombre"));
        categoria.setDescripcion(rs.getString("descripcion"));
        categoria.setCategoriaPadre(rs.getObject("categoria_padre", Integer.class));
        categoria.setIcono(rs.getString("icono"));

        // Convertir Boolean de BD a String para compatibilidad con el código Java
        boolean estadoBD = rs.getBoolean("estado");
        categoria.setEstado(estadoBD ? "ACTIVO" : "INACTIVO");

        categoria.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        categoria.setFechaModificacion(rs.getTimestamp("fecha_modificacion"));
        categoria.setCreadoPor(rs.getObject("creado_por", Integer.class));
        categoria.setModificadoPor(rs.getObject("modificado_por", Integer.class));

        // Mapear cantidad de productos
        try {
            categoria.setCantidadProductos(rs.getInt("cantidad_productos"));
        } catch (SQLException e) {
            categoria.setCantidadProductos(0);
        }

        return categoria;
    }

    // Métodos adicionales para compatibilidad
    public boolean guardar(Categoria categoria) {
        return crear(categoria);
    }

    public List<Categoria> obtenerCategorias() {
        return obtenerActivas();
    }
}
