package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.ProductoDAO;
import com.pos.puntoventaocr.dao.CategoriaDAO;
import com.pos.puntoventaocr.dao.BitacoraDAO;
import com.pos.puntoventaocr.models.Producto;
import com.pos.puntoventaocr.models.Categoria;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.fxml.FXMLLoader;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class GestionarProductosController {

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colMarca;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, String> colEstado;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ComboBox<String> cmbEstado;

    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnActualizar;

    // Panel de detalles
    @FXML private ImageView imgProducto;
    @FXML private Label lblCodigo;
    @FXML private Label lblNombre;
    @FXML private Label lblDescripcion;
    @FXML private Label lblCategoria;
    @FXML private Label lblMarca;
    @FXML private Label lblPrecioCompra;
    @FXML private Label lblPrecioVenta;
    @FXML private Label lblStock;
    @FXML private Label lblStockMinimo;
    @FXML private Label lblUnidadMedida;
    @FXML private Label lblEstado;
    @FXML private Label lblFechaCreacion;
    @FXML private Label lblFechaModificacion;

    @FXML private Button btnAjustarStock;
    @FXML private Button btnCambiarPrecios;
    @FXML private Button btnDuplicar;

    // Barra de estado
    @FXML private Label lblTotalProductos;
    @FXML private Label lblProductosActivos;
    @FXML private Label lblBajoStock;
    @FXML private Label lblEstadoCarga;

    private ProductoDAO productoDAO;
    private CategoriaDAO categoriaDAO;
    private BitacoraDAO bitacoraDAO;
    private SessionManager sessionManager;
    private ObservableList<Producto> listaProductos;

    public void initialize() {
        productoDAO = new ProductoDAO();
        categoriaDAO = new CategoriaDAO();
        bitacoraDAO = new BitacoraDAO();
        sessionManager = SessionManager.getInstance();
        listaProductos = FXCollections.observableArrayList();

        configurarTabla();
        configurarEventos();
        configurarPermisos(); // Agregar configuración de permisos
        cargarDatos();
    }

    private void configurarTabla() {
        // Configurar columnas
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(cellData -> {
            Categoria cat = cellData.getValue().getCategoria();
            return new SimpleStringProperty(cat != null ? cat.getNombre() : "Sin categoría");
        });
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("cantidadStock"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Asociar datos a la tabla
        tablaProductos.setItems(listaProductos);

        // Listener para selección
        tablaProductos.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    mostrarDetallesProducto(newSelection);
                    btnEditar.setDisable(false);
                    btnEliminar.setDisable(false);
                } else {
                    limpiarDetalles();
                    btnEditar.setDisable(true);
                    btnEliminar.setDisable(true);
                }
            }
        );
    }

    private void configurarEventos() {
        // Búsqueda en tiempo real
        txtBuscar.textProperty().addListener((obs, oldText, newText) -> filtrarProductos());

        // Filtros
        cmbCategoria.setOnAction(e -> filtrarProductos());
        cmbEstado.setOnAction(e -> filtrarProductos());
    }

    private void cargarDatos() {
        try {
            // Cargar productos
            List<Producto> productos = productoDAO.obtenerTodos();
            listaProductos.setAll(productos);

            // Cargar categorías
            List<Categoria> categorias = categoriaDAO.obtenerActivas();
            cmbCategoria.getItems().clear();
            cmbCategoria.getItems().add(null); // Opción "Todas"
            cmbCategoria.getItems().addAll(categorias);

            // Configurar estados
            cmbEstado.getItems().clear();
            cmbEstado.getItems().addAll("Todos", "ACTIVO", "INACTIVO", "AGOTADO");
            cmbEstado.setValue("Todos");

            actualizarEstadisticas();
            lblEstadoCarga.setText("Datos cargados correctamente");

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error cargando datos: " + e.getMessage());
            lblEstadoCarga.setText("Error cargando datos");
        }
    }

    private void filtrarProductos() {
        String textoBusqueda = txtBuscar.getText().toLowerCase().trim();
        Categoria categoriaSeleccionada = cmbCategoria.getValue();
        String estadoSeleccionado = cmbEstado.getValue();

        List<Producto> productosFiltrados = productoDAO.obtenerTodos().stream()
            .filter(p -> {
                // Filtro por texto
                if (!textoBusqueda.isEmpty()) {
                    return p.getNombre().toLowerCase().contains(textoBusqueda) ||
                           (p.getCodigoBarras() != null && p.getCodigoBarras().toLowerCase().contains(textoBusqueda)) ||
                           (p.getMarca() != null && p.getMarca().toLowerCase().contains(textoBusqueda));
                }
                return true;
            })
            .filter(p -> {
                // Filtro por categoría
                if (categoriaSeleccionada != null) {
                    return p.getCategoria() != null &&
                           p.getCategoria().getIdCategoria() == categoriaSeleccionada.getIdCategoria();
                }
                return true;
            })
            .filter(p -> {
                // Filtro por estado
                if (estadoSeleccionado != null && !"Todos".equals(estadoSeleccionado)) {
                    return estadoSeleccionado.equals(p.getEstado());
                }
                return true;
            })
            .toList();

        listaProductos.setAll(productosFiltrados);
        actualizarEstadisticas();
    }

    private void mostrarDetallesProducto(Producto producto) {
        if (producto == null) {
            limpiarDetalles();
            return;
        }

        lblCodigo.setText("Código: " + (producto.getCodigoBarras() != null ? producto.getCodigoBarras() : "N/A"));
        lblNombre.setText("Nombre: " + producto.getNombre());
        lblDescripcion.setText("Descripción: " + (producto.getDescripcionCorta() != null ? producto.getDescripcionCorta() : "N/A"));
        lblCategoria.setText("Categoría: " + (producto.getCategoria() != null ? producto.getCategoria().getNombre() : "Sin categoría"));
        lblMarca.setText("Marca: " + (producto.getMarca() != null ? producto.getMarca() : "N/A"));
        lblPrecioCompra.setText("Precio Compra: $" + producto.getPrecioCompra());
        lblPrecioVenta.setText("Precio Venta: $" + producto.getPrecioVenta());
        lblStock.setText("Stock: " + producto.getCantidadStock());
        lblStockMinimo.setText("Stock Mínimo: " + producto.getStockMinimo());
        lblUnidadMedida.setText("Unidad: " + producto.getUnidadMedida());
        lblEstado.setText("Estado: " + producto.getEstadoDisplay());

        // Cargar imagen del producto
        if (producto.getRutaImagen() != null && !producto.getRutaImagen().isEmpty()) {
            try {
                File archivoImagen = new File(producto.getRutaImagen());
                if (archivoImagen.exists()) {
                    Image imagen = new Image(archivoImagen.toURI().toString());
                    imgProducto.setImage(imagen);
                } else {
                    imgProducto.setImage(null);
                }
            } catch (Exception e) {
                imgProducto.setImage(null);
                System.err.println("Error cargando imagen del producto: " + e.getMessage());
            }
        } else {
            imgProducto.setImage(null);
        }

        if (producto.getFechaCreacion() != null) {
            lblFechaCreacion.setText("Creado: " + producto.getFechaCreacion().toString());
        }
        if (producto.getFechaModificacion() != null) {
            lblFechaModificacion.setText("Modificado: " + producto.getFechaModificacion().toString());
        }
    }

    private void limpiarDetalles() {
        lblCodigo.setText("Código: -");
        lblNombre.setText("Nombre: -");
        lblDescripcion.setText("Descripción: -");
        lblCategoria.setText("Categoría: -");
        lblMarca.setText("Marca: -");
        lblPrecioCompra.setText("Precio Compra: -");
        lblPrecioVenta.setText("Precio Venta: -");
        lblStock.setText("Stock: -");
        lblStockMinimo.setText("Stock Mínimo: -");
        lblUnidadMedida.setText("Unidad: -");
        lblEstado.setText("Estado: -");
        lblFechaCreacion.setText("Creado: -");
        lblFechaModificacion.setText("Modificado: -");
    }

    private void actualizarEstadisticas() {
        int total = listaProductos.size();
        long activos = listaProductos.stream().filter(p -> "ACTIVO".equals(p.getEstado())).count();
        long bajoStock = listaProductos.stream().filter(Producto::hasBajoStock).count();

        lblTotalProductos.setText("Total productos: " + total);
        lblProductosActivos.setText("Activos: " + activos);
        lblBajoStock.setText("Bajo stock: " + bajoStock);
    }

    private void configurarPermisos() {
        // Verificar permisos del usuario actual
        if (sessionManager.getUsuarioActual() != null) {
            String rol = sessionManager.getUsuarioActual().getNombreRol().toUpperCase();

            if ("CAJERO".equals(rol)) {
                // El cajero solo puede consultar productos, no modificar
                btnNuevo.setDisable(true);
                btnEditar.setDisable(true);
                btnEliminar.setDisable(true);
                btnAjustarStock.setDisable(true);
                btnCambiarPrecios.setDisable(true);
                btnDuplicar.setDisable(true);

                // Cambiar tooltip para informar al usuario
                btnNuevo.setTooltip(new Tooltip("Sin permisos para crear productos"));
                btnEditar.setTooltip(new Tooltip("Sin permisos para editar productos"));
                btnEliminar.setTooltip(new Tooltip("Sin permisos para eliminar productos"));
                btnAjustarStock.setTooltip(new Tooltip("Sin permisos para ajustar stock"));
                btnCambiarPrecios.setTooltip(new Tooltip("Sin permisos para cambiar precios"));
                btnDuplicar.setTooltip(new Tooltip("Sin permisos para duplicar productos"));
            }
        }
    }

    @FXML
    private void nuevoProducto() {
        try {
            // Cargar el FXML del diálogo
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/productos/nuevo_producto.fxml"));
            DialogPane dialogPane = loader.load();

            NuevoProductoController controller = loader.getController();

            // Crear y configurar el diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Nuevo Producto");
            dialog.setHeaderText("Crear un nuevo producto");

            // Mostrar el diálogo y procesar el resultado
            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                Producto nuevoProducto = controller.obtenerProducto();
                if (nuevoProducto != null) {
                    if (productoDAO.guardar(nuevoProducto)) {
                        AlertUtils.showInfo("Éxito", "Producto creado correctamente");

                        // Registrar en bitácora
                        Integer idUsuario = SessionManager.getInstance().getCurrentUserId();
                        if (idUsuario != null && idUsuario > 0) {
                            bitacoraDAO.registrarCreacionProducto(idUsuario, nuevoProducto.getNombre(),
                                nuevoProducto.getCodigoBarras());
                        }

                        actualizarLista();

                        // Seleccionar el producto recién creado
                        for (Producto p : listaProductos) {
                            if (p.getIdProducto() == nuevoProducto.getIdProducto()) {
                                tablaProductos.getSelectionModel().select(p);
                                break;
                            }
                        }
                    } else {
                        AlertUtils.showError("Error", "No se pudo crear el producto");
                    }
                }
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error abriendo diálogo de nuevo producto: " + e.getMessage());
        }
    }

    @FXML
    private void editarProducto() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.showWarning("Selección", "Seleccione un producto para editar");
            return;
        }

        try {
            // Guardar datos anteriores para bitácora
            String datosAnteriores = String.format("{\"nombre\":\"%s\",\"precio_venta\":%s,\"stock\":%d}",
                seleccionado.getNombre(), seleccionado.getPrecioVenta(), seleccionado.getCantidadStock());

            // Cargar el FXML del diálogo
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/productos/nuevo_producto.fxml"));
            DialogPane dialogPane = loader.load();

            NuevoProductoController controller = loader.getController();
            controller.setProducto(seleccionado);

            // Crear y configurar el diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Editar Producto");
            dialog.setHeaderText("Modificar información del producto");

            // Mostrar el diálogo y procesar el resultado
            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                Producto productoEditado = controller.obtenerProducto();
                if (productoEditado != null) {
                    if (productoDAO.actualizar(productoEditado)) {
                        AlertUtils.showInfo("Éxito", "Producto actualizado correctamente");

                        // Registrar en bitácora
                        Integer idUsuario = SessionManager.getInstance().getCurrentUserId();
                        if (idUsuario != null && idUsuario > 0) {
                            String datosNuevos = String.format("{\"nombre\":\"%s\",\"precio_venta\":%s,\"stock\":%d}",
                                productoEditado.getNombre(), productoEditado.getPrecioVenta(), productoEditado.getCantidadStock());
                            bitacoraDAO.registrarModificacionProducto(idUsuario, productoEditado.getNombre(),
                                datosAnteriores, datosNuevos);
                        }

                        actualizarLista();

                        // Mantener la selección
                        for (Producto p : listaProductos) {
                            if (p.getIdProducto() == productoEditado.getIdProducto()) {
                                tablaProductos.getSelectionModel().select(p);
                                break;
                            }
                        }
                    } else {
                        AlertUtils.showError("Error", "No se pudo actualizar el producto");
                    }
                }
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error abriendo diálogo de edición: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarProducto() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.showWarning("Selección", "Seleccione un producto para eliminar");
            return;
        }

        Optional<ButtonType> resultado = AlertUtils.showConfirmation("Confirmar", "¿Está seguro de eliminar el producto: " + seleccionado.getNombre() + "?");
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                if (productoDAO.eliminar(seleccionado.getIdProducto())) {
                    AlertUtils.showInfo("Éxito", "Producto eliminado correctamente");

                    // Registrar en bitácora
                    Integer idUsuario = SessionManager.getInstance().getCurrentUserId();
                    if (idUsuario != null && idUsuario > 0) {
                        bitacoraDAO.registrarEliminacionProducto(idUsuario, seleccionado.getNombre(),
                            seleccionado.getCodigoBarras());
                    }

                    actualizarLista();
                } else {
                    AlertUtils.showError("Error", "No se pudo eliminar el producto");
                }
            } catch (Exception e) {
                AlertUtils.showError("Error", "Error eliminando producto: " + e.getMessage());
            }
        }
    }

    @FXML
    private void actualizarLista() {
        cargarDatos();
    }

    @FXML
    private void ajustarStock() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.showWarning("Selección", "Seleccione un producto para ajustar el stock");
            return;
        }

        try {
            // Cargar el FXML del diálogo
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/productos/ajustar_stock.fxml"));
            DialogPane dialogPane = loader.load();

            AjustarStockController controller = loader.getController();
            controller.setProducto(seleccionado);

            // Crear y configurar el diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Ajustar Stock");
            dialog.setHeaderText("Ajuste de inventario - " + seleccionado.getNombre());

            // Mostrar el diálogo y procesar el resultado
            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (controller.aplicarAjuste()) {
                    actualizarLista();

                    // Mantener la selección en el producto actualizado
                    for (Producto p : listaProductos) {
                        if (p.getIdProducto() == seleccionado.getIdProducto()) {
                            tablaProductos.getSelectionModel().select(p);
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error abriendo diálogo de ajuste de stock: " + e.getMessage());
        }
    }

    @FXML
    private void cambiarPrecios() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.showWarning("Selección", "Seleccione un producto para cambiar precios");
            return;
        }

        // Usar el mismo diálogo de edición pero enfocado en precios
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/productos/nuevo_producto.fxml"));
            DialogPane dialogPane = loader.load();

            NuevoProductoController controller = loader.getController();
            controller.setProducto(seleccionado);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Cambiar Precios");
            dialog.setHeaderText("Actualizar precios - " + seleccionado.getNombre());

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                Producto productoEditado = controller.obtenerProducto();
                if (productoEditado != null) {
                    if (productoDAO.actualizar(productoEditado)) {
                        AlertUtils.showInfo("Éxito", "Precios actualizados correctamente");
                        actualizarLista();

                        // Mantener la selección
                        for (Producto p : listaProductos) {
                            if (p.getIdProducto() == productoEditado.getIdProducto()) {
                                tablaProductos.getSelectionModel().select(p);
                                break;
                            }
                        }
                    } else {
                        AlertUtils.showError("Error", "No se pudieron actualizar los precios");
                    }
                }
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error abriendo diálogo de precios: " + e.getMessage());
        }
    }

    @FXML
    private void duplicarProducto() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.showWarning("Selección", "Seleccione un producto para duplicar");
            return;
        }

        try {
            // Cargar el FXML del diálogo
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/productos/duplicar_producto.fxml"));
            DialogPane dialogPane = loader.load();

            DuplicarProductoController controller = loader.getController();
            controller.setProductoOriginal(seleccionado);

            // Crear y configurar el diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Duplicar Producto");
            dialog.setHeaderText("Crear copia de: " + seleccionado.getNombre());

            // Mostrar el diálogo y procesar el resultado
            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                Producto productoDuplicado = controller.crearProductoDuplicado();
                if (productoDuplicado != null) {
                    if (productoDAO.guardar(productoDuplicado)) {
                        String resumen = controller.getResumenDuplicacion();
                        AlertUtils.showInfo("Éxito", "Producto duplicado correctamente\n\n" + resumen);
                        actualizarLista();

                        // Seleccionar el producto duplicado
                        for (Producto p : listaProductos) {
                            if (p.getIdProducto() == productoDuplicado.getIdProducto()) {
                                tablaProductos.getSelectionModel().select(p);
                                break;
                            }
                        }
                    } else {
                        AlertUtils.showError("Error", "No se pudo duplicar el producto");
                    }
                }
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error abriendo diálogo de duplicación: " + e.getMessage());
        }
    }
}
