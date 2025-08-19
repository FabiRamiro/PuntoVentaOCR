package com.pos.puntoventaocr.dao;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.models.ComprobanteOCR;
import com.pos.puntoventaocr.models.ComprobanteOCR.EstadoOCR;
import com.pos.puntoventaocr.models.Venta;
import com.pos.puntoventaocr.models.Usuario;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ComprobanteOCRDAO {
    private VentaDAO ventaDAO;
    private UsuarioDAO usuarioDAO;

    public ComprobanteOCRDAO() {
        this.ventaDAO = new VentaDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    // Crear nuevo comprobante OCR
    public boolean crear(ComprobanteOCR comprobante) {
        String sql = "INSERT INTO comprobantes_ocr (id_venta, imagen_original, imagen_procesada, " +
                "banco_emisor, cuenta_remitente, monto_detectado, fecha_transferencia, " +
                "referencia_operacion, nombre_beneficiario, estado_validacion, datos_extraidos, " +
                "fecha_procesamiento, observaciones) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, comprobante.getVenta().getIdVenta());
            pstmt.setString(2, comprobante.getImagenOriginal());
            pstmt.setString(3, comprobante.getImagenProcesada());
            pstmt.setString(4, comprobante.getBancoEmisor());
            pstmt.setString(5, comprobante.getCuentaRemitente());
            pstmt.setBigDecimal(6, comprobante.getMontoDetectado());

            if (comprobante.getFechaTransferencia() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(comprobante.getFechaTransferencia()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }

            pstmt.setString(8, comprobante.getReferenciaOperacion());
            pstmt.setString(9, comprobante.getNombreBeneficiario());
            pstmt.setString(10, comprobante.getEstadoValidacion().name());
            pstmt.setString(11, comprobante.getDatosExtraidos());
            pstmt.setTimestamp(12, Timestamp.valueOf(comprobante.getFechaProcesamiento()));
            pstmt.setString(13, comprobante.getObservaciones());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    comprobante.setIdComprobante(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al crear comprobante OCR: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Actualizar comprobante OCR
    public boolean actualizar(ComprobanteOCR comprobante) {
        String sql = "UPDATE comprobantes_ocr SET imagen_procesada = ?, banco_emisor = ?, " +
                "cuenta_remitente = ?, monto_detectado = ?, fecha_transferencia = ?, " +
                "referencia_operacion = ?, nombre_beneficiario = ?, estado_validacion = ?, " +
                "datos_extraidos = ?, usuario_validador = ?, observaciones = ? " +
                "WHERE id_comprobante = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, comprobante.getImagenProcesada());
            pstmt.setString(2, comprobante.getBancoEmisor());
            pstmt.setString(3, comprobante.getCuentaRemitente());
            pstmt.setBigDecimal(4, comprobante.getMontoDetectado());

            if (comprobante.getFechaTransferencia() != null) {
                pstmt.setTimestamp(5, Timestamp.valueOf(comprobante.getFechaTransferencia()));
            } else {
                pstmt.setNull(5, Types.TIMESTAMP);
            }

            pstmt.setString(6, comprobante.getReferenciaOperacion());
            pstmt.setString(7, comprobante.getNombreBeneficiario());
            pstmt.setString(8, comprobante.getEstadoValidacion().name());
            pstmt.setString(9, comprobante.getDatosExtraidos());

            if (comprobante.getUsuarioValidador() != null) {
                pstmt.setInt(10, comprobante.getUsuarioValidador().getIdUsuario());
            } else {
                pstmt.setNull(10, Types.INTEGER);
            }

            pstmt.setString(11, comprobante.getObservaciones());
            pstmt.setInt(12, comprobante.getIdComprobante());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar comprobante OCR: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Buscar comprobante por ID
    public ComprobanteOCR buscarPorId(int idComprobante) {
        String sql = "SELECT co.*, v.numero_venta, v.total as venta_total, " +
                "u.nombre_usuario, u.nombre as usuario_nombre, u.apellidos as usuario_apellidos " +
                "FROM comprobantes_ocr co " +
                "INNER JOIN ventas v ON co.id_venta = v.id_venta " +
                "LEFT JOIN usuarios u ON co.usuario_validador = u.id_usuario " +
                "WHERE co.id_comprobante = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idComprobante);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearComprobanteOCR(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar comprobante OCR por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Buscar comprobante por referencia
    public ComprobanteOCR buscarPorReferencia(String referencia) {
        String sql = "SELECT co.*, v.numero_venta, v.total as venta_total, " +
                "u.nombre_usuario, u.nombre as usuario_nombre, u.apellidos as usuario_apellidos " +
                "FROM comprobantes_ocr co " +
                "INNER JOIN ventas v ON co.id_venta = v.id_venta " +
                "LEFT JOIN usuarios u ON co.usuario_validador = u.id_usuario " +
                "WHERE co.referencia_operacion = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, referencia);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearComprobanteOCR(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar comprobante por referencia: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Listar comprobantes por estado
    public List<ComprobanteOCR> listarPorEstado(EstadoOCR estado) {
        List<ComprobanteOCR> comprobantes = new ArrayList<>();
        String sql = "SELECT co.*, v.numero_venta, v.total as venta_total, " +
                "u.nombre_usuario, u.nombre as usuario_nombre, u.apellidos as usuario_apellidos " +
                "FROM comprobantes_ocr co " +
                "INNER JOIN ventas v ON co.id_venta = v.id_venta " +
                "LEFT JOIN usuarios u ON co.usuario_validador = u.id_usuario " +
                "WHERE co.estado_validacion = ? " +
                "ORDER BY co.fecha_procesamiento DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, estado.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                comprobantes.add(mapearComprobanteOCR(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar comprobantes por estado: " + e.getMessage());
            e.printStackTrace();
        }

        return comprobantes;
    }

    // Listar comprobantes pendientes de validación
    public List<ComprobanteOCR> listarPendientes() {
        return listarPorEstado(EstadoOCR.PENDIENTE);
    }

    // Listar todos los comprobantes
    public List<ComprobanteOCR> listarTodos() {
        List<ComprobanteOCR> comprobantes = new ArrayList<>();
        String sql = "SELECT co.*, v.numero_venta, v.total as venta_total, " +
                "u.nombre_usuario, u.nombre as usuario_nombre, u.apellidos as usuario_apellidos " +
                "FROM comprobantes_ocr co " +
                "INNER JOIN ventas v ON co.id_venta = v.id_venta " +
                "LEFT JOIN usuarios u ON co.usuario_validador = u.id_usuario " +
                "ORDER BY co.fecha_procesamiento DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                comprobantes.add(mapearComprobanteOCR(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar comprobantes OCR: " + e.getMessage());
            e.printStackTrace();
        }

        return comprobantes;
    }

    // Listar comprobantes por fecha
    public List<ComprobanteOCR> listarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<ComprobanteOCR> comprobantes = new ArrayList<>();
        String sql = "SELECT co.*, v.numero_venta, v.total as venta_total, " +
                "u.nombre_usuario, u.nombre as usuario_nombre, u.apellidos as usuario_apellidos " +
                "FROM comprobantes_ocr co " +
                "INNER JOIN ventas v ON co.id_venta = v.id_venta " +
                "LEFT JOIN usuarios u ON co.usuario_validador = u.id_usuario " +
                "WHERE co.fecha_procesamiento BETWEEN ? AND ? " +
                "ORDER BY co.fecha_procesamiento DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
            pstmt.setTimestamp(2, Timestamp.valueOf(fechaFin));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                comprobantes.add(mapearComprobanteOCR(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar comprobantes por fecha: " + e.getMessage());
            e.printStackTrace();
        }

        return comprobantes;
    }

    // Aprobar comprobante
    public boolean aprobar(int idComprobante, int idUsuarioValidador) {
        String sql = "UPDATE comprobantes_ocr SET estado_validacion = ?, usuario_validador = ? " +
                "WHERE id_comprobante = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, EstadoOCR.VALIDADO.name());
            pstmt.setInt(2, idUsuarioValidador);
            pstmt.setInt(3, idComprobante);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al aprobar comprobante: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Rechazar comprobante
    public boolean rechazar(int idComprobante, int idUsuarioValidador, String motivo) {
        String sql = "UPDATE comprobantes_ocr SET estado_validacion = ?, usuario_validador = ?, " +
                "observaciones = ? WHERE id_comprobante = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, EstadoOCR.RECHAZADO.name());
            pstmt.setInt(2, idUsuarioValidador);
            pstmt.setString(3, motivo);
            pstmt.setInt(4, idComprobante);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al rechazar comprobante: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Verificar si existe una referencia duplicada
    public boolean existeReferencia(String referencia) {
        String sql = "SELECT COUNT(*) FROM comprobantes_ocr WHERE referencia_operacion = ? " +
                "AND estado_validacion != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, referencia);
            pstmt.setString(2, EstadoOCR.RECHAZADO.name());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar referencia duplicada: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Eliminar comprobante OCR
    public boolean eliminar(int idComprobante) {
        String sql = "DELETE FROM comprobantes_ocr WHERE id_comprobante = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idComprobante);
            int filasAfectadas = pstmt.executeUpdate();

            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar comprobante: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Buscar comprobantes con filtros
    public List<ComprobanteOCR> buscarConFiltros(java.time.LocalDate fechaInicio, 
                                                  java.time.LocalDate fechaFin, 
                                                  EstadoOCR estadoFiltro) {
        List<ComprobanteOCR> comprobantes = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT co.*, v.numero_venta, v.total as venta_total, " +
                "u.nombre_usuario, u.nombre as usuario_nombre, u.apellidos as usuario_apellidos " +
                "FROM comprobantes_ocr co " +
                "INNER JOIN ventas v ON co.id_venta = v.id_venta " +
                "LEFT JOIN usuarios u ON co.usuario_validador = u.id_usuario " +
                "WHERE 1=1 ");

        if (fechaInicio != null) {
            sql.append("AND DATE(co.fecha_procesamiento) >= ? ");
        }
        if (fechaFin != null) {
            sql.append("AND DATE(co.fecha_procesamiento) <= ? ");
        }
        if (estadoFiltro != null) {
            sql.append("AND co.estado_validacion = ? ");
        }

        sql.append("ORDER BY co.fecha_procesamiento DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (fechaInicio != null) {
                pstmt.setDate(paramIndex++, java.sql.Date.valueOf(fechaInicio));
            }
            if (fechaFin != null) {
                pstmt.setDate(paramIndex++, java.sql.Date.valueOf(fechaFin));
            }
            if (estadoFiltro != null) {
                pstmt.setString(paramIndex++, estadoFiltro.name());
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ComprobanteOCR comprobante = mapearComprobanteOCR(rs);
                comprobantes.add(comprobante);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar comprobantes con filtros: " + e.getMessage());
            e.printStackTrace();
        }

        return comprobantes;
    }

    // Obtener estadísticas de comprobantes
    public EstadisticasOCR obtenerEstadisticas() {
        String sql = "SELECT " +
                "SUM(CASE WHEN estado_validacion = 'PENDIENTE' THEN 1 ELSE 0 END) as pendientes, " +
                "SUM(CASE WHEN estado_validacion = 'VALIDADO' THEN 1 ELSE 0 END) as validados, " +
                "SUM(CASE WHEN estado_validacion = 'RECHAZADO' THEN 1 ELSE 0 END) as rechazados, " +
                "SUM(CASE WHEN estado_validacion = 'ERROR_PROCESAMIENTO' THEN 1 ELSE 0 END) as errores, " +
                "COUNT(*) as total " +
                "FROM comprobantes_ocr";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return new EstadisticasOCR(
                        rs.getInt("pendientes"),
                        rs.getInt("validados"),
                        rs.getInt("rechazados"),
                        rs.getInt("errores"),
                        rs.getInt("total")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas OCR: " + e.getMessage());
            e.printStackTrace();
        }

        return new EstadisticasOCR(0, 0, 0, 0, 0);
    }

    // Mapear ResultSet a ComprobanteOCR
    private ComprobanteOCR mapearComprobanteOCR(ResultSet rs) throws SQLException {
        ComprobanteOCR comprobante = new ComprobanteOCR();

        comprobante.setIdComprobante(rs.getInt("id_comprobante"));
        comprobante.setImagenOriginal(rs.getString("imagen_original"));
        comprobante.setImagenProcesada(rs.getString("imagen_procesada"));
        comprobante.setBancoEmisor(rs.getString("banco_emisor"));
        comprobante.setCuentaRemitente(rs.getString("cuenta_remitente"));
        comprobante.setMontoDetectado(rs.getBigDecimal("monto_detectado"));

        Timestamp fechaTransferencia = rs.getTimestamp("fecha_transferencia");
        if (fechaTransferencia != null) {
            comprobante.setFechaTransferencia(fechaTransferencia.toLocalDateTime());
        }

        comprobante.setReferenciaOperacion(rs.getString("referencia_operacion"));
        comprobante.setNombreBeneficiario(rs.getString("nombre_beneficiario"));
        comprobante.setEstadoValidacion(EstadoOCR.valueOf(rs.getString("estado_validacion")));
        comprobante.setDatosExtraidos(rs.getString("datos_extraidos"));
        comprobante.setFechaProcesamiento(rs.getTimestamp("fecha_procesamiento").toLocalDateTime());
        comprobante.setObservaciones(rs.getString("observaciones"));

        // Mapear venta básica
        Venta venta = new Venta();
        venta.setIdVenta(rs.getInt("id_venta"));
        venta.setNumeroVenta(rs.getString("numero_venta"));
        venta.setTotal(rs.getBigDecimal("venta_total"));
        comprobante.setVenta(venta);

        // Mapear usuario validador si existe
        int idUsuarioValidador = rs.getInt("usuario_validador");
        if (!rs.wasNull()) {
            Usuario usuarioValidador = new Usuario();
            usuarioValidador.setIdUsuario(idUsuarioValidador);
            usuarioValidador.setNombreUsuario(rs.getString("nombre_usuario"));
            usuarioValidador.setNombre(rs.getString("usuario_nombre"));
            usuarioValidador.setApellidos(rs.getString("usuario_apellidos"));
            comprobante.setUsuarioValidador(usuarioValidador);
        }

        return comprobante;
    }

    // Clase para estadísticas OCR
    public static class EstadisticasOCR {
        private int pendientes;
        private int validados;
        private int rechazados;
        private int errores;
        private int total;

        public EstadisticasOCR(int pendientes, int validados, int rechazados, int errores, int total) {
            this.pendientes = pendientes;
            this.validados = validados;
            this.rechazados = rechazados;
            this.errores = errores;
            this.total = total;
        }

        // Getters
        public int getPendientes() { return pendientes; }
        public int getValidados() { return validados; }
        public int getRechazados() { return rechazados; }
        public int getErrores() { return errores; }
        public int getTotal() { return total; }

        public double getPorcentajeValidados() {
            return total > 0 ? (validados * 100.0) / total : 0.0;
        }

        public double getPorcentajeRechazados() {
            return total > 0 ? (rechazados * 100.0) / total : 0.0;
        }

        public double getPorcentajePendientes() {
            return total > 0 ? (pendientes * 100.0) / total : 0.0;
        }
    }
}