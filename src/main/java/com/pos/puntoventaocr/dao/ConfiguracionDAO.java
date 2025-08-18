package com.pos.puntoventaocr.dao;

import com.pos.puntoventaocr.config.DatabaseConnection;
import com.pos.puntoventaocr.models.Usuario;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ConfiguracionDAO {

    // Caché de configuraciones para mejor rendimiento
    private static Map<String, String> configuracionesCache = new HashMap<>();
    private static long ultimaActualizacionCache = 0;
    private static final long CACHE_TIMEOUT = 300000; // 5 minutos

    public boolean guardarConfiguracion(String clave, String valor, int idUsuario) {
        String sql = "INSERT INTO configuraciones (clave, valor, modificado_por) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE valor = VALUES(valor), modificado_por = VALUES(modificado_por), " +
                    "fecha_modificacion = CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, clave);
            stmt.setString(2, valor);
            stmt.setInt(3, idUsuario);

            boolean resultado = stmt.executeUpdate() > 0;
            
            // Limpiar caché para forzar recarga
            if (resultado) {
                configuracionesCache.clear();
                ultimaActualizacionCache = 0;
            }
            
            return resultado;

        } catch (SQLException e) {
            System.err.println("Error guardando configuración: " + e.getMessage());
            return false;
        }
    }

    public String obtenerConfiguracion(String clave) {
        actualizarCacheSiEsNecesario();
        return configuracionesCache.get(clave);
    }

    public String obtenerConfiguracion(String clave, String valorPorDefecto) {
        String valor = obtenerConfiguracion(clave);
        return valor != null ? valor : valorPorDefecto;
    }

    public boolean obtenerConfiguracionBoolean(String clave, boolean valorPorDefecto) {
        String valor = obtenerConfiguracion(clave);
        if (valor == null) return valorPorDefecto;
        return Boolean.parseBoolean(valor);
    }

    public int obtenerConfiguracionInt(String clave, int valorPorDefecto) {
        String valor = obtenerConfiguracion(clave);
        if (valor == null) return valorPorDefecto;
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return valorPorDefecto;
        }
    }

    public double obtenerConfiguracionDouble(String clave, double valorPorDefecto) {
        try {
            String valor = obtenerConfiguracion(clave);
            return valor != null ? Double.parseDouble(valor) : valorPorDefecto;
        } catch (NumberFormatException e) {
            return valorPorDefecto;
        }
    }

    public BigDecimal obtenerConfiguracionDecimal(String clave, BigDecimal valorPorDefecto) {
        try {
            String valor = obtenerConfiguracion(clave);
            return valor != null ? new BigDecimal(valor) : valorPorDefecto;
        } catch (NumberFormatException e) {
            return valorPorDefecto;
        }
    }

    // Métodos específicos para configuraciones del sistema
    public String getCuentaDestinoSistema() {
        return obtenerConfiguracion("sistema.cuenta_destino", "0102-0000-1234567890");
    }

    public String getNombreBeneficiarioSistema() {
        return obtenerConfiguracion("sistema.nombre_beneficiario", "TU EMPRESA C.A.");
    }

    // NUEVOS MÉTODOS para configuración completa de transferencias
    public String getRifEmpresa() {
        return obtenerConfiguracion("empresa.rif", "J-00000000-0");
    }

    public String getBancoEmpresa() {
        return obtenerConfiguracion("empresa.banco", "Banco de Venezuela");
    }

    public String getTipoCuentaEmpresa() {
        return obtenerConfiguracion("empresa.tipo_cuenta", "Corriente");
    }

    public Map<String, String> obtenerConfiguracionesPorCategoria(String categoria) {
        String sql = "SELECT clave, valor FROM configuraciones WHERE categoria = ?";
        Map<String, String> configuraciones = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoria);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                configuraciones.put(rs.getString("clave"), rs.getString("valor"));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo configuraciones por categoría: " + e.getMessage());
        }

        return configuraciones;
    }

    public Map<String, Map<String, Object>> obtenerTodasLasConfiguraciones() {
        String sql = "SELECT c.*, u.nombre_usuario " +
                    "FROM configuraciones c " +
                    "LEFT JOIN usuarios u ON c.modificado_por = u.id_usuario " +
                    "ORDER BY c.categoria, c.clave";

        Map<String, Map<String, Object>> configuraciones = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String categoria = rs.getString("categoria");
                if (!configuraciones.containsKey(categoria)) {
                    configuraciones.put(categoria, new HashMap<>());
                }

                Map<String, Object> config = new HashMap<>();
                config.put("clave", rs.getString("clave"));
                config.put("valor", rs.getString("valor"));
                config.put("descripcion", rs.getString("descripcion"));
                config.put("tipo_dato", rs.getString("tipo_dato"));
                config.put("modificado_por", rs.getString("nombre_usuario"));
                config.put("fecha_modificacion", rs.getTimestamp("fecha_modificacion"));

                configuraciones.get(categoria).put(rs.getString("clave"), config);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo todas las configuraciones: " + e.getMessage());
        }

        return configuraciones;
    }

    public boolean eliminarConfiguracion(String clave) {
        String sql = "DELETE FROM configuraciones WHERE clave = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, clave);
            boolean resultado = stmt.executeUpdate() > 0;
            
            // Limpiar caché
            if (resultado) {
                configuracionesCache.clear();
                ultimaActualizacionCache = 0;
            }
            
            return resultado;

        } catch (SQLException e) {
            System.err.println("Error eliminando configuración: " + e.getMessage());
            return false;
        }
    }

    private void actualizarCacheSiEsNecesario() {
        long tiempoActual = System.currentTimeMillis();
        
        if (configuracionesCache.isEmpty() || 
            (tiempoActual - ultimaActualizacionCache) > CACHE_TIMEOUT) {
            cargarConfiguracionesEnCache();
            ultimaActualizacionCache = tiempoActual;
        }
    }

    private void cargarConfiguracionesEnCache() {
        String sql = "SELECT clave, valor FROM configuraciones";
        configuracionesCache.clear();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                configuracionesCache.put(rs.getString("clave"), rs.getString("valor"));
            }

            System.out.println("✅ Configuraciones cargadas en caché: " + configuracionesCache.size());

        } catch (SQLException e) {
            System.err.println("Error cargando configuraciones en caché: " + e.getMessage());
        }
    }

    // Métodos de conveniencia para configuraciones específicas
    public double getIvaPorcentaje() {
        return obtenerConfiguracionDouble("ventas.iva_porcentaje", 16.0);
    }

    public String getSerieVentas() {
        return obtenerConfiguracion("ventas.numero_serie", "VTA");
    }

    public String getMensajeTicket() {
        return obtenerConfiguracion("ventas.mensaje_ticket", "Gracias por su compra");
    }

    public String getNombreEmpresa() {
        return obtenerConfiguracion("empresa.nombre", "Mi Empresa");
    }

    public String getRfcEmpresa() {
        return obtenerConfiguracion("empresa.rfc", "XAXX010101000");
    }

    public String getDireccionEmpresa() {
        return obtenerConfiguracion("empresa.direccion", "Dirección no configurada");
    }

    public String getTelefonoEmpresa() {
        return obtenerConfiguracion("empresa.telefono", "555-0000");
    }

    public int getTimeoutSesion() {
        return obtenerConfiguracionInt("sistema.timeout_sesion", 30);
    }

    public boolean isBackupAutomatico() {
        return obtenerConfiguracionBoolean("sistema.backup_automatico", false);
    }

    // Método para forzar recarga del caché
    public void recargarCache() {
        configuracionesCache.clear();
        ultimaActualizacionCache = 0;
        actualizarCacheSiEsNecesario();
    }
}
