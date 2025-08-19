package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.ProductoDAO;
import com.pos.puntoventaocr.dao.CategoriaDAO;
import com.pos.puntoventaocr.models.Producto;
import com.pos.puntoventaocr.models.Categoria;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GestionarProductosController implements Initializable {

    // Controles de la tabla
    @FXML private TableView<Producto> tableProductos;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, String> colEstado;
    @FXML private TableColumn<Producto, String> colCodigo;

    // Controles del formulario
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcionCorta;
    @FXML private TextArea txtDescripcionLarga;
    @FXML private TextField txtPrecioCompra;
    @FXML private TextField txtPrecioVenta;
    @FXML private TextField txtStock;
    @FXML private TextField txtStockMinimo;
    @FXML private ComboBox<String> cmbUnidadMedida;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private TextField txtCodigoBarras;
    @FXML private CheckBox chkEstado;
    @FXML private ImageView imgProducto;
    @FXML private Label lblRutaImagen;

    // Controles de búsqueda y filtros
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Categoria> cmbFiltroCategoria;
    @FXML private ComboBox<String> cmbFiltroEstado;

    // Botones
    @FXML private Button btnNuevo;
    @FXML private Button btnGuardar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;
    @FXML private Button btnCancelar;
    @FXML private Button btnSeleccionarImagen;
    @FXML private Button btnDuplicar;

    private ProductoDAO productoDAO;
    private CategoriaDAO categoriaDAO;
    private SessionManager sessionManager;
    private ObservableList<Producto> productosData;
    private Producto productoSeleccionado;
    private String rutaImagenSeleccionada;
    private boolean modoEdicion = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productoDAO = new ProductoDAO();
        categoriaDAO = new CategoriaDAO();
        sessionManager = SessionManager.getInstance();
        productosData = FXCollections.observableArrayList();

        configurarTabla();
        configurarFormulario();
        configurarEventos();
        cargarDatos();
        limpiarFormulario();
    }

    private void configurarTabla() {
        // Configurar columnas
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategoria().getNombre()));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("cantidadStock"));
        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isEstado() ? "Activo" : "Inactivo"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));

        // MEJORADO: Formato de celdas de precio con estilos consistentes
        colPrecio.setCellFactory(tc -> new TableCell<Producto, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal precio, boolean empty) {
                super.updateItem(precio, empty);
                if (empty || precio == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("$%.2f", precio));
                    
                    // Mantener colores de texto apropiados
                    if (getTableRow() != null && getTableRow().isSelected()) {
                        setStyle("-fx-text-fill: #1976D2; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // MEJORADO: Formato de celda de stock con colores mejorados
        colStock.setCellFactory(tc -> new TableCell<Producto, Integer>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(stock.toString());
                    
                    // Aplicar estilos según stock y selección
                    if (getTableRow() != null && getTableRow().isSelected()) {
                        if (stock == 0) {
                            setStyle("-fx-background-color: rgba(244, 67, 54, 0.2); -fx-text-fill: #C62828; -fx-font-weight: bold;");
                        } else if (stock <= 5) {
                            setStyle("-fx-background-color: rgba(255, 152, 0, 0.2); -fx-text-fill: #EF6C00; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #1976D2; -fx-font-weight: bold;");
                        }
                    } else {
                        if (stock == 0) {
                            setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                        } else if (stock <= 5) {
                            setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                        }
                    }
                }
            }
        });

        // Configurar selección
        tableProductos.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    productoSeleccionado = newSelection;
                    if (newSelection != null) {
                        cargarProductoEnFormulario(newSelection);
                    }
                });

        tableProductos.setItems(productosData);
        
        // NUEVO: Configurar estilo de selección de la tabla
        tableProductos.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Agregar listener para mantener estilos consistentes
        tableProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (oldSelection != null) {
                tableProductos.refresh(); // Refrescar para actualizar estilos
            }
        });
    }

    private void configurarFormulario() {
        // Configurar unidades de medida
        cmbUnidadMedida.setItems(FXCollections.observableArrayList(
                "Pieza", "Kilogramo", "Gramo", "Litro", "Mililitro", "Metro", "Centímetro", "Caja", "Paquete"
        ));
        cmbUnidadMedida.setValue("Pieza");

        // Configurar filtros
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                "Todos", "Activos", "Inactivos"
        ));
        cmbFiltroEstado.setValue("Activos");

        // Configurar imagen por defecto
        cargarImagenPorDefecto();

        // Estado inicial - solo lectura
        habilitarFormulario(false);
        chkEstado.setSelected(true);
    }

    private void configurarEventos() {
        // Eventos de búsqueda y filtros
        txtBuscar.textProperty().addListener((obs, oldText, newText) -> filtrarProductos());
        cmbFiltroCategoria.setOnAction(e -> filtrarProductos());
        cmbFiltroEstado.setOnAction(e -> filtrarProductos());

        // Validaciones en tiempo real
        txtPrecioCompra.textProperty().addListener((obs, oldText, newText) -> validarNumero(txtPrecioCompra, newText));
        txtPrecioVenta.textProperty().addListener((obs, oldText, newText) -> validarNumero(txtPrecioVenta, newText));
        txtStock.textProperty().addListener((obs, oldText, newText) -> validarEntero(txtStock, newText));
        txtStockMinimo.textProperty().addListener((obs, oldText, newText) -> validarEntero(txtStockMinimo, newText));
    }

    private void cargarDatos() {
        cargarProductos();
        cargarCategorias();
    }

    private void cargarProductos() {
        try {
            List<Producto> productos = productoDAO.listarTodos();
            productosData.setAll(productos);
        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "No se pudieron cargar los productos: " + e.getMessage());
        }
    }

    private void cargarCategorias() {
        try {
            List<Categoria> categorias = categoriaDAO.listarActivas();

            cmbCategoria.setItems(FXCollections.observableArrayList(categorias));

            // Para filtros, agregar opción "Todas"
            ObservableList<Categoria> categoriasConTodas = FXCollections.observableArrayList();
            Categoria todas = new Categoria();
            todas.setNombre("Todas");
            categoriasConTodas.add(todas);
            categoriasConTodas.addAll(categorias);
            cmbFiltroCategoria.setItems(categoriasConTodas);
            cmbFiltroCategoria.setValue(todas);

        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "No se pudieron cargar las categorías: " + e.getMessage());
        }
    }

    // === MÉTODOS DE ACCIÓN ===

    @FXML
    private void handleNuevo(ActionEvent event) {
        modoEdicion = false;
        limpiarFormulario();
        habilitarFormulario(true);
        txtNombre.requestFocus();
        sessionManager.registrarActividad("Iniciado registro de nuevo producto");
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        if (validarFormulario()) {
            Producto producto = construirProductoDesdeFormulario();

            try {
                if (modoEdicion) {
                    producto.setIdProducto(productoSeleccionado.getIdProducto());
                    producto.setModificadoPor(sessionManager.getUsuarioActual().getIdUsuario());

                    if (productoDAO.actualizar(producto)) {
                        AlertUtils.mostrarExito("Éxito", "Producto actualizado correctamente");
                        sessionManager.registrarActividad("Producto modificado: " + producto.getNombre());
                    } else {
                        AlertUtils.mostrarError("Error", "No se pudo actualizar el producto");
                        return;
                    }
                } else {
                    producto.setCreadoPor(sessionManager.getUsuarioActual().getIdUsuario());

                    if (productoDAO.crear(producto)) {
                        AlertUtils.mostrarExito("Éxito", "Producto registrado correctamente");
                        sessionManager.registrarActividad("Producto creado: " + producto.getNombre());
                    } else {
                        AlertUtils.mostrarError("Error", "No se pudo registrar el producto");
                        return;
                    }
                }

                cargarProductos();
                limpiarFormulario();
                habilitarFormulario(false);

            } catch (Exception e) {
                AlertUtils.mostrarError("Error", "Error al guardar el producto: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleModificar(ActionEvent event) {
        if (productoSeleccionado != null) {
            modoEdicion = true;
            habilitarFormulario(true);
            txtNombre.requestFocus();
            sessionManager.registrarActividad("Iniciada modificación de producto: " + productoSeleccionado.getNombre());
        } else {
            AlertUtils.mostrarAdvertencia("Selección", "Debe seleccionar un producto para modificar");
        }
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        if (productoSeleccionado != null) {
            if (AlertUtils.mostrarConfirmacion("Confirmar Eliminación",
                    "¿Está seguro que desea eliminar el producto '" + productoSeleccionado.getNombre() + "'?")) {

                try {
                    if (productoDAO.eliminar(productoSeleccionado.getIdProducto())) {
                        AlertUtils.mostrarExito("Éxito", "Producto eliminado correctamente");
                        sessionManager.registrarActividad("Producto eliminado: " + productoSeleccionado.getNombre());
                        cargarProductos();
                        limpiarFormulario();
                    } else {
                        AlertUtils.mostrarError("Error", "No se pudo eliminar el producto");
                    }
                } catch (Exception e) {
                    AlertUtils.mostrarError("Error", "Error al eliminar el producto: " + e.getMessage());
                }
            }
        } else {
            AlertUtils.mostrarAdvertencia("Selección", "Debe seleccionar un producto para eliminar");
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        if (modoEdicion && camposModificados()) {
            if (AlertUtils.mostrarConfirmacion("Cancelar",
                    "¿Está seguro que desea cancelar? Se perderán los cambios no guardados.")) {
                cancelarOperacion();
            }
        } else {
            cancelarOperacion();
        }
    }

    @FXML
    private void handleDuplicar(ActionEvent event) {
        if (productoSeleccionado != null) {
            modoEdicion = false;
            cargarProductoEnFormulario(productoSeleccionado);
            habilitarFormulario(true);

            // Limpiar campos únicos
            txtNombre.setText(productoSeleccionado.getNombre() + " - Copia");
            txtCodigoBarras.clear();
            lblRutaImagen.setText("Seleccione una imagen...");
            cargarImagenPorDefecto();

            txtNombre.requestFocus();
            sessionManager.registrarActividad("Duplicando producto: " + productoSeleccionado.getNombre());
        } else {
            AlertUtils.mostrarAdvertencia("Selección", "Debe seleccionar un producto para duplicar");
        }
    }

    @FXML
    private void handleSeleccionarImagen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen del Producto");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File file = fileChooser.showOpenDialog(btnSeleccionarImagen.getScene().getWindow());
        if (file != null) {
            try {
                // Cargar y mostrar la imagen
                Image image = new Image(file.toURI().toString());
                imgProducto.setImage(image);

                // Guardar la ruta para usar después
                rutaImagenSeleccionada = file.getAbsolutePath();
                lblRutaImagen.setText(file.getName());

            } catch (Exception e) {
                AlertUtils.mostrarError("Error", "No se pudo cargar la imagen: " + e.getMessage());
            }
        }
    }

    // === MÉTODOS AUXILIARES ===

    private void filtrarProductos() {
        String textoBusqueda = txtBuscar.getText().toLowerCase();
        Categoria categoriaFiltro = cmbFiltroCategoria.getValue();
        String estadoFiltro = cmbFiltroEstado.getValue();

        List<Producto> productos;
        try {
            productos = productoDAO.listarTodos();
        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "Error al filtrar productos: " + e.getMessage());
            return;
        }

        List<Producto> productosFiltrados = productos.stream()
                .filter(p -> {
                    // Filtro por texto
                    boolean cumpleTexto = textoBusqueda.isEmpty() ||
                            p.getNombre().toLowerCase().contains(textoBusqueda) ||
                            p.getCodigoBarras().toLowerCase().contains(textoBusqueda);

                    // Filtro por categoría
                    boolean cumpleCategoria = categoriaFiltro == null ||
                            "Todas".equals(categoriaFiltro.getNombre()) ||
                            p.getCategoria().getIdCategoria() == categoriaFiltro.getIdCategoria();

                    // Filtro por estado
                    boolean cumpleEstado = "Todos".equals(estadoFiltro) ||
                            ("Activos".equals(estadoFiltro) && p.isEstado()) ||
                            ("Inactivos".equals(estadoFiltro) && !p.isEstado());

                    return cumpleTexto && cumpleCategoria && cumpleEstado;
                })
                .toList();

        productosData.setAll(productosFiltrados);
    }

    private void cargarProductoEnFormulario(Producto producto) {
        txtNombre.setText(producto.getNombre());
        txtDescripcionCorta.setText(producto.getDescripcionCorta());
        txtDescripcionLarga.setText(producto.getDescripcionLarga());
        txtPrecioCompra.setText(producto.getPrecioCompra().toString());
        txtPrecioVenta.setText(producto.getPrecioVenta().toString());
        txtStock.setText(String.valueOf(producto.getCantidadStock()));
        txtStockMinimo.setText(String.valueOf(producto.getStockMinimo()));
        cmbUnidadMedida.setValue(producto.getUnidadMedida());
        cmbCategoria.setValue(producto.getCategoria());
        txtCodigoBarras.setText(producto.getCodigoBarras());
        chkEstado.setSelected(producto.isEstado());

        // Cargar imagen
        if (producto.getRutaImagen() != null && !producto.getRutaImagen().isEmpty()) {
            try {
                File imageFile = new File(producto.getRutaImagen());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imgProducto.setImage(image);
                    lblRutaImagen.setText(imageFile.getName());
                    rutaImagenSeleccionada = producto.getRutaImagen();
                } else {
                    cargarImagenPorDefecto();
                    lblRutaImagen.setText("Imagen no encontrada");
                }
            } catch (Exception e) {
                cargarImagenPorDefecto();
                lblRutaImagen.setText("Error al cargar imagen");
            }
        } else {
            cargarImagenPorDefecto();
            lblRutaImagen.setText("Sin imagen");
        }
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtDescripcionCorta.clear();
        txtDescripcionLarga.clear();
        txtPrecioCompra.clear();
        txtPrecioVenta.clear();
        txtStock.clear();
        txtStockMinimo.clear();
        cmbUnidadMedida.setValue("Pieza");
        cmbCategoria.setValue(null);
        txtCodigoBarras.clear();
        chkEstado.setSelected(true);
        cargarImagenPorDefecto();
        lblRutaImagen.setText("Seleccione una imagen...");
        rutaImagenSeleccionada = null;
    }

    private void habilitarFormulario(boolean habilitar) {
        txtNombre.setDisable(!habilitar);
        txtDescripcionCorta.setDisable(!habilitar);
        txtDescripcionLarga.setDisable(!habilitar);
        txtPrecioCompra.setDisable(!habilitar);
        txtPrecioVenta.setDisable(!habilitar);
        txtStock.setDisable(!habilitar);
        txtStockMinimo.setDisable(!habilitar);
        cmbUnidadMedida.setDisable(!habilitar);
        cmbCategoria.setDisable(!habilitar);
        txtCodigoBarras.setDisable(!habilitar);
        chkEstado.setDisable(!habilitar);
        btnSeleccionarImagen.setDisable(!habilitar);

        btnGuardar.setDisable(!habilitar);
        btnCancelar.setDisable(!habilitar);
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        // Validar nombre
        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("- El nombre es obligatorio\n");
        }

        // Validar categoría
        if (cmbCategoria.getValue() == null) {
            errores.append("- Debe seleccionar una categoría\n");
        }

        // Validar precios
        try {
            BigDecimal precioCompra = new BigDecimal(txtPrecioCompra.getText());
            if (precioCompra.compareTo(BigDecimal.ZERO) < 0) {
                errores.append("- El precio de compra debe ser mayor o igual a 0\n");
            }
        } catch (NumberFormatException e) {
            errores.append("- El precio de compra debe ser un número válido\n");
        }

        try {
            BigDecimal precioVenta = new BigDecimal(txtPrecioVenta.getText());
            if (precioVenta.compareTo(BigDecimal.ZERO) <= 0) {
                errores.append("- El precio de venta debe ser mayor a 0\n");
            }
        } catch (NumberFormatException e) {
            errores.append("- El precio de venta debe ser un número válido\n");
        }

        // Validar stock
        try {
            int stock = Integer.parseInt(txtStock.getText());
            if (stock < 0) {
                errores.append("- El stock no puede ser negativo\n");
            }
        } catch (NumberFormatException e) {
            errores.append("- El stock debe ser un número entero válido\n");
        }

        // Validar stock mínimo
        try {
            int stockMinimo = Integer.parseInt(txtStockMinimo.getText());
            if (stockMinimo < 0) {
                errores.append("- El stock mínimo no puede ser negativo\n");
            }
        } catch (NumberFormatException e) {
            errores.append("- El stock mínimo debe ser un número entero válido\n");
        }

        // Validar código de barras único
        if (!txtCodigoBarras.getText().trim().isEmpty()) {
            int idExcluir = modoEdicion ? productoSeleccionado.getIdProducto() : 0;
            if (productoDAO.existeCodigoBarras(txtCodigoBarras.getText(), idExcluir)) {
                errores.append("- El código de barras ya existe\n");
            }
        }

        if (errores.length() > 0) {
            AlertUtils.mostrarErrorValidacion("Errores de Validación", "", errores.toString());
            return false;
        }

        return true;
    }

    private Producto construirProductoDesdeFormulario() {
        Producto producto = new Producto();
        producto.setNombre(txtNombre.getText().trim());
        producto.setDescripcionCorta(txtDescripcionCorta.getText().trim());
        producto.setDescripcionLarga(txtDescripcionLarga.getText().trim());
        producto.setPrecioCompra(new BigDecimal(txtPrecioCompra.getText()));
        producto.setPrecioVenta(new BigDecimal(txtPrecioVenta.getText()));
        producto.setCantidadStock(Integer.parseInt(txtStock.getText()));
        producto.setStockMinimo(Integer.parseInt(txtStockMinimo.getText()));
        producto.setUnidadMedida(cmbUnidadMedida.getValue());
        producto.setCategoria(cmbCategoria.getValue());
        producto.setCodigoBarras(txtCodigoBarras.getText().trim());
        producto.setEstado(chkEstado.isSelected());
        producto.setRutaImagen(rutaImagenSeleccionada);

        return producto;
    }

    private void validarNumero(TextField campo, String nuevoTexto) {
        if (!nuevoTexto.matches("\\d*\\.?\\d*")) {
            campo.setText(nuevoTexto.replaceAll("[^\\d.]", ""));
        }
    }

    private void validarEntero(TextField campo, String nuevoTexto) {
        if (!nuevoTexto.matches("\\d*")) {
            campo.setText(nuevoTexto.replaceAll("[^\\d]", ""));
        }
    }

    private boolean camposModificados() {
        // Implementar lógica para detectar si los campos han sido modificados
        return !txtNombre.getText().isEmpty() || !txtPrecioVenta.getText().isEmpty();
    }

    private void cancelarOperacion() {
        limpiarFormulario();
        habilitarFormulario(false);
        modoEdicion = false;
        sessionManager.registrarActividad("Operación de producto cancelada");
    }

    private void cargarImagenPorDefecto() {
        try {
            // Cargar imagen por defecto desde recursos
            Image imagenPorDefecto = new Image(getClass().getResourceAsStream("/images/producto_default.png"));
            imgProducto.setImage(imagenPorDefecto);
        } catch (Exception e) {
            // Si no existe la imagen por defecto, crear una imagen vacía
            imgProducto.setImage(null);
        }
    }
}