package com.pos.puntoventaocr.dao;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.models.Devolucion;
import com.pos.puntoventaocr.models.Venta;
import com.pos.puntoventaocr.models.Usuario;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DevolucionDAO {

    private VentaDAO ventaDAO;
    private UsuarioDAO usuarioDAO;

    public DevolucionDAO() {
        this.ventaDAO = new VentaDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    public boolean guardar(Devolucion devolucion) {
        String sql = "INSERT INTO devoluciones (numero_devolucion, id_venta_original, motivo, " +
                    "monto_total, estado, id_procesado_por, observaciones, fecha_creacion) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, devolucion.getNumeroDevolucion());
            stmt.setInt(2, devolucion.getVentaOriginal().getIdVenta());
            stmt.setString(3, devolucion.getMotivo());
            stmt.setBigDecimal(4, devolucion.getMontoTotal());
            stmt.setString(5, devolucion.getEstado());
            stmt.setInt(6, devolucion.getProcesadoPor().getIdUsuario());
            stmt.setString(7, devolucion.getObservaciones());
            stmt.setTimestamp(8, Timestamp.valueOf(devolucion.getFechaCreacion()));

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    devolucion.setIdDevolucion(keys.getInt(1));
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error guardando devolución: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Devolucion devolucion) {
        String sql = "UPDATE devoluciones SET estado = ?, id_autorizado_por = ?, " +
                    "observaciones = ?, fecha_procesamiento = ?, fecha_devolucion = ? " +
                    "WHERE id_devolucion = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, devolucion.getEstado());
            stmt.setObject(2, devolucion.getAutorizadoPor() != null ? 
                         devolucion.getAutorizadoPor().getIdUsuario() : null);
            stmt.setString(3, devolucion.getObservaciones());
            stmt.setTimestamp(4, devolucion.getFechaProcesamiento() != null ? 
                            Timestamp.valueOf(devolucion.getFechaProcesamiento()) : null);
            stmt.setTimestamp(5, devolucion.getFechaDevolucion() != null ? 
                            Timestamp.valueOf(devolucion.getFechaDevolucion()) : null);
            stmt.setInt(6, devolucion.getIdDevolucion());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando devolución: " + e.getMessage());
            return false;
        }
    }

    public List<Devolucion> obtenerTodas() {
        String sql = "SELECT d.*, v.numero_venta, v.total as venta_total, " +
                    "up.nombre as procesado_nombre, up.apellidos as procesado_apellido, " +
                    "ua.nombre as autorizado_nombre, ua.apellidos as autorizado_apellido " +
                    "FROM devoluciones d " +
                    "LEFT JOIN ventas v ON d.id_venta_original = v.id_venta " +
                    "LEFT JOIN usuarios up ON d.id_procesado_por = up.id_usuario " +
                    "LEFT JOIN usuarios ua ON d.id_autorizado_por = ua.id_usuario " +
                    "ORDER BY d.fecha_creacion DESC";

        List<Devolucion> devoluciones = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                devoluciones.add(mapearDevolucion(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo devoluciones: " + e.getMessage());
        }

        return devoluciones;
    }

    public List<Devolucion> obtenerPorEstado(String estado) {
        String sql = "SELECT d.*, v.numero_venta, v.total as venta_total, " +
                    "up.nombre as procesado_nombre, up.apellidos as procesado_apellido, " +
                    "ua.nombre as autorizado_nombre, ua.apellidos as autorizado_apellido " +
                    "FROM devoluciones d " +
                    "LEFT JOIN ventas v ON d.id_venta_original = v.id_venta " +
                    "LEFT JOIN usuarios up ON d.id_procesado_por = up.id_usuario " +
                    "LEFT JOIN usuarios ua ON d.id_autorizado_por = ua.id_usuario " +
                    "WHERE d.estado = ? " +
                    "ORDER BY d.fecha_creacion DESC";

        List<Devolucion> devoluciones = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                devoluciones.add(mapearDevolucion(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo devoluciones por estado: " + e.getMessage());
        }

        return devoluciones;
    }

    public List<Devolucion> obtenerPorRangoFechas(LocalDate fechaDesde, LocalDate fechaHasta) {
        String sql = "SELECT d.*, v.numero_venta, v.total as venta_total, " +
                    "up.nombre as procesado_nombre, up.apellidos as procesado_apellido, " +
                    "ua.nombre as autorizado_nombre, ua.apellidos as autorizado_apellido " +
                    "FROM devoluciones d " +
                    "LEFT JOIN ventas v ON d.id_venta_original = v.id_venta " +
                    "LEFT JOIN usuarios up ON d.id_procesado_por = up.id_usuario " +
                    "LEFT JOIN usuarios ua ON d.id_autorizado_por = ua.id_usuario " +
                    "WHERE DATE(d.fecha_creacion) BETWEEN ? AND ? " +
                    "ORDER BY d.fecha_creacion DESC";

        List<Devolucion> devoluciones = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fechaDesde));
            stmt.setDate(2, Date.valueOf(fechaHasta));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                devoluciones.add(mapearDevolucion(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo devoluciones por fechas: " + e.getMessage());
        }

        return devoluciones;
    }

    public Devolucion obtenerPorNumero(String numeroDevolucion) {
        String sql = "SELECT d.*, v.numero_venta, v.total as venta_total, " +
                    "up.nombre as procesado_nombre, up.apellidos as procesado_apellido, " +
                    "ua.nombre as autorizado_nombre, ua.apellidos as autorizado_apellido " +
                    "FROM devoluciones d " +
                    "LEFT JOIN ventas v ON d.id_venta_original = v.id_venta " +
                    "LEFT JOIN usuarios up ON d.id_procesado_por = up.id_usuario " +
                    "LEFT JOIN usuarios ua ON d.id_autorizado_por = ua.id_usuario " +
                    "WHERE d.numero_devolucion = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroDevolucion);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearDevolucion(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo devolución por número: " + e.getMessage());
        }

        return null;
    }

    private Devolucion mapearDevolucion(ResultSet rs) throws SQLException {
        Devolucion devolucion = new Devolucion();

        devolucion.setIdDevolucion(rs.getInt("id_devolucion"));
        devolucion.setNumeroDevolucion(rs.getString("numero_devolucion"));
        devolucion.setMotivo(rs.getString("motivo"));
        devolucion.setMontoTotal(rs.getBigDecimal("monto_total"));
        devolucion.setEstado(rs.getString("estado"));
        devolucion.setObservaciones(rs.getString("observaciones"));

        // Fechas
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            devolucion.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }

        Timestamp fechaProcesamiento = rs.getTimestamp("fecha_procesamiento");
        if (fechaProcesamiento != null) {
            devolucion.setFechaProcesamiento(fechaProcesamiento.toLocalDateTime());
        }

        Timestamp fechaDevolucion = rs.getTimestamp("fecha_devolucion");
        if (fechaDevolucion != null) {
            devolucion.setFechaDevolucion(fechaDevolucion.toLocalDateTime());
        }

        // Venta original
        Venta venta = new Venta();
        venta.setIdVenta(rs.getInt("id_venta_original"));
        venta.setNumeroVenta(rs.getString("numero_venta"));
        venta.setTotal(rs.getBigDecimal("venta_total"));
        devolucion.setVentaOriginal(venta);

        // Usuario que procesó
        int idProcesadoPor = rs.getInt("id_procesado_por");
        if (!rs.wasNull()) {
            Usuario procesadoPor = new Usuario();
            procesadoPor.setIdUsuario(idProcesadoPor);
            procesadoPor.setNombre(rs.getString("procesado_nombre"));
            procesadoPor.setApellido(rs.getString("procesado_apellido"));
            devolucion.setProcesadoPor(procesadoPor);
        }

        // Usuario que autorizó
        int idAutorizadoPor = rs.getInt("id_autorizado_por");
        if (!rs.wasNull()) {
            Usuario autorizadoPor = new Usuario();
            autorizadoPor.setIdUsuario(idAutorizadoPor);
            autorizadoPor.setNombre(rs.getString("autorizado_nombre"));
            autorizadoPor.setApellido(rs.getString("autorizado_apellido"));
            devolucion.setAutorizadoPor(autorizadoPor);
        }

        return devolucion;
    }
}
