# Proyecto Final

## Sistema de Punto de Venta con OCR para validación

## Transferencias (Java + UML + OCR)

## 1. Entregables Generales

❑ Código fuente Java completo y funcional

❑ Scripts SQL de base de datos

❑ Inicio de sesión con validación

❑ Roles: cajero, gerente y administrador

❑ Registro y edición de usuarios

❑ Bitácora de sesiones

❑ Cierre de sesión seguro

❑ Bloqueo tras intentos fallidos

❑ Gestión de permisos

**Ventas**

❑ Registro de venta

❑ Selección de productos múltiples

❑ Cálculo automático de subtotal, IVA y total

❑ Métodos de pago: efectivo, tarjeta, transferencia

❑ Impresión o simulación de ticket

❑ Historial de ventas

❑ Anulación de ventas con motivo

❑ Filtros por fecha y usuario

**Gestión de Productos**

❑ Alta, baja y modificación de productos

❑ Formulario de registro con validaciones:

❑ Nombre del producto (obligatorio)

❑ Descripción corta y larga

❑ Imagen principal del producto (formato JPG/PNG)

❑ Botón para seleccionar imagen desde el disco

❑ Vista previa de la imagen seleccionada

❑ Precio de compra y precio de venta

❑ Cantidad en stock

❑ Unidad de medida (p. ej., pieza, litro, kilo)

❑ Categoría (editable por el administrador)

❑ Código de barras único o código interno del
producto

❑ Estado del producto: Activo / Inactivo

❑ Categorización dinámica de productos (ej. bebidas, snacks,
limpieza)

❑ Filtro por categoría, marca, estado y stock

❑ Validación automática de códigos duplicados

❑ Alerta por productos con bajo stock o próximos a
agotarse

❑ Compatibilidad con escaner de codigo de barras

❑ Iconos para acciones rápidas (editar, eliminar, ver)

❑ Confirmación antes de eliminar un producto

❑ Registro de quien creó y quien modificó el producto

❑ Fecha de alta y última modificación

❑ Botón para duplicar producto existente (clonar)

❑ Sincronización del inventario tras cada venta

❑ Deshabilitar productos automáticamente si stock = 0 (opcional)

**OCR para Transferencias**

❑ Carga de comprobante (formatos soportados: JPG, PNG,
PDF)

❑ Implementación de motor OCR (Tesseract u otro equivalente)

❑ Extracción automática de datos clave:

❑ Nombre del banco emisor

❑ Número de cuenta del remitente (si aplica)

❑ Monto transferido

❑ Fecha de la transferencia

❑ Referencia de la operación (número clave)

❑ Nombre del beneficiario (opcional)

❑ Visualización de los datos extraídos en un formulario para
validación

❑ Edición manual de campos en caso de errores del OCR

❑ Validación cruzada con venta registrada:

❑ Coincidencia del monto con el total de venta

❑ Referencia única no usada previamente

❑ Validación temporal (la venta debe haber sido realizada en el mismo
día)

❑ Asignación automática de transferencia a la venta correspondiente

❑ Registro en base de datos del resultado OCR:

❑ Imagen original del comprobante (almacenada)

❑ Datos extraídos

❑ Estado de validación (validado, pendiente, rechazado)

❑ Venta asociada

❑ Usuario que realizó la verificación

❑ Visualización del historial de comprobantes OCR procesados:

❑ Filtro por fecha, monto, estado, referencia

❑ Descarga del comprobante original

❑ Detalles de validación

❑ Manejo de errores:

❑ Mensaje de OCR fallido si imagen ilegible

❑ Opción de intentar con otra imagen

❑ Indicador visual (color o ícono) del estado de validación

❑ Confirmación final antes de asociar transferencia a venta

❑ Protección contra doble registro de una misma
transferencia

❑ Pruebas con comprobantes reales de diferentes bancos

**Reportes**

❑ Reporte de ventas (día, semana, mes)

❑ Productos más vendidos

❑ Reporte de ganancias

❑ Exportar a PDF

❑ Reporte de stock

❑ Filtros por fecha y categoría