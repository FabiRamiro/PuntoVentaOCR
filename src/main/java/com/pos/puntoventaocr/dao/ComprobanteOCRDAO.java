package com.pos.puntoventaocr.dao;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.models.ComprobanteOCR;
import com.pos.puntoventaocr.models.Usuario;
import com.pos.puntoventaocr.models.Venta;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ComprobanteOCRDAO {

    public boolean guardar(ComprobanteOCR comprobante) {
        String sql = "INSERT INTO comprobantes_ocr (id_venta, imagen_original, imagen_procesada, " +
                    "banco_emisor, cuenta_remitente, monto_detectado, fecha_transferencia, " +
                    "referencia_operacion, nombre_beneficiario, estado_validacion, datos_extraidos, " +
                    "id_usuario_validador, observaciones) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setObject(1, comprobante.getVenta() != null ? comprobante.getVenta().getIdVenta() : null);
            stmt.setString(2, comprobante.getImagenOriginal());
            stmt.setString(3, comprobante.getImagenProcesada());
            stmt.setString(4, comprobante.getBancoEmisor());
            stmt.setString(5, comprobante.getCuentaRemitente());
            stmt.setBigDecimal(6, comprobante.getMontoDetectado());
            stmt.setObject(7, comprobante.getFechaTransferencia() != null ?
                          Date.valueOf(comprobante.getFechaTransferencia()) : null);
            stmt.setString(8, comprobante.getReferenciaOperacion());
            stmt.setString(9, comprobante.getNombreBeneficiario());
            stmt.setString(10, comprobante.getEstadoValidacion());

            // Validar que datos_extraidos sea JSON válido antes de guardar
            String datosExtraidos = comprobante.getDatosExtraidos();
            if (datosExtraidos == null || datosExtraidos.trim().isEmpty()) {
                datosExtraidos = "{}";
            }

            // Verificar que el JSON sea válido antes de guardarlo
            if (!esJSONValido(datosExtraidos)) {
                System.err.println("⚠️ WARNING: Datos extraídos no son JSON válido, creando JSON seguro");
                datosExtraidos = "{\"error\": \"Datos no válidos como JSON\", \"texto_raw\": \"Ver observaciones\"}";
                // Agregar el texto original a las observaciones si hay espacio
                String observacionesActuales = comprobante.getObservaciones();
                if (observacionesActuales == null) {
                    observacionesActuales = "";
                }
                observacionesActuales += (observacionesActuales.isEmpty() ? "" : " | ") +
                                       "Texto OCR original disponible en logs";
                comprobante.setObservaciones(observacionesActuales);
            }

            stmt.setString(11, datosExtraidos);
            stmt.setObject(12, comprobante.getUsuarioValidador() != null ?
                          comprobante.getUsuarioValidador().getIdUsuario() : null);
            stmt.setString(13, comprobante.getObservaciones());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    comprobante.setIdComprobante(keys.getInt(1));
                }
                System.out.println("✅ Comprobante OCR guardado exitosamente con ID: " + comprobante.getIdComprobante());
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("❌ Error guardando comprobante OCR: " + e.getMessage());

            // Proporcionar información más específica sobre errores de JSON
            if (e.getMessage().contains("Invalid JSON") || e.getMessage().contains("Data truncation")) {
                System.err.println("🔍 Error relacionado con JSON en datos_extraidos:");
                System.err.println("   - Verificar que el texto extraído esté bien formateado");
                System.err.println("   - El campo datos_extraidos debe contener JSON válido");
                String datosDebug = comprobante.getDatosExtraidos();
                if (datosDebug != null && datosDebug.length() > 100) {
                    System.err.println("   - Primeros 100 caracteres: " + datosDebug.substring(0, 100));
                }
            }

            e.printStackTrace();
            return false;
        }
    }

    public List<ComprobanteOCR> obtenerTodos() {
        String sql = "SELECT c.*, v.numero_venta, u.nombre as usuario_nombre, u.apellidos as usuario_apellido " +
                    "FROM comprobantes_ocr c " +
                    "LEFT JOIN ventas v ON c.id_venta = v.id_venta " +
                    "LEFT JOIN usuarios u ON c.id_usuario_validador = u.id_usuario " +
                    "ORDER BY c.fecha_procesamiento DESC";

        List<ComprobanteOCR> comprobantes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                comprobantes.add(mapearComprobante(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo comprobantes OCR: " + e.getMessage());
        }

        return comprobantes;
    }

    public List<ComprobanteOCR> obtenerPorFiltros(LocalDate fechaDesde, LocalDate fechaHasta,
                                                  String estado, String usuario) {
        StringBuilder sql = new StringBuilder(
            "SELECT c.*, v.numero_venta, u.nombre as usuario_nombre, u.apellidos as usuario_apellido " +
            "FROM comprobantes_ocr c " +
            "LEFT JOIN ventas v ON c.id_venta = v.id_venta " +
            "LEFT JOIN usuarios u ON c.id_usuario_validador = u.id_usuario " +
            "WHERE 1=1 ");

        List<Object> parametros = new ArrayList<>();

        if (fechaDesde != null) {
            sql.append("AND DATE(c.fecha_procesamiento) >= ? ");
            parametros.add(Date.valueOf(fechaDesde));
        }

        if (fechaHasta != null) {
            sql.append("AND DATE(c.fecha_procesamiento) <= ? ");
            parametros.add(Date.valueOf(fechaHasta));
        }

        if (estado != null && !"Todos".equals(estado)) {
            sql.append("AND c.estado_validacion = ? ");
            parametros.add(estado);
        }

        if (usuario != null && !"Todos".equals(usuario)) {
            sql.append("AND CONCAT(u.nombre, ' ', u.apellidos) = ? ");
            parametros.add(usuario);
        }

        sql.append("ORDER BY c.fecha_procesamiento DESC");

        List<ComprobanteOCR> comprobantes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                comprobantes.add(mapearComprobante(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo comprobantes por filtros: " + e.getMessage());
        }

        return comprobantes;
    }

    public boolean actualizar(ComprobanteOCR comprobante) {
        String sql = "UPDATE comprobantes_ocr SET estado_validacion = ?, id_usuario_validador = ?, " +
                    "observaciones = ?, fecha_validacion = CURRENT_TIMESTAMP " +
                    "WHERE id_comprobante = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, comprobante.getEstadoValidacion());
            stmt.setObject(2, comprobante.getUsuarioValidador() != null ?
                          comprobante.getUsuarioValidador().getIdUsuario() : null);
            stmt.setString(3, comprobante.getObservaciones());
            stmt.setInt(4, comprobante.getIdComprobante());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando comprobante OCR: " + e.getMessage());
            return false;
        }
    }

    public boolean existeReferencia(String referencia) {
        if (referencia == null || referencia.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM comprobantes_ocr WHERE referencia_operacion = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referencia.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private ComprobanteOCR mapearComprobante(ResultSet rs) throws SQLException {
        ComprobanteOCR comprobante = new ComprobanteOCR();

        comprobante.setIdComprobante(rs.getInt("id_comprobante"));
        comprobante.setImagenOriginal(rs.getString("imagen_original"));
        comprobante.setImagenProcesada(rs.getString("imagen_procesada"));
        comprobante.setBancoEmisor(rs.getString("banco_emisor"));
        comprobante.setCuentaRemitente(rs.getString("cuenta_remitente"));
        comprobante.setMontoDetectado(rs.getBigDecimal("monto_detectado"));

        Date fechaTransferencia = rs.getDate("fecha_transferencia");
        if (fechaTransferencia != null) {
            comprobante.setFechaTransferencia(fechaTransferencia.toLocalDate());
        }

        comprobante.setReferenciaOperacion(rs.getString("referencia_operacion"));
        comprobante.setNombreBeneficiario(rs.getString("nombre_beneficiario"));
        comprobante.setEstadoValidacion(rs.getString("estado_validacion"));
        comprobante.setDatosExtraidos(rs.getString("datos_extraidos"));
        comprobante.setObservaciones(rs.getString("observaciones"));
        comprobante.setFechaProcesamiento(rs.getTimestamp("fecha_procesamiento").toLocalDateTime());

        // Mapear venta si existe
        int idVenta = rs.getInt("id_venta");
        if (!rs.wasNull()) {
            Venta venta = new Venta();
            venta.setIdVenta(idVenta);
            venta.setNumeroVenta(rs.getString("numero_venta"));
            comprobante.setVenta(venta);
        }

        // Mapear usuario validador si existe
        int idUsuarioValidador = rs.getInt("id_usuario_validador");
        if (!rs.wasNull()) {
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(idUsuarioValidador);
            usuario.setNombre(rs.getString("usuario_nombre"));
            usuario.setApellido(rs.getString("usuario_apellido"));
            comprobante.setUsuarioValidador(usuario);
        }

        return comprobante;
    }

    private boolean esJSONValido(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }

        try {
            // Verificación básica de estructura JSON
            String jsonTrim = json.trim();
            if (!jsonTrim.startsWith("{") || !jsonTrim.endsWith("}")) {
                return false;
            }

            // Verificar que no tenga caracteres de control problemáticos
            if (jsonTrim.matches(".*[\u0000-\u001F\u007F-\u009F].*")) {
                return false;
            }

            // Verificar balanceo básico de llaves y comillas
            int llaves = 0;
            boolean enCadena = false;
            char anterior = '\0';

            for (char c : jsonTrim.toCharArray()) {
                if (c == '"' && anterior != '\\') {
                    enCadena = !enCadena;
                } else if (!enCadena) {
                    if (c == '{') llaves++;
                    else if (c == '}') llaves--;
                }
                anterior = c;
            }

            return llaves == 0 && !enCadena;

        } catch (Exception e) {
            System.err.println("Error validando JSON: " + e.getMessage());
            return false;
        }
    }
}
