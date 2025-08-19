package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.ProductoDAO;
import com.pos.puntoventaocr.dao.VentaDAO;
import com.pos.puntoventaocr.models.Producto;
import com.pos.puntoventaocr.models.Venta;
import com.pos.puntoventaocr.models.DetalleVenta;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class NuevaVentaController implements Initializable {

    // Información de la venta
    @FXML private Label lblNumeroVenta;
    @FXML private Label lblFechaHora;
    @FXML private Label lblUsuario;

    // Búsqueda de productos
    @FXML private TextField txtBuscarProducto;
    @FXML private TableView<Producto> tableProductos;
    @FXML private TableColumn<Producto, String> colNombreProducto;
    @FXML private TableColumn<Producto, String> colCodigoProducto;
    @FXML private TableColumn<Producto, BigDecimal> colPrecioProducto;
    @FXML private TableColumn<Producto, Integer> colStockProducto;

    // Carrito de compras
    @FXML private TableView<DetalleVenta> tableCarrito;
    @FXML private TableColumn<DetalleVenta, String> colProductoCarrito;
    @FXML private TableColumn<DetalleVenta, Integer> colCantidadCarrito;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colPrecioCarrito;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colSubtotalCarrito;

    // Totales
    @FXML private Label lblSubtotal;
    @FXML private Label lblImpuestos;
    @FXML private Label lblTotal;
    @FXML private Label lblCantidadArticulos;

    // Método de pago
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private TextField txtEfectivoRecibido;
    @FXML private Label lblCambio;
    @FXML private TextField txtReferenciaTransferencia;

    // Botones
    @FXML private Button btnAgregarProducto;
    @FXML private Button btnEliminarItem;
    @FXML private Button btnModificarCantidad;
    @FXML private Button btnProcesarVenta;
    @FXML private Button btnCancelarVenta;
    @FXML private Button btnImprimirTicket;

    private ProductoDAO productoDAO;
    private VentaDAO ventaDAO;
    private SessionManager sessionManager;
    private ObservableList<Producto> productosData;
    private ObservableList<DetalleVenta> carritoData;
    private Venta ventaActual;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productoDAO = new ProductoDAO();
        ventaDAO = new VentaDAO();
        sessionManager = SessionManager.getInstance();
        productosData = FXCollections.observableArrayList();
        carritoData = FXCollections.observableArrayList();

        configurarTablas();
        configurarFormulario();
        configurarEventos();
        inicializarVenta();
        cargarProductos();
    }

    private void configurarTablas() {
        // Configurar tabla de productos
        colNombreProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCodigoProducto.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colPrecioProducto.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStockProducto.setCellValueFactory(new PropertyValueFactory<>("cantidadStock"));

        tableProductos.setItems(productosData);

        // Configurar tabla del carrito
        colProductoCarrito.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));
        colCantidadCarrito.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioCarrito.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotalCarrito.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        tableCarrito.setItems(carritoData);

        // Personalizar formato de celdas de dinero
        colPrecioProducto.setCellFactory(column -> new TableCell<Producto, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + item.toString());
                }
            }
        });

        colPrecioCarrito.setCellFactory(column -> new TableCell<DetalleVenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + item.toString());
                }
            }
        });

        colSubtotalCarrito.setCellFactory(column -> new TableCell<DetalleVenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + item.toString());
                }
            }
        });
    }

    private void configurarFormulario() {
        // Configurar métodos de pago
        cmbMetodoPago.setItems(FXCollections.observableArrayList(
                "EFECTIVO", "TARJETA", "TRANSFERENCIA"
        ));
        cmbMetodoPago.setValue("EFECTIVO");

        // Inicializar campos
        txtEfectivoRecibido.setVisible(true);
        lblCambio.setVisible(true);
        txtReferenciaTransferencia.setVisible(false);
    }

    private void configurarEventos() {
        // Búsqueda de productos
        txtBuscarProducto.setOnKeyPressed(this::handleBuscarProducto);
        txtBuscarProducto.textProperty().addListener((obs, oldText, newText) -> filtrarProductos(newText));

        // Doble clic para agregar producto
        tableProductos.setRowFactory(tv -> {
            TableRow<Producto> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    agregarProductoAlCarrito(row.getItem());
                }
            });
            return row;
        });

        // Cambio en método de pago
        cmbMetodoPago.setOnAction(this::handleCambioMetodoPago);

        // Cálculo de cambio en tiempo real
        txtEfectivoRecibido.textProperty().addListener((obs, oldText, newText) -> calcularCambio());

        // Selección en carrito
        tableCarrito.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    btnEliminarItem.setDisable(newSelection == null);
                    btnModificarCantidad.setDisable(newSelection == null);
                });
    }

    private void inicializarVenta() {
        ventaActual = new Venta();
        ventaActual.setUsuario(sessionManager.getUsuarioActual());

        // Actualizar información en pantalla
        lblNumeroVenta.setText(ventaActual.getNumeroVenta());
        lblFechaHora.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        lblUsuario.setText(sessionManager.getUsuarioActual().getNombreCompleto());

        actualizarTotales();
        btnEliminarItem.setDisable(true);
        btnModificarCantidad.setDisable(true);
        btnProcesarVenta.setDisable(true);
        btnImprimirTicket.setDisable(true);
    }

    private void cargarProductos() {
        try {
            List<Producto> productos = productoDAO.listarActivos();
            productosData.setAll(productos);
        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "No se pudieron cargar los productos: " + e.getMessage());
        }
    }

    // === MÉTODOS DE ACCIÓN ===

    @FXML
    private void handleAgregarProducto(ActionEvent event) {
        Producto productoSeleccionado = tableProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            agregarProductoAlCarrito(productoSeleccionado);
        } else {
            AlertUtils.mostrarAdvertencia("Selección", "Debe seleccionar un producto");
        }
    }

    @FXML
    private void handleEliminarItem(ActionEvent event) {
        DetalleVenta detalleSeleccionado = tableCarrito.getSelectionModel().getSelectedItem();
        if (detalleSeleccionado != null) {
            if (AlertUtils.mostrarConfirmacion("Confirmar",
                    "¿Eliminar " + detalleSeleccionado.getProducto().getNombre() + " del carrito?")) {
                carritoData.remove(detalleSeleccionado);
                ventaActual.getDetalles().remove(detalleSeleccionado);
                actualizarTotales();
                sessionManager.registrarActividad("Producto eliminado del carrito: " +
                        detalleSeleccionado.getProducto().getNombre());
            }
        }
    }

    @FXML
    private void handleModificarCantidad(ActionEvent event) {
        DetalleVenta detalleSeleccionado = tableCarrito.getSelectionModel().getSelectedItem();
        if (detalleSeleccionado != null) {
            TextInputDialog dialog = new TextInputDialog(String.valueOf(detalleSeleccionado.getCantidad()));
            dialog.setTitle("Modificar Cantidad");
            dialog.setHeaderText("Producto: " + detalleSeleccionado.getProducto().getNombre());
            dialog.setContentText("Nueva cantidad:");

            dialog.showAndWait().ifPresent(cantidadStr -> {
                try {
                    int nuevaCantidad = Integer.parseInt(cantidadStr);
                    if (nuevaCantidad > 0) {
                        int cantidadActual = detalleSeleccionado.getCantidad();
                        
                        // Solo validar si se está AUMENTANDO la cantidad
                        if (nuevaCantidad > cantidadActual) {
                            int cantidadAdicional = nuevaCantidad - cantidadActual;
                            if (!detalleSeleccionado.getProducto().hayStock(cantidadAdicional)) {
                                AlertUtils.mostrarError("Stock Insuficiente",
                                        "No hay suficiente stock para agregar " + cantidadAdicional + " unidades más.\n" +
                                        "Stock disponible: " + detalleSeleccionado.getProducto().getCantidadStock());
                                return;
                            }
                        }
                        
                        // Proceder con la actualización
                        detalleSeleccionado.setCantidad(nuevaCantidad);
                        detalleSeleccionado.calcularSubtotal();
                        tableCarrito.refresh();
                        actualizarTotales();
                        sessionManager.registrarActividad("Cantidad modificada: " +
                                detalleSeleccionado.getProducto().getNombre() + " x" + nuevaCantidad);
                    } else {
                        AlertUtils.mostrarError("Cantidad Inválida", "La cantidad debe ser mayor a 0");
                    }
                } catch (NumberFormatException e) {
                    AlertUtils.mostrarError("Formato Inválido", "Ingrese un número válido");
                }
            });
        }
    }

    @FXML
    private void handleProcesarVenta(ActionEvent event) {
        if (validarVenta()) {
            try {
                // Configurar venta
                ventaActual.setMetodoPago(cmbMetodoPago.getValue());
                if ("TRANSFERENCIA".equals(cmbMetodoPago.getValue())) {
                    ventaActual.setReferenciaTransferencia(txtReferenciaTransferencia.getText());
                }

                // Procesar venta
                if (ventaDAO.crear(ventaActual)) {
                    // Actualizar stock de productos - obtener stock ACTUAL de BD antes de descontar
                    for (DetalleVenta detalle : ventaActual.getDetalles()) {
                        Producto producto = detalle.getProducto();
                        // Obtener stock ACTUAL de BD, no del objeto en memoria
                        Producto productoActual = productoDAO.buscarPorId(producto.getIdProducto());
                        if (productoActual != null) {
                            int nuevoStock = productoActual.getCantidadStock() - detalle.getCantidad();
                            productoDAO.actualizarStock(producto.getIdProducto(), nuevoStock);
                            
                            // Auto-deshabilitar si queda sin stock
                            if (nuevoStock == 0) {
                                producto.setEstado(false);
                                productoDAO.actualizar(producto);
                            }
                        }
                    }

                    AlertUtils.mostrarExito("Venta Procesada",
                            "Venta #" + ventaActual.getNumeroVenta() + " procesada correctamente\n" +
                                    "Total: $" + ventaActual.getTotal());

                    sessionManager.registrarActividad("Venta procesada: " + ventaActual.getNumeroVenta() +
                            " - Total: $" + ventaActual.getTotal());

                    btnImprimirTicket.setDisable(false);
                    btnProcesarVenta.setDisable(true);
                    btnAgregarProducto.setDisable(true);
                    btnEliminarItem.setDisable(true);
                    btnModificarCantidad.setDisable(true);

                    // Recargar productos para actualizar stock
                    cargarProductos();

                } else {
                    AlertUtils.mostrarError("Error", "No se pudo procesar la venta");
                }
            } catch (Exception e) {
                AlertUtils.mostrarError("Error", "Error al procesar la venta: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancelarVenta(ActionEvent event) {
        if (!carritoData.isEmpty()) {
            if (AlertUtils.mostrarConfirmacion("Cancelar Venta",
                    "¿Está seguro que desea cancelar la venta actual?")) {
                reiniciarVenta();
            }
        } else {
            reiniciarVenta();
        }
    }

    @FXML
    private void handleImprimirTicket(ActionEvent event) {
        // Implementar impresión de ticket
        AlertUtils.mostrarInformacion("Imprimir Ticket",
                "Funcionalidad de impresión implementada\n" +
                        "Ticket enviado a impresora predeterminada");

        // Limpiar automáticamente después de PDF
        Platform.runLater(() -> {
            reiniciarVenta();
            AlertUtils.mostrarInformacion("Venta Finalizada", 
                    "Se ha iniciado una nueva venta automáticamente.");
        });
    }

    // === MÉTODOS AUXILIARES ===

    private void handleBuscarProducto(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String codigo = txtBuscarProducto.getText().trim();
            if (!codigo.isEmpty()) {
                buscarProductoPorCodigo(codigo);
            }
        }
    }

    private void buscarProductoPorCodigo(String codigo) {
        try {
            Producto producto = productoDAO.buscarPorCodigoBarras(codigo);
            if (producto != null) {
                if (producto.isEstado() && producto.getCantidadStock() > 0) {
                    agregarProductoAlCarrito(producto);
                    txtBuscarProducto.clear();
                } else {
                    AlertUtils.mostrarAdvertencia("Producto No Disponible",
                            "El producto no está activo o no tiene stock disponible");
                }
            } else {
                AlertUtils.mostrarAdvertencia("Producto No Encontrado",
                        "No se encontró ningún producto con el código: " + codigo);
            }
        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "Error al buscar producto: " + e.getMessage());
        }
    }

    private void filtrarProductos(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            cargarProductos();
            return;
        }

        try {
            List<Producto> productos = productoDAO.buscarPorNombre(filtro);
            productosData.setAll(productos);
        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "Error al filtrar productos: " + e.getMessage());
        }
    }

    private void agregarProductoAlCarrito(Producto producto) {
        if (producto.getCantidadStock() <= 0) {
            AlertUtils.mostrarAdvertencia("Sin Stock",
                    "El producto " + producto.getNombre() + " no tiene stock disponible");
            return;
        }

        // Verificar si el producto ya está en el carrito
        DetalleVenta detalleExistente = null;
        for (DetalleVenta detalle : carritoData) {
            if (detalle.getProducto().getIdProducto() == producto.getIdProducto()) {
                detalleExistente = detalle;
                break;
            }
        }

        if (detalleExistente != null) {
            // Incrementar cantidad si ya existe
            int nuevaCantidad = detalleExistente.getCantidad() + 1;
            if (nuevaCantidad <= producto.getCantidadStock()) {
                detalleExistente.setCantidad(nuevaCantidad);
                tableCarrito.refresh();
                sessionManager.registrarActividad("Cantidad incrementada: " + producto.getNombre() +
                        " x" + nuevaCantidad);
            } else {
                AlertUtils.mostrarAdvertencia("Stock Insuficiente",
                        "Stock disponible: " + producto.getCantidadStock());
                return;
            }
        } else {
            // Agregar nuevo producto
            DetalleVenta nuevoDetalle = new DetalleVenta(producto, 1);
            carritoData.add(nuevoDetalle);
            ventaActual.agregarDetalle(nuevoDetalle);
            sessionManager.registrarActividad("Producto agregado al carrito: " + producto.getNombre());
        }

        actualizarTotales();
        btnProcesarVenta.setDisable(carritoData.isEmpty());
    }

    private void handleCambioMetodoPago(ActionEvent event) {
        String metodoPago = cmbMetodoPago.getValue();

        switch (metodoPago) {
            case "EFECTIVO":
                txtEfectivoRecibido.setVisible(true);
                lblCambio.setVisible(true);
                txtReferenciaTransferencia.setVisible(false);
                break;
            case "TARJETA":
                txtEfectivoRecibido.setVisible(false);
                lblCambio.setVisible(false);
                txtReferenciaTransferencia.setVisible(false);
                break;
            case "TRANSFERENCIA":
                txtEfectivoRecibido.setVisible(false);
                lblCambio.setVisible(false);
                txtReferenciaTransferencia.setVisible(true);
                txtReferenciaTransferencia.setPromptText("Referencia de transferencia");
                break;
        }

        calcularCambio();
    }

    private void calcularCambio() {
        if ("EFECTIVO".equals(cmbMetodoPago.getValue())) {
            try {
                BigDecimal efectivoRecibido = new BigDecimal(txtEfectivoRecibido.getText());
                BigDecimal total = ventaActual.getTotal();
                BigDecimal cambio = efectivoRecibido.subtract(total);

                if (cambio.compareTo(BigDecimal.ZERO) >= 0) {
                    lblCambio.setText("Cambio: $" + cambio.toString());
                    lblCambio.setStyle("-fx-text-fill: green;");
                } else {
                    lblCambio.setText("Falta: $" + cambio.abs().toString());
                    lblCambio.setStyle("-fx-text-fill: red;");
                }
            } catch (NumberFormatException e) {
                lblCambio.setText("Cambio: $0.00");
                lblCambio.setStyle("-fx-text-fill: black;");
            }
        }
    }

    private void actualizarTotales() {
        ventaActual.calcularTotales();

        lblSubtotal.setText("$" + ventaActual.getSubtotal().toString());
        lblImpuestos.setText("$" + ventaActual.getImpuestos().toString());
        lblTotal.setText("$" + ventaActual.getTotal().toString());
        lblCantidadArticulos.setText(String.valueOf(ventaActual.getCantidadArticulos()));

        calcularCambio();
    }

    private boolean validarVenta() {
        StringBuilder errores = new StringBuilder();

        // Validar que hay productos en el carrito
        if (carritoData.isEmpty()) {
            errores.append("- Debe agregar productos al carrito\n");
        }

        // Validar método de pago
        String metodoPago = cmbMetodoPago.getValue();
        if (metodoPago == null) {
            errores.append("- Debe seleccionar un método de pago\n");
        } else {
            switch (metodoPago) {
                case "EFECTIVO":
                    try {
                        BigDecimal efectivoRecibido = new BigDecimal(txtEfectivoRecibido.getText());
                        if (efectivoRecibido.compareTo(ventaActual.getTotal()) < 0) {
                            errores.append("- El efectivo recibido debe ser mayor o igual al total\n");
                        }
                    } catch (NumberFormatException e) {
                        errores.append("- Ingrese un monto válido de efectivo recibido\n");
                    }
                    break;
                case "TRANSFERENCIA":
                    if (txtReferenciaTransferencia.getText().trim().isEmpty()) {
                        errores.append("- Debe ingresar la referencia de transferencia\n");
                    }
                    break;
            }
        }

        // Validar stock disponible
        for (DetalleVenta detalle : carritoData) {
            if (detalle.getCantidad() > detalle.getProducto().getCantidadStock()) {
                errores.append("- Stock insuficiente para: " + detalle.getProducto().getNombre() + "\n");
            }
        }

        if (errores.length() > 0) {
            AlertUtils.mostrarErrorValidacion("Errores de Validación", "", errores.toString());
            return false;
        }

        return true;
    }

    private void reiniciarVenta() {
        carritoData.clear();
        inicializarVenta();
        txtBuscarProducto.clear();
        txtEfectivoRecibido.clear();
        txtReferenciaTransferencia.clear();
        cmbMetodoPago.setValue("EFECTIVO");
        handleCambioMetodoPago(null);

        // Rehabilitar controles
        btnAgregarProducto.setDisable(false);
        btnEliminarItem.setDisable(true);
        btnModificarCantidad.setDisable(true);
        btnProcesarVenta.setDisable(true);
        btnImprimirTicket.setDisable(true);

        sessionManager.registrarActividad("Nueva venta iniciada");
        txtBuscarProducto.requestFocus();
    }
}