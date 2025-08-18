package com.pos.puntoventaocr.dao;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.models.Categoria;
import com.pos.puntoventaocr.models.Producto;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public boolean guardar(Producto producto) {
        String sql = "INSERT INTO productos (codigo_barras, codigo_interno, nombre, descripcion_corta, " +
                    "descripcion_larga, id_categoria, marca, precio_compra, precio_venta, stock, " +
                    "stock_minimo, unidad_medida, imagen, estado, creado_por) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, producto.getCodigoBarras());
            stmt.setString(2, producto.getCodigoInterno());
            stmt.setString(3, producto.getNombre());
            stmt.setString(4, producto.getDescripcionCorta());
            stmt.setString(5, producto.getDescripcionLarga());
            stmt.setObject(6, producto.getCategoria() != null ? producto.getCategoria().getIdCategoria() : null);
            stmt.setString(7, producto.getMarca());
            stmt.setBigDecimal(8, producto.getPrecioCompra());
            stmt.setBigDecimal(9, producto.getPrecioVenta());
            stmt.setInt(10, producto.getCantidadStock());
            stmt.setInt(11, producto.getStockMinimo());
            stmt.setString(12, producto.getUnidadMedida());
            stmt.setString(13, producto.getRutaImagen());
            stmt.setString(14, producto.getEstado());
            stmt.setObject(15, producto.getCreadoPor());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    producto.setIdProducto(keys.getInt(1));
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error guardando producto: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Producto producto) {
        String sql = "UPDATE productos SET codigo_barras = ?, codigo_interno = ?, nombre = ?, " +
                    "descripcion_corta = ?, descripcion_larga = ?, id_categoria = ?, marca = ?, " +
                    "precio_compra = ?, precio_venta = ?, stock = ?, stock_minimo = ?, " +
                    "unidad_medida = ?, imagen = ?, estado = ?, modificado_por = ?, " +
                    "fecha_modificacion = CURRENT_TIMESTAMP WHERE id_producto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, producto.getCodigoBarras());
            stmt.setString(2, producto.getCodigoInterno());
            stmt.setString(3, producto.getNombre());
            stmt.setString(4, producto.getDescripcionCorta());
            stmt.setString(5, producto.getDescripcionLarga());
            stmt.setObject(6, producto.getCategoria() != null ? producto.getCategoria().getIdCategoria() : null);
            stmt.setString(7, producto.getMarca());
            stmt.setBigDecimal(8, producto.getPrecioCompra());
            stmt.setBigDecimal(9, producto.getPrecioVenta());
            stmt.setInt(10, producto.getCantidadStock());
            stmt.setInt(11, producto.getStockMinimo());
            stmt.setString(12, producto.getUnidadMedida());
            stmt.setString(13, producto.getRutaImagen());
            stmt.setString(14, producto.getEstado());
            stmt.setObject(15, producto.getModificadoPor());
            stmt.setInt(16, producto.getIdProducto());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando producto: " + e.getMessage());
            return false;
        }
    }

    public Producto obtenerPorId(int idProducto) {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, c.descripcion as categoria_descripcion " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.id_categoria = c.id_categoria " +
                    "WHERE p.id_producto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProducto);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearProducto(rs);
            }
            return null;

        } catch (SQLException e) {
            System.err.println("Error obteniendo producto por ID: " + e.getMessage());
            return null;
        }
    }

    public Producto obtenerPorCodigoBarras(String codigoBarras) {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, c.descripcion as categoria_descripcion " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.id_categoria = c.id_categoria " +
                    "WHERE p.codigo_barras = ? AND p.estado = 'ACTIVO'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigoBarras);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearProducto(rs);
            }
            return null;

        } catch (SQLException e) {
            System.err.println("Error obteniendo producto por código de barras: " + e.getMessage());
            return null;
        }
    }

    public Producto obtenerPorCodigo(String codigoInterno) {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, c.descripcion as categoria_descripcion " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.id_categoria = c.id_categoria " +
                    "WHERE p.codigo_interno = ? AND p.estado = 'ACTIVO'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigoInterno);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearProducto(rs);
            }
            return null;

        } catch (SQLException e) {
            System.err.println("Error obteniendo producto por código interno: " + e.getMessage());
            return null;
        }
    }

    public List<Producto> obtenerTodos() {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, c.descripcion as categoria_descripcion " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.id_categoria = c.id_categoria " +
                    "ORDER BY p.nombre";

        List<Producto> productos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo productos: " + e.getMessage());
        }

        return productos;
    }

    public List<Producto> buscarPorNombre(String nombre) {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, c.descripcion as categoria_descripcion " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.id_categoria = c.id_categoria " +
                    "WHERE p.nombre LIKE ? AND p.estado = 'ACTIVO' " +
                    "ORDER BY p.nombre";

        List<Producto> productos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nombre + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error buscando productos por nombre: " + e.getMessage());
        }

        return productos;
    }

    public List<Producto> obtenerPorCategoria(int idCategoria) {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, c.descripcion as categoria_descripcion " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.id_categoria = c.id_categoria " +
                    "WHERE p.id_categoria = ? AND p.estado = 'ACTIVO' " +
                    "ORDER BY p.nombre";

        List<Producto> productos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCategoria);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo productos por categoría: " + e.getMessage());
        }

        return productos;
    }

    public List<Producto> obtenerConBajoStock() {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, c.descripcion as categoria_descripcion " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.id_categoria = c.id_categoria " +
                    "WHERE p.stock <= p.stock_minimo AND p.estado = 'ACTIVO' " +
                    "ORDER BY p.stock ASC";

        List<Producto> productos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo productos con bajo stock: " + e.getMessage());
        }

        return productos;
    }

    public boolean actualizarStock(int idProducto, int nuevaCantidad) {
        String sql = "UPDATE productos SET stock = ?, fecha_modificacion = CURRENT_TIMESTAMP " +
                    "WHERE id_producto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, nuevaCantidad);
            stmt.setInt(2, idProducto);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando stock: " + e.getMessage());
            return false;
        }
    }

    // Métodos faltantes que necesitan los controladores
    public List<Producto> obtenerProductosConBajoStock() {
        return obtenerConBajoStock();
    }

    public boolean crear(Producto producto) {
        return guardar(producto);
    }

    public boolean eliminar(int idProducto) {
        String sql = "UPDATE productos SET estado = 'INACTIVO', fecha_modificacion = CURRENT_TIMESTAMP " +
                    "WHERE id_producto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProducto);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error eliminando producto: " + e.getMessage());
            return false;
        }
    }

    public List<com.pos.puntoventaocr.controllers.ReporteProductosController.ProductoVendido> obtenerProductosMasVendidos(
            java.time.LocalDate fechaDesde, java.time.LocalDate fechaHasta, String categoria, Integer top) {

        List<com.pos.puntoventaocr.controllers.ReporteProductosController.ProductoVendido> productos = new ArrayList<>();

        String sql = "SELECT p.codigo_interno, p.nombre, c.nombre as categoria, " +
                    "SUM(dv.cantidad) as cantidad_vendida, SUM(dv.subtotal) as monto_total " +
                    "FROM detalle_ventas dv " +
                    "JOIN productos p ON dv.id_producto = p.id_producto " +
                    "JOIN ventas v ON dv.id_venta = v.id_venta " +
                    "LEFT JOIN categorias c ON p.id_categoria = c.id_categoria " +
                    "WHERE DATE(v.fecha) BETWEEN ? AND ? ";

        if (!"Todas".equals(categoria)) {
            sql += "AND c.nombre = ? ";
        }

        sql += "GROUP BY p.id_producto, p.codigo_interno, p.nombre, c.nombre " +
               "ORDER BY cantidad_vendida DESC ";

        if (top != null && top > 0) {
            sql += "LIMIT " + top;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fechaDesde));
            stmt.setDate(2, Date.valueOf(fechaHasta));

            if (!"Todas".equals(categoria)) {
                stmt.setString(3, categoria);
            }

            ResultSet rs = stmt.executeQuery();

            // Primero calculamos el total para los porcentajes
            List<TempProducto> tempProductos = new ArrayList<>();
            double totalVentas = 0;

            while (rs.next()) {
                double monto = rs.getDouble("monto_total");
                totalVentas += monto;

                TempProducto temp = new TempProducto();
                temp.codigo = rs.getString("codigo_interno");
                temp.nombre = rs.getString("nombre");
                temp.categoria = rs.getString("categoria");
                temp.cantidadVendida = rs.getInt("cantidad_vendida");
                temp.montoTotal = monto;
                tempProductos.add(temp);
            }

            // Ahora creamos los productos finales con porcentajes
            int posicion = 1;
            for (TempProducto temp : tempProductos) {
                double porcentaje = totalVentas > 0 ? (temp.montoTotal / totalVentas) * 100 : 0;

                com.pos.puntoventaocr.controllers.ReporteProductosController.ProductoVendido producto =
                    new com.pos.puntoventaocr.controllers.ReporteProductosController.ProductoVendido(
                        posicion++,
                        temp.codigo,
                        temp.nombre,
                        temp.categoria,
                        temp.cantidadVendida,
                        temp.montoTotal,
                        porcentaje
                    );
                productos.add(producto);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo productos más vendidos: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    public boolean existeCodigoBarras(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM productos WHERE codigo_barras = ? AND estado != 'ELIMINADO'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigoBarras.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando código de barras: " + e.getMessage());
        }

        return false;
    }

    // Clase auxiliar para almacenar datos temporalmente
    private static class TempProducto {
        String codigo;
        String nombre;
        String categoria;
        int cantidadVendida;
        double montoTotal;
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();

        producto.setIdProducto(rs.getInt("id_producto"));
        producto.setCodigoBarras(rs.getString("codigo_barras"));
        producto.setCodigoInterno(rs.getString("codigo_interno"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcionCorta(rs.getString("descripcion_corta"));
        producto.setDescripcionLarga(rs.getString("descripcion_larga"));
        producto.setMarca(rs.getString("marca"));
        producto.setPrecioCompra(rs.getBigDecimal("precio_compra"));
        producto.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        producto.setCantidadStock(rs.getInt("stock"));
        producto.setStockMinimo(rs.getInt("stock_minimo"));
        producto.setUnidadMedida(rs.getString("unidad_medida"));
        producto.setRutaImagen(rs.getString("imagen"));
        producto.setEstado(rs.getString("estado"));
        producto.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        producto.setFechaModificacion(rs.getTimestamp("fecha_modificacion"));

        // Mapear categoría si existe
        int idCategoria = rs.getInt("id_categoria");
        if (!rs.wasNull()) {
            Categoria categoria = new Categoria();
            categoria.setIdCategoria(idCategoria);
            categoria.setNombre(rs.getString("categoria_nombre"));
            categoria.setDescripcion(rs.getString("categoria_descripcion"));
            producto.setCategoria(categoria);
        }

        return producto;
    }
}
