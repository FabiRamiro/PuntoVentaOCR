package com.pos.puntoventaocr.utils;

import com.pos.puntoventaocr.models.Usuario;
import java.time.LocalDateTime;

public class SessionManager {
    private static SessionManager instance;
    private Usuario usuarioActual;
    private LocalDateTime inicioSesion;
    private String ipCliente;
    private boolean sesionActiva;

    // Constructor privado para patrón Singleton
    private SessionManager() {
        this.sesionActiva = false;
    }

    // Método para obtener la instancia única
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Inicia una nueva sesión
     */
    public void iniciarSesion(Usuario usuario) {
        this.usuarioActual = usuario;
        this.inicioSesion = LocalDateTime.now();
        this.sesionActiva = true;
        this.ipCliente = obtenerIPLocal();

        System.out.println("Sesión iniciada para: " + usuario.getNombreCompleto() +
                " (" + usuario.getRol().getNombreRol() + ")");
    }

    /**
     * Cierra la sesión actual
     */
    public void cerrarSesion() {
        if (usuarioActual != null) {
            System.out.println("Sesión cerrada para: " + usuarioActual.getNombreCompleto());
        }

        this.usuarioActual = null;
        this.inicioSesion = null;
        this.sesionActiva = false;
        this.ipCliente = null;
    }

    /**
     * Verifica si hay una sesión activa
     */
    public boolean hayUsuarioAutenticado() {
        return sesionActiva && usuarioActual != null;
    }

    /**
     * Verifica si el usuario actual tiene un permiso específico
     */
    public boolean tienePermiso(String permiso) {
        if (!hayUsuarioAutenticado()) {
            return false;
        }
        return usuarioActual.validarPermisos(permiso);
    }

    /**
     * Verifica si el usuario actual tiene uno de los roles especificados
     */
    public boolean tieneRol(String... roles) {
        if (!hayUsuarioAutenticado()) {
            return false;
        }

        String rolUsuario = usuarioActual.getRol().getNombreRol().toUpperCase();
        for (String rol : roles) {
            if (rolUsuario.equals(rol.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtiene el tiempo transcurrido desde el inicio de sesión
     */
    public long getTiempoSesionMinutos() {
        if (inicioSesion != null) {
            return java.time.Duration.between(inicioSesion, LocalDateTime.now()).toMinutes();
        }
        return 0;
    }

    /**
     * Verifica si la sesión ha expirado (por ejemplo, después de 8 horas)
     */
    public boolean sesionExpirada() {
        return getTiempoSesionMinutos() > 480; // 8 horas = 480 minutos
    }

    /**
     * Renueva la sesión (actualiza el tiempo de inicio)
     */
    public void renovarSesion() {
        if (hayUsuarioAutenticado()) {
            this.inicioSesion = LocalDateTime.now();
        }
    }

    /**
     * Obtiene información completa de la sesión
     */
    public String getInfoSesion() {
        if (!hayUsuarioAutenticado()) {
            return "Sin sesión activa";
        }

        return String.format(
                "Usuario: %s | Rol: %s | Tiempo activo: %d min | IP: %s",
                usuarioActual.getNombreCompleto(),
                usuarioActual.getRol().getNombreRol(),
                getTiempoSesionMinutos(),
                ipCliente != null ? ipCliente : "N/A"
        );
    }

    /**
     * Obtiene la IP local del cliente
     */
    private String obtenerIPLocal() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    // Método de conveniencia para establecer usuario (usado en LoginController)
    public void setUsuarioActual(Usuario usuario) {
        iniciarSesion(usuario);
    }

    // Getters
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public LocalDateTime getInicioSesion() {
        return inicioSesion;
    }

    public String getIpCliente() {
        return ipCliente;
    }

    public boolean isSesionActiva() {
        return sesionActiva;
    }

    /**
     * Método para logging de actividades del usuario
     */
    public void registrarActividad(String accion) {
        if (hayUsuarioAutenticado()) {
            System.out.println(String.format("[%s] %s - %s realizó: %s",
                    LocalDateTime.now(),
                    usuarioActual.getNombreUsuario(),
                    usuarioActual.getRol().getNombreRol(),
                    accion
            ));
        }
    }

    /**
     * Obtiene el nombre del usuario para mostrar en la interfaz
     */
    public String getNombreUsuarioDisplay() {
        if (hayUsuarioAutenticado()) {
            return usuarioActual.getNombreCompleto() + " (" + usuarioActual.getRol().getNombreRol() + ")";
        }
        return "Usuario no autenticado";
    }
}