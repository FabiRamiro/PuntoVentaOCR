package com.pos.puntoventaocr.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BitacoraAcciones {
    private int idBitacora;
    private Usuario usuario;
    private String accion;
    private String modulo;
    private String descripcion;
    private String datosAnteriores;
    private String datosNuevos;
    private String ipAddress;
    private LocalDateTime fecha;
    private String resultado;

    // Constructor vacío
    public BitacoraAcciones() {
        this.fecha = LocalDateTime.now();
        this.resultado = "EXITOSO";
    }

    // Constructor con parámetros básicos
    public BitacoraAcciones(Usuario usuario, String accion, String modulo, String descripcion) {
        this();
        this.usuario = usuario;
        this.accion = accion;
        this.modulo = modulo;
        this.descripcion = descripcion;
    }

    // Constructor completo
    public BitacoraAcciones(Usuario usuario, String accion, String modulo, String descripcion,
                            String datosAnteriores, String datosNuevos, String ipAddress) {
        this(usuario, accion, modulo, descripcion);
        this.datosAnteriores = datosAnteriores;
        this.datosNuevos = datosNuevos;
        this.ipAddress = ipAddress;
    }

    // Métodos estáticos para crear registros comunes
    public static BitacoraAcciones crearLogin(Usuario usuario, String ip) {
        return new BitacoraAcciones(usuario, "LOGIN", "SEGURIDAD",
                "Inicio de sesión exitoso", null, null, ip);
    }

    public static BitacoraAcciones crearLogout(Usuario usuario, String ip) {
        return new BitacoraAcciones(usuario, "LOGOUT", "SEGURIDAD",
                "Cierre de sesión", null, null, ip);
    }

    public static BitacoraAcciones crearCreacion(Usuario usuario, String modulo, String objeto, String datos) {
        return new BitacoraAcciones(usuario, "CREAR", modulo,
                "Creación de " + objeto, null, datos, null);
    }

    public static BitacoraAcciones crearModificacion(Usuario usuario, String modulo, String objeto,
                                                     String datosAnteriores, String datosNuevos) {
        return new BitacoraAcciones(usuario, "MODIFICAR", modulo,
                "Modificación de " + objeto, datosAnteriores, datosNuevos, null);
    }

    public static BitacoraAcciones crearEliminacion(Usuario usuario, String modulo, String objeto, String datos) {
        return new BitacoraAcciones(usuario, "ELIMINAR", modulo,
                "Eliminación de " + objeto, datos, null, null);
    }

    public static BitacoraAcciones crearConsulta(Usuario usuario, String modulo, String descripcion) {
        return new BitacoraAcciones(usuario, "CONSULTAR", modulo, descripcion);
    }

    public static BitacoraAcciones crearError(Usuario usuario, String modulo, String descripcion, String error) {
        BitacoraAcciones bitacora = new BitacoraAcciones(usuario, "ERROR", modulo, descripcion);
        bitacora.setResultado("ERROR: " + error);
        return bitacora;
    }

    // Métodos de negocio
    public void registrar() {
        // Este método sería implementado por el DAO correspondiente
        // Por ahora solo registramos en consola para propósitos de desarrollo
        System.out.println(this.toString());
    }

    public void marcarComoError(String mensajeError) {
        this.resultado = "ERROR: " + mensajeError;
    }

    public void marcarComoExitoso() {
        this.resultado = "EXITOSO";
    }

    public boolean fueExitoso() {
        return "EXITOSO".equals(resultado);
    }

    public boolean huboError() {
        return resultado != null && resultado.startsWith("ERROR");
    }

    public String getResumenActividad() {
        StringBuilder resumen = new StringBuilder();

        if (usuario != null) {
            resumen.append(usuario.getNombreUsuario());
        } else {
            resumen.append("Sistema");
        }

        resumen.append(" - ").append(accion);

        if (modulo != null) {
            resumen.append(" en ").append(modulo);
        }

        resumen.append(" (").append(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append(")");

        return resumen.toString();
    }

    public String getTipoAccion() {
        if (accion == null) return "DESCONOCIDA";

        switch (accion.toUpperCase()) {
            case "LOGIN":
            case "LOGOUT":
                return "SEGURIDAD";
            case "CREAR":
            case "MODIFICAR":
            case "ELIMINAR":
                return "MANTENIMIENTO";
            case "CONSULTAR":
            case "BUSCAR":
            case "LISTAR":
                return "CONSULTA";
            case "VENTA":
            case "ANULAR_VENTA":
                return "TRANSACCIONAL";
            case "BACKUP":
            case "RESTAURAR":
                return "SISTEMA";
            default:
                return "GENERAL";
        }
    }

    public int getNivelRiesgo() {
        if (accion == null) return 1;

        switch (accion.toUpperCase()) {
            case "ELIMINAR":
            case "ANULAR_VENTA":
            case "MODIFICAR_USUARIO":
            case "CAMBIAR_ROL":
                return 3; // Alto riesgo
            case "CREAR":
            case "MODIFICAR":
            case "LOGIN":
                return 2; // Riesgo medio
            case "CONSULTAR":
            case "BUSCAR":
            case "LISTAR":
                return 1; // Bajo riesgo
            default:
                return 2;
        }
    }

    public boolean requiereAuditoria() {
        return getNivelRiesgo() >= 2 || huboError();
    }

    // Getters y Setters
    public int getIdBitacora() {
        return idBitacora;
    }

    public void setIdBitacora(int idBitacora) {
        this.idBitacora = idBitacora;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDatosAnteriores() {
        return datosAnteriores;
    }

    public void setDatosAnteriores(String datosAnteriores) {
        this.datosAnteriores = datosAnteriores;
    }

    public String getDatosNuevos() {
        return datosNuevos;
    }

    public void setDatosNuevos(String datosNuevos) {
        this.datosNuevos = datosNuevos;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    @Override
    public String toString() {
        return "BitacoraAcciones{" +
                "fecha=" + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) +
                ", usuario=" + (usuario != null ? usuario.getNombreUsuario() : "Sistema") +
                ", accion='" + accion + '\'' +
                ", modulo='" + modulo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", resultado='" + resultado + '\'' +
                ", ip='" + ipAddress + '\'' +
                '}';
    }

    // Clase interna para constantes de módulos
    public static class Modulos {
        public static final String USUARIOS = "USUARIOS";
        public static final String PRODUCTOS = "PRODUCTOS";
        public static final String VENTAS = "VENTAS";
        public static final String INVENTARIO = "INVENTARIO";
        public static final String OCR = "OCR";
        public static final String REPORTES = "REPORTES";
        public static final String SEGURIDAD = "SEGURIDAD";
        public static final String SISTEMA = "SISTEMA";
        public static final String CATEGORIAS = "CATEGORIAS";
        public static final String CLIENTES = "CLIENTES";
    }

    // Clase interna para constantes de acciones
    public static class Acciones {
        public static final String LOGIN = "LOGIN";
        public static final String LOGOUT = "LOGOUT";
        public static final String CREAR = "CREAR";
        public static final String MODIFICAR = "MODIFICAR";
        public static final String ELIMINAR = "ELIMINAR";
        public static final String CONSULTAR = "CONSULTAR";
        public static final String BUSCAR = "BUSCAR";
        public static final String LISTAR = "LISTAR";
        public static final String EXPORTAR = "EXPORTAR";
        public static final String IMPRIMIR = "IMPRIMIR";
        public static final String PROCESAR_OCR = "PROCESAR_OCR";
        public static final String VALIDAR_OCR = "VALIDAR_OCR";
        public static final String RECHAZAR_OCR = "RECHAZAR_OCR";
        public static final String REALIZAR_VENTA = "REALIZAR_VENTA";
        public static final String ANULAR_VENTA = "ANULAR_VENTA";
        public static final String CAMBIAR_PASSWORD = "CAMBIAR_PASSWORD";
        public static final String BACKUP = "BACKUP";
        public static final String RESTAURAR = "RESTAURAR";
        public static final String ERROR = "ERROR";
    }
}