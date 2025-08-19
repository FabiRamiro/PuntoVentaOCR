package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.ProductoDAO;
import com.pos.puntoventaocr.dao.CategoriaDAO;
import com.pos.puntoventaocr.models.Producto;
import com.pos.puntoventaocr.models.Categoria;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.DateUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ProductoController implements Initializable {

    // Tabla de productos
    @FXML private TableView<Producto> tableProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecioCompra;
    @FXML private TableColumn<Producto, BigDecimal> colPrecioVenta;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, String> colEstado;
    @FXML private TableColumn<Producto, String> colFechaCreacion;

    // Panel de detalles del producto
    @FXML private VBox panelImagen;
    @FXML private Label lblDetalleCodigo;
    @FXML private Label lblDetalleNombre;
    @FXML private Label lblDetalleDescripcion;
    @FXML private Label lblDetallePrecioCompra;
    @FXML private Label lblDetallePrecioVenta;
    @FXML private Label lblDetalleStock;
    @FXML private Label lblDetalleGanancia;

    // Filtros
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Categoria> cmbFiltroCategoria;
    @FXML private ComboBox<String> cmbFiltroEstado;

    private ProductoDAO productoDAO;
    private CategoriaDAO categoriaDAO;
    private ObservableList<Producto> productosData;
    private FilteredList<Producto> listaFiltrada;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productoDAO = new ProductoDAO();
        categoriaDAO = new CategoriaDAO();
        productosData = FXCollections.observableArrayList();
        
        configurarTabla();
        configurarFiltros();
        cargarDatos();
        limpiarDetalles();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(cellData -> {
            Categoria categoria = cellData.getValue().getCategoria();
            return new SimpleStringProperty(
                    categoria != null ? categoria.getNombre() : "Sin categoría"
            );
        });
        colPrecioCompra.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        colPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("cantidadStock"));
        colEstado.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(
                    cellData.getValue().isEstado() ? "Activo" : "Inactivo"
            );
        });
        colFechaCreacion.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(
                    DateUtils.formatearFecha(cellData.getValue().getFechaCreacion())
            );
        });

        // MEJORADO: Formato de celdas de precio con estilos consistentes
        colPrecioCompra.setCellFactory(tc -> new TableCell<Producto, BigDecimal>() {
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
                        setStyle("-fx-text-fill: #1976D2;");
                    } else {
                        setStyle("-fx-text-fill: #333333;");
                    }
                }
            }
        });

        colPrecioVenta.setCellFactory(tc -> new TableCell<Producto, BigDecimal>() {
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

        // Configurar lista filtrada
        listaFiltrada = new FilteredList<>(productosData, p -> true);
        tableProductos.setItems(listaFiltrada);
        
        // NUEVO: Configurar estilo de selección de la tabla
        tableProductos.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Configurar listener para mostrar detalles del producto seleccionado
        tableProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                mostrarDetallesProducto(newSelection);
            } else {
                limpiarDetalles();
            }
            
            if (oldSelection != null) {
                tableProductos.refresh(); // Refrescar para actualizar estilos
            }
        });
    }

    private void configurarFiltros() {
        // Configurar filtros de estado
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                "Todos", "Activos", "Inactivos"
        ));
        cmbFiltroEstado.setValue("Todos");
        
        // Configurar eventos de filtrado
        txtBuscar.textProperty().addListener((obs, oldText, newText) -> aplicarFiltros());
        cmbFiltroCategoria.setOnAction(e -> aplicarFiltros());
        cmbFiltroEstado.setOnAction(e -> aplicarFiltros());
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

    private void aplicarFiltros() {
        listaFiltrada.setPredicate(producto -> {
            if (producto == null) return false;

            // Filtro por texto
            String textoBusqueda = txtBuscar.getText();
            if (textoBusqueda != null && !textoBusqueda.isEmpty()) {
                String busqueda = textoBusqueda.toLowerCase();
                boolean cumpleTexto = producto.getNombre().toLowerCase().contains(busqueda) ||
                        (producto.getCodigoBarras() != null && producto.getCodigoBarras().toLowerCase().contains(busqueda));
                if (!cumpleTexto) return false;
            }

            // Filtro por categoría
            Categoria categoriaFiltro = cmbFiltroCategoria.getValue();
            if (categoriaFiltro != null && !"Todas".equals(categoriaFiltro.getNombre())) {
                if (producto.getCategoria() == null || 
                    producto.getCategoria().getIdCategoria() != categoriaFiltro.getIdCategoria()) {
                    return false;
                }
            }

            // Filtro por estado
            String estadoFiltro = cmbFiltroEstado.getValue();
            if (!"Todos".equals(estadoFiltro)) {
                boolean cumpleEstado = ("Activos".equals(estadoFiltro) && producto.isEstado()) ||
                        ("Inactivos".equals(estadoFiltro) && !producto.isEstado());
                if (!cumpleEstado) return false;
            }

            return true;
        });
    }

    private void mostrarDetallesProducto(Producto producto) {
        lblDetalleCodigo.setText(producto.getCodigoBarras() != null ? producto.getCodigoBarras() : "Sin código");
        lblDetalleNombre.setText(producto.getNombre());
        lblDetalleDescripcion.setText(producto.getDescripcionCorta() != null ? producto.getDescripcionCorta() : "Sin descripción");
        lblDetallePrecioCompra.setText(String.format("$%.2f", producto.getPrecioCompra()));
        lblDetallePrecioVenta.setText(String.format("$%.2f", producto.getPrecioVenta()));
        lblDetalleStock.setText(producto.getCantidadStock() + " " + producto.getUnidadMedida());

        BigDecimal ganancia = producto.calcularGanancia();
        double porcentaje = producto.calcularPorcentajeGanancia();
        lblDetalleGanancia.setText(String.format("$%.2f (%.1f%%)", ganancia, porcentaje));

        // Aplicar color según el stock
        if (producto.getCantidadStock() == 0) {
            lblDetalleStock.setStyle("-fx-text-fill: red;");
        } else if (producto.esStockBajo(5)) {
            lblDetalleStock.setStyle("-fx-text-fill: orange;");
        } else {
            lblDetalleStock.setStyle("-fx-text-fill: green;");
        }

        // NUEVO: Cargar y mostrar imagen del producto
        cargarImagenProducto(producto);
    }

    /**
     * Carga y muestra la imagen del producto en el panel de detalles
     */
    private void cargarImagenProducto(Producto producto) {
        try {
            panelImagen.getChildren().clear();
            
            String imagenUrl = producto.getImagenUrl();
            
            if (imagenUrl != null && !imagenUrl.trim().isEmpty()) {
                // Verificar si es una URL web o una ruta local
                if (imagenUrl.startsWith("http://") || imagenUrl.startsWith("https://")) {
                    // Imagen web
                    try {
                        Image imagen = new Image(imagenUrl, true); // true para cargar en background
                        ImageView imageView = crearImageView(imagen);
                        panelImagen.getChildren().add(imageView);
                        
                        // Manejar errores de carga
                        imagen.errorProperty().addListener((obs, oldError, newError) -> {
                            if (newError) {
                                mostrarImagenNoDisponible();
                            }
                        });
                        
                    } catch (Exception e) {
                        System.err.println("Error cargando imagen web: " + e.getMessage());
                        mostrarImagenNoDisponible();
                    }
                    
                } else {
                    // Imagen local
                    File archivoImagen = new File(imagenUrl);
                    if (archivoImagen.exists() && archivoImagen.isFile()) {
                        try {
                            String fileUrl = archivoImagen.toURI().toString();
                            Image imagen = new Image(fileUrl);
                            ImageView imageView = crearImageView(imagen);
                            panelImagen.getChildren().add(imageView);
                        } catch (Exception e) {
                            System.err.println("Error cargando imagen local: " + e.getMessage());
                            mostrarImagenNoDisponible();
                        }
                    } else {
                        System.err.println("Archivo de imagen no encontrado: " + imagenUrl);
                        mostrarImagenNoDisponible();
                    }
                }
            } else {
                // No hay imagen configurada
                mostrarImagenNoDisponible();
            }
            
        } catch (Exception e) {
            System.err.println("Error general cargando imagen: " + e.getMessage());
            mostrarImagenNoDisponible();
        }
    }

    /**
     * Crea un ImageView configurado para mostrar la imagen del producto
     */
    private ImageView crearImageView(Image imagen) {
        ImageView imageView = new ImageView(imagen);
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1px;");
        
        // Agregar cursor de mano para indicar que se puede hacer clic
        imageView.setOnMouseEntered(e -> imageView.getScene().setCursor(javafx.scene.Cursor.HAND));
        imageView.setOnMouseExited(e -> imageView.getScene().setCursor(javafx.scene.Cursor.DEFAULT));
        
        // Permitir hacer clic para ver imagen en tamaño completo
        imageView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) { // Doble clic
                mostrarImagenCompleta(imagen);
            }
        });
        
        return imageView;
    }

    /**
     * Muestra un mensaje cuando no hay imagen disponible
     */
    private void mostrarImagenNoDisponible() {
        Label lblSinImagen = new Label("Sin imagen");
        lblSinImagen.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 14px; -fx-alignment: center;");
        
        // Crear un rectángulo gris como placeholder
        javafx.scene.shape.Rectangle placeholder = new javafx.scene.shape.Rectangle(200, 200);
        placeholder.setFill(javafx.scene.paint.Color.LIGHTGRAY);
        placeholder.setStroke(javafx.scene.paint.Color.GRAY);
        placeholder.setStrokeWidth(1);
        
        StackPane placeholderPane = new StackPane();
        placeholderPane.getChildren().addAll(placeholder, lblSinImagen);
        
        panelImagen.getChildren().add(placeholderPane);
    }

    /**
     * Muestra la imagen en una ventana modal para verla en tamaño completo
     */
    private void mostrarImagenCompleta(Image imagen) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Imagen del Producto");
            alert.setHeaderText("Vista Completa");
            
            ImageView imageViewGrande = new ImageView(imagen);
            imageViewGrande.setFitWidth(600);
            imageViewGrande.setFitHeight(600);
            imageViewGrande.setPreserveRatio(true);
            imageViewGrande.setSmooth(true);
            
            ScrollPane scrollPane = new ScrollPane(imageViewGrande);
            scrollPane.setPrefSize(620, 620);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            
            alert.getDialogPane().setContent(scrollPane);
            alert.getDialogPane().setPrefSize(650, 700);
            alert.showAndWait();
            
        } catch (Exception e) {
            mostrarError("Error al mostrar imagen: " + e.getMessage());
        }
    }

    private void limpiarDetalles() {
        lblDetalleCodigo.setText("-");
        lblDetalleNombre.setText("-");
        lblDetalleDescripcion.setText("-");
        lblDetallePrecioCompra.setText("-");
        lblDetallePrecioVenta.setText("-");
        lblDetalleStock.setText("-");
        lblDetalleGanancia.setText("-");
        lblDetalleStock.setStyle("");
        
        // NUEVO: Limpiar imagen
        panelImagen.getChildren().clear();
        mostrarImagenNoDisponible();
    }

    private void mostrarError(String mensaje) {
        AlertUtils.mostrarError("Error", mensaje);
    }
}