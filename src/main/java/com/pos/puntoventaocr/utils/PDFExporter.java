package com.pos.puntoventaocr.utils;

import com.pos.puntoventaocr.models.Venta;
import com.pos.puntoventaocr.controllers.ReporteProductosController.ProductoVendido;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExporter {

    public static boolean exportarReporteVentas(List<Venta> ventas, LocalDate fechaDesde, LocalDate fechaHasta, 
                                               String totalVentas, String montoTotal, String promedioDiario, 
                                               String ticketPromedio, Window parentWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Ventas");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );
        fileChooser.setInitialFileName("reporte_ventas_" + 
            fechaDesde.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "_" +
            fechaHasta.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv");

        java.io.File archivo = fileChooser.showSaveDialog(parentWindow);
        
        if (archivo != null) {
            try (FileWriter writer = new FileWriter(archivo)) {
                // Encabezado del reporte
                writer.append("REPORTE DE VENTAS\n");
                writer.append("Período: ").append(fechaDesde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                      .append(" - ").append(fechaHasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
                writer.append("Generado: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
                
                // Resumen
                writer.append("RESUMEN\n");
                writer.append("Total de Ventas: ").append(totalVentas).append("\n");
                writer.append("Monto Total: ").append(montoTotal).append("\n");
                writer.append("Promedio Diario: ").append(promedioDiario).append("\n");
                writer.append("Ticket Promedio: ").append(ticketPromedio).append("\n\n");
                
                // Detalle de ventas
                writer.append("DETALLE DE VENTAS\n");
                writer.append("Fecha,Número,Cliente,Usuario,Subtotal,IVA,Total\n");
                
                for (Venta venta : ventas) {
                    writer.append(venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append(",");
                    writer.append(venta.getNumeroVenta()).append(",");
                    writer.append(venta.getCliente() != null ? venta.getCliente().getNombre() : "N/A").append(",");
                    writer.append(venta.getUsuario() != null ? 
                        venta.getUsuario().getNombre() + " " + venta.getUsuario().getApellido() : "N/A").append(",");
                    writer.append(String.valueOf(venta.getSubtotal())).append(",");
                    writer.append(String.valueOf(venta.getIva())).append(",");
                    writer.append(String.valueOf(venta.getTotal())).append("\n");
                }
                
                return true;
                
            } catch (IOException e) {
                AlertUtils.showError("Error", "Error al exportar el reporte: " + e.getMessage());
                return false;
            }
        }
        
        return false;
    }

    public static boolean exportarReporteProductos(List<ProductoVendido> productos, LocalDate fechaDesde, 
                                                  LocalDate fechaHasta, String totalProductos, 
                                                  String unidadesTotales, String montoTotal, Window parentWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Productos");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );
        fileChooser.setInitialFileName("reporte_productos_" + 
            fechaDesde.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "_" +
            fechaHasta.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv");

        java.io.File archivo = fileChooser.showSaveDialog(parentWindow);
        
        if (archivo != null) {
            try (FileWriter writer = new FileWriter(archivo)) {
                // Encabezado del reporte
                writer.append("REPORTE DE PRODUCTOS MÁS VENDIDOS\n");
                writer.append("Período: ").append(fechaDesde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                      .append(" - ").append(fechaHasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
                writer.append("Generado: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
                
                // Resumen
                writer.append("RESUMEN\n");
                writer.append("Total de Productos: ").append(totalProductos).append("\n");
                writer.append("Unidades Vendidas: ").append(unidadesTotales).append("\n");
                writer.append("Monto Total: ").append(montoTotal).append("\n\n");
                
                // Detalle de productos
                writer.append("DETALLE DE PRODUCTOS\n");
                writer.append("Posición,Código,Nombre,Categoría,Cantidad Vendida,Monto Total,Porcentaje\n");
                
                for (ProductoVendido producto : productos) {
                    writer.append(String.valueOf(producto.getPosicion())).append(",");
                    writer.append(producto.getCodigo()).append(",");
                    writer.append(producto.getNombre()).append(",");
                    writer.append(producto.getCategoria()).append(",");
                    writer.append(String.valueOf(producto.getCantidadVendida())).append(",");
                    writer.append(String.format("%.2f", producto.getMontoTotal())).append(",");
                    writer.append(String.format("%.2f%%", producto.getPorcentajeVentas())).append("\n");
                }
                
                return true;
                
            } catch (IOException e) {
                AlertUtils.showError("Error", "Error al exportar el reporte: " + e.getMessage());
                return false;
            }
        }
        
        return false;
    }

    public static boolean exportarReporteInventario(List<com.pos.puntoventaocr.models.Producto> productos, 
                                                   String valorTotal, String productosActivos, 
                                                   String productosBajoStock, Window parentWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Inventario");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );
        fileChooser.setInitialFileName("reporte_inventario_" + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv");

        java.io.File archivo = fileChooser.showSaveDialog(parentWindow);
        
        if (archivo != null) {
            try (FileWriter writer = new FileWriter(archivo)) {
                // Encabezado del reporte
                writer.append("REPORTE DE INVENTARIO\n");
                writer.append("Generado: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
                
                // Resumen
                writer.append("RESUMEN\n");
                writer.append("Productos Activos: ").append(productosActivos).append("\n");
                writer.append("Productos con Bajo Stock: ").append(productosBajoStock).append("\n");
                writer.append("Valor Total del Inventario: ").append(valorTotal).append("\n\n");
                
                // Detalle de productos
                writer.append("DETALLE DEL INVENTARIO\n");
                writer.append("Código,Nombre,Categoría,Stock Actual,Stock Mínimo,Precio Compra,Precio Venta,Valor Total,Estado\n");
                
                for (com.pos.puntoventaocr.models.Producto producto : productos) {
                    writer.append(producto.getCodigoInterno() != null ? producto.getCodigoInterno() : "").append(",");
                    writer.append(producto.getNombre()).append(",");
                    writer.append(producto.getCategoria() != null ? producto.getCategoria().getNombre() : "Sin categoría").append(",");
                    writer.append(String.valueOf(producto.getCantidadStock())).append(",");
                    writer.append(String.valueOf(producto.getStockMinimo())).append(",");
                    writer.append(String.valueOf(producto.getPrecioCompra())).append(",");
                    writer.append(String.valueOf(producto.getPrecioVenta())).append(",");
                    writer.append(String.valueOf(producto.getPrecioCompra().multiply(java.math.BigDecimal.valueOf(producto.getCantidadStock())))).append(",");
                    writer.append(producto.getEstado()).append("\n");
                }
                
                return true;
                
            } catch (IOException e) {
                AlertUtils.showError("Error", "Error al exportar el reporte: " + e.getMessage());
                return false;
            }
        }
        
        return false;
    }
}
