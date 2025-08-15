-- Crear base de datos
CREATE DATABASE IF NOT EXISTS punto_venta_ocr;
USE punto_venta_ocr;

-- Tabla de Roles
CREATE TABLE IF NOT EXISTS roles (
                                     id_rol INT PRIMARY KEY AUTO_INCREMENT,
                                     nombre_rol VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    estado BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Insertar roles básicos
INSERT INTO roles (nombre_rol, descripcion) VALUES
                                                ('ADMINISTRADOR', 'Acceso total al sistema'),
                                                ('GERENTE', 'Acceso a reportes y gestión de productos'),
                                                ('CAJERO', 'Acceso a ventas y consulta de productos');

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
                                        id_usuario INT PRIMARY KEY AUTO_INCREMENT,
                                        nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    telefono VARCHAR(20),
    id_rol INT NOT NULL,
    estado ENUM('ACTIVO', 'INACTIVO', 'BLOQUEADO', 'SUSPENDIDO') DEFAULT 'ACTIVO',
    intentos_fallidos INT DEFAULT 0,
    bloqueado BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creado_por INT,
    modificado_por INT,
    FOREIGN KEY (id_rol) REFERENCES roles(id_rol),
    FOREIGN KEY (creado_por) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (modificado_por) REFERENCES usuarios(id_usuario)
    );

-- Usuario administrador por defecto (password: admin123)
INSERT INTO usuarios (nombre_usuario, password, nombre, apellidos, email, id_rol)
VALUES ('admin', '$2a$10$DJeI4pGc6QqFY8XwZBxDyOKZTQzHvDDQzV6nmVmNjKxA5KYPyqfLm',
        'Administrador', 'Sistema', 'admin@pos.com', 1);

-- Tabla de Sesiones
CREATE TABLE IF NOT EXISTS sesiones (
                                        id_sesion INT PRIMARY KEY AUTO_INCREMENT,
                                        id_usuario INT NOT NULL,
                                        fecha_inicio DATETIME NOT NULL,
                                        fecha_fin DATETIME,
                                        ip_address VARCHAR(45),
    token_sesion VARCHAR(255),
    estado ENUM('ACTIVA', 'EXPIRADA', 'CERRADA') DEFAULT 'ACTIVA',
    navegador VARCHAR(255),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
    );

-- Tabla de Categorías
CREATE TABLE IF NOT EXISTS categorias (
                                          id_categoria INT PRIMARY KEY AUTO_INCREMENT,
                                          nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    categoria_padre INT,
    estado BOOLEAN DEFAULT TRUE,
    icono VARCHAR(50),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (categoria_padre) REFERENCES categorias(id_categoria)
    );

-- (Prueba) Insertar categorías básicas
INSERT INTO categorias (nombre, descripcion) VALUES
                                                 ('Bebidas', 'Bebidas y refrescos'),
                                                 ('Snacks', 'Botanas y golosinas'),
                                                 ('Limpieza', 'Productos de limpieza'),
                                                 ('Abarrotes', 'Productos básicos');

-- Tabla de Productos
CREATE TABLE IF NOT EXISTS productos (
                                         id_producto INT PRIMARY KEY AUTO_INCREMENT,
                                         codigo_barras VARCHAR(50) UNIQUE,
    codigo_interno VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion_corta VARCHAR(255),
    descripcion_larga TEXT,
    id_categoria INT,
    marca VARCHAR(100),
    precio_compra DECIMAL(10,2) NOT NULL,
    precio_venta DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 0,
    stock_minimo INT DEFAULT 5,
    unidad_medida VARCHAR(20) DEFAULT 'PIEZA',
    imagen VARCHAR(255),
    estado ENUM('ACTIVO', 'INACTIVO', 'AGOTADO', 'DESCONTINUADO') DEFAULT 'ACTIVO',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creado_por INT,
    modificado_por INT,
    FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria),
    FOREIGN KEY (creado_por) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (modificado_por) REFERENCES usuarios(id_usuario)
    );

-- Tabla de Clientes
CREATE TABLE IF NOT EXISTS clientes (
                                        id_cliente INT PRIMARY KEY AUTO_INCREMENT,
                                        nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100),
    rfc VARCHAR(13),
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion TEXT,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    credito DECIMAL(10,2) DEFAULT 0,
    estado BOOLEAN DEFAULT TRUE
    );

-- Tabla de Ventas
CREATE TABLE IF NOT EXISTS ventas (
                                      id_venta INT PRIMARY KEY AUTO_INCREMENT,
                                      numero_venta VARCHAR(20) UNIQUE NOT NULL,
    fecha DATETIME NOT NULL,
    id_usuario INT NOT NULL,
    id_cliente INT,
    subtotal DECIMAL(10,2) NOT NULL,
    iva DECIMAL(10,2) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    metodo_pago ENUM('EFECTIVO', 'TARJETA', 'TRANSFERENCIA') NOT NULL,
    estado ENUM('COMPLETADA', 'PENDIENTE', 'ANULADA', 'EN_PROCESO') DEFAULT 'COMPLETADA',
    motivo_anulacion TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente)
    );

-- Tabla de Detalle de Ventas
CREATE TABLE IF NOT EXISTS detalle_ventas (
                                              id_detalle INT PRIMARY KEY AUTO_INCREMENT,
                                              id_venta INT NOT NULL,
                                              id_producto INT NOT NULL,
                                              cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    descuento DECIMAL(10,2) DEFAULT 0,
    FOREIGN KEY (id_venta) REFERENCES ventas(id_venta),
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
    );

-- Tabla de Comprobantes OCR
CREATE TABLE IF NOT EXISTS comprobantes_ocr (
                                                id_comprobante INT PRIMARY KEY AUTO_INCREMENT,
                                                id_venta INT,
                                                imagen_original VARCHAR(255) NOT NULL,
    imagen_procesada VARCHAR(255),
    banco_emisor VARCHAR(100),
    cuenta_remitente VARCHAR(50),
    monto_detectado DECIMAL(10,2),
    fecha_transferencia DATE,
    referencia_operacion VARCHAR(100) UNIQUE,
    nombre_beneficiario VARCHAR(200),
    estado_validacion ENUM('VALIDADO', 'PENDIENTE', 'RECHAZADO', 'ERROR_PROCESAMIENTO') DEFAULT 'PENDIENTE',
    datos_extraidos JSON,
    fecha_procesamiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_usuario_validador INT,
    observaciones TEXT,
    FOREIGN KEY (id_venta) REFERENCES ventas(id_venta),
    FOREIGN KEY (id_usuario_validador) REFERENCES usuarios(id_usuario)
    );

-- Tabla de Bitácora de Acciones
CREATE TABLE IF NOT EXISTS bitacora_acciones (
                                                 id_bitacora INT PRIMARY KEY AUTO_INCREMENT,
                                                 id_usuario INT NOT NULL,
                                                 accion VARCHAR(100) NOT NULL,
    modulo VARCHAR(50) NOT NULL,
    descripcion TEXT,
    datos_anteriores JSON,
    datos_nuevos JSON,
    ip_address VARCHAR(45),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resultado VARCHAR(50),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
    );

-- Tabla de Alertas de Stock
CREATE TABLE IF NOT EXISTS alertas_stock (
                                             id_alerta INT PRIMARY KEY AUTO_INCREMENT,
                                             id_producto INT NOT NULL,
                                             tipo_alerta ENUM('STOCK_BAJO', 'STOCK_AGOTADO', 'PROXIMO_AGOTARSE') NOT NULL,
    stock_actual INT NOT NULL,
    stock_minimo INT NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('PENDIENTE', 'NOTIFICADA', 'RESUELTA') DEFAULT 'PENDIENTE',
    id_usuario_notificado INT,
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
    FOREIGN KEY (id_usuario_notificado) REFERENCES usuarios(id_usuario)
    );

CREATE INDEX idx_usuarios_nombre ON usuarios(nombre_usuario);
CREATE INDEX idx_productos_codigo ON productos(codigo_barras);
CREATE INDEX idx_ventas_fecha ON ventas(fecha);
CREATE INDEX idx_sesiones_usuario ON sesiones(id_usuario);
CREATE INDEX idx_comprobantes_referencia ON comprobantes_ocr(referencia_operacion);