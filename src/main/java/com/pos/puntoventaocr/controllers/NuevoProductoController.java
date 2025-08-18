package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.CategoriaDAO;
import com.pos.puntoventaocr.dao.ProductoDAO;
import com.pos.puntoventaocr.models.Categoria;
import com.pos.puntoventaocr.models.Producto;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class NuevoProductoController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtCodigoBarras;
    @FXML private TextField txtCodigoInterno;
    @FXML private TextField txtMarca;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ComboBox<String> cmbUnidadMedida;
    @FXML private TextField txtDescripcionCorta;
    @FXML private TextArea txtDescripcionLarga;
    @FXML private TextField txtPrecioCompra;
    @FXML private TextField txtPrecioVenta;
    @FXML private TextField txtStock;
    @FXML private TextField txtStockMinimo;
    @FXML private CheckBox chkActivo;

    // Campos de imagen
    @FXML private ImageView imgPreview;
    @FXML private Label lblImagenInfo;
    @FXML private Button btnSeleccionarImagen;
    @FXML private Button btnEliminarImagen;

    private CategoriaDAO categoriaDAO;
    private ProductoDAO productoDAO;
    private Producto producto;
    private boolean esEdicion = false;
    private String rutaImagenTemporal;

    public void initialize() {
        categoriaDAO = new CategoriaDAO();
        productoDAO = new ProductoDAO();

        configurarCampos();
        cargarDatos();
        configurarDragAndDrop();
    }

    private void configurarCampos() {
        // Configurar unidades de medida
        cmbUnidadMedida.setItems(FXCollections.observableArrayList(
            "PIEZA", "KILOGRAMO", "GRAMO", "LITRO", "MILILITRO",
            "METRO", "CENTIMETRO", "CAJA", "PAQUETE", "DOCENA"
        ));
        cmbUnidadMedida.setValue("PIEZA");

        // Configurar campos numéricos
        txtStock.setText("0");
        txtStockMinimo.setText("5");
        txtPrecioCompra.setText("0.00");
        txtPrecioVenta.setText("0.00");

        // Validaciones en tiempo real
        txtPrecioCompra.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtPrecioCompra.setText(oldVal);
            }
        });

        txtPrecioVenta.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtPrecioVenta.setText(oldVal);
            }
        });

        txtStock.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtStock.setText(oldVal);
            }
        });

        txtStockMinimo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtStockMinimo.setText(oldVal);
            }
        });
    }

    private void cargarDatos() {
        try {
            // Cargar categorías
            List<Categoria> categorias = categoriaDAO.obtenerActivas();
            cmbCategoria.setItems(FXCollections.observableArrayList(categorias));

            // Configurar display de categorías
            cmbCategoria.setCellFactory(listView -> new ListCell<Categoria>() {
                @Override
                protected void updateItem(Categoria categoria, boolean empty) {
                    super.updateItem(categoria, empty);
                    if (empty || categoria == null) {
                        setText(null);
                    } else {
                        setText(categoria.getNombre());
                    }
                }
            });

            cmbCategoria.setButtonCell(new ListCell<Categoria>() {
                @Override
                protected void updateItem(Categoria categoria, boolean empty) {
                    super.updateItem(categoria, empty);
                    if (empty || categoria == null) {
                        setText("Seleccionar categoría");
                    } else {
                        setText(categoria.getNombre());
                    }
                }
            });

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error cargando datos: " + e.getMessage());
        }
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
        this.esEdicion = true;
        cargarDatosProducto();
    }

    private void cargarDatosProducto() {
        if (producto != null) {
            txtNombre.setText(producto.getNombre());
            txtCodigoBarras.setText(producto.getCodigoBarras());
            txtCodigoInterno.setText(producto.getCodigoInterno());
            txtMarca.setText(producto.getMarca());
            txtDescripcionCorta.setText(producto.getDescripcionCorta());
            txtDescripcionLarga.setText(producto.getDescripcionLarga());

            if (producto.getPrecioCompra() != null) {
                txtPrecioCompra.setText(producto.getPrecioCompra().toString());
            }
            if (producto.getPrecioVenta() != null) {
                txtPrecioVenta.setText(producto.getPrecioVenta().toString());
            }

            txtStock.setText(String.valueOf(producto.getCantidadStock()));
            txtStockMinimo.setText(String.valueOf(producto.getStockMinimo()));
            cmbUnidadMedida.setValue(producto.getUnidadMedida());
            chkActivo.setSelected("ACTIVO".equals(producto.getEstado()));

            // Cargar imagen
            if (producto.getRutaImagen() != null) {
                File file = new File(producto.getRutaImagen());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    imgPreview.setImage(image);
                    lblImagenInfo.setText("Imagen cargada: " + file.getName());
                } else {
                    imgPreview.setImage(null);
                    lblImagenInfo.setText("No hay imagen");
                }
            } else {
                imgPreview.setImage(null);
                lblImagenInfo.setText("No hay imagen");
            }

            // Seleccionar categoría
            if (producto.getCategoria() != null) {
                for (Categoria cat : cmbCategoria.getItems()) {
                    if (cat.getIdCategoria() == producto.getCategoria().getIdCategoria()) {
                        cmbCategoria.setValue(cat);
                        break;
                    }
                }
            }
        }
    }

    public boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("- El nombre es obligatorio\n");
        }

        if (cmbCategoria.getValue() == null) {
            errores.append("- Debe seleccionar una categoría\n");
        }

        try {
            BigDecimal precioCompra = new BigDecimal(txtPrecioCompra.getText());
            if (precioCompra.compareTo(BigDecimal.ZERO) < 0) {
                errores.append("- El precio de compra no puede ser negativo\n");
            }
        } catch (NumberFormatException e) {
            errores.append("- El precio de compra debe ser un número válido\n");
        }

        try {
            BigDecimal precioVenta = new BigDecimal(txtPrecioVenta.getText());
            if (precioVenta.compareTo(BigDecimal.ZERO) < 0) {
                errores.append("- El precio de venta no puede ser negativo\n");
            }
        } catch (NumberFormatException e) {
            errores.append("- El precio de venta debe ser un número válido\n");
        }

        try {
            int stock = Integer.parseInt(txtStock.getText());
            if (stock < 0) {
                errores.append("- El stock no puede ser negativo\n");
            }
        } catch (NumberFormatException e) {
            errores.append("- El stock debe ser un número entero válido\n");
        }

        try {
            int stockMinimo = Integer.parseInt(txtStockMinimo.getText());
            if (stockMinimo < 0) {
                errores.append("- El stock mínimo no puede ser negativo\n");
            }
        } catch (NumberFormatException e) {
            errores.append("- El stock mínimo debe ser un número entero válido\n");
        }

        if (errores.length() > 0) {
            AlertUtils.showError("Errores de validación", errores.toString());
            return false;
        }

        return true;
    }

    public Producto obtenerProducto() {
        if (!validarCampos()) {
            return null;
        }

        try {
            if (producto == null) {
                producto = new Producto();
            }

            producto.setNombre(txtNombre.getText().trim());
            producto.setCodigoBarras(txtCodigoBarras.getText().trim());
            producto.setCodigoInterno(txtCodigoInterno.getText().trim());
            producto.setMarca(txtMarca.getText().trim());
            producto.setDescripcionCorta(txtDescripcionCorta.getText().trim());
            producto.setDescripcionLarga(txtDescripcionLarga.getText().trim());
            producto.setCategoria(cmbCategoria.getValue());
            producto.setUnidadMedida(cmbUnidadMedida.getValue());
            producto.setPrecioCompra(new BigDecimal(txtPrecioCompra.getText()));
            producto.setPrecioVenta(new BigDecimal(txtPrecioVenta.getText()));
            producto.setCantidadStock(Integer.parseInt(txtStock.getText()));
            producto.setStockMinimo(Integer.parseInt(txtStockMinimo.getText()));
            producto.setEstado(chkActivo.isSelected() ? "ACTIVO" : "INACTIVO");

            // Guardar ruta de imagen
            if (rutaImagenTemporal != null) {
                producto.setRutaImagen(rutaImagenTemporal);
            } else if (imgPreview.getImage() == null) {
                producto.setRutaImagen(null);
            }
            // Si no hay rutaImagenTemporal pero hay imagen, mantener la ruta existente

            if (!esEdicion) {
                producto.setCreadoPor(SessionManager.getInstance().getUsuarioActual().getIdUsuario());
            } else {
                producto.setModificadoPor(SessionManager.getInstance().getUsuarioActual().getIdUsuario());
            }

            return producto;

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error procesando datos: " + e.getMessage());
            return null;
        }
    }

    private String guardarImagenTemporal(Image image) {
        try {
            // Crear directorio temporal si no existe
            Path directorioTemporal = Files.createTempDirectory("imagenes_producto");

            // Definir archivo de destino
            String nombreArchivo = "IMG_" + System.currentTimeMillis() + ".png";
            Path archivoDestino = directorioTemporal.resolve(nombreArchivo);

            // Guardar imagen en archivo
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(nombreArchivo);
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos PNG", "*.png"),
                new FileChooser.ExtensionFilter("Archivos JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
            );

            Stage stage = (Stage) txtNombre.getScene().getWindow();
            File archivoSeleccionado = fileChooser.showSaveDialog(stage);

            if (archivoSeleccionado != null) {
                // Copiar archivo a la ubicación seleccionada
                Files.copy(archivoDestino, archivoSeleccionado.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return archivoSeleccionado.getAbsolutePath();
            }

        } catch (IOException e) {
            AlertUtils.showError("Error", "Error guardando imagen: " + e.getMessage());
        }

        return null;
    }

    public boolean isEsEdicion() {
        return esEdicion;
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles() && hayImagenEnArchivos(db.getFiles())) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean exito = false;

        if (db.hasFiles()) {
            List<File> archivos = db.getFiles();
            for (File archivo : archivos) {
                if (esArchivoImagen(archivo)) {
                    cargarImagen(archivo);
                    exito = true;
                    break; // Solo cargar la primera imagen válida
                }
            }
        }

        event.setDropCompleted(exito);
        event.consume();
    }

    private void configurarDragAndDrop() {
        // Configurar drag and drop en el contenedor de la imagen
        imgPreview.getParent().setOnDragOver(this::manejarDragOver);
        imgPreview.getParent().setOnDragDropped(this::manejarDragDropped);
    }

    private void manejarDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles() && hayImagenEnArchivos(db.getFiles())) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void manejarDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean exito = false;

        if (db.hasFiles()) {
            List<File> archivos = db.getFiles();
            for (File archivo : archivos) {
                if (esArchivoImagen(archivo)) {
                    cargarImagen(archivo);
                    exito = true;
                    break; // Solo cargar la primera imagen válida
                }
            }
        }

        event.setDropCompleted(exito);
        event.consume();
    }

    private boolean hayImagenEnArchivos(List<File> archivos) {
        return archivos.stream().anyMatch(this::esArchivoImagen);
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen del Producto");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("PNG", "*.png"),
            new FileChooser.ExtensionFilter("JPG", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        Stage stage = (Stage) txtNombre.getScene().getWindow();
        File archivoSeleccionado = fileChooser.showOpenDialog(stage);

        if (archivoSeleccionado != null) {
            cargarImagen(archivoSeleccionado);
        }
    }

    @FXML
    private void eliminarImagen() {
        imgPreview.setImage(null);
        lblImagenInfo.setText("Arrastra una imagen aquí o usa el botón para seleccionar");
        btnEliminarImagen.setVisible(false);
        rutaImagenTemporal = null;
    }

    private void cargarImagen(File archivo) {
        try {
            // Validar que sea una imagen
            if (!esArchivoImagen(archivo)) {
                AlertUtils.showWarning("Archivo no válido", "El archivo seleccionado no es una imagen válida.");
                return;
            }

            // Validar tamaño del archivo (máximo 10MB)
            if (archivo.length() > 10 * 1024 * 1024) {
                AlertUtils.showWarning("Archivo muy grande", "La imagen no puede ser mayor a 10MB.");
                return;
            }

            Image image = new Image(archivo.toURI().toString());
            imgPreview.setImage(image);
            lblImagenInfo.setText("Imagen cargada: " + archivo.getName());
            btnEliminarImagen.setVisible(true);

            // Copiar imagen a directorio temporal de la aplicación
            rutaImagenTemporal = copiarImagenTemporal(archivo);

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error cargando imagen: " + e.getMessage());
        }
    }

    private boolean esArchivoImagen(File archivo) {
        String nombre = archivo.getName().toLowerCase();
        return nombre.endsWith(".png") || nombre.endsWith(".jpg") ||
               nombre.endsWith(".jpeg") || nombre.endsWith(".gif") ||
               nombre.endsWith(".bmp");
    }

    private String copiarImagenTemporal(File archivoOriginal) {
        try {
            // Crear directorio de imágenes si no existe
            Path directorioImagenes = Paths.get("imagenes", "productos");
            Files.createDirectories(directorioImagenes);

            // Generar nombre único para la imagen
            String extension = obtenerExtension(archivoOriginal.getName());
            String nombreUnico = "IMG_" + System.currentTimeMillis() + extension;
            Path archivoDestino = directorioImagenes.resolve(nombreUnico);

            // Copiar archivo
            Files.copy(archivoOriginal.toPath(), archivoDestino, StandardCopyOption.REPLACE_EXISTING);

            return archivoDestino.toString();

        } catch (IOException e) {
            AlertUtils.showError("Error", "Error copiando imagen: " + e.getMessage());
            return null;
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        int puntoIndex = nombreArchivo.lastIndexOf('.');
        if (puntoIndex != -1 && puntoIndex < nombreArchivo.length() - 1) {
            return nombreArchivo.substring(puntoIndex);
        }
        return ".png"; // Extensión por defecto
    }
}
