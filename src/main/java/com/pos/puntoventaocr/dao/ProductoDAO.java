package com.pos.puntoventaocr.dao;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.models.Producto;
import com.pos.puntoventaocr.models.Categoria;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {
    private CategoriaDAO categoriaDAO;

    public ProductoDAO() {
        this.categoriaDAO = new CategoriaDAO();
    }

    // Crear nuevo producto
    public boolean crear(Producto producto) {
        String sql = "INSERT INTO productos (nombre, descripcion_corta, descripcion_larga, ruta_imagen, " +
                "precio_compra, precio_venta, cantidad_stock, unidad_medida, id_categoria, " +
                "codigo_barras, estado, stock_minimo, creado_por) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, producto.getNombre());
            pstmt.setString(2, producto.getDescripcionCorta());
            pstmt.setString(3, producto.getDescripcionLarga());
            pstmt.setString(4, producto.getRutaImagen());
            pstmt.setBigDecimal(5, producto.getPrecioCompra());
            pstmt.setBigDecimal(6, producto.getPrecioVenta());
            pstmt.setInt(7, producto.getCantidadStock());
            pstmt.setString(8, producto.getUnidadMedida());
            pstmt.setInt(9, producto.getCategoria().getIdCategoria());
            pstmt.setString(10, producto.getCodigoBarras());
            pstmt.setBoolean(11, producto.isEstado());
            pstmt.setInt(12, producto.getStockMinimo());
            pstmt.setObject(13, producto.getCreadoPor());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    producto.setIdProducto(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Actualizar producto existente
    public boolean actualizar(Producto producto) {
        String sql = "UPDATE productos SET nombre = ?, descripcion_corta = ?, descripcion_larga = ?, " +
                "ruta_imagen = ?, precio_compra = ?, precio_venta = ?, cantidad_stock = ?, " +
                "unidad_medida = ?, id_categoria = ?, codigo_barras = ?, estado = ?, " +
                "stock_minimo = ?, fecha_modificacion = CURRENT_TIMESTAMP, modificado_por = ? " +
                "WHERE id_producto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, producto.getNombre());
            pstmt.setString(2, producto.getDescripcionCorta());
            pstmt.setString(3, producto.getDescripcionLarga());
            pstmt.setString(4, producto.getRutaImagen());
            pstmt.setBigDecimal(5, producto.getPrecioCompra());
            pstmt.setBigDecimal(6, producto.getPrecioVenta());
            pstmt.setInt(7, producto.getCantidadStock());
            pstmt.setString(8, producto.getUnidadMedida());
            pstmt.setInt(9, producto.getCategoria().getIdCategoria());
            pstmt.setString(10, producto.getCodigoBarras());
            pstmt.setBoolean(11, producto.isEstado());
            pstmt.setInt(12, producto.getStockMinimo());
            pstmt.setObject(13, producto.getModificadoPor());
            pstmt.setInt(14, producto.getIdProducto());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Buscar producto por ID
    public Producto buscarPorId(int idProducto) {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, c.descripcion as categoria_descripcion " +
                "FROM productos p " +
                "INNER JOIN categorias c ON p.id_categoria = c.id_categoria " +
                "WHERE p.id_producto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProducto);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearProducto(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar producto por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Buscar producto por código de barras
    public Producto buscarPorCodigoBarras(String codigoBarras) {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, c.descripcion as categoria_descripcion " +
                "FROM productos p " +
                "INNER JOIN categorias c ON p.id_categoria = c.id_categoria " +
                "WHERE p.codigo_barras = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codigoBarras);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearProducto(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar producto por código: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Listar todos los productos
    public List<Producto> listarTodos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as categoria_nombre, c.descripcion as categoria_descripcion " +
                "FROM productos p " +
                "INNER JOIN categorias c ON p.id_categoria = c.id_categoria " +
                "ORDER BY p.nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar productos: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    // Listar productos activos
    public List<Producto> listarActivos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as categoria_nombre, c.descripcion as categoria_descripcion " +
                "FROM productos p " +
                "INNER JOIN categorias c ON p.id_categoria = c.id_categoria " +
                "WHERE p.estado = TRUE ORDER BY p.nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar productos activos: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    // Listar productos por categoría
    public List<Producto> listarPorCategoria(int idCategoria) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as categoria_nombre, c.descripcion as categoria_descripcion " +
                "FROM productos p " +
                "INNER JOIN categorias c ON p.id_categoria = c.id_categoria " +
                "WHERE p.id_categoria = ? AND p.estado = TRUE ORDER BY p.nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCategoria);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar productos por categoría: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    // Buscar productos por nombre
    public List<Producto> buscarPorNombre(String nombre) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as categoria_nombre, c.descripcion as categoria_descripcion " +
                "FROM productos p " +
                "INNER JOIN categorias c ON p.id_categoria = c.id_categoria " +
                "WHERE p.nombre LIKE ? AND p.estado = TRUE ORDER BY p.nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + nombre + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar productos por nombre: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    // Listar productos con bajo stock
    public List<Producto> listarBajoStock() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as categoria_nombre, c.descripcion as categoria_descripcion " +
                "FROM productos p " +
                "INNER JOIN categorias c ON p.id_categoria = c.id_categoria " +
                "WHERE p.cantidad_stock <= p.stock_minimo AND p.estado = TRUE ORDER BY p.cantidad_stock";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar productos con bajo stock: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    // Actualizar stock de producto
    public boolean actualizarStock(int idProducto, int nuevoStock) {
        String sql = "UPDATE productos SET cantidad_stock = ? WHERE id_producto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nuevoStock);
            pstmt.setInt(2, idProducto);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar stock: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Verificar si el código de barras ya existe
    public boolean existeCodigoBarras(String codigoBarras, int idProductoExcluir) {
        String sql = "SELECT COUNT(*) FROM productos WHERE codigo_barras = ? AND id_producto != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codigoBarras);
            pstmt.setInt(2, idProductoExcluir);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar código de barras: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Eliminar producto (soft delete)
    public boolean eliminar(int idProducto) {
        String sql = "UPDATE productos SET estado = FALSE WHERE id_producto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProducto);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Mapear ResultSet a objeto Producto
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(rs.getInt("id_producto"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcionCorta(rs.getString("descripcion_corta"));
        producto.setDescripcionLarga(rs.getString("descripcion_larga"));
        producto.setRutaImagen(rs.getString("ruta_imagen"));
        producto.setPrecioCompra(rs.getBigDecimal("precio_compra"));
        producto.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        producto.setCantidadStock(rs.getInt("cantidad_stock"));
        producto.setUnidadMedida(rs.getString("unidad_medida"));
        producto.setCodigoBarras(rs.getString("codigo_barras"));
        producto.setEstado(rs.getBoolean("estado"));
        producto.setStockMinimo(rs.getInt("stock_minimo"));

        // Mapear categoría
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(rs.getInt("id_categoria"));
        categoria.setNombre(rs.getString("categoria_nombre"));
        categoria.setDescripcion(rs.getString("categoria_descripcion"));
        producto.setCategoria(categoria);

        // Fechas
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            producto.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }

        Timestamp fechaModificacion = rs.getTimestamp("fecha_modificacion");
        if (fechaModificacion != null) {
            producto.setFechaModificacion(fechaModificacion.toLocalDateTime());
        }

        return producto;
    }
}