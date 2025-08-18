package com.pos.puntoventaocr.utils;

import com.pos.puntoventaocr.models.DetalleVenta;
import com.pos.puntoventaocr.models.Venta;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TicketPrinter {

    private static final int ANCHO_TICKET = 40; // Caracteres por línea

    public static boolean imprimirTicket(Venta venta) {
        try {
            String contenidoTicket = generarContenidoTicket(venta);

            // Buscar impresora predeterminada
            PrintService impresora = PrintServiceLookup.lookupDefaultPrintService();

            if (impresora == null) {
                // Si no hay impresora predeterminada, buscar cualquier impresora disponible
                PrintService[] impresoras = PrintServiceLookup.lookupPrintServices(null, null);
                if (impresoras.length > 0) {
                    impresora = impresoras[0];
                } else {
                    // Si no hay impresoras, mostrar el ticket en pantalla
                    mostrarTicketEnPantalla(contenidoTicket);
                    return true;
                }
            }

            // Crear el trabajo de impresión
            DocPrintJob trabajoImpresion = impresora.createPrintJob();

            // Configurar atributos de impresión
            PrintRequestAttributeSet atributos = new HashPrintRequestAttributeSet();
            atributos.add(new Copies(1));
            atributos.add(MediaSizeName.ISO_A4); // Usar formato A4

            // Crear el documento a imprimir
            byte[] bytes = contenidoTicket.getBytes(StandardCharsets.UTF_8);
            DocFlavor sabor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            Doc documento = new SimpleDoc(bytes, sabor, null);

            // Imprimir
            trabajoImpresion.print(documento, atributos);

            return true;

        } catch (Exception e) {
            System.err.println("Error imprimiendo ticket: " + e.getMessage());
            // En caso de error, mostrar el ticket en pantalla como fallback
            try {
                String contenidoTicket = generarContenidoTicket(venta);
                mostrarTicketEnPantalla(contenidoTicket);
                return true;
            } catch (Exception ex) {
                System.err.println("Error generando ticket: " + ex.getMessage());
                return false;
            }
        }
    }

    private static String generarContenidoTicket(Venta venta) {
        StringBuilder ticket = new StringBuilder();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        // Encabezado
        ticket.append(centrarTexto("PUNTO DE VENTA")).append("\n");
        ticket.append(centrarTexto("Sistema POS OCR")).append("\n");
        ticket.append(repetirCaracter("=", ANCHO_TICKET)).append("\n");

        // Información de la venta
        ticket.append("Ticket: ").append(venta.getNumeroVenta()).append("\n");
        ticket.append("Fecha: ").append(formato.format(new Date())).append("\n");
        ticket.append("Vendedor: ").append(venta.getUsuario().getNombreCompleto()).append("\n");
        if (venta.getCliente() != null) {
            ticket.append("Cliente: ").append(venta.getCliente().getNombre()).append("\n");
        }
        ticket.append(repetirCaracter("-", ANCHO_TICKET)).append("\n");

        // Detalles de productos
        ticket.append("CANT  DESCRIPCION           PRECIO   TOTAL\n");
        ticket.append(repetirCaracter("-", ANCHO_TICKET)).append("\n");

        for (DetalleVenta detalle : venta.getDetalles()) {
            String cantidad = String.format("%4s", detalle.getCantidad().intValue());
            String nombre = truncarTexto(detalle.getProducto().getNombre(), 17);
            String precio = String.format("%8s", "$" + detalle.getPrecioUnitario());
            String total = String.format("%8s", "$" + detalle.getSubtotal());

            ticket.append(cantidad).append("  ")
                   .append(nombre).append("  ")
                   .append(precio).append("  ")
                   .append(total).append("\n");
        }

        ticket.append(repetirCaracter("-", ANCHO_TICKET)).append("\n");

        // Totales
        ticket.append(alinearDerecha("Subtotal: $" + venta.getSubtotal())).append("\n");
        ticket.append(alinearDerecha("IVA (16%): $" + venta.getIva())).append("\n");
        ticket.append(repetirCaracter("=", ANCHO_TICKET)).append("\n");
        ticket.append(alinearDerecha("TOTAL: $" + venta.getTotal())).append("\n");
        ticket.append(repetirCaracter("=", ANCHO_TICKET)).append("\n");

        // Método de pago
        ticket.append("Método de pago: ").append(venta.getMetodoPago()).append("\n");

        // Pie de página
        ticket.append("\n");
        ticket.append(centrarTexto("¡Gracias por su compra!")).append("\n");
        ticket.append(centrarTexto("Conserve su ticket")).append("\n");
        ticket.append("\n\n\n"); // Espacios para corte de papel

        return ticket.toString();
    }

    private static void mostrarTicketEnPantalla(String contenidoTicket) {
        // Mostrar el ticket en una ventana emergente
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Ticket de Venta");
            alert.setHeaderText("Impresión de Ticket");

            // Crear un TextArea para mostrar el ticket con fuente monoespaciada
            javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(contenidoTicket);
            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px;");
            textArea.setPrefSize(500, 600);

            alert.getDialogPane().setContent(textArea);
            alert.setResizable(true);
            alert.showAndWait();
        });
    }

    // Métodos utilitarios para formateo
    private static String centrarTexto(String texto) {
        int espacios = (ANCHO_TICKET - texto.length()) / 2;
        return repetirCaracter(" ", Math.max(0, espacios)) + texto;
    }

    private static String alinearDerecha(String texto) {
        int espacios = ANCHO_TICKET - texto.length();
        return repetirCaracter(" ", Math.max(0, espacios)) + texto;
    }

    private static String truncarTexto(String texto, int maxLength) {
        if (texto.length() <= maxLength) {
            return String.format("%-" + maxLength + "s", texto);
        }
        return texto.substring(0, maxLength - 3) + "...";
    }

    private static String repetirCaracter(String caracter, int veces) {
        return caracter.repeat(Math.max(0, veces));
    }
}
