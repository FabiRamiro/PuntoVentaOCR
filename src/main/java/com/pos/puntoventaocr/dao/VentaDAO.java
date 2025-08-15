package com.pos.puntoventaocr.dao;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.models.Venta;
import com.pos.puntoventaocr.models.DetalleVenta;
import com.pos.puntoventaocr.models.Usuario;
import com.pos.puntoventaocr.models.Producto;
import com.pos.puntoventaocr.models.Categoria;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {
    private UsuarioDAO usuarioDAO;
    private ProductoDAO productoDAO;

    public VentaDAO() {
        this.usuarioDAO = new UsuarioDAO();
        this.productoDAO = new ProductoDAO();
    }

    // Crear nueva venta
    public boolean crear(Venta venta) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Insertar venta
            String sqlVenta = "INSERT INTO ventas (numero_venta, fecha_venta, id_usuario, metodo_pago, " +
                    "subtotal, impuestos, total, estado, observaciones, referencia_transferencia) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pstmtVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            pstmtVenta.setString(1, venta.getNumeroVenta());
            pstmtVenta.setTimestamp(2, Timestamp.valueOf(venta.getFechaVenta()));
            pstmtVenta.setInt(3, venta.getUsuario().getIdUsuario());
            pstmtVenta.setString(4, venta.getMetodoPago());
            pstmtVenta.setBigDecimal(5, venta.getSubtotal());
            pstmtVenta.setBigDecimal(6, venta.getImpuestos());
            pstmtVenta.setBigDecimal(7, venta.getTotal());
            pstmtVenta.setString(8, venta.getEstado());
            pstmtVenta.setString(9, venta.getObservaciones());
            pstmtVenta.setString(10, venta.getReferenciaTransferencia());

            int filasVenta = pstmtVenta.executeUpdate();

            if (filasVenta > 0) {
                ResultSet rs = pstmtVenta.getGeneratedKeys();
                if (rs.next()) {
                    venta.setIdVenta(rs.getInt(1));
                }

                // Insertar detalles de venta
                String sqlDetalle = "INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, " +
                        "precio_unitario, subtotal, descuento) VALUES (?, ?, ?, ?, ?, ?)";

                PreparedStatement pstmtDetalle = conn.prepareStatement(sqlDetalle);

                for (DetalleVenta detalle : venta.getDetalles()) {
                    pstmtDetalle.setInt(1, venta.getIdVenta());
                    pstmtDetalle.setInt(2, detalle.getProducto().getIdProducto());
                    pstmtDetalle.setInt(3, detalle.getCantidad());
                    pstmtDetalle.setBigDecimal(4, detalle.getPrecioUnitario());
                    pstmtDetalle.setBigDecimal(5, detalle.getSubtotal());
                    pstmtDetalle.setBigDecimal(6, detalle.getDescuento());
                    pstmtDetalle.addBatch();
                }

                pstmtDetalle.executeBatch();
                conn.commit();
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al crear venta: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Error al restaurar autocommit: " + e.getMessage());
            }
        }

        return false;
    }

    // Buscar venta por ID
    public Venta buscarPorId(int idVenta) {
        String sql = "SELECT v.*, u.nombre_usuario, u.nombre, u.apellidos " +
                "FROM ventas v " +
                "INNER JOIN usuarios u ON v.id_usuario = u.id_usuario " +
                "WHERE v.id_venta = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idVenta);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Venta venta = mapearVenta(rs);
                cargarDetallesVenta(venta);
                return venta;
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar venta por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Buscar venta por número
    public Venta buscarPorNumero(String numeroVenta) {
        String sql = "SELECT v.*, u.nombre_usuario, u.nombre, u.apellidos " +
                "FROM ventas v " +
                "INNER JOIN usuarios u ON v.id_usuario = u.id_usuario " +
                "WHERE v.numero_venta = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, numeroVenta);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Venta venta = mapearVenta(rs);
                cargarDetallesVenta(venta);
                return venta;
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar venta por número: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Listar todas las ventas
    public List<Venta> listarTodas() {
        return listarVentas(null, null, null);
    }

    // Listar ventas por fecha
    public List<Venta> listarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return listarVentas(fechaInicio, fechaFin, null);
    }

    // Listar ventas por usuario
    public List<Venta> listarPorUsuario(int idUsuario) {
        return listarVentas(null, null, idUsuario);
    }

    // Listar ventas con filtros
    public List<Venta> listarVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin, Integer idUsuario) {
        List<Venta> ventas = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT v.*, u.nombre_usuario, u.nombre, u.apellidos " +
                        "FROM ventas v " +
                        "INNER JOIN usuarios u ON v.id_usuario = u.id_usuario " +
                        "WHERE 1=1 ");

        if (fechaInicio != null) {
            sql.append("AND v.fecha_venta >= ? ");
        }
        if (fechaFin != null) {
            sql.append("AND v.fecha_venta <= ? ");
        }
        if (idUsuario != null) {
            sql.append("AND v.id_usuario = ? ");
        }

        sql.append("ORDER BY v.fecha_venta DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (fechaInicio != null) {
                pstmt.setTimestamp(paramIndex++, Timestamp.valueOf(fechaInicio));
            }
            if (fechaFin != null) {
                pstmt.setTimestamp(paramIndex++, Timestamp.valueOf(fechaFin));
            }
            if (idUsuario != null) {
                pstmt.setInt(paramIndex++, idUsuario);
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Venta venta = mapearVenta(rs);
                cargarDetallesVenta(venta);
                ventas.add(venta);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar ventas: " + e.getMessage());
            e.printStackTrace();
        }

        return ventas;
    }

    // Anular venta
    public boolean anular(int idVenta, String motivo, int usuarioAnula) {
        String sql = "UPDATE ventas SET estado = 'ANULADA', motivo_anulacion = ?, " +
                "fecha_anulacion = CURRENT_TIMESTAMP, anulado_por = ? WHERE id_venta = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, motivo);
            pstmt.setInt(2, usuarioAnula);
            pstmt.setInt(3, idVenta);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al anular venta: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Obtener estadísticas de ventas
    public VentaEstadisticas obtenerEstadisticas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        String sql = "SELECT COUNT(*) as total_ventas, SUM(total) as total_ingresos, " +
                "AVG(total) as promedio_venta FROM ventas " +
                "WHERE fecha_venta BETWEEN ? AND ? AND estado = 'COMPLETADA'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
            pstmt.setTimestamp(2, Timestamp.valueOf(fechaFin));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new VentaEstadisticas(
                        rs.getInt("total_ventas"),
                        rs.getBigDecimal("total_ingresos"),
                        rs.getBigDecimal("promedio_venta")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
            e.printStackTrace();
        }

        return new VentaEstadisticas(0, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    // Obtener productos más vendidos
    public List<ProductoVendido> obtenerProductosMasVendidos(LocalDateTime fechaInicio, LocalDateTime fechaFin, int limite) {
        List<ProductoVendido> productos = new ArrayList<>();
        String sql = "SELECT p.nombre, SUM(dv.cantidad) as total_vendido, " +
                "SUM(dv.subtotal) as total_ingresos " +
                "FROM detalle_ventas dv " +
                "INNER JOIN productos p ON dv.id_producto = p.id_producto " +
                "INNER JOIN ventas v ON dv.id_venta = v.id_venta " +
                "WHERE v.fecha_venta BETWEEN ? AND ? AND v.estado = 'COMPLETADA' " +
                "GROUP BY p.id_producto, p.nombre " +
                "ORDER BY total_vendido DESC " +
                "LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
            pstmt.setTimestamp(2, Timestamp.valueOf(fechaFin));
            pstmt.setInt(3, limite);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                productos.add(new ProductoVendido(
                        rs.getString("nombre"),
                        rs.getInt("total_vendido"),
                        rs.getBigDecimal("total_ingresos")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener productos más vendidos: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    // Métodos privados auxiliares
    private void cargarDetallesVenta(Venta venta) {
        String sql = "SELECT dv.*, p.nombre, p.codigo_barras, c.nombre as categoria_nombre " +
                "FROM detalle_ventas dv " +
                "INNER JOIN productos p ON dv.id_producto = p.id_producto " +
                "INNER JOIN categorias c ON p.id_categoria = c.id_categoria " +
                "WHERE dv.id_venta = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, venta.getIdVenta());
            ResultSet rs = pstmt.executeQuery();

            List<DetalleVenta> detalles = new ArrayList<>();
            while (rs.next()) {
                DetalleVenta detalle = mapearDetalleVenta(rs);
                detalles.add(detalle);
            }

            venta.setDetalles(detalles);

        } catch (SQLException e) {
            System.err.println("Error al cargar detalles de venta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta venta = new Venta();
        venta.setIdVenta(rs.getInt("id_venta"));
        venta.setNumeroVenta(rs.getString("numero_venta"));

        Timestamp fechaVenta = rs.getTimestamp("fecha_venta");
        if (fechaVenta != null) {
            venta.setFechaVenta(fechaVenta.toLocalDateTime());
        }

        // Mapear usuario básico
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombreUsuario(rs.getString("nombre_usuario"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellidos(rs.getString("apellidos"));
        venta.setUsuario(usuario);

        venta.setMetodoPago(rs.getString("metodo_pago"));
        venta.setSubtotal(rs.getBigDecimal("subtotal"));
        venta.setImpuestos(rs.getBigDecimal("impuestos"));
        venta.setTotal(rs.getBigDecimal("total"));
        venta.setEstado(rs.getString("estado"));
        venta.setObservaciones(rs.getString("observaciones"));
        venta.setReferenciaTransferencia(rs.getString("referencia_transferencia"));

        // Fechas de anulación
        Timestamp fechaAnulacion = rs.getTimestamp("fecha_anulacion");
        if (fechaAnulacion != null) {
            venta.setFechaAnulacion(fechaAnulacion.toLocalDateTime());
        }
        venta.setMotivoAnulacion(rs.getString("motivo_anulacion"));

        Object anuladoPor = rs.getObject("anulado_por");
        if (anuladoPor != null) {
            venta.setAnuladoPor((Integer) anuladoPor);
        }

        return venta;
    }

    private DetalleVenta mapearDetalleVenta(ResultSet rs) throws SQLException {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setIdDetalle(rs.getInt("id_detalle"));
        detalle.setCantidad(rs.getInt("cantidad"));
        detalle.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        detalle.setSubtotal(rs.getBigDecimal("subtotal"));
        detalle.setDescuento(rs.getBigDecimal("descuento"));

        // Mapear producto básico
        Producto producto = new Producto();
        producto.setIdProducto(rs.getInt("id_producto"));
        producto.setNombre(rs.getString("nombre"));
        producto.setCodigoBarras(rs.getString("codigo_barras"));

        // Categoría básica
        Categoria categoria = new Categoria();
        categoria.setNombre(rs.getString("categoria_nombre"));
        producto.setCategoria(categoria);

        detalle.setProducto(producto);

        return detalle;
    }

    // Clases auxiliares para estadísticas
    public static class VentaEstadisticas {
        private int totalVentas;
        private BigDecimal totalIngresos;
        private BigDecimal promedioVenta;

        public VentaEstadisticas(int totalVentas, BigDecimal totalIngresos, BigDecimal promedioVenta) {
            this.totalVentas = totalVentas;
            this.totalIngresos = totalIngresos != null ? totalIngresos : BigDecimal.ZERO;
            this.promedioVenta = promedioVenta != null ? promedioVenta : BigDecimal.ZERO;
        }

        // Getters
        public int getTotalVentas() { return totalVentas; }
        public BigDecimal getTotalIngresos() { return totalIngresos; }
        public BigDecimal getPromedioVenta() { return promedioVenta; }
    }

    public static class ProductoVendido {
        private String nombre;
        private int cantidadVendida;
        private BigDecimal totalIngresos;

        public ProductoVendido(String nombre, int cantidadVendida, BigDecimal totalIngresos) {
            this.nombre = nombre;
            this.cantidadVendida = cantidadVendida;
            this.totalIngresos = totalIngresos != null ? totalIngresos : BigDecimal.ZERO;
        }

        // Getters
        public String getNombre() { return nombre; }
        public int getCantidadVendida() { return cantidadVendida; }
        public BigDecimal getTotalIngresos() { return totalIngresos; }
    }
}