package com.pos.puntoventaocr.dao;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.models.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    public boolean guardar(Venta venta) {
        String sqlVenta = "INSERT INTO ventas (numero_venta, fecha, id_usuario, id_cliente, subtotal, " +
                         "iva, total, metodo_pago, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlDetalle = "INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, " +
                           "subtotal, descuento) VALUES (?, ?, ?, ?, ?, ?)";

        String sqlActualizarStock = "UPDATE productos SET stock = stock - ? WHERE id_producto = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Insertar venta
            try (PreparedStatement stmtVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                stmtVenta.setString(1, venta.getNumeroVenta());
                stmtVenta.setTimestamp(2, Timestamp.valueOf(venta.getFecha()));
                stmtVenta.setInt(3, venta.getUsuario().getIdUsuario());
                stmtVenta.setObject(4, venta.getCliente() != null ? venta.getCliente().getIdCliente() : null);
                stmtVenta.setBigDecimal(5, venta.getSubtotal());
                stmtVenta.setBigDecimal(6, venta.getIva());
                stmtVenta.setBigDecimal(7, venta.getTotal());
                stmtVenta.setString(8, venta.getMetodoPago());
                stmtVenta.setString(9, venta.getEstado());

                int filasAfectadas = stmtVenta.executeUpdate();
                if (filasAfectadas == 0) {
                    conn.rollback();
                    return false;
                }

                ResultSet keys = stmtVenta.getGeneratedKeys();
                if (keys.next()) {
                    venta.setIdVenta(keys.getInt(1));
                } else {
                    conn.rollback();
                    return false;
                }
            }

            // Insertar detalles de venta
            try (PreparedStatement stmtDetalle = conn.prepareStatement(sqlDetalle)) {
                for (DetalleVenta detalle : venta.getDetalles()) {
                    stmtDetalle.setInt(1, venta.getIdVenta());
                    stmtDetalle.setInt(2, detalle.getProducto().getIdProducto());
                    stmtDetalle.setBigDecimal(3, detalle.getCantidad());
                    stmtDetalle.setBigDecimal(4, detalle.getPrecioUnitario());
                    stmtDetalle.setBigDecimal(5, detalle.getSubtotal());
                    stmtDetalle.setBigDecimal(6, detalle.getDescuento());
                    stmtDetalle.addBatch();
                }
                stmtDetalle.executeBatch();
            }

            // Actualizar stock de productos
            try (PreparedStatement stmtStock = conn.prepareStatement(sqlActualizarStock)) {
                for (DetalleVenta detalle : venta.getDetalles()) {
                    stmtStock.setInt(1, detalle.getCantidad().intValue());
                    stmtStock.setInt(2, detalle.getProducto().getIdProducto());
                    stmtStock.addBatch();
                }
                stmtStock.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error en rollback: " + ex.getMessage());
                }
            }
            System.err.println("Error guardando venta: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error cerrando conexión: " + e.getMessage());
                }
            }
        }
    }

    public List<Venta> obtenerTodas() {
        String sql = "SELECT v.*, u.nombre as usuario_nombre, u.apellidos as usuario_apellido, " +
                    "c.nombre as cliente_nombre " +
                    "FROM ventas v " +
                    "LEFT JOIN usuarios u ON v.id_usuario = u.id_usuario " +
                    "LEFT JOIN clientes c ON v.id_cliente = c.id_cliente " +
                    "ORDER BY v.fecha DESC";

        List<Venta> ventas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ventas.add(mapearVenta(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo ventas: " + e.getMessage());
        }

        return ventas;
    }

    public List<Venta> obtenerVentasPorFiltros(LocalDate fechaDesde, LocalDate fechaHasta,
                                               String usuario, String estado) {
        StringBuilder sql = new StringBuilder(
            "SELECT v.*, u.nombre as usuario_nombre, u.apellidos as usuario_apellido, " +
            "c.nombre as cliente_nombre " +
            "FROM ventas v " +
            "LEFT JOIN usuarios u ON v.id_usuario = u.id_usuario " +
            "LEFT JOIN clientes c ON v.id_cliente = c.id_cliente " +
            "WHERE 1=1 ");

        List<Object> parametros = new ArrayList<>();

        if (fechaDesde != null) {
            sql.append("AND DATE(v.fecha) >= ? ");
            parametros.add(Date.valueOf(fechaDesde));
        }

        if (fechaHasta != null) {
            sql.append("AND DATE(v.fecha) <= ? ");
            parametros.add(Date.valueOf(fechaHasta));
        }

        if (usuario != null && !"Todos".equals(usuario)) {
            sql.append("AND CONCAT(u.nombre, ' ', u.apellidos) = ? ");
            parametros.add(usuario);
        }

        if (estado != null && !"Todos".equals(estado)) {
            sql.append("AND v.estado = ? ");
            parametros.add(estado);
        }

        sql.append("ORDER BY v.fecha DESC");

        List<Venta> ventas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ventas.add(mapearVenta(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo ventas por filtros: " + e.getMessage());
        }

        return ventas;
    }

    public Venta obtenerPorId(int idVenta) {
        String sql = "SELECT v.*, u.nombre as usuario_nombre, u.apellidos as usuario_apellido, " +
                    "c.nombre as cliente_nombre " +
                    "FROM ventas v " +
                    "LEFT JOIN usuarios u ON v.id_usuario = u.id_usuario " +
                    "LEFT JOIN clientes c ON v.id_cliente = c.id_cliente " +
                    "WHERE v.id_venta = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVenta);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearVenta(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo venta por ID: " + e.getMessage());
        }

        return null;
    }

    public Venta obtenerPorNumero(String numeroVenta) {
        String sql = "SELECT v.*, u.nombre as usuario_nombre, u.apellidos as usuario_apellido, " +
                    "c.nombre as cliente_nombre " +
                    "FROM ventas v " +
                    "LEFT JOIN usuarios u ON v.id_usuario = u.id_usuario " +
                    "LEFT JOIN clientes c ON v.id_cliente = c.id_cliente " +
                    "WHERE v.numero_venta = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroVenta);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearVenta(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo venta por número: " + e.getMessage());
        }

        return null;
    }

    public String generarNumeroVenta() {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(numero_venta, 5) AS UNSIGNED)), 0) + 1 as siguiente " +
                    "FROM ventas WHERE numero_venta LIKE 'VTA-%'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int siguiente = rs.getInt("siguiente");
                return String.format("VTA-%06d", siguiente);
            }

        } catch (SQLException e) {
            System.err.println("Error generando número de venta: " + e.getMessage());
        }

        return "VTA-000001";
    }

    public List<Venta> obtenerPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        String sql = "SELECT v.*, u.nombre as usuario_nombre, u.apellidos as usuario_apellido, " +
                    "c.nombre as cliente_nombre, c.apellidos as cliente_apellidos " +
                    "FROM ventas v " +
                    "INNER JOIN usuarios u ON v.id_usuario = u.id_usuario " +
                    "LEFT JOIN clientes c ON v.id_cliente = c.id_cliente " +
                    "WHERE DATE(v.fecha) BETWEEN ? AND ? " +
                    "ORDER BY v.fecha DESC";

        List<Venta> ventas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fechaInicio));
            stmt.setDate(2, Date.valueOf(fechaFin));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ventas.add(mapearVenta(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo ventas por rango de fechas: " + e.getMessage());
        }

        return ventas;
    }

    public boolean anularVenta(int idVenta, String motivoAnulacion) {
        String sql = "UPDATE ventas SET estado = 'ANULADA', motivo_anulacion = ? " +
                    "WHERE id_venta = ? AND estado = 'COMPLETADA'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, motivoAnulacion);
            stmt.setInt(2, idVenta);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error anulando venta: " + e.getMessage());
            return false;
        }
    }

    public List<String> obtenerUsuarios() {
        String sql = "SELECT DISTINCT CONCAT(u.nombre, ' ', u.apellidos) as nombre_completo " +
                    "FROM ventas v " +
                    "INNER JOIN usuarios u ON v.id_usuario = u.id_usuario " +
                    "ORDER BY nombre_completo";

        List<String> usuarios = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(rs.getString("nombre_completo"));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo usuarios: " + e.getMessage());
        }

        return usuarios;
    }

    /**
     * Obtiene las ventas por transferencia que no han sido validadas con OCR
     * @return Lista de ventas por transferencia pendientes de validación OCR
     */
    public List<Venta> obtenerVentasTransferenciaSinValidarOCR() {
        String sql = "SELECT v.*, u.nombre as usuario_nombre, u.apellidos as usuario_apellido, " +
                    "c.nombre as cliente_nombre " +
                    "FROM ventas v " +
                    "LEFT JOIN usuarios u ON v.id_usuario = u.id_usuario " +
                    "LEFT JOIN clientes c ON v.id_cliente = c.id_cliente " +
                    "LEFT JOIN comprobantes_ocr ocr ON v.id_venta = ocr.id_venta " +
                    "WHERE v.metodo_pago = 'TRANSFERENCIA' " +
                    "AND v.estado = 'COMPLETADA' " +
                    "AND ocr.id_comprobante IS NULL " +
                    "ORDER BY v.fecha DESC";

        List<Venta> ventasPendientes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ventasPendientes.add(mapearVenta(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo ventas por transferencia sin validar OCR: " + e.getMessage());
        }

        return ventasPendientes;
    }

    /**
     * Verifica si una venta tiene comprobante OCR validado
     * @param idVenta ID de la venta a verificar
     * @return true si tiene comprobante validado, false en caso contrario
     */
    public boolean tieneComprobanteOCRValidado(int idVenta) {
        String sql = "SELECT COUNT(*) FROM comprobantes_ocr " +
                    "WHERE id_venta = ? AND estado_validacion = 'VALIDADO'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVenta);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando comprobante OCR: " + e.getMessage());
        }

        return false;
    }

    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta venta = new Venta();

        venta.setIdVenta(rs.getInt("id_venta"));
        venta.setNumeroVenta(rs.getString("numero_venta"));
        venta.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        venta.setSubtotal(rs.getBigDecimal("subtotal"));
        venta.setIva(rs.getBigDecimal("iva"));
        venta.setTotal(rs.getBigDecimal("total"));
        venta.setMetodoPago(rs.getString("metodo_pago"));
        venta.setEstado(rs.getString("estado"));

        // Mapear usuario
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombre(rs.getString("usuario_nombre"));
        usuario.setApellido(rs.getString("usuario_apellido"));
        venta.setUsuario(usuario);

        // Mapear cliente si existe
        int idCliente = rs.getInt("id_cliente");
        if (!rs.wasNull()) {
            Cliente cliente = new Cliente();
            cliente.setIdCliente(idCliente);
            cliente.setNombre(rs.getString("cliente_nombre"));
            venta.setCliente(cliente);
        }

        return venta;
    }
}
