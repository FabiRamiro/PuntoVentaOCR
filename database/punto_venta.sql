-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost
-- Tiempo de generación: 19-08-2025 a las 07:13:22
-- Versión del servidor: 8.0.43-0ubuntu0.22.04.1
-- Versión de PHP: 8.1.2-1ubuntu2.22

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `punto_venta`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `alertas_stock`
--

CREATE TABLE `alertas_stock` (
  `id_alerta` int NOT NULL,
  `id_producto` int NOT NULL,
  `tipo_alerta` enum('STOCK_BAJO','STOCK_AGOTADO','PROXIMO_AGOTARSE') NOT NULL,
  `stock_actual` int NOT NULL,
  `stock_minimo` int NOT NULL,
  `fecha` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `estado` enum('PENDIENTE','NOTIFICADA','RESUELTA') DEFAULT 'PENDIENTE',
  `id_usuario_notificado` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `bitacora_acciones`
--

CREATE TABLE `bitacora_acciones` (
  `id_bitacora` int NOT NULL,
  `id_usuario` int NOT NULL,
  `accion` varchar(100) NOT NULL,
  `modulo` varchar(50) NOT NULL,
  `descripcion` text,
  `datos_anteriores` json DEFAULT NULL,
  `datos_nuevos` json DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `fecha` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `resultado` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `bitacora_acciones`
--

INSERT INTO `bitacora_acciones` (`id_bitacora`, `id_usuario`, `accion`, `modulo`, `descripcion`, `datos_anteriores`, `datos_nuevos`, `ip_address`, `fecha`, `resultado`) VALUES
(2, 2, 'RESET_PASSWORD', 'USUARIOS', 'Reseteó la contraseña del usuario: admin', NULL, NULL, '127.0.0.1', '2025-08-17 23:08:26', 'EXITOSO'),
(3, 2, 'LOGOUT', 'SISTEMA', 'Usuario cerró sesión', NULL, NULL, '127.0.0.1', '2025-08-17 23:08:37', 'EXITOSO'),
(5, 2, 'LOGIN_EXITOSO', 'AUTENTICACION', 'Intento de login de usuario: admin. Login exitoso - Usuario: admin', '{}', '{}', '127.0.1.1', '2025-08-17 23:44:27', 'EXITOSO'),
(6, 2, 'LOGOUT', 'SISTEMA', 'Usuario cerró sesión', '{}', '{}', '127.0.0.1', '2025-08-17 23:45:42', 'EXITOSO'),
(7, 3, 'LOGIN_EXITOSO', 'AUTENTICACION', 'Intento de login de usuario: fabian. Login exitoso - Usuario: fabian', '{}', '{}', '127.0.1.1', '2025-08-17 23:45:51', 'EXITOSO'),
(8, 3, 'CAMBIO_PASSWORD', 'USUARIOS', 'Usuario cambió su contraseña', '{}', '{}', '127.0.0.1', '2025-08-17 23:46:18', 'EXITOSO'),
(9, 3, 'LOGOUT', 'SISTEMA', 'Usuario cerró sesión', '{}', '{}', '127.0.0.1', '2025-08-17 23:46:23', 'EXITOSO'),
(10, 3, 'LOGIN_FALLIDO', 'AUTENTICACION', 'Intento de login de usuario: fabian. Credenciales incorrectas - Usuario: fabian', '{}', '{}', '127.0.1.1', '2025-08-17 23:46:32', 'FALLIDO'),
(11, 3, 'LOGIN_EXITOSO', 'AUTENTICACION', 'Intento de login de usuario: fabian. Login exitoso - Usuario: fabian', '{}', '{}', '127.0.1.1', '2025-08-17 23:46:46', 'EXITOSO'),
(12, 3, 'RESET_PASSWORD', 'USUARIOS', 'Reseteó la contraseña del usuario: fabian', '{}', '{}', '127.0.0.1', '2025-08-17 23:47:25', 'EXITOSO'),
(13, 3, 'LOGIN_FALLIDO', 'AUTENTICACION', 'Intento de login de usuario: fabian. Credenciales incorrectas - Usuario: fabian', '{}', '{}', '127.0.1.1', '2025-08-18 00:16:48', 'FALLIDO'),
(14, 3, 'LOGIN_FALLIDO', 'AUTENTICACION', 'Intento de login de usuario: fabian. Credenciales incorrectas - Usuario: fabian', '{}', '{}', '127.0.1.1', '2025-08-18 00:16:59', 'FALLIDO'),
(15, 3, 'LOGIN_EXITOSO', 'AUTENTICACION', 'Intento de login de usuario: fabian. Login exitoso - Usuario: fabian', '{}', '{}', '127.0.1.1', '2025-08-18 00:17:09', 'EXITOSO'),
(16, 3, 'LOGIN_EXITOSO', 'AUTENTICACION', 'Intento de login de usuario: fabian. Login exitoso - Usuario: fabian', '{}', '{}', '127.0.1.1', '2025-08-18 00:27:46', 'EXITOSO'),
(17, 3, 'LOGIN_EXITOSO', 'AUTENTICACION', 'Intento de login de usuario: fabian. Login exitoso - Usuario: fabian', '{}', '{}', '127.0.1.1', '2025-08-18 00:31:10', 'EXITOSO'),
(18, 3, 'LOGIN_EXITOSO', 'AUTENTICACION', 'Intento de login de usuario: fabian. Login exitoso - Usuario: fabian', '{}', '{}', '127.0.1.1', '2025-08-18 00:49:04', 'EXITOSO'),
(19, 3, 'LOGIN_EXITOSO', 'AUTENTICACION', 'Intento de login de usuario: fabian. Login exitoso - Usuario: fabian', '{}', '{}', '127.0.1.1', '2025-08-18 00:59:59', 'EXITOSO'),
(20, 3, 'LOGIN_EXITOSO', 'AUTENTICACION', 'Intento de login de usuario: fabian. Login exitoso - Usuario: fabian', '{}', '{}', '127.0.1.1', '2025-08-18 01:23:15', 'EXITOSO'),
(21, 3, 'LOGIN_EXITOSO', 'AUTENTICACION', 'Intento de login de usuario: fabian. Login exitoso - Usuario: fabian', '{}', '{}', '127.0.1.1', '2025-08-18 01:54:19', 'EXITOSO'),
(22, 3, 'LOGIN_EXITOSO', 'AUTENTICACION', 'Intento de login de usuario: fabian. Login exitoso - Usuario: fabian', '{}', '{}', '127.0.1.1', '2025-08-18 02:22:04', 'EXITOSO'),
(23, 3, 'LOGIN_EXITOSO', 'AUTENTICACION', 'Intento de login de usuario: fabian. Login exitoso - Usuario: fabian', '{}', '{}', '127.0.1.1', '2025-08-18 03:39:27', 'EXITOSO'),
(24, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.1.1', '2025-08-18 05:20:00', 'EXITOSO'),
(25, 3, 'CREAR_VENTA', 'VENTAS', 'Registró venta #VTA-000018 por $134.56', NULL, '{\"total\": 134.56, \"metodo_pago\": \"EFECTIVO\", \"numero_venta\": \"VTA-000018\"}', '127.0.0.1', '2025-08-18 05:21:51', 'EXITOSO'),
(26, 3, 'CREAR_VENTA', 'VENTAS', 'Registró venta #VTA-000019 por $201.84', NULL, '{\"total\": 201.84, \"metodo_pago\": \"EFECTIVO\", \"numero_venta\": \"VTA-000019\"}', '127.0.0.1', '2025-08-18 05:22:20', 'EXITOSO'),
(27, 3, 'CREAR_VENTA', 'VENTAS', 'Registró venta #VTA-000020 por $67.28', NULL, '{\"total\": 67.28, \"metodo_pago\": \"TARJETA\", \"numero_venta\": \"VTA-000020\"}', '127.0.0.1', '2025-08-18 05:22:49', 'EXITOSO'),
(28, 3, 'CREAR_VENTA', 'VENTAS', 'Registró venta #VTA-000021 por $134.56', NULL, '{\"total\": 134.56, \"metodo_pago\": \"TRANSFERENCIA\", \"numero_venta\": \"VTA-000021\"}', '127.0.0.1', '2025-08-18 05:23:25', 'EXITOSO'),
(29, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.1.1', '2025-08-18 05:36:59', 'EXITOSO'),
(30, 3, 'CREAR_VENTA', 'VENTAS', 'Registró venta #VTA-000022 por $134.56', NULL, '{\"total\": 134.56, \"metodo_pago\": \"TRANSFERENCIA\", \"numero_venta\": \"VTA-000022\"}', '127.0.0.1', '2025-08-18 05:37:25', 'EXITOSO'),
(31, 3, 'PROCESAR_OCR', 'OCR', 'Procesó imagen OCR: IMG_1755478824942.jpeg - 1 productos detectados', NULL, '{\"archivo\": \"IMG_1755478824942.jpeg\", \"productos_detectados\": 1}', '127.0.0.1', '2025-08-18 05:37:47', 'EXITOSO'),
(32, 3, 'VALIDAR_OCR', 'OCR', 'Validó resultado OCR: IMG_1755478824942.jpeg - RECHAZADO - Esta incorrecto el comprobante', NULL, '{\"accion\": \"RECHAZADO - Esta incorrecto el comprobante\", \"archivo\": \"IMG_1755478824942.jpeg\"}', '127.0.0.1', '2025-08-18 05:38:25', 'EXITOSO'),
(33, 3, 'Acceso a Gestión de Devoluciones', 'SISTEMA', 'Acción realizada desde menú principal', NULL, NULL, '127.0.0.1', '2025-08-18 05:39:26', 'EXITOSO'),
(34, 3, 'Acceso a Gestión de Devoluciones', 'SISTEMA', 'Acción realizada desde menú principal', NULL, NULL, '127.0.0.1', '2025-08-18 05:40:00', 'EXITOSO'),
(35, 3, 'Acceso a Inventario Físico', 'SISTEMA', 'Acción realizada desde menú principal', NULL, NULL, '127.0.0.1', '2025-08-18 05:44:50', 'EXITOSO'),
(36, 3, 'AJUSTE_STOCK', 'PRODUCTOS', 'Ajustó stock del producto: FabiOS - Ajuste por inventario físico', '{\"stock_anterior\": 1}', '{\"stock_nuevo\": 25}', '127.0.0.1', '2025-08-18 05:45:41', 'EXITOSO'),
(37, 3, 'Acceso a Inventario Físico', 'SISTEMA', 'Acción realizada desde menú principal', NULL, NULL, '127.0.0.1', '2025-08-18 05:45:59', 'EXITOSO'),
(38, 3, 'GUARDAR_INVENTARIO', 'PRODUCTOS', 'Guardó conteo de inventario físico', NULL, NULL, '127.0.0.1', '2025-08-18 05:46:23', 'EXITOSO'),
(39, 3, 'GUARDAR_INVENTARIO', 'PRODUCTOS', 'Guardó conteo de inventario físico', NULL, NULL, '127.0.0.1', '2025-08-18 05:46:29', 'EXITOSO'),
(40, 3, 'GUARDAR_INVENTARIO', 'PRODUCTOS', 'Guardó conteo de inventario físico', NULL, NULL, '127.0.0.1', '2025-08-18 05:46:36', 'EXITOSO'),
(41, 3, 'AJUSTE_STOCK', 'PRODUCTOS', 'Ajustó stock del producto: FabiOS - Ajuste por inventario físico', '{\"stock_anterior\": 1}', '{\"stock_nuevo\": 25}', '127.0.0.1', '2025-08-18 05:46:47', 'EXITOSO'),
(42, 3, 'Acceso a Configuración OCR', 'SISTEMA', 'Acción realizada desde menú principal', NULL, NULL, '127.0.0.1', '2025-08-18 05:48:29', 'EXITOSO'),
(43, 3, 'CONFIGURACION', 'SISTEMA', 'CONFIGURACION_SISTEMA: Configuraciones del sistema actualizadas', NULL, NULL, '127.0.0.1', '2025-08-18 05:49:07', 'EXITOSO'),
(44, 3, 'Acceso a Configuración OCR', 'SISTEMA', 'Acción realizada desde menú principal', NULL, NULL, '127.0.0.1', '2025-08-18 05:49:40', 'EXITOSO'),
(45, 3, 'GENERAR_REPORTE', 'REPORTES', 'Generó reporte: REPORTE_VENTAS', NULL, '{\"tipo\": \"REPORTE_VENTAS\", \"parametros\": \"Fechas: 2025-08-01 a 2025-08-18, Usuario: Todos, Registros: 22\"}', '127.0.0.1', '2025-08-18 05:49:54', 'EXITOSO'),
(46, 3, 'GENERAR_REPORTE', 'REPORTES', 'Generó reporte: REPORTE_VENTAS', NULL, '{\"tipo\": \"REPORTE_VENTAS\", \"parametros\": \"Fechas: 2025-08-01 a 2025-08-18, Usuario: Todos, Registros: 22\"}', '127.0.0.1', '2025-08-18 05:50:50', 'EXITOSO'),
(47, 3, 'Acceso a Reporte de Ganancias', 'SISTEMA', 'Acción realizada desde menú principal', NULL, NULL, '127.0.0.1', '2025-08-18 05:53:20', 'EXITOSO'),
(48, 3, 'LOGOUT', 'AUTENTICACION', 'Usuario cerró sesión', NULL, NULL, '127.0.0.1', '2025-08-18 05:56:37', 'EXITOSO'),
(51, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.1.1', '2025-08-18 05:57:05', 'EXITOSO'),
(52, 3, 'RESET_PASSWORD', 'USUARIOS', 'Reseteó la contraseña del usuario: puga', NULL, NULL, '127.0.0.1', '2025-08-18 05:57:22', 'EXITOSO'),
(53, 3, 'LOGOUT', 'AUTENTICACION', 'Usuario cerró sesión', NULL, NULL, '127.0.0.1', '2025-08-18 05:57:31', 'EXITOSO'),
(55, 4, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.1.1', '2025-08-18 05:57:55', 'EXITOSO'),
(56, 4, 'CREAR_VENTA', 'VENTAS', 'Registró venta #VTA-000023 por $134.56', NULL, '{\"total\": 134.56, \"metodo_pago\": \"EFECTIVO\", \"numero_venta\": \"VTA-000023\"}', '127.0.0.1', '2025-08-18 05:58:15', 'EXITOSO'),
(57, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.1.1', '2025-08-18 06:39:18', 'EXITOSO'),
(58, 3, 'Acceso a Gestión de Devoluciones', 'SISTEMA', 'Acción realizada desde menú principal', NULL, NULL, '127.0.0.1', '2025-08-18 06:41:46', 'EXITOSO'),
(59, 3, 'Acceso a Inventario Físico', 'SISTEMA', 'Acción realizada desde menú principal', NULL, NULL, '127.0.0.1', '2025-08-18 06:42:41', 'EXITOSO'),
(60, 3, 'GUARDAR_INVENTARIO', 'PRODUCTOS', 'Guardó conteo de inventario físico', NULL, NULL, '127.0.0.1', '2025-08-18 06:42:51', 'EXITOSO'),
(61, 3, 'AJUSTE_INVENTARIO', 'PRODUCTOS', 'Ajuste de inventario - Producto: FabiOS, Stock anterior: 1, Stock nuevo: 30, Diferencia: 29', NULL, NULL, '127.0.0.1', '2025-08-18 06:43:03', 'EXITOSO'),
(62, 3, 'GENERAR_REPORTE', 'REPORTES', 'Generó reporte: REPORTE_VENTAS', NULL, '{\"tipo\": \"REPORTE_VENTAS\", \"parametros\": \"Fechas: 2025-08-01 a 2025-08-18, Usuario: Todos, Registros: 23\"}', '127.0.0.1', '2025-08-18 06:44:20', 'EXITOSO'),
(63, 3, 'Acceso a Reporte de Ganancias', 'SISTEMA', 'Acción realizada desde menú principal', NULL, NULL, '127.0.0.1', '2025-08-18 06:45:39', 'EXITOSO'),
(64, 3, 'CONFIGURACION', 'SISTEMA', 'CONFIGURACION_SISTEMA: Configuraciones del sistema actualizadas', NULL, NULL, '127.0.0.1', '2025-08-18 06:50:21', 'EXITOSO'),
(65, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.1.1', '2025-08-18 07:09:20', 'EXITOSO'),
(66, 3, 'GENERAR_REPORTE', 'REPORTES', 'Generó reporte: REPORTE_VENTAS', NULL, '{\"tipo\": \"REPORTE_VENTAS\", \"parametros\": \"Fechas: 2025-08-01 a 2025-08-18, Usuario: Todos, Registros: 23\"}', '127.0.0.1', '2025-08-18 07:12:32', 'EXITOSO'),
(67, 3, 'CREAR_USUARIO', 'USUARIOS', 'Creó el usuario: ale', NULL, '{\"usuario\": \"ale\"}', '127.0.0.1', '2025-08-18 07:14:03', 'EXITOSO'),
(68, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 07:39:07', 'EXITOSO'),
(69, 3, 'UPDATE', 'USUARIOS', 'Usuario modificado: Nombre: Alejandro, Email: ale@gmail.com, Rol: VENDEDOR, Estado: ACTIVO', NULL, NULL, '127.0.0.1', '2025-08-18 07:39:32', 'EXITOSO'),
(70, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 07:50:18', 'EXITOSO'),
(71, 3, 'UPDATE', 'USUARIOS', 'Usuario modificado: Nombre: Alejandro, Email: ale@gmail.com, Rol: GERENTE, Estado: ACTIVO', NULL, NULL, '127.0.0.1', '2025-08-18 07:50:38', 'EXITOSO'),
(72, 3, 'UPDATE', 'USUARIOS', 'Usuario modificado: Nombre: Cristian, Email: cristian@gmail.com, Rol: CAJERO, Estado: ACTIVO', NULL, NULL, '127.0.0.1', '2025-08-18 07:51:10', 'EXITOSO'),
(73, 3, 'RESET_PASSWORD', 'USUARIOS', 'Reseteó la contraseña del usuario: ale', NULL, NULL, '127.0.0.1', '2025-08-18 07:51:18', 'EXITOSO'),
(74, 3, 'RESET_PASSWORD', 'USUARIOS', 'Reseteó la contraseña del usuario: cristian', NULL, NULL, '127.0.0.1', '2025-08-18 07:51:25', 'EXITOSO'),
(75, 3, 'Acceso a Respaldo de Base de Datos', 'SISTEMA', 'Acción realizada desde menú principal', NULL, NULL, '127.0.0.1', '2025-08-18 07:52:56', 'EXITOSO'),
(76, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 07:59:57', 'EXITOSO'),
(77, 3, 'Acceso a Respaldo de Base de Datos', 'SISTEMA', 'Acción realizada desde menú principal', NULL, NULL, '127.0.0.1', '2025-08-18 08:02:09', 'EXITOSO'),
(78, 3, 'LOGOUT', 'AUTENTICACION', 'Usuario cerró sesión', NULL, NULL, '127.0.0.1', '2025-08-18 08:02:23', 'EXITOSO'),
(79, 6, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 08:02:31', 'EXITOSO'),
(80, 6, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 08:17:19', 'EXITOSO'),
(81, 6, 'LOGOUT', 'AUTENTICACION', 'Usuario cerró sesión', NULL, NULL, '127.0.0.1', '2025-08-18 08:18:17', 'EXITOSO'),
(82, 5, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 08:18:28', 'EXITOSO'),
(83, 5, 'LOGOUT', 'AUTENTICACION', 'Usuario cerró sesión', NULL, NULL, '127.0.0.1', '2025-08-18 08:20:12', 'EXITOSO'),
(84, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 08:20:23', 'EXITOSO'),
(85, 3, 'UPDATE', 'CONFIGURACION', 'Configuración modificada: CONFIGURACION_SISTEMA = Configuraciones del sistema actualizadas', NULL, NULL, '127.0.0.1', '2025-08-18 08:23:07', 'EXITOSO'),
(86, 3, 'CREATE', 'VENTAS', 'Venta registrada: VTA-000024 - Total: $67.28 - TRANSFERENCIA', NULL, NULL, '127.0.0.1', '2025-08-18 08:24:22', 'EXITOSO'),
(87, 3, 'PROCESS', 'OCR', 'OCR procesado: WhatsApp Image 2025-08-18 at 02.25.53.jpeg - Productos detectados: 1 - EXITOSO', NULL, NULL, '127.0.0.1', '2025-08-18 08:27:15', 'EXITOSO'),
(88, 3, 'PROCESS', 'OCR', 'OCR procesado: WhatsApp Image 2025-08-18 at 02.25.53.jpeg - Productos detectados: 1 - EXITOSO', NULL, NULL, '127.0.0.1', '2025-08-18 08:29:42', 'EXITOSO'),
(89, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 08:45:38', 'EXITOSO'),
(90, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 08:45:40', 'EXITOSO'),
(91, 3, 'PROCESS', 'OCR', 'OCR procesado: WhatsApp Image 2025-08-18 at 02.25.53.jpeg - Productos detectados: 1 - EXITOSO', NULL, NULL, '127.0.0.1', '2025-08-18 08:48:53', 'EXITOSO'),
(92, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 08:59:43', 'EXITOSO'),
(93, 3, 'PROCESS', 'OCR', 'OCR procesado: WhatsApp Image 2025-08-18 at 02.25.53.jpeg - Productos detectados: 1 - EXITOSO', NULL, NULL, '127.0.0.1', '2025-08-18 09:01:05', 'EXITOSO'),
(94, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 09:07:46', 'EXITOSO'),
(95, 3, 'PROCESS', 'OCR', 'OCR procesado: WhatsApp Image 2025-08-18 at 02.25.53.jpeg - Productos detectados: 1 - EXITOSO', NULL, NULL, '127.0.0.1', '2025-08-18 09:08:14', 'EXITOSO'),
(96, 3, 'VALIDATE', 'OCR', 'OCR validado: WhatsApp Image 2025-08-18 at 02.25.53.jpeg - VALIDADO', NULL, NULL, '127.0.0.1', '2025-08-18 09:10:57', 'EXITOSO'),
(97, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 15:15:29', 'EXITOSO'),
(98, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 15:21:10', 'EXITOSO'),
(99, 3, 'UPDATE', 'CONFIGURACION', 'Configuración modificada: CONFIGURACION_SISTEMA = Configuraciones del sistema actualizadas', NULL, NULL, '127.0.0.1', '2025-08-18 15:24:16', 'EXITOSO'),
(100, 3, 'CREATE', 'VENTAS', 'Venta registrada: VTA-000025 - Total: $67.28 - TRANSFERENCIA', NULL, NULL, '127.0.0.1', '2025-08-18 15:25:19', 'EXITOSO'),
(101, 3, 'PROCESS', 'OCR', 'OCR procesado: WhatsApp Image 2025-08-18 at 09.22.56.jpeg - Productos detectados: 1 - EXITOSO', NULL, NULL, '127.0.0.1', '2025-08-18 15:25:47', 'EXITOSO'),
(102, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 15:46:52', 'EXITOSO'),
(103, 3, 'CREATE', 'VENTAS', 'Venta registrada: VTA-000026 - Total: $67.28 - TRANSFERENCIA', NULL, NULL, '127.0.0.1', '2025-08-18 15:48:22', 'EXITOSO'),
(104, 3, 'PROCESS', 'OCR', 'OCR procesado: Comprobante BBVA.jpeg - Productos detectados: 0 - EXITOSO', NULL, NULL, '127.0.0.1', '2025-08-18 15:48:44', 'EXITOSO'),
(105, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 16:00:01', 'EXITOSO'),
(106, 3, 'PROCESS', 'OCR', 'OCR procesado: Comprobante BBVA.jpeg - Productos detectados: 1 - EXITOSO', NULL, NULL, '127.0.0.1', '2025-08-18 16:00:43', 'EXITOSO'),
(107, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 16:19:12', 'EXITOSO'),
(108, 3, 'PROCESS', 'OCR', 'OCR procesado: Comprobante BBVA.jpeg - Productos detectados: 1 - EXITOSO', NULL, NULL, '127.0.0.1', '2025-08-18 16:19:46', 'EXITOSO'),
(109, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 16:25:35', 'EXITOSO'),
(110, 3, 'PROCESS', 'OCR', 'OCR procesado: Comprobante BBVA.jpeg - Productos detectados: 1 - EXITOSO', NULL, NULL, '127.0.0.1', '2025-08-18 16:26:11', 'EXITOSO'),
(111, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-18 16:50:47', 'EXITOSO'),
(112, 3, 'PROCESS', 'OCR', 'OCR procesado: Comprobante BBVA.jpeg - Productos detectados: 1 - EXITOSO', NULL, NULL, '127.0.0.1', '2025-08-18 16:51:34', 'EXITOSO'),
(113, 3, 'VALIDATE', 'OCR', 'OCR validado: Comprobante BBVA.jpeg - VALIDADO', NULL, NULL, '127.0.0.1', '2025-08-18 16:54:01', 'EXITOSO'),
(114, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-19 04:01:39', 'EXITOSO'),
(115, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-19 04:07:29', 'EXITOSO'),
(116, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-19 04:16:31', 'EXITOSO'),
(117, 3, 'LOGIN', 'AUTENTICACION', 'Usuario inició sesión', NULL, NULL, '127.0.0.1', '2025-08-19 04:20:22', 'EXITOSO'),
(118, 3, 'BACKUP', 'SISTEMA', 'Respaldo de base de datos creado en: /home/fabian/Respaldos_POS', NULL, NULL, '127.0.0.1', '2025-08-19 04:21:04', 'EXITOSO');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categorias`
--

CREATE TABLE `categorias` (
  `id_categoria` int NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text,
  `categoria_padre` int DEFAULT NULL,
  `estado` tinyint(1) DEFAULT '1',
  `icono` varchar(50) DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_modificacion` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `creado_por` int DEFAULT NULL,
  `modificado_por` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `categorias`
--

INSERT INTO `categorias` (`id_categoria`, `nombre`, `descripcion`, `categoria_padre`, `estado`, `icono`, `fecha_creacion`, `fecha_modificacion`, `creado_por`, `modificado_por`) VALUES
(1, 'Bebidass', 'Bebidas y refrescos', NULL, 1, NULL, '2025-08-17 06:08:35', '2025-08-18 00:18:34', NULL, 3),
(2, 'Snacks', 'Botanas y golosinas', NULL, 1, NULL, '2025-08-17 06:08:35', '2025-08-18 00:18:43', NULL, NULL),
(4, 'Abarrotes', 'Productos básicoss', NULL, 1, NULL, '2025-08-17 06:08:35', '2025-08-18 00:18:29', NULL, NULL),
(6, 'Cuidado Personal', 'Cuidado Personal', NULL, 1, NULL, '2025-08-18 00:19:01', NULL, 3, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `clientes`
--

CREATE TABLE `clientes` (
  `id_cliente` int NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellidos` varchar(100) DEFAULT NULL,
  `rfc` varchar(13) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `direccion` text,
  `fecha_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `credito` decimal(10,2) DEFAULT '0.00',
  `estado` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `comprobantes_ocr`
--

CREATE TABLE `comprobantes_ocr` (
  `id_comprobante` int NOT NULL,
  `id_venta` int DEFAULT NULL,
  `imagen_original` varchar(255) NOT NULL,
  `imagen_procesada` varchar(255) DEFAULT NULL,
  `banco_emisor` varchar(100) DEFAULT NULL,
  `cuenta_remitente` varchar(50) DEFAULT NULL,
  `monto_detectado` decimal(10,2) DEFAULT NULL,
  `fecha_transferencia` date DEFAULT NULL,
  `referencia_operacion` varchar(100) DEFAULT NULL,
  `nombre_beneficiario` varchar(200) DEFAULT NULL,
  `estado_validacion` enum('VALIDADO','PENDIENTE','RECHAZADO','ERROR_PROCESAMIENTO') DEFAULT 'PENDIENTE',
  `datos_extraidos` json DEFAULT NULL,
  `fecha_procesamiento` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `id_usuario_validador` int DEFAULT NULL,
  `observaciones` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `comprobantes_ocr`
--

INSERT INTO `comprobantes_ocr` (`id_comprobante`, `id_venta`, `imagen_original`, `imagen_procesada`, `banco_emisor`, `cuenta_remitente`, `monto_detectado`, `fecha_transferencia`, `referencia_operacion`, `nombre_beneficiario`, `estado_validacion`, `datos_extraidos`, `fecha_procesamiento`, `id_usuario_validador`, `observaciones`) VALUES
(1, NULL, '/home/fabian/IdeaProjects/PuntoVentaOCR/imagenes/productos/IMG_1755478824942.jpeg', NULL, '', '', 0.00, NULL, '', '', 'RECHAZADO', NULL, '2025-08-18 05:38:24', 3, 'Esta incorrecto el comprobante'),
(2, 26, '/home/fabian/Descargas/WhatsApp Image 2025-08-18 at 02.25.53.jpeg', NULL, 'Nu', '', 67.28, '2025-08-18', '810185', '', 'VALIDADO', '{\"longitud_texto\": 948, \"texto_extraido\": \"nu\\n\\nComprobante de transferencia\\n\\nAutorización 18 AGO 2025, 02:25:12 AM (hora\\nde CDMX)\\n\\nMonto                                           $67.28\\n\\nsPEl\\n\\nTipo de transferencia              Transferencia gratuita\\n\\nConcepto\\n\\nTransferencia\\n\\nNúmero de referencia                                      180825\\n\\nCuenta destino\\n\\nOsvaldo Fabian Ramiro Balboa\\n\\nNombre:                       Dato no verificado por Nu\\nEntidad                                  BBVA MEXICO\\nTarjeta de débito                                             0953\\nEstatus                                               Aceptada\\nClave de         NU38KBHM4U8T8LFOFN92J3EALLAT\\nrastreo\\n\\nCuenta origen\\n\\nNombre               Osvaldo Fabian Ramiro Balboa\\nEntidad                                Nu México\\nCLABE                                         ..-0606\\n\\nNu México Financiera, S.A. de CV, Sociedad\\nFinanciera Popular\\n\\nReferencia interna\\n16822e362-791d-A55f-87dd-2298dcaad55d\\n\\nMás información 5\\n\", \"banco_detectado\": \"BBVA\", \"monto_detectado\": 67.28, \"fecha_procesamiento\": \"2025-08-18T03:08:13.926827256\", \"referencia_detectada\": \"180825\"}', '2025-08-18 09:10:56', 3, ''),
(3, 28, '/home/fabian/Descargas/Comprobante BBVA.jpeg', NULL, 'BBVA', '3773', 67.28, '2025-08-18', 'MBANO1002508180065503904', 'TU EMPRESA C:A', 'VALIDADO', '{\"longitud_texto\": 213, \"texto_extraido\": \"{\\\"referenciaOperacion\\\":\\\"MBANO1002508180065503904\\\",\\\"montoDetectado\\\":\\\"67.28\\\",\\\"nombreBeneficiario\\\":\\\"Osvaldo Fabian Ramiro Balboa\\\",\\\"fechaTransferencia\\\":\\\"2025-08-18T00:00\\\",\\\"cuentaRemitente\\\":\\\"3773\\\",\\\"bancoEmisor\\\":\\\"BBVA\\\"}\", \"banco_detectado\": \"BBVA\", \"monto_detectado\": 67.28, \"fecha_procesamiento\": \"2025-08-18T10:51:35.814731557\", \"referencia_detectada\": \"MBANO1002508180065503904\"}', '2025-08-18 16:54:00', 3, '');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `configuraciones`
--

CREATE TABLE `configuraciones` (
  `id_configuracion` int NOT NULL,
  `clave` varchar(100) NOT NULL,
  `valor` text NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `categoria` varchar(50) DEFAULT 'GENERAL',
  `tipo_dato` enum('STRING','INTEGER','DECIMAL','BOOLEAN') DEFAULT 'STRING',
  `modificado_por` int DEFAULT NULL,
  `fecha_modificacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `configuraciones`
--

INSERT INTO `configuraciones` (`id_configuracion`, `clave`, `valor`, `descripcion`, `categoria`, `tipo_dato`, `modificado_por`, `fecha_modificacion`) VALUES
(1, 'sistema.nombre', 'PuntoVentaOCR', 'Nombre del sistema', 'GENERAL', 'STRING', NULL, '2025-08-17 22:30:12'),
(2, 'sistema.version', '1.0.0', 'Versión del sistema', 'GENERAL', 'STRING', NULL, '2025-08-17 22:30:12'),
(3, 'ventas.iva_porcentaje', '16.0', 'Porcentaje de IVA a aplicar', 'VENTAS', 'DECIMAL', 3, '2025-08-18 15:24:03'),
(4, 'ventas.numero_serie', 'VTA', 'Serie para numeración de ventas', 'VENTAS', 'STRING', 3, '2025-08-18 15:24:04'),
(5, 'ventas.mensaje_ticket', 'Gracias por su compra', 'Mensaje en el ticket de venta', 'VENTAS', 'STRING', 3, '2025-08-18 15:24:05'),
(6, 'sistema.backup_automatico', 'true', 'Activar backup automático', 'SISTEMA', 'BOOLEAN', 3, '2025-08-18 15:24:07'),
(7, 'sistema.timeout_sesion', '30', 'Tiempo de inactividad en minutos', 'SISTEMA', 'INTEGER', 3, '2025-08-18 15:24:07'),
(8, 'empresa.nombre', 'Prismas LS', 'Nombre de la empresa', 'EMPRESA', 'STRING', 3, '2025-08-18 15:23:56'),
(9, 'empresa.rfc', 'XAXX010101001', 'RFC de la empresa', 'EMPRESA', 'STRING', 3, '2025-08-18 15:23:58'),
(10, 'empresa.direccion', 'Calle LS AV. Figuras', 'Dirección física', 'EMPRESA', 'STRING', 3, '2025-08-18 15:24:00'),
(11, 'empresa.telefono', '8348348344', 'Teléfono de contacto', 'EMPRESA', 'STRING', 3, '2025-08-18 15:24:02'),
(19, 'ventas.permitir_sin_stock', 'false', NULL, 'GENERAL', 'STRING', 3, '2025-08-18 15:24:06'),
(22, 'sistema.moneda', 'MXN', NULL, 'GENERAL', 'STRING', 3, '2025-08-18 15:24:08'),
(23, 'sistema.ruta_backup', '', NULL, 'GENERAL', 'STRING', 3, '2025-08-18 15:24:09'),
(24, 'ocr.motor', 'Tesseract', NULL, 'GENERAL', 'STRING', 3, '2025-08-18 15:24:10'),
(25, 'ocr.carpeta_archivos', '/home/fabian/Documentos', NULL, 'GENERAL', 'STRING', 3, '2025-08-18 15:24:10'),
(26, 'ocr.validacion_automatica', 'true', NULL, 'GENERAL', 'STRING', 3, '2025-08-18 15:24:11'),
(87, 'banco.empresa', 'Nu', NULL, 'GENERAL', 'STRING', 3, '2025-08-18 15:24:12'),
(88, 'banco.tipo_cuenta', 'Corriente', NULL, 'GENERAL', 'STRING', 3, '2025-08-18 15:24:13'),
(89, 'banco.cuenta_destino', '15914732193', NULL, 'GENERAL', 'STRING', 3, '2025-08-18 15:24:13'),
(90, 'banco.nombre_beneficiario', 'Osvaldo Fabian Ramiro Balboa', NULL, 'GENERAL', 'STRING', 3, '2025-08-18 15:24:14'),
(91, 'banco.rif_bancario', '012180015914737731', NULL, 'GENERAL', 'STRING', 3, '2025-08-18 15:24:15'),
(92, 'banco.email_notificacion', 'fabi@gmail.com', NULL, 'GENERAL', 'STRING', 3, '2025-08-18 15:24:16');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `detalle_devoluciones`
--

CREATE TABLE `detalle_devoluciones` (
  `id_detalle_devolucion` int NOT NULL,
  `id_devolucion` int NOT NULL,
  `id_producto` int NOT NULL,
  `cantidad` decimal(10,2) NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `motivo_item` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `detalle_ventas`
--

CREATE TABLE `detalle_ventas` (
  `id_detalle` int NOT NULL,
  `id_venta` int NOT NULL,
  `id_producto` int NOT NULL,
  `cantidad` decimal(10,2) NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `descuento` decimal(10,2) DEFAULT '0.00'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `detalle_ventas`
--

INSERT INTO `detalle_ventas` (`id_detalle`, `id_venta`, `id_producto`, `cantidad`, `precio_unitario`, `subtotal`, `descuento`) VALUES
(1, 1, 1, 4.00, 58.00, 232.00, 0.00),
(2, 2, 1, 4.00, 58.00, 232.00, 0.00),
(3, 3, 1, 4.00, 58.00, 232.00, 0.00),
(4, 4, 1, 4.00, 58.00, 232.00, 0.00),
(5, 5, 1, 4.00, 58.00, 232.00, 0.00),
(6, 6, 1, 3.00, 58.00, 174.00, 0.00),
(7, 7, 1, 6.00, 58.00, 348.00, 0.00),
(8, 7, 2, 1.00, 150.00, 150.00, 0.00),
(9, 8, 1, 2.00, 58.00, 116.00, 0.00),
(10, 10, 1, 1.00, 58.00, 58.00, 0.00),
(11, 11, 1, 1.00, 58.00, 58.00, 0.00),
(12, 12, 1, 1.00, 58.00, 58.00, 0.00),
(13, 14, 2, 1.00, 150.00, 150.00, 0.00),
(14, 15, 1, 2.00, 58.00, 116.00, 0.00),
(15, 16, 1, 4.00, 58.00, 232.00, 0.00),
(16, 17, 1, 2.00, 58.00, 116.00, 0.00),
(17, 18, 1, 1.00, 58.00, 58.00, 0.00),
(18, 19, 1, 1.00, 58.00, 58.00, 0.00),
(19, 20, 1, 2.00, 58.00, 116.00, 0.00),
(20, 21, 1, 3.00, 58.00, 174.00, 0.00),
(21, 22, 1, 1.00, 58.00, 58.00, 0.00),
(22, 23, 1, 2.00, 58.00, 116.00, 0.00),
(23, 24, 1, 2.00, 58.00, 116.00, 0.00),
(24, 25, 1, 2.00, 58.00, 116.00, 0.00),
(25, 26, 1, 1.00, 58.00, 58.00, 0.00),
(26, 27, 1, 1.00, 58.00, 58.00, 0.00),
(27, 28, 1, 1.00, 58.00, 58.00, 0.00);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `devoluciones`
--

CREATE TABLE `devoluciones` (
  `id_devolucion` int NOT NULL,
  `numero_devolucion` varchar(20) NOT NULL,
  `id_venta_original` int NOT NULL,
  `motivo` varchar(255) NOT NULL,
  `monto_total` decimal(10,2) NOT NULL,
  `estado` enum('PENDIENTE','APROBADA','RECHAZADA','PROCESADA') DEFAULT 'PENDIENTE',
  `id_procesado_por` int NOT NULL,
  `id_autorizado_por` int DEFAULT NULL,
  `observaciones` text,
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_procesamiento` timestamp NULL DEFAULT NULL,
  `fecha_devolucion` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `devoluciones`
--

INSERT INTO `devoluciones` (`id_devolucion`, `numero_devolucion`, `id_venta_original`, `motivo`, `monto_total`, `estado`, `id_procesado_por`, `id_autorizado_por`, `observaciones`, `fecha_creacion`, `fecha_procesamiento`, `fecha_devolucion`) VALUES
(1, 'DEV-1755499335420', 1, 'No le gusto al cliente', 269.12, 'PENDIENTE', 3, NULL, NULL, '2025-08-18 06:42:15', NULL, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `productos`
--

CREATE TABLE `productos` (
  `id_producto` int NOT NULL,
  `codigo_barras` varchar(50) DEFAULT NULL,
  `codigo_interno` varchar(50) NOT NULL,
  `nombre` varchar(200) NOT NULL,
  `descripcion_corta` varchar(255) DEFAULT NULL,
  `descripcion_larga` text,
  `id_categoria` int DEFAULT NULL,
  `marca` varchar(100) DEFAULT NULL,
  `precio_compra` decimal(10,2) NOT NULL,
  `precio_venta` decimal(10,2) NOT NULL,
  `stock` int DEFAULT '0',
  `stock_minimo` int DEFAULT '5',
  `unidad_medida` varchar(20) DEFAULT 'PIEZA',
  `imagen` varchar(255) DEFAULT NULL,
  `estado` enum('ACTIVO','INACTIVO','AGOTADO','DESCONTINUADO') DEFAULT 'ACTIVO',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_modificacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creado_por` int DEFAULT NULL,
  `modificado_por` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `productos`
--

INSERT INTO `productos` (`id_producto`, `codigo_barras`, `codigo_interno`, `nombre`, `descripcion_corta`, `descripcion_larga`, `id_categoria`, `marca`, `precio_compra`, `precio_venta`, `stock`, `stock_minimo`, `unidad_medida`, `imagen`, `estado`, `fecha_creacion`, `fecha_modificacion`, `creado_por`, `modificado_por`) VALUES
(1, '1111111000101', '1234567890123', 'Lumbre Peluche', 'El mejor peluche de tu lifeeee', 'Compralo YA que esperas', 4, 'Lumbreboy', 35.00, 58.00, 27, 5, 'PIEZA', 'imagenes/productos/IMG_1755478824942.jpeg', 'ACTIVO', '2025-08-18 00:50:45', '2025-08-18 15:48:20', 3, NULL),
(2, '1234554321123', '', 'FabiOS', 'Lo mejor', 'De lo mejor', 4, 'Fabian Corp', 20.00, 150.00, 30, 10, 'PAQUETE', 'imagenes/productos/IMG_1755478913509.jpeg', 'ACTIVO', '2025-08-18 01:01:56', '2025-08-18 06:43:02', 3, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `roles`
--

CREATE TABLE `roles` (
  `id_rol` int NOT NULL,
  `nombre_rol` varchar(50) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `estado` tinyint(1) DEFAULT '1',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `roles`
--

INSERT INTO `roles` (`id_rol`, `nombre_rol`, `descripcion`, `estado`, `fecha_creacion`) VALUES
(1, 'ADMINISTRADOR', 'Acceso total al sistema', 1, '2025-08-17 06:08:35'),
(2, 'GERENTE', 'Acceso a reportes y gestión de productos', 1, '2025-08-17 06:08:35'),
(3, 'CAJERO', 'Acceso a ventas y consulta de productos', 1, '2025-08-17 06:08:35');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `sesiones`
--

CREATE TABLE `sesiones` (
  `id_sesion` int NOT NULL,
  `id_usuario` int NOT NULL,
  `fecha_inicio` datetime NOT NULL,
  `fecha_fin` datetime DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `token_sesion` varchar(255) DEFAULT NULL,
  `estado` enum('ACTIVA','EXPIRADA','CERRADA') DEFAULT 'ACTIVA',
  `navegador` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `sesiones`
--

INSERT INTO `sesiones` (`id_sesion`, `id_usuario`, `fecha_inicio`, `fecha_fin`, `ip_address`, `token_sesion`, `estado`, `navegador`) VALUES
(1, 3, '2025-08-18 07:50:16', NULL, NULL, NULL, 'ACTIVA', NULL),
(2, 3, '2025-08-18 07:59:54', NULL, NULL, NULL, 'ACTIVA', NULL),
(3, 6, '2025-08-18 08:02:30', NULL, NULL, NULL, 'ACTIVA', NULL),
(4, 6, '2025-08-18 08:17:17', NULL, NULL, NULL, 'ACTIVA', NULL),
(5, 5, '2025-08-18 08:18:26', NULL, NULL, NULL, 'ACTIVA', NULL),
(6, 3, '2025-08-18 08:20:21', NULL, NULL, NULL, 'ACTIVA', NULL),
(7, 3, '2025-08-18 08:45:36', NULL, NULL, NULL, 'ACTIVA', NULL),
(8, 3, '2025-08-18 08:45:36', NULL, NULL, NULL, 'ACTIVA', NULL),
(9, 3, '2025-08-18 08:59:41', NULL, NULL, NULL, 'ACTIVA', NULL),
(10, 3, '2025-08-18 09:07:44', NULL, NULL, NULL, 'ACTIVA', NULL),
(11, 3, '2025-08-18 15:15:25', NULL, NULL, NULL, 'ACTIVA', NULL),
(12, 3, '2025-08-18 15:21:07', NULL, NULL, NULL, 'ACTIVA', NULL),
(13, 3, '2025-08-18 15:46:50', NULL, NULL, NULL, 'ACTIVA', NULL),
(14, 3, '2025-08-18 15:59:59', NULL, NULL, NULL, 'ACTIVA', NULL),
(15, 3, '2025-08-18 16:19:10', NULL, NULL, NULL, 'ACTIVA', NULL),
(16, 3, '2025-08-18 16:25:33', NULL, NULL, NULL, 'ACTIVA', NULL),
(17, 3, '2025-08-18 16:50:44', NULL, NULL, NULL, 'ACTIVA', NULL),
(18, 3, '2025-08-19 04:01:37', NULL, NULL, NULL, 'ACTIVA', NULL),
(19, 3, '2025-08-19 04:07:26', NULL, NULL, NULL, 'ACTIVA', NULL),
(20, 3, '2025-08-19 04:16:29', NULL, NULL, NULL, 'ACTIVA', NULL),
(21, 3, '2025-08-19 04:20:20', NULL, NULL, NULL, 'ACTIVA', NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id_usuario` int NOT NULL,
  `nombre_usuario` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellidos` varchar(100) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `id_rol` int NOT NULL,
  `estado` enum('ACTIVO','INACTIVO','BLOQUEADO','SUSPENDIDO') DEFAULT 'ACTIVO',
  `intentos_fallidos` int DEFAULT '0',
  `bloqueado` tinyint(1) DEFAULT '0',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_modificacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creado_por` int DEFAULT NULL,
  `modificado_por` int DEFAULT NULL,
  `ultimo_acceso` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id_usuario`, `nombre_usuario`, `password`, `nombre`, `apellidos`, `email`, `telefono`, `id_rol`, `estado`, `intentos_fallidos`, `bloqueado`, `fecha_creacion`, `fecha_modificacion`, `creado_por`, `modificado_por`, `ultimo_acceso`) VALUES
(2, 'admin', '$2a$10$lT7Ns/7xLSa0GCOsj/xVpOMRxqRIG8yjk5qws8OuhXWaGaOQeoIja', 'ADMINISTRADOR', 'Sistema', 'admin@pos.com', NULL, 1, 'INACTIVO', 0, 0, '2025-08-17 06:54:53', '2025-08-17 23:49:56', NULL, NULL, NULL),
(3, 'fabian', '$2a$10$oaeAk/kDrfudZurWgV25YOvygaJdMddJSjhB30wCm2sPcsfX1/A9q', 'Fabian', 'Ramiro', 'fabian@gmail.com', NULL, 1, 'ACTIVO', 0, 0, '2025-08-17 23:09:32', '2025-08-19 04:20:20', NULL, NULL, '2025-08-19 04:20:20'),
(4, 'puga', '$2a$10$.0ejdwXpRcGJf1IYyMzwKOvsvKx77c78vM6sJcRPUWhCL06con4zG', 'Jose', 'Puga', 'puga@gmail.com', NULL, 1, 'ACTIVO', 0, 0, '2025-08-17 23:10:09', '2025-08-18 05:57:52', NULL, NULL, NULL),
(5, 'cristian', '$2a$10$MAVxhsH6KKMoWLWkTN5dEeZdD4SKycFEPtysloL8NLRHehDMHtph6', 'Cristian', 'Ramiro', 'cristian@gmail.com', NULL, 3, 'ACTIVO', 0, 0, '2025-08-17 23:10:47', '2025-08-18 08:18:26', NULL, 3, '2025-08-18 08:18:26'),
(6, 'ale', '$2a$10$9zu0YEAlr/r.jGxSjmtuk.lXiEYSJRcZsCETpFetXrYjYmy9Pbe6S', 'Alejandro', 'Varela', 'ale@gmail.com', NULL, 2, 'ACTIVO', 0, 0, '2025-08-18 07:14:02', '2025-08-18 08:17:17', NULL, 3, '2025-08-18 08:17:17');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `ventas`
--

CREATE TABLE `ventas` (
  `id_venta` int NOT NULL,
  `numero_venta` varchar(20) NOT NULL,
  `fecha` datetime NOT NULL,
  `id_usuario` int NOT NULL,
  `id_cliente` int DEFAULT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `iva` decimal(10,2) NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `metodo_pago` enum('EFECTIVO','TARJETA','TRANSFERENCIA') NOT NULL,
  `estado` enum('COMPLETADA','PENDIENTE','ANULADA','EN_PROCESO') DEFAULT 'COMPLETADA',
  `motivo_anulacion` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `ventas`
--

INSERT INTO `ventas` (`id_venta`, `numero_venta`, `fecha`, `id_usuario`, `id_cliente`, `subtotal`, `iva`, `total`, `metodo_pago`, `estado`, `motivo_anulacion`) VALUES
(1, 'VTA-000001', '2025-08-18 01:27:44', 3, NULL, 232.00, 37.12, 269.12, 'TARJETA', 'COMPLETADA', NULL),
(2, 'VTA-000002', '2025-08-18 01:30:16', 3, NULL, 232.00, 37.12, 269.12, 'TRANSFERENCIA', 'COMPLETADA', NULL),
(3, 'VTA-000003', '2025-08-18 02:06:54', 3, NULL, 232.00, 37.12, 269.12, 'TARJETA', 'COMPLETADA', NULL),
(4, 'VTA-000004', '2025-08-18 02:08:41', 3, NULL, 232.00, 37.12, 269.12, 'EFECTIVO', 'COMPLETADA', NULL),
(5, 'VTA-000005', '2025-08-18 02:09:37', 3, NULL, 232.00, 37.12, 269.12, 'EFECTIVO', 'COMPLETADA', NULL),
(6, 'VTA-000006', '2025-08-18 02:11:11', 3, NULL, 174.00, 27.84, 201.84, 'EFECTIVO', 'COMPLETADA', NULL),
(7, 'VTA-000007', '2025-08-18 02:22:17', 3, NULL, 498.00, 79.68, 577.68, 'EFECTIVO', 'COMPLETADA', NULL),
(8, 'VTA-000008', '2025-08-18 02:25:16', 3, NULL, 116.00, 18.56, 134.56, 'EFECTIVO', 'COMPLETADA', NULL),
(10, 'VTA-000009', '2025-08-18 02:26:25', 3, NULL, 58.00, 9.28, 67.28, 'EFECTIVO', 'COMPLETADA', NULL),
(11, 'VTA-000010', '2025-08-18 02:26:53', 3, NULL, 58.00, 9.28, 67.28, 'EFECTIVO', 'COMPLETADA', NULL),
(12, 'VTA-000011', '2025-08-18 02:27:39', 3, NULL, 58.00, 9.28, 67.28, 'EFECTIVO', 'COMPLETADA', NULL),
(14, 'VTA-000012', '2025-08-18 02:28:17', 3, NULL, 150.00, 24.00, 174.00, 'TARJETA', 'COMPLETADA', NULL),
(15, 'VTA-000013', '2025-08-18 02:28:34', 3, NULL, 116.00, 18.56, 134.56, 'TARJETA', 'COMPLETADA', NULL),
(16, 'VTA-000014', '2025-08-18 03:46:31', 3, NULL, 232.00, 37.12, 269.12, 'EFECTIVO', 'COMPLETADA', NULL),
(17, 'VTA-000015', '2025-08-18 03:47:02', 3, NULL, 116.00, 18.56, 134.56, 'EFECTIVO', 'COMPLETADA', NULL),
(18, 'VTA-000016', '2025-08-18 03:49:36', 3, NULL, 58.00, 9.28, 67.28, 'EFECTIVO', 'COMPLETADA', NULL),
(19, 'VTA-000017', '2025-08-18 03:49:56', 3, NULL, 58.00, 9.28, 67.28, 'EFECTIVO', 'COMPLETADA', NULL),
(20, 'VTA-000018', '2025-08-18 05:20:39', 3, NULL, 116.00, 18.56, 134.56, 'EFECTIVO', 'COMPLETADA', NULL),
(21, 'VTA-000019', '2025-08-18 05:22:01', 3, NULL, 174.00, 27.84, 201.84, 'EFECTIVO', 'COMPLETADA', NULL),
(22, 'VTA-000020', '2025-08-18 05:22:40', 3, NULL, 58.00, 9.28, 67.28, 'TARJETA', 'COMPLETADA', NULL),
(23, 'VTA-000021', '2025-08-18 05:23:01', 3, NULL, 116.00, 18.56, 134.56, 'TRANSFERENCIA', 'COMPLETADA', NULL),
(24, 'VTA-000022', '2025-08-18 05:37:08', 3, NULL, 116.00, 18.56, 134.56, 'TRANSFERENCIA', 'ANULADA', 'fue incorrecta'),
(25, 'VTA-000023', '2025-08-18 05:58:03', 4, NULL, 116.00, 18.56, 134.56, 'EFECTIVO', 'COMPLETADA', NULL),
(26, 'VTA-000024', '2025-08-18 08:24:01', 3, NULL, 58.00, 9.28, 67.28, 'TRANSFERENCIA', 'COMPLETADA', NULL),
(27, 'VTA-000025', '2025-08-18 15:24:50', 3, NULL, 58.00, 9.28, 67.28, 'TRANSFERENCIA', 'COMPLETADA', NULL),
(28, 'VTA-000026', '2025-08-18 15:48:09', 3, NULL, 58.00, 9.28, 67.28, 'TRANSFERENCIA', 'COMPLETADA', NULL);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `alertas_stock`
--
ALTER TABLE `alertas_stock`
  ADD PRIMARY KEY (`id_alerta`),
  ADD KEY `id_producto` (`id_producto`),
  ADD KEY `id_usuario_notificado` (`id_usuario_notificado`);

--
-- Indices de la tabla `bitacora_acciones`
--
ALTER TABLE `bitacora_acciones`
  ADD PRIMARY KEY (`id_bitacora`),
  ADD KEY `id_usuario` (`id_usuario`);

--
-- Indices de la tabla `categorias`
--
ALTER TABLE `categorias`
  ADD PRIMARY KEY (`id_categoria`),
  ADD KEY `categoria_padre` (`categoria_padre`),
  ADD KEY `fk_categoria_creado_por` (`creado_por`),
  ADD KEY `fk_categoria_modificado_por` (`modificado_por`);

--
-- Indices de la tabla `clientes`
--
ALTER TABLE `clientes`
  ADD PRIMARY KEY (`id_cliente`);

--
-- Indices de la tabla `comprobantes_ocr`
--
ALTER TABLE `comprobantes_ocr`
  ADD PRIMARY KEY (`id_comprobante`),
  ADD UNIQUE KEY `referencia_operacion` (`referencia_operacion`),
  ADD KEY `id_venta` (`id_venta`),
  ADD KEY `id_usuario_validador` (`id_usuario_validador`),
  ADD KEY `idx_comprobantes_referencia` (`referencia_operacion`);

--
-- Indices de la tabla `configuraciones`
--
ALTER TABLE `configuraciones`
  ADD PRIMARY KEY (`id_configuracion`),
  ADD UNIQUE KEY `clave` (`clave`),
  ADD KEY `modificado_por` (`modificado_por`);

--
-- Indices de la tabla `detalle_devoluciones`
--
ALTER TABLE `detalle_devoluciones`
  ADD PRIMARY KEY (`id_detalle_devolucion`),
  ADD KEY `id_devolucion` (`id_devolucion`),
  ADD KEY `id_producto` (`id_producto`);

--
-- Indices de la tabla `detalle_ventas`
--
ALTER TABLE `detalle_ventas`
  ADD PRIMARY KEY (`id_detalle`),
  ADD KEY `id_venta` (`id_venta`),
  ADD KEY `id_producto` (`id_producto`);

--
-- Indices de la tabla `devoluciones`
--
ALTER TABLE `devoluciones`
  ADD PRIMARY KEY (`id_devolucion`),
  ADD UNIQUE KEY `numero_devolucion` (`numero_devolucion`),
  ADD KEY `id_venta_original` (`id_venta_original`),
  ADD KEY `id_procesado_por` (`id_procesado_por`),
  ADD KEY `id_autorizado_por` (`id_autorizado_por`);

--
-- Indices de la tabla `productos`
--
ALTER TABLE `productos`
  ADD PRIMARY KEY (`id_producto`),
  ADD UNIQUE KEY `codigo_interno` (`codigo_interno`),
  ADD UNIQUE KEY `codigo_barras` (`codigo_barras`),
  ADD KEY `id_categoria` (`id_categoria`),
  ADD KEY `creado_por` (`creado_por`),
  ADD KEY `modificado_por` (`modificado_por`),
  ADD KEY `idx_productos_codigo` (`codigo_barras`);

--
-- Indices de la tabla `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`id_rol`),
  ADD UNIQUE KEY `nombre_rol` (`nombre_rol`);

--
-- Indices de la tabla `sesiones`
--
ALTER TABLE `sesiones`
  ADD PRIMARY KEY (`id_sesion`),
  ADD KEY `idx_sesiones_usuario` (`id_usuario`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `nombre_usuario` (`nombre_usuario`),
  ADD KEY `id_rol` (`id_rol`),
  ADD KEY `creado_por` (`creado_por`),
  ADD KEY `modificado_por` (`modificado_por`),
  ADD KEY `idx_usuarios_nombre` (`nombre_usuario`);

--
-- Indices de la tabla `ventas`
--
ALTER TABLE `ventas`
  ADD PRIMARY KEY (`id_venta`),
  ADD UNIQUE KEY `numero_venta` (`numero_venta`),
  ADD KEY `id_usuario` (`id_usuario`),
  ADD KEY `id_cliente` (`id_cliente`),
  ADD KEY `idx_ventas_fecha` (`fecha`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `alertas_stock`
--
ALTER TABLE `alertas_stock`
  MODIFY `id_alerta` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `bitacora_acciones`
--
ALTER TABLE `bitacora_acciones`
  MODIFY `id_bitacora` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=119;

--
-- AUTO_INCREMENT de la tabla `categorias`
--
ALTER TABLE `categorias`
  MODIFY `id_categoria` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `clientes`
--
ALTER TABLE `clientes`
  MODIFY `id_cliente` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `comprobantes_ocr`
--
ALTER TABLE `comprobantes_ocr`
  MODIFY `id_comprobante` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `configuraciones`
--
ALTER TABLE `configuraciones`
  MODIFY `id_configuracion` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=135;

--
-- AUTO_INCREMENT de la tabla `detalle_devoluciones`
--
ALTER TABLE `detalle_devoluciones`
  MODIFY `id_detalle_devolucion` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `detalle_ventas`
--
ALTER TABLE `detalle_ventas`
  MODIFY `id_detalle` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT de la tabla `devoluciones`
--
ALTER TABLE `devoluciones`
  MODIFY `id_devolucion` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `productos`
--
ALTER TABLE `productos`
  MODIFY `id_producto` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `roles`
--
ALTER TABLE `roles`
  MODIFY `id_rol` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `sesiones`
--
ALTER TABLE `sesiones`
  MODIFY `id_sesion` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id_usuario` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `ventas`
--
ALTER TABLE `ventas`
  MODIFY `id_venta` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `alertas_stock`
--
ALTER TABLE `alertas_stock`
  ADD CONSTRAINT `alertas_stock_ibfk_1` FOREIGN KEY (`id_producto`) REFERENCES `productos` (`id_producto`),
  ADD CONSTRAINT `alertas_stock_ibfk_2` FOREIGN KEY (`id_usuario_notificado`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `bitacora_acciones`
--
ALTER TABLE `bitacora_acciones`
  ADD CONSTRAINT `bitacora_acciones_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `categorias`
--
ALTER TABLE `categorias`
  ADD CONSTRAINT `categorias_ibfk_1` FOREIGN KEY (`categoria_padre`) REFERENCES `categorias` (`id_categoria`),
  ADD CONSTRAINT `fk_categoria_creado_por` FOREIGN KEY (`creado_por`) REFERENCES `usuarios` (`id_usuario`),
  ADD CONSTRAINT `fk_categoria_modificado_por` FOREIGN KEY (`modificado_por`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `comprobantes_ocr`
--
ALTER TABLE `comprobantes_ocr`
  ADD CONSTRAINT `comprobantes_ocr_ibfk_1` FOREIGN KEY (`id_venta`) REFERENCES `ventas` (`id_venta`),
  ADD CONSTRAINT `comprobantes_ocr_ibfk_2` FOREIGN KEY (`id_usuario_validador`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `configuraciones`
--
ALTER TABLE `configuraciones`
  ADD CONSTRAINT `configuraciones_ibfk_1` FOREIGN KEY (`modificado_por`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `detalle_devoluciones`
--
ALTER TABLE `detalle_devoluciones`
  ADD CONSTRAINT `detalle_devoluciones_ibfk_1` FOREIGN KEY (`id_devolucion`) REFERENCES `devoluciones` (`id_devolucion`),
  ADD CONSTRAINT `detalle_devoluciones_ibfk_2` FOREIGN KEY (`id_producto`) REFERENCES `productos` (`id_producto`);

--
-- Filtros para la tabla `detalle_ventas`
--
ALTER TABLE `detalle_ventas`
  ADD CONSTRAINT `detalle_ventas_ibfk_1` FOREIGN KEY (`id_venta`) REFERENCES `ventas` (`id_venta`),
  ADD CONSTRAINT `detalle_ventas_ibfk_2` FOREIGN KEY (`id_producto`) REFERENCES `productos` (`id_producto`);

--
-- Filtros para la tabla `devoluciones`
--
ALTER TABLE `devoluciones`
  ADD CONSTRAINT `devoluciones_ibfk_1` FOREIGN KEY (`id_venta_original`) REFERENCES `ventas` (`id_venta`),
  ADD CONSTRAINT `devoluciones_ibfk_2` FOREIGN KEY (`id_procesado_por`) REFERENCES `usuarios` (`id_usuario`),
  ADD CONSTRAINT `devoluciones_ibfk_3` FOREIGN KEY (`id_autorizado_por`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `productos`
--
ALTER TABLE `productos`
  ADD CONSTRAINT `productos_ibfk_1` FOREIGN KEY (`id_categoria`) REFERENCES `categorias` (`id_categoria`),
  ADD CONSTRAINT `productos_ibfk_2` FOREIGN KEY (`creado_por`) REFERENCES `usuarios` (`id_usuario`),
  ADD CONSTRAINT `productos_ibfk_3` FOREIGN KEY (`modificado_por`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `sesiones`
--
ALTER TABLE `sesiones`
  ADD CONSTRAINT `sesiones_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD CONSTRAINT `usuarios_ibfk_1` FOREIGN KEY (`id_rol`) REFERENCES `roles` (`id_rol`),
  ADD CONSTRAINT `usuarios_ibfk_2` FOREIGN KEY (`creado_por`) REFERENCES `usuarios` (`id_usuario`),
  ADD CONSTRAINT `usuarios_ibfk_3` FOREIGN KEY (`modificado_por`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `ventas`
--
ALTER TABLE `ventas`
  ADD CONSTRAINT `ventas_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`),
  ADD CONSTRAINT `ventas_ibfk_2` FOREIGN KEY (`id_cliente`) REFERENCES `clientes` (`id_cliente`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
