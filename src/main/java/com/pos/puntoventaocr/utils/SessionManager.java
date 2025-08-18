package com.pos.puntoventaocr.utils;

import com.pos.puntoventaocr.models.Usuario;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Administra la sesión del usuario actual en el sistema
 */
public class SessionManager {

    private static SessionManager instance;
    private Usuario usuarioActual;
    private LocalDateTime inicioSesion;
    private String direccionIP;

    // Constructor privado para patrón Singleton
    private SessionManager() {

    }

    /**
     * Obtiene la instancia única del SessionManager
     * @return instancia del SessionManager
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Establece el usuario actual de la sesión
     * @param usuario Usuario que ha iniciado sesión
     */
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        this.inicioSesion = LocalDateTime.now();
        System.out.println("✅ DEBUG: Sesión establecida para usuario: " +
            (usuario != null ? usuario.getNombreUsuario() : "null"));
    }

    /**
     * Obtiene el usuario actual de la sesión
     * @return Usuario actual o null si no hay sesión activa
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Establece la dirección IP de la sesión
     * @param ip Dirección IP del cliente
     */
    public void setDireccionIP(String ip) {
        this.direccionIP = ip;
    }

    /**
     * Obtiene la dirección IP de la sesión
     * @return Dirección IP o "127.0.0.1" por defecto
     */
    public String getDireccionIP() {
        return direccionIP != null ? direccionIP : "127.0.0.1";
    }

    /**
     * Obtiene la fecha y hora de inicio de sesión
     * @return LocalDateTime del inicio de sesión
     */
    public LocalDateTime getInicioSesion() {
        return inicioSesion;
    }

    /**
     * Verifica si hay una sesión activa
     * @return true si hay un usuario logueado, false en caso contrario
     */
    public boolean tieneSesionActiva() {
        return usuarioActual != null;
    }

    /**
     * Verifica si el usuario actual tiene un rol específico
     * @param rol Rol a verificar
     * @return true si el usuario tiene el rol especificado
     */
    public boolean tieneRol(String rol) {
        if (usuarioActual == null || usuarioActual.getRol() == null) {
            return false;
        }
        return rol.equalsIgnoreCase(usuarioActual.getNombreRol());
    }

    /**
     * Verifica si el usuario actual es administrador
     * @return true si es administrador
     */
    public boolean esAdministrador() {
        return tieneRol("ADMINISTRADOR");
    }

    /**
     * Verifica si el usuario actual es gerente
     * @return true si es gerente
     */
    public boolean esGerente() {
        return tieneRol("GERENTE");
    }

    /**
     * Verifica si el usuario actual es cajero
     * @return true si es cajero
     */
    public boolean esCajero() {
        return tieneRol("CAJERO");
    }

    /**
     * Obtiene el nombre completo del usuario actual
     * @return Nombre completo o "Usuario desconocido"
     */
    public String getNombreUsuarioActual() {
        if (usuarioActual != null) {
            return usuarioActual.getNombreCompleto();
        }
        return "Usuario desconocido";
    }

    /**
     * Obtiene el ID del usuario actual
     * @return ID del usuario o 0 si no hay sesión activa
     */
    public int getIdUsuarioActual() {
        if (usuarioActual != null) {
            return usuarioActual.getIdUsuario();
        }
        return 0;
    }

    /**
     * Obtiene el ID del usuario actual (método de compatibilidad)
     * @return ID del usuario o 0 si no hay sesión activa
     */
    public int getCurrentUserId() {
        return getIdUsuarioActual();
    }

    /**
     * Limpia la sesión actual
     */
    public void limpiarSesion() {
        System.out.println("🔄 DEBUG: Limpiando sesión de usuario: " +
            (usuarioActual != null ? usuarioActual.getNombreUsuario() : "sin usuario"));

        this.usuarioActual = null;
        this.inicioSesion = null;
        this.direccionIP = null;

        System.out.println("✅ DEBUG: Sesión limpiada correctamente");
    }

    /**
     * Cierra la sesión actual (alias para limpiarSesion)
     */
    public void cerrarSesion() {
        limpiarSesion();
    }

    /**
     * Obtiene información resumida de la sesión actual
     * @return String con información de la sesión
     */
    public String getInfoSesion() {
        if (usuarioActual == null) {
            return "Sin sesión activa";
        }

        String duracion = "";
        if (inicioSesion != null) {
            LocalDateTime ahora = LocalDateTime.now();
            long minutos = java.time.Duration.between(inicioSesion, ahora).toMinutes();

            if (minutos < 60) {
                duracion = minutos + " minutos";
            } else {
                long horas = minutos / 60;
                long minutosRestantes = minutos % 60;
                duracion = horas + " horas y " + minutosRestantes + " minutos";
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return String.format("Usuario: %s | Rol: %s | Inicio: %s | Duración: %s",
                usuarioActual.getNombreCompleto(),
                usuarioActual.getRol().getNombreRol(),
                inicioSesion != null ? inicioSesion.format(formatter) : "N/A",
                duracion);
    }

    /**
     * Verifica si el usuario actual tiene permisos para una acción específica
     * @param accion Acción a verificar
     * @return true si tiene permisos
     */
    public boolean tienePermiso(String accion) {
        if (usuarioActual == null) {
            return false;
        }

        String rol = usuarioActual.getNombreRol().toUpperCase();

        switch (accion.toUpperCase()) {
            case "ADMINISTRAR_USUARIOS":
            case "GESTIONAR_ROLES":
            case "CONFIGURAR_SISTEMA":
            case "RESPALDO_BD":
            case "BITACORA":
                return "ADMINISTRADOR".equals(rol) || "GERENTE".equals(rol);

            case "GESTIONAR_PRODUCTOS":
            case "REPORTES":
            case "OCR":
                return !"CAJERO".equals(rol);

            case "VENTAS":
            case "CONSULTAR_PRODUCTOS":
                return true; // Todos los roles pueden realizar ventas

            default:
                return "ADMINISTRADOR".equals(rol);
        }
    }
}