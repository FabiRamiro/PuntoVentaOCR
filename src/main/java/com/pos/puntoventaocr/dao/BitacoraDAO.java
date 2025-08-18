package com.pos.puntoventaocr.dao;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.models.Bitacora;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BitacoraDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    // Método genérico para registrar cualquier acción
    public void registrarAccion(int idUsuario, String accion, String modulo, String descripcion,
                               String datosAnteriores, String datosNuevos, String ipAddress, String resultado) {
        String sql = "INSERT INTO bitacora_acciones (id_usuario, accion, modulo, descripcion, " +
                    "datos_anteriores, datos_nuevos, ip_address, resultado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            pstmt.setString(2, accion);
            pstmt.setString(3, modulo);
            pstmt.setString(4, descripcion);
            pstmt.setString(5, datosAnteriores);
            pstmt.setString(6, datosNuevos);
            pstmt.setString(7, ipAddress);
            pstmt.setString(8, resultado);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método simplificado para registrar acciones básicas
    public void registrarAccion(int idUsuario, String accion, String modulo, String descripcion) {
        registrarAccion(idUsuario, accion, modulo, descripcion, null, null, "127.0.0.1", "EXITOSO");
    }

    // LOGIN Y AUTENTICACIÓN
    public void registrarLogin(int idUsuario, String ipAddress, String resultado) {
        registrarAccion(idUsuario, "LOGIN", "AUTENTICACION",
            "Usuario inició sesión", null, null, ipAddress, resultado);
    }

    // Sobrecarga para compatibilidad con LoginController
    public void registrarLogin(int idUsuario, String ipAddress) {
        registrarLogin(idUsuario, ipAddress, "EXITOSO");
    }

    public void registrarLogout(int idUsuario, String ipAddress) {
        registrarAccion(idUsuario, "LOGOUT", "AUTENTICACION",
            "Usuario cerró sesión", null, null, ipAddress, "EXITOSO");
    }

    // GESTIÓN DE USUARIOS
    public void registrarCreacionUsuario(int idUsuario, String nombreUsuarioCreado, String ipAddress) {
        registrarAccion(idUsuario, "CREATE", "USUARIOS",
            "Usuario creado: " + nombreUsuarioCreado, null, null, ipAddress, "EXITOSO");
    }

    public void registrarModificacionUsuario(int idUsuario, String nombreUsuarioModificado, String descripcion) {
        registrarAccion(idUsuario, "UPDATE", "USUARIOS", descripcion);
    }

    public void registrarEliminacionUsuario(int idUsuario, String nombreUsuarioEliminado, String ipAddress) {
        registrarAccion(idUsuario, "DELETE", "USUARIOS",
            "Usuario eliminado: " + nombreUsuarioEliminado, null, null, ipAddress, "EXITOSO");
    }

    // GESTIÓN DE PRODUCTOS
    public void registrarCreacionProducto(Integer idUsuario, String nombreProducto, String ipAddress) {
        registrarAccion(idUsuario, "CREATE", "PRODUCTOS",
            "Producto creado: " + nombreProducto, null, null, ipAddress, "EXITOSO");
    }

    public void registrarModificacionProducto(Integer idUsuario, String nombreProducto, String datosAnteriores, String datosNuevos) {
        registrarAccion(idUsuario, "UPDATE", "PRODUCTOS",
            "Producto modificado: " + nombreProducto, datosAnteriores, datosNuevos, "127.0.0.1", "EXITOSO");
    }

    public void registrarEliminacionProducto(Integer idUsuario, String nombreProducto, String ipAddress) {
        registrarAccion(idUsuario, "DELETE", "PRODUCTOS",
            "Producto eliminado: " + nombreProducto, null, null, ipAddress, "EXITOSO");
    }

    // GESTIÓN DE VENTAS
    public void registrarVenta(Integer idUsuario, String numeroVenta, double total, String detalles) {
        registrarAccion(idUsuario, "CREATE", "VENTAS",
            "Venta registrada: " + numeroVenta + " - Total: $" + total + " - " + detalles);
    }

    // CONFIGURACIÓN DEL SISTEMA
    public void registrarConfiguracion(int idUsuario, String configuracion, String valor) {
        registrarAccion(idUsuario, "UPDATE", "CONFIGURACION",
            "Configuración modificada: " + configuracion + " = " + valor);
    }

    // REPORTES
    public void registrarGeneracionReporte(Integer idUsuario, String tipoReporte, String parametros) {
        registrarAccion(idUsuario, "EXPORT", "REPORTES",
            "Reporte generado: " + tipoReporte + " - Parámetros: " + parametros);
    }

    // OCR Y VALIDACIONES
    public void registrarProcesamientoOCR(Integer idUsuario, String archivoOCR, int productosDetectados, String resultado) {
        registrarAccion(idUsuario, "PROCESS", "OCR",
            "OCR procesado: " + archivoOCR + " - Productos detectados: " + productosDetectados + " - " + resultado);
    }

    public void registrarValidacionOCR(Integer idUsuario, String archivoOCR, String resultado) {
        registrarAccion(idUsuario, "VALIDATE", "OCR",
            "OCR validado: " + archivoOCR + " - " + resultado);
    }

    // MÉTODOS PARA EL CONTROLADOR DE BITÁCORA
    public List<Bitacora> obtenerTodos() {
        String sql = "SELECT b.*, u.nombre_usuario " +
                    "FROM bitacora_acciones b " +
                    "LEFT JOIN usuarios u ON b.id_usuario = u.id_usuario " +
                    "ORDER BY b.fecha DESC LIMIT 1000";

        List<Bitacora> registros = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("🔍 DEBUG: Ejecutando consulta de bitácora...");

            int contador = 0;
            while (rs.next()) {
                registros.add(mapearBitacora(rs));
                contador++;
            }

            System.out.println("✅ DEBUG: Se encontraron " + contador + " registros en bitácora");

        } catch (SQLException e) {
            System.err.println("❌ ERROR obteniendo registros de bitácora: " + e.getMessage());
            e.printStackTrace();
        }

        return registros;
    }

    public List<Bitacora> obtenerConFiltros(LocalDate fechaInicio, LocalDate fechaFin,
                                           String accion, String modulo, String usuario) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.*, u.nombre_usuario ");
        sql.append("FROM bitacora_acciones b ");
        sql.append("LEFT JOIN usuarios u ON b.id_usuario = u.id_usuario ");
        sql.append("WHERE 1=1 ");

        List<Object> parametros = new ArrayList<>();

        if (fechaInicio != null) {
            sql.append("AND DATE(b.fecha) >= ? ");
            parametros.add(fechaInicio);
        }

        if (fechaFin != null) {
            sql.append("AND DATE(b.fecha) <= ? ");
            parametros.add(fechaFin);
        }

        if (accion != null && !accion.trim().isEmpty()) {
            sql.append("AND b.accion = ? ");
            parametros.add(accion);
        }

        if (modulo != null && !modulo.trim().isEmpty()) {
            sql.append("AND b.modulo = ? ");
            parametros.add(modulo);
        }

        if (usuario != null && !usuario.trim().isEmpty()) {
            sql.append("AND u.nombre_usuario LIKE ? ");
            parametros.add("%" + usuario + "%");
        }

        sql.append("ORDER BY b.fecha DESC LIMIT 5000");

        List<Bitacora> registros = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                registros.add(mapearBitacora(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo registros filtrados: " + e.getMessage());
            e.printStackTrace();
        }

        return registros;
    }

    private Bitacora mapearBitacora(ResultSet rs) throws SQLException {
        Bitacora bitacora = new Bitacora();

        bitacora.setIdBitacora(rs.getInt("id_bitacora"));
        bitacora.setIdUsuario(rs.getInt("id_usuario"));
        bitacora.setNombreUsuario(rs.getString("nombre_usuario"));
        bitacora.setAccion(rs.getString("accion"));
        bitacora.setModulo(rs.getString("modulo"));
        bitacora.setDetalles(rs.getString("descripcion"));
        bitacora.setDireccionIp(rs.getString("ip_address"));

        // Cambiar de fecha_hora a fecha para coincidir con tu BD
        Timestamp timestamp = rs.getTimestamp("fecha");
        if (timestamp != null) {
            bitacora.setFechaHora(timestamp.toLocalDateTime());
        }

        return bitacora;
    }
}
