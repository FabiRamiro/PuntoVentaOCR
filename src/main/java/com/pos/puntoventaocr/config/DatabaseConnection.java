package com.pos.puntoventaocr.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Configuración LOCAL (temporal) para pruebas
    private static final boolean USE_LOCAL = false; // Cambiar a false para usar remoto

    // Configuración LOCAL
    private static final String LOCAL_URL = "jdbc:mysql://localhost:3306/punto_venta?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true";
    private static final String LOCAL_USER = "root";
    private static final String LOCAL_PASSWORD = ""; // Cambiar por tu password local

    // Configuración REMOTA
    private static final String REMOTE_HOST = "165.22.180.74";
    private static final String REMOTE_PORT = "3306";
    private static final String REMOTE_DATABASE = "punto_venta";
    private static final String REMOTE_USER = "admin";
    private static final String REMOTE_PASSWORD = "c99011f0ea6a8886193d6ab93cc66ed83e7478b278e2456a";

    private static Connection connection = null;

    // Constructor privado para evitar instanciación
    private DatabaseConnection() {}

    // Obtener una conexión a la base de datos
    public static Connection getConnection() {
        try {
            // Si ya tenemos una conexión válida, la retornamos
            if (connection != null && !connection.isClosed()) {
                return connection;
            }

            // Cargar el driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            if (USE_LOCAL) {
                // Conexión LOCAL
                System.out.println("🔄 Intentando conexión LOCAL...");
                System.out.println("URL: " + LOCAL_URL);

                connection = DriverManager.getConnection(LOCAL_URL, LOCAL_USER, LOCAL_PASSWORD);

                if (connection != null && !connection.isClosed()) {
                    System.out.println("✅ CONEXIÓN LOCAL EXITOSA");
                    return connection;
                }
            } else {
                // Conexión REMOTA (URLs con diferentes configuraciones)
                String[] remoteUrls = {
                    "jdbc:mysql://" + REMOTE_HOST + ":" + REMOTE_PORT + "/" + REMOTE_DATABASE +
                    "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true",

                    "jdbc:mysql://" + REMOTE_HOST + ":" + REMOTE_PORT + "/" + REMOTE_DATABASE +
                    "?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&verifyServerCertificate=false",

                    "jdbc:mysql://" + REMOTE_HOST + ":" + REMOTE_PORT + "/" + REMOTE_DATABASE +
                    "?useSSL=false&serverTimezone=UTC"
                };

                for (int i = 0; i < remoteUrls.length; i++) {
                    try {
                        System.out.println("🔄 Intentando conexión REMOTA " + (i + 1) + "...");
                        System.out.println("URL: " + remoteUrls[i]);

                        connection = DriverManager.getConnection(remoteUrls[i], REMOTE_USER, REMOTE_PASSWORD);

                        if (connection != null && !connection.isClosed()) {
                            System.out.println("✅ CONEXIÓN REMOTA EXITOSA con configuración " + (i + 1));
                            return connection;
                        }

                    } catch (SQLException e) {
                        System.err.println("❌ Configuración remota " + (i + 1) + " falló: " + e.getMessage());
                        if (i == remoteUrls.length - 1) {
                            throw e;
                        }
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            System.err.println("❌ ERROR CRÍTICO: Driver MySQL no encontrado");
            System.err.println("💡 Ejecute la aplicación con: java -jar target/PuntoVentaOCR-1.0-SNAPSHOT.jar");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ ERROR DE CONEXIÓN: No se pudo conectar a la base de datos");

            if (USE_LOCAL) {
                System.err.println("🔧 SOLUCIÓN PARA CONEXIÓN LOCAL:");
                System.err.println("1. Instalar MySQL localmente");
                System.err.println("2. Crear usuario root sin password o cambiar LOCAL_PASSWORD");
                System.err.println("3. El sistema creará automáticamente la base de datos");
            } else {
                System.err.println("🔧 SOLUCIÓN PARA CONEXIÓN REMOTA:");
                System.err.println("1. Verificar que el usuario 'admin' tenga permisos en 'punto_venta'");
                System.err.println("2. Ejecutar en el servidor MySQL:");
                System.err.println("   GRANT ALL PRIVILEGES ON punto_venta.* TO 'admin'@'%';");
                System.err.println("   FLUSH PRIVILEGES;");
            }

            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Cerrar la conexión a la base de datos
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔐 Conexión cerrada");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }

    // Probar la conexión a la base de datos
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                conn.createStatement().executeQuery("SELECT 1").close();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Test de conexión falló: " + e.getMessage());
        }
        return false;
    }

    // Método para obtener información de conexión
    public static String getConnectionInfo() {
        if (USE_LOCAL) {
            return "Modo: LOCAL | Host: localhost:3306 | DB: punto_venta | User: " + LOCAL_USER;
        } else {
            return "Modo: REMOTO | Host: " + REMOTE_HOST + ":" + REMOTE_PORT + " | DB: " + REMOTE_DATABASE + " | User: " + REMOTE_USER;
        }
    }

    // Cambiar modo de conexión
    public static void setUseLocal(boolean useLocal) {
        // Para futuras mejoras - por ahora cambiar USE_LOCAL manualmente
    }
}