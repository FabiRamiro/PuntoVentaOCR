package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.ProductoDAO;
import com.pos.puntoventaocr.models.Producto;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AjustarStockController {

    @FXML private Label lblProducto;
    @FXML private Label lblStockActual;
    @FXML private RadioButton rbEntrada;
    @FXML private RadioButton rbSalida;
    @FXML private RadioButton rbAjuste;
    @FXML private TextField txtCantidad;
    @FXML private Label lblNuevoStock;
    @FXML private ComboBox<String> cmbMotivo;
    @FXML private TextArea txtObservaciones;

    private Producto producto;
    private ProductoDAO productoDAO;
    private ToggleGroup grupoTipoAjuste;

    public void initialize() {
        productoDAO = new ProductoDAO();
        configurarControles();
        cargarMotivos();
    }

    private void configurarControles() {
        // Configurar grupo de radio buttons
        grupoTipoAjuste = new ToggleGroup();
        rbEntrada.setToggleGroup(grupoTipoAjuste);
        rbSalida.setToggleGroup(grupoTipoAjuste);
        rbAjuste.setToggleGroup(grupoTipoAjuste);

        // Listener para actualizar el nuevo stock cuando cambie la cantidad o tipo
        txtCantidad.textProperty().addListener((obs, oldVal, newVal) -> {
            // Validar que solo se ingresen números
            if (!newVal.matches("\\d*")) {
                txtCantidad.setText(oldVal);
            } else {
                actualizarNuevoStock();
            }
        });

        grupoTipoAjuste.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            actualizarNuevoStock();
            actualizarPlaceholder();
        });
    }

    private void cargarMotivos() {
        cmbMotivo.setItems(FXCollections.observableArrayList(
            "Recepción de mercancía",
            "Devolución de cliente",
            "Ajuste por inventario físico",
            "Venta",
            "Producto dañado",
            "Producto vencido",
            "Robo/Pérdida",
            "Transferencia a otra sucursal",
            "Corrección de error",
            "Otros"
        ));
    }

    private void actualizarPlaceholder() {
        if (rbEntrada.isSelected()) {
            txtCantidad.setPromptText("Cantidad a agregar");
        } else if (rbSalida.isSelected()) {
            txtCantidad.setPromptText("Cantidad a restar");
        } else if (rbAjuste.isSelected()) {
            txtCantidad.setPromptText("Cantidad exacta final");
        }
    }

    private void actualizarNuevoStock() {
        if (producto == null || txtCantidad.getText().isEmpty()) {
            lblNuevoStock.setText("Nuevo stock: -");
            return;
        }

        try {
            int cantidad = Integer.parseInt(txtCantidad.getText());
            int stockActual = producto.getCantidadStock();
            int nuevoStock;

            if (rbEntrada.isSelected()) {
                nuevoStock = stockActual + cantidad;
                lblNuevoStock.setText("Nuevo stock: " + nuevoStock + " (+" + cantidad + ")");
                lblNuevoStock.setStyle("-fx-text-fill: #28a745;"); // Verde para entradas
            } else if (rbSalida.isSelected()) {
                nuevoStock = stockActual - cantidad;
                lblNuevoStock.setText("Nuevo stock: " + nuevoStock + " (-" + cantidad + ")");
                if (nuevoStock < 0) {
                    lblNuevoStock.setStyle("-fx-text-fill: #dc3545;"); // Rojo para stock negativo
                } else {
                    lblNuevoStock.setStyle("-fx-text-fill: #ffc107;"); // Amarillo para salidas
                }
            } else if (rbAjuste.isSelected()) {
                nuevoStock = cantidad;
                int diferencia = nuevoStock - stockActual;
                String signo = diferencia >= 0 ? "+" : "";
                lblNuevoStock.setText("Nuevo stock: " + nuevoStock + " (" + signo + diferencia + ")");
                if (diferencia > 0) {
                    lblNuevoStock.setStyle("-fx-text-fill: #28a745;");
                } else if (diferencia < 0) {
                    lblNuevoStock.setStyle("-fx-text-fill: #ffc107;");
                } else {
                    lblNuevoStock.setStyle("-fx-text-fill: #6c757d;");
                }
            }

        } catch (NumberFormatException e) {
            lblNuevoStock.setText("Nuevo stock: -");
            lblNuevoStock.setStyle("-fx-text-fill: #6c757d;");
        }
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
        actualizarInfoProducto();
    }

    private void actualizarInfoProducto() {
        if (producto != null) {
            lblProducto.setText(producto.getNombre());
            lblStockActual.setText("Stock actual: " + producto.getCantidadStock() + " " + producto.getUnidadMedida());
            actualizarNuevoStock();
        }
    }

    public boolean validarDatos() {
        StringBuilder errores = new StringBuilder();

        if (producto == null) {
            errores.append("- No se ha seleccionado un producto\n");
        }

        if (grupoTipoAjuste.getSelectedToggle() == null) {
            errores.append("- Debe seleccionar el tipo de ajuste\n");
        }

        if (txtCantidad.getText().trim().isEmpty()) {
            errores.append("- Debe ingresar una cantidad\n");
        } else {
            try {
                int cantidad = Integer.parseInt(txtCantidad.getText());
                if (cantidad <= 0) {
                    errores.append("- La cantidad debe ser mayor a 0\n");
                }

                // Validar que no se genere stock negativo en salidas
                if (rbSalida.isSelected() && cantidad > producto.getCantidadStock()) {
                    errores.append("- No se puede restar más stock del disponible\n");
                }

            } catch (NumberFormatException e) {
                errores.append("- La cantidad debe ser un número entero válido\n");
            }
        }

        if (cmbMotivo.getValue() == null || cmbMotivo.getValue().trim().isEmpty()) {
            errores.append("- Debe seleccionar un motivo para el ajuste\n");
        }

        if (errores.length() > 0) {
            AlertUtils.showError("Errores de validación", errores.toString());
            return false;
        }

        return true;
    }

    public boolean aplicarAjuste() {
        if (!validarDatos()) {
            return false;
        }

        try {
            int cantidad = Integer.parseInt(txtCantidad.getText());
            int stockAnterior = producto.getCantidadStock();
            int nuevoStock;

            if (rbEntrada.isSelected()) {
                producto.aumentarStock(cantidad);
                nuevoStock = producto.getCantidadStock();
            } else if (rbSalida.isSelected()) {
                producto.reducirStock(cantidad);
                nuevoStock = producto.getCantidadStock();
            } else { // rbAjuste
                producto.setCantidadStock(cantidad);
                nuevoStock = cantidad;
            }

            // Actualizar en base de datos
            boolean exito = productoDAO.actualizar(producto);

            if (exito) {
                // Aquí podrías registrar el movimiento en una tabla de historial
                // registrarMovimientoStock(stockAnterior, nuevoStock);
                
                AlertUtils.showInfo("Éxito", 
                    "Stock ajustado correctamente\n" +
                    "Stock anterior: " + stockAnterior + "\n" +
                    "Stock nuevo: " + nuevoStock);
                return true;
            } else {
                AlertUtils.showError("Error", "No se pudo actualizar el stock en la base de datos");
                return false;
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error aplicando ajuste: " + e.getMessage());
            return false;
        }
    }

    public String getMotivo() {
        return cmbMotivo.getValue();
    }

    public String getObservaciones() {
        return txtObservaciones.getText().trim();
    }

    public String getTipoAjuste() {
        if (rbEntrada.isSelected()) return "ENTRADA";
        if (rbSalida.isSelected()) return "SALIDA";
        if (rbAjuste.isSelected()) return "AJUSTE";
        return "";
    }

    public int getCantidad() {
        try {
            return Integer.parseInt(txtCantidad.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
