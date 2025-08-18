package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.ProductoDAO;
import com.pos.puntoventaocr.dao.VentaDAO;
import com.pos.puntoventaocr.dao.BitacoraDAO;
import com.pos.puntoventaocr.models.*;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import com.pos.puntoventaocr.utils.TicketPrinter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class NuevaVentaController {

    @FXML private Label lblNumeroVenta;
    @FXML private TextField txtBuscarProducto;
    @FXML private Button btnBuscarProducto;
    @FXML private Button btnEscanearCodigo;

    @FXML private TableView<Producto> tablaProductosDisponibles;
    @FXML private TableColumn<Producto, String> colCodigoDisponible;
    @FXML private TableColumn<Producto, String> colNombreDisponible;
    @FXML private TableColumn<Producto, BigDecimal> colPrecioDisponible;
    @FXML private TableColumn<Producto, Integer> colStockDisponible;
    @FXML private TableColumn<Producto, String> colAccionDisponible;

    @FXML private TableView<DetalleVenta> tablaCarrito;
    @FXML private TableColumn<DetalleVenta, String> colCodigoCarrito;
    @FXML private TableColumn<DetalleVenta, String> colNombreCarrito;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colCantidadCarrito;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colPrecioCarrito;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colSubtotalCarrito;
    @FXML private TableColumn<DetalleVenta, String> colAccionesCarrito;

    @FXML private Button btnLimpiarCarrito;

    // Resumen
    @FXML private Label lblCantidadArticulos;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIVA;
    @FXML private Label lblTotal;

    // Método de pago
    @FXML private ToggleGroup grupoMetodoPago;
    @FXML private RadioButton rbEfectivo;
    @FXML private RadioButton rbTarjeta;
    @FXML private RadioButton rbTransferencia;
    @FXML private VBox panelEfectivo;
    @FXML private VBox panelTransferencia;
    @FXML private TextField txtPagoEfectivo;
    @FXML private Label lblCambio;
    @FXML private Button btnValidarTransferencia;

    // Botones
    @FXML private Button btnProcesarVenta;
    @FXML private Button btnCancelarVenta;
    @FXML private Button btnImprimirTicket;

    // Estado
    @FXML private Label lblEstadoVenta;
    @FXML private Label lblUsuarioVendedor;
    @FXML private Label lblFechaHoraVenta;

    private ProductoDAO productoDAO;
    private VentaDAO ventaDAO;
    private BitacoraDAO bitacoraDAO;
    private SessionManager sessionManager;
    private ObservableList<Producto> productosDisponibles;
    private ObservableList<Producto> todosLosProductos; // Lista completa para filtrado
    private ObservableList<DetalleVenta> carritoItems;
    private Venta ventaActual;

    public void initialize() {
        productoDAO = new ProductoDAO();
        ventaDAO = new VentaDAO();
        bitacoraDAO = new BitacoraDAO();
        sessionManager = SessionManager.getInstance();
        productosDisponibles = FXCollections.observableArrayList();
        todosLosProductos = FXCollections.observableArrayList();
        carritoItems = FXCollections.observableArrayList();

        configurarTablas();
        configurarEventos();
        cargarTodosLosProductos(); // Cargar productos al inicio
        inicializarVenta();
    }

    private void configurarTablas() {
        // Tabla productos disponibles
        colCodigoDisponible.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colNombreDisponible.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecioDisponible.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStockDisponible.setCellValueFactory(new PropertyValueFactory<>("cantidadStock"));

        // Configurar columna de acción con botones
        colAccionDisponible.setCellFactory(col -> new TableCell<Producto, String>() {
            private final Button btnAgregar = new Button("Agregar");

            {
                btnAgregar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                btnAgregar.setOnAction(event -> {
                    Producto producto = getTableView().getItems().get(getIndex());
                    agregarAlCarrito(producto);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Producto producto = getTableView().getItems().get(getIndex());
                    // Verificar si el producto ya está en el carrito
                    boolean estaEnCarrito = carritoItems.stream()
                        .anyMatch(detalle -> detalle.getProducto().getIdProducto() == producto.getIdProducto());

                    if (estaEnCarrito) {
                        btnAgregar.setText("En Carrito");
                        btnAgregar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
                        btnAgregar.setDisable(true);
                    } else {
                        btnAgregar.setText("Agregar");
                        btnAgregar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                        btnAgregar.setDisable(false);
                    }
                    setGraphic(btnAgregar);
                }
            }
        });

        tablaProductosDisponibles.setItems(productosDisponibles);

        // Tabla carrito
        colCodigoCarrito.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getProducto().getCodigoBarras()));
        colNombreCarrito.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));
        colCantidadCarrito.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioCarrito.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotalCarrito.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        // Configurar columna de acciones del carrito con botones
        colAccionesCarrito.setCellFactory(col -> new TableCell<DetalleVenta, String>() {
            private final Button btnEliminar = new Button("Eliminar");
            private final Button btnMenos = new Button("-");
            private final Button btnMas = new Button("+");
            private final HBox hbox = new HBox(5);

            {
                btnEliminar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                btnMenos.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white;");
                btnMas.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");

                btnEliminar.setOnAction(event -> {
                    DetalleVenta detalle = getTableView().getItems().get(getIndex());
                    eliminarDelCarrito(detalle);
                });

                btnMenos.setOnAction(event -> {
                    DetalleVenta detalle = getTableView().getItems().get(getIndex());
                    modificarCantidad(detalle, -1);
                });

                btnMas.setOnAction(event -> {
                    DetalleVenta detalle = getTableView().getItems().get(getIndex());
                    modificarCantidad(detalle, 1);
                });

                hbox.getChildren().addAll(btnMenos, btnMas, btnEliminar);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });

        tablaCarrito.setItems(carritoItems);
    }

    private void configurarEventos() {
        // Búsqueda en tiempo real - modificado para filtrar desde el primer carácter
        txtBuscarProducto.textProperty().addListener((obs, oldText, newText) -> {
            filtrarProductos(newText);
        });

        // Método de pago
        grupoMetodoPago.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            actualizarPanelesPago();
        });

        // Pago en efectivo
        txtPagoEfectivo.textProperty().addListener((obs, oldText, newText) -> {
            calcularCambio();
        });

        // Listener para carrito
        carritoItems.addListener((javafx.collections.ListChangeListener<DetalleVenta>) c -> {
            recalcularTotales();
            btnProcesarVenta.setDisable(carritoItems.isEmpty());
            // NUEVA FUNCIONALIDAD: Actualizar botones de productos disponibles
            tablaProductosDisponibles.refresh();
        });

        // Doble clic en productos disponibles para agregar
        tablaProductosDisponibles.setRowFactory(tv -> {
            TableRow<Producto> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    agregarAlCarrito(row.getItem());
                }
            });
            return row;
        });
    }

    private void inicializarVenta() {
        ventaActual = new Venta();
        ventaActual.setNumeroVenta(ventaDAO.generarNumeroVenta());
        ventaActual.setUsuario(sessionManager.getUsuarioActual());
        ventaActual.setFecha(LocalDateTime.now());
        ventaActual.setMetodoPago("EFECTIVO"); // Establecer método de pago por defecto

        lblNumeroVenta.setText("Venta #: " + ventaActual.getNumeroVenta());
        lblUsuarioVendedor.setText("Vendedor: " + sessionManager.getUsuarioActual().getNombreCompleto());

        actualizarReloj();
        recalcularTotales();
    }

    private void actualizarReloj() {
        // Actualizar fecha y hora cada segundo
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
                lblFechaHoraVenta.setText(LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            })
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML
    private void buscarProducto() {
        String termino = txtBuscarProducto.getText().trim();
        filtrarProductos(termino);
    }

    private void cargarTodosLosProductos() {
        try {
            List<Producto> productos = productoDAO.obtenerTodos();
            todosLosProductos.setAll(productos.stream()
                .filter(Producto::estaActivo)
                .toList());
            productosDisponibles.setAll(todosLosProductos); // Mostrar todos al inicio
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error cargando productos: " + e.getMessage());
        }
    }

    private void filtrarProductos(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            // Si no hay término de búsqueda, mostrar todos los productos
            productosDisponibles.setAll(todosLosProductos);
            return;
        }

        String terminoLower = termino.toLowerCase().trim();

        // Filtrar productos que coincidan con el término
        List<Producto> productosFiltrados = todosLosProductos.stream()
            .filter(producto ->
                producto.getCodigoBarras().toLowerCase().contains(terminoLower) ||
                producto.getNombre().toLowerCase().contains(terminoLower) ||
                (producto.getCodigoInterno() != null &&
                 producto.getCodigoInterno().toLowerCase().contains(terminoLower))
            )
            .limit(20) // Limitar resultados para mejor rendimiento
            .toList();

        productosDisponibles.setAll(productosFiltrados);
    }

    private void agregarAlCarrito(Producto producto) {
        if (producto == null) return;

        // Verificar si ya está en el carrito
        for (DetalleVenta detalle : carritoItems) {
            if (detalle.getProducto().getIdProducto() == producto.getIdProducto()) {
                // Incrementar cantidad
                BigDecimal nuevaCantidad = detalle.getCantidad().add(BigDecimal.ONE);
                if (nuevaCantidad.intValue() <= producto.getCantidadStock()) {
                    detalle.setCantidad(nuevaCantidad);
                    detalle.calcularSubtotal();
                    tablaCarrito.refresh();
                    recalcularTotales(); // FIJO: Asegurar recálculo de totales
                    tablaProductosDisponibles.refresh(); // FIJO: Actualizar tabla de productos
                    return;
                } else {
                    AlertUtils.showWarning("Stock", "No hay suficiente stock disponible");
                    return;
                }
            }
        }

        // Agregar nuevo producto
        if (producto.getCantidadStock() > 0) {
            DetalleVenta detalle = new DetalleVenta(producto, BigDecimal.ONE);
            detalle.setVenta(ventaActual);
            carritoItems.add(detalle);
            recalcularTotales(); // FIJO: Asegurar recálculo de totales
            tablaProductosDisponibles.refresh(); // FIJO: Actualizar tabla de productos
        } else {
            AlertUtils.showWarning("Stock", "Producto sin stock disponible");
        }
    }

    @FXML
    private void limpiarCarrito() {
        if (carritoItems.isEmpty()) return;

        Optional<ButtonType> resultado = AlertUtils.showConfirmation("Confirmar", "¿Está seguro de limpiar el carrito?");
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            carritoItems.clear();
        }
    }

    private void recalcularTotales() {
        if (carritoItems.isEmpty()) {
            lblCantidadArticulos.setText("0");
            lblSubtotal.setText("$0.00");
            lblIVA.setText("$0.00");
            lblTotal.setText("$0.00");
            return;
        }

        int cantidadTotal = carritoItems.stream()
            .mapToInt(d -> d.getCantidad().intValue())
            .sum();

        BigDecimal subtotal = carritoItems.stream()
            .map(DetalleVenta::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal iva = subtotal.multiply(new BigDecimal("0.16"));
        BigDecimal total = subtotal.add(iva);

        ventaActual.setSubtotal(subtotal);
        ventaActual.setIva(iva);
        ventaActual.setTotal(total);

        lblCantidadArticulos.setText(String.valueOf(cantidadTotal));
        lblSubtotal.setText(String.format("$%.2f", subtotal));
        lblIVA.setText(String.format("$%.2f", iva));
        lblTotal.setText(String.format("$%.2f", total));

        calcularCambio();
    }

    private void actualizarPanelesPago() {
        Toggle selected = grupoMetodoPago.getSelectedToggle();

        // Actualizar visibilidad y gestión de espacio de los paneles
        boolean esEfectivo = (selected == rbEfectivo);
        boolean esTransferencia = (selected == rbTransferencia);

        panelEfectivo.setVisible(esEfectivo);
        panelEfectivo.setManaged(esEfectivo);

        panelTransferencia.setVisible(esTransferencia);
        panelTransferencia.setManaged(esTransferencia);

        if (selected == rbEfectivo) {
            ventaActual.setMetodoPago("EFECTIVO");
        } else if (selected == rbTarjeta) {
            ventaActual.setMetodoPago("TARJETA");
        } else if (selected == rbTransferencia) {
            ventaActual.setMetodoPago("TRANSFERENCIA");
        }
    }

    private void calcularCambio() {
        if (!rbEfectivo.isSelected() || txtPagoEfectivo.getText().trim().isEmpty()) {
            lblCambio.setText("Cambio: $0.00");
            return;
        }

        try {
            BigDecimal pagoRecibido = new BigDecimal(txtPagoEfectivo.getText().trim());
            BigDecimal cambio = pagoRecibido.subtract(ventaActual.getTotal());

            if (cambio.compareTo(BigDecimal.ZERO) >= 0) {
                lblCambio.setText(String.format("Cambio: $%.2f", cambio));
                lblCambio.setStyle("-fx-text-fill: green;");
            } else {
                lblCambio.setText(String.format("Falta: $%.2f", cambio.abs()));
                lblCambio.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            lblCambio.setText("Cambio: $0.00");
        }
    }

    @FXML
    private void procesarVenta() {
        if (carritoItems.isEmpty()) {
            AlertUtils.showWarning("Carrito vacío", "Agregue productos al carrito para procesar la venta");
            return;
        }

        // Validar método de pago
        if (rbEfectivo.isSelected()) {
            try {
                BigDecimal pagoRecibido = new BigDecimal(txtPagoEfectivo.getText().trim());
                if (pagoRecibido.compareTo(ventaActual.getTotal()) < 0) {
                    AlertUtils.showWarning("Pago insuficiente", "El pago recibido es menor al total de la venta");
                    return;
                }
            } catch (NumberFormatException e) {
                AlertUtils.showWarning("Pago inválido", "Ingrese un monto válido");
                return;
            }
        } else if (rbTransferencia.isSelected()) {
            // No se requiere validación adicional, se asume que el usuario subió el comprobante
            ventaActual.setComprobanteTransferenciaSubido(true);
        }

        Optional<ButtonType> resultado = AlertUtils.showConfirmation("Confirmar Venta", "¿Está seguro de procesar esta venta?");
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                // Establecer detalles antes de guardar
                ventaActual.setDetalles(carritoItems.stream().toList());

                // Guardar venta (incluye actualización automática del stock en BD)
                if (ventaDAO.guardar(ventaActual)) {
                    // Registrar en bitácora
                    Integer idUsuario = SessionManager.getInstance().getCurrentUserId();
                    if (idUsuario != null && idUsuario > 0) {
                        bitacoraDAO.registrarVenta(idUsuario, ventaActual.getNumeroVenta(),
                            ventaActual.getTotal().doubleValue(), ventaActual.getMetodoPago());
                    }

                    // Recargar productos desde BD para obtener stock actualizado
                    recargarProductosConStockActualizado();

                    AlertUtils.showInfo("Éxito", "Venta procesada correctamente");
                    btnImprimirTicket.setVisible(true);
                    btnProcesarVenta.setDisable(true);
                    lblEstadoVenta.setText("Venta completada - Puede imprimir ticket");

                    // NUEVA FUNCIONALIDAD: Si es transferencia, abrir módulo OCR automáticamente
                    if (rbTransferencia.isSelected()) {
                        abrirModuloOCRParaVenta(ventaActual);
                    }
                } else {
                    AlertUtils.showError("Error", "No se pudo procesar la venta");
                }
            } catch (Exception e) {
                AlertUtils.showError("Error", "Error procesando venta: " + e.getMessage());
            }
        }
    }

    /**
     * Abre el módulo OCR automáticamente para validar una venta por transferencia
     */
    private void abrirModuloOCRParaVenta(Venta venta) {
        try {
            // Mostrar diálogo informativo
            Optional<ButtonType> respuesta = AlertUtils.showConfirmation(
                "Validación OCR Requerida",
                "Esta venta requiere validación de comprobante de transferencia.\n\n" +
                "¿Desea abrir el módulo de validación OCR ahora?\n\n" +
                "Venta: " + venta.getNumeroVenta() + "\n" +
                "Total: $" + venta.getTotal()
            );

            if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
                // Cargar el FXML del validador OCR
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
                loader.setLocation(getClass().getResource("/fxml/ocr/validar_ocr.fxml"));

                javafx.scene.Parent root = loader.load();

                // Obtener el controlador y pasarle la venta
                ValidarOCRController ocrController = loader.getController();
                ocrController.inicializarConVenta(venta);

                // Crear nueva ventana
                javafx.stage.Stage ocrStage = new javafx.stage.Stage();
                ocrStage.setTitle("Validar Comprobante OCR - Venta " + venta.getNumeroVenta());
                ocrStage.setScene(new javafx.scene.Scene(root));

                // Configurar la ventana
                ocrStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
                ocrStage.initOwner(btnProcesarVenta.getScene().getWindow());

                // Centrar la ventana
                ocrStage.centerOnScreen();

                // Mostrar información en la venta actual
                lblEstadoVenta.setText("Módulo OCR abierto - Valide el comprobante de transferencia");

                // Mostrar la ventana
                ocrStage.show();

            } else {
                // Usuario decidió no abrir OCR ahora
                AlertUtils.showInfo("Recordatorio",
                    "Recuerde validar el comprobante de transferencia más tarde.\n" +
                    "Puede acceder desde el menú OCR → Validar Comprobantes");

                lblEstadoVenta.setText("Venta completada - Pendiente validación OCR");
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "No se pudo abrir el módulo OCR: " + e.getMessage());
            System.err.println("Error abriendo módulo OCR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recarga los productos desde la base de datos para obtener el stock actualizado
     * después de procesar una venta. Esto evita duplicación en la actualización del stock.
     */
    private void recargarProductosConStockActualizado() {
        try {
            List<Producto> productos = productoDAO.obtenerTodos();
            todosLosProductos.setAll(productos.stream()
                .filter(Producto::estaActivo)
                .toList());

            // Actualizar la lista filtrada manteniendo el filtro actual
            String filtroActual = txtBuscarProducto.getText();
            filtrarProductos(filtroActual);

            // Refrescar la tabla para mostrar el stock actualizado
            tablaProductosDisponibles.refresh();
        } catch (Exception e) {
            System.err.println("Error recargando productos: " + e.getMessage());
        }
    }

    private void eliminarDelCarrito(DetalleVenta detalle) {
        if (detalle == null) return;

        Optional<ButtonType> resultado = AlertUtils.showConfirmation("Confirmar",
            "��Eliminar " + detalle.getProducto().getNombre() + " del carrito?");

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            carritoItems.remove(detalle);
        }
    }

    private void modificarCantidad(DetalleVenta detalle, int cambio) {
        if (detalle == null) return;

        BigDecimal nuevaCantidad = detalle.getCantidad().add(new BigDecimal(cambio));

        // Validar que la cantidad no sea menor a 1
        if (nuevaCantidad.compareTo(BigDecimal.ZERO) <= 0) {
            eliminarDelCarrito(detalle);
            return;
        }

        // Validar stock disponible
        if (nuevaCantidad.intValue() > detalle.getProducto().getCantidadStock()) {
            AlertUtils.showWarning("Stock insuficiente",
                "Solo hay " + detalle.getProducto().getCantidadStock() + " unidades disponibles");
            return;
        }

        detalle.setCantidad(nuevaCantidad);
        detalle.calcularSubtotal();
        tablaCarrito.refresh();

        // CRÍTICO: Forzar recálculo de totales después de modificar cantidad
        recalcularTotales();
    }

    @FXML
    private void escanearCodigo() {
        // Crear un diálogo personalizado que se mantenga abierto
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Escáner de Código de Barras");
        dialog.setHeaderText("Escáner activo - Escanee o ingrese códigos de barras");

        // Configurar botones
        ButtonType escanearButton = new ButtonType("Escanear/Agregar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cerrarButton = new ButtonType("Cerrar Escáner", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(escanearButton, cerrarButton);

        // Crear contenido del diálogo
        VBox content = new VBox(10);
        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Código de barras...");
        Label lblStatus = new Label("Listo para escanear");
        lblStatus.setStyle("-fx-text-fill: green;");

        content.getChildren().addAll(
            new Label("Ingrese el código de barras:"),
            txtCodigo,
            lblStatus
        );

        dialog.getDialogPane().setContent(content);

        // Configurar el resultado del diálogo
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == escanearButton) {
                return txtCodigo.getText().trim();
            }
            return null;
        });

        // Manejar el evento de escaneo/agregar en un bucle
        boolean mantenerAbierto = true;
        while (mantenerAbierto) {
            // Limpiar el campo para el siguiente escaneo
            txtCodigo.clear();
            lblStatus.setText("Listo para escanear");
            lblStatus.setStyle("-fx-text-fill: green;");

            // Enfocar el campo de texto
            Platform.runLater(() -> txtCodigo.requestFocus());

            Optional<String> resultado = dialog.showAndWait();

            if (resultado.isPresent()) {
                String codigo = resultado.get();
                if (!codigo.isEmpty()) {
                    // Buscar y agregar producto
                    Producto producto = productoDAO.obtenerPorCodigoBarras(codigo);
                    if (producto != null && producto.estaActivo()) {
                        agregarAlCarrito(producto);
                        lblStatus.setText("✓ Producto agregado: " + producto.getNombre());
                        lblStatus.setStyle("-fx-text-fill: green;");
                        lblEstadoVenta.setText("Producto escaneado: " + producto.getNombre());
                    } else {
                        lblStatus.setText("⚠ Código no encontrado: " + codigo);
                        lblStatus.setStyle("-fx-text-fill: red;");
                    }
                    // Continuar en el bucle para mantener el diálogo abierto
                } else {
                    lblStatus.setText("⚠ Ingrese un código válido");
                    lblStatus.setStyle("-fx-text-fill: orange;");
                }
            } else {
                // Usuario cerró el diálogo
                mantenerAbierto = false;
            }
        }
    }

    @FXML
    private void cancelarVenta() {
        Optional<ButtonType> resultado = AlertUtils.showConfirmation("Cancelar Venta", "¿Está seguro de cancelar esta venta?");
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            carritoItems.clear();
            btnImprimirTicket.setVisible(false); // Ocultar botón de imprimir si se cancela
            inicializarVenta();
            lblEstadoVenta.setText("Venta cancelada");
        }
    }

    @FXML
    private void imprimirTicket() {
        if (ventaActual == null || ventaActual.getDetalles() == null || ventaActual.getDetalles().isEmpty()) {
            AlertUtils.showWarning("Error", "No hay una venta válida para imprimir");
            return;
        }

        try {
            // Mostrar indicador de carga
            lblEstadoVenta.setText("Imprimiendo ticket...");
            btnImprimirTicket.setDisable(true);

            // Imprimir el ticket
            boolean impresionExitosa = TicketPrinter.imprimirTicket(ventaActual);

            if (impresionExitosa) {
                // Mostrar mensaje de éxito
                AlertUtils.showInfo("Éxito", "Ticket impreso correctamente");

                // Limpiar la venta actual y resetear la interfaz
                limpiarVentaCompleta();

                lblEstadoVenta.setText("Ticket impreso - Lista para nueva venta");
            } else {
                AlertUtils.showError("Error", "No se pudo imprimir el ticket");
                btnImprimirTicket.setDisable(false);
                lblEstadoVenta.setText("Error en impresión");
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error imprimiendo ticket: " + e.getMessage());
            btnImprimirTicket.setDisable(false);
            lblEstadoVenta.setText("Error en impresión");
        }
    }

    /**
     * Limpia completamente la venta actual y resetea la interfaz para una nueva venta
     */
    private void limpiarVentaCompleta() {
        // Limpiar carrito
        carritoItems.clear();

        // Ocultar y rehabilitar botón de imprimir ticket
        btnImprimirTicket.setVisible(false);
        btnImprimirTicket.setDisable(false);

        // Resetear estado del botón de procesar venta
        btnProcesarVenta.setDisable(true); // Se habilitará cuando agreguen productos

        // Limpiar campo de pago en efectivo
        txtPagoEfectivo.clear();

        // Resetear botón de transferencia a su estado original
        btnValidarTransferencia.setText("Validar Transferencia");
        btnValidarTransferencia.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        btnValidarTransferencia.setDisable(false);

        // Seleccionar efectivo por defecto
        rbEfectivo.setSelected(true);

        // Inicializar nueva venta
        inicializarVenta();

        // Actualizar estado visual
        lblEstadoVenta.setText("Lista para nueva venta");
    }

    @FXML
    private void abrirModuloOCR() {
        try {
            // Cargar la ventana del módulo OCR
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/ocr/validar_ocr.fxml")
            );

            javafx.scene.Parent root = loader.load();

            // Crear nueva ventana
            javafx.stage.Stage ocrStage = new javafx.stage.Stage();
            ocrStage.setTitle("Validar Comprobante - Módulo OCR");
            ocrStage.setScene(new javafx.scene.Scene(root));
            ocrStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            ocrStage.initOwner(btnValidarTransferencia.getScene().getWindow());

            // Configurar la ventana
            ocrStage.setResizable(true);
            ocrStage.setMinWidth(800);
            ocrStage.setMinHeight(600);

            // Obtener el controlador del OCR y configurarlo para transferencias
            com.pos.puntoventaocr.controllers.ValidarOCRController ocrController = loader.getController();

            // Configurar callback para cuando se valide el comprobante
            ocrStage.setOnHidden(e -> {
                // Verificar si el comprobante fue validado exitosamente
                if (ocrController != null && ocrController.isComprobanteValidado()) {
                    btnValidarTransferencia.setText("✓ Comprobante Validado");
                    btnValidarTransferencia.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                    btnValidarTransferencia.setDisable(true);
                    lblEstadoVenta.setText("Comprobante de transferencia validado con OCR");

                    // Marcar que el comprobante fue validado
                    ventaActual.setComprobanteTransferenciaSubido(true);
                } else {
                    lblEstadoVenta.setText("Validación de comprobante cancelada");
                }
            });

            // Mostrar la ventana
            ocrStage.showAndWait();

        } catch (Exception e) {
            AlertUtils.showError("Error", "No se pudo abrir el módulo OCR: " + e.getMessage());
            lblEstadoVenta.setText("Error abriendo módulo OCR");
        }
    }
}
