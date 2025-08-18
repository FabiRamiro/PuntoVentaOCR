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
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;

public class DuplicarProductoController {

    @FXML private Label lblProductoOriginal;
    @FXML private Label lblInfoOriginal;
    @FXML private TextField txtNuevoNombre;
    @FXML private TextField txtNuevoCodigo;
    @FXML private TextField txtNuevaMarca;
    @FXML private ComboBox<Categoria> cmbNuevaCategoria;
    @FXML private CheckBox chkCopiarPrecios;
    @FXML private CheckBox chkCopiarStock;
    @FXML private CheckBox chkCopiarDescripciones;
    @FXML private CheckBox chkCopiarImagen;
    @FXML private VBox panelStockManual;
    @FXML private TextField txtStockInicial;

    private Producto productoOriginal;
    private CategoriaDAO categoriaDAO;
    private ProductoDAO productoDAO;

    public void initialize() {
        categoriaDAO = new CategoriaDAO();
        productoDAO = new ProductoDAO();

        configurarControles();
        cargarCategorias();
    }

    private void configurarControles() {
        // Listener para mostrar/ocultar panel de stock manual
        chkCopiarStock.selectedProperty().addListener((obs, oldVal, newVal) -> {
            panelStockManual.setVisible(!newVal);
            panelStockManual.setManaged(!newVal);
        });

        // Validación numérica para stock inicial
        txtStockInicial.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtStockInicial.setText(oldVal);
            }
        });

        // Inicialmente ocultar panel de stock manual
        panelStockManual.setVisible(false);
        panelStockManual.setManaged(false);
    }

    private void cargarCategorias() {
        try {
            List<Categoria> categorias = categoriaDAO.obtenerActivas();
            cmbNuevaCategoria.setItems(FXCollections.observableArrayList(categorias));

            // Configurar display de categorías
            cmbNuevaCategoria.setCellFactory(listView -> new ListCell<Categoria>() {
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

            cmbNuevaCategoria.setButtonCell(new ListCell<Categoria>() {
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
            AlertUtils.showError("Error", "Error cargando categorías: " + e.getMessage());
        }
    }

    public void setProductoOriginal(Producto producto) {
        this.productoOriginal = producto;
        actualizarInfoProducto();
        precargarDatos();
    }

    private void actualizarInfoProducto() {
        if (productoOriginal != null) {
            lblProductoOriginal.setText(productoOriginal.getNombre());

            String info = String.format("Código: %s | Marca: %s | Precio: $%.2f | Stock: %d",
                productoOriginal.getCodigoBarras() != null ? productoOriginal.getCodigoBarras() : "N/A",
                productoOriginal.getMarca() != null ? productoOriginal.getMarca() : "N/A",
                productoOriginal.getPrecioVenta(),
                productoOriginal.getCantidadStock()
            );
            lblInfoOriginal.setText(info);
        }
    }

    private void precargarDatos() {
        if (productoOriginal != null) {
            // Precargar nombre con sufijo
            txtNuevoNombre.setText(productoOriginal.getNombre() + " - Copia");

            // Precargar marca
            txtNuevaMarca.setText(productoOriginal.getMarca());

            // Seleccionar la misma categoría
            if (productoOriginal.getCategoria() != null) {
                for (Categoria cat : cmbNuevaCategoria.getItems()) {
                    if (cat.getIdCategoria() == productoOriginal.getCategoria().getIdCategoria()) {
                        cmbNuevaCategoria.setValue(cat);
                        break;
                    }
                }
            }

            // Generar código sugerido
            if (productoOriginal.getCodigoBarras() != null && !productoOriginal.getCodigoBarras().isEmpty()) {
                String codigoSugerido = generarCodigoSugerido(productoOriginal.getCodigoBarras());
                txtNuevoCodigo.setPromptText("Sugerido: " + codigoSugerido);
            }
        }
    }

    private String generarCodigoSugerido(String codigoOriginal) {
        // Intentar agregar sufijo numérico
        for (int i = 1; i <= 999; i++) {
            String nuevoCodigo = codigoOriginal + "-" + String.format("%03d", i);
            if (!productoDAO.existeCodigoBarras(nuevoCodigo)) {
                return nuevoCodigo;
            }
        }
        return codigoOriginal + "-COPY";
    }

    public boolean validarDatos() {
        StringBuilder errores = new StringBuilder();

        if (productoOriginal == null) {
            errores.append("- No se ha seleccionado un producto original\n");
        }

        if (txtNuevoNombre.getText().trim().isEmpty()) {
            errores.append("- El nombre del nuevo producto es obligatorio\n");
        }

        if (cmbNuevaCategoria.getValue() == null) {
            errores.append("- Debe seleccionar una categoría\n");
        }

        // Validar código si se ingresó
        String nuevoCodigo = txtNuevoCodigo.getText().trim();
        if (!nuevoCodigo.isEmpty() && productoDAO.existeCodigoBarras(nuevoCodigo)) {
            errores.append("- El código de barras ya existe\n");
        }

        // Validar stock inicial si no se copia el stock
        if (!chkCopiarStock.isSelected()) {
            try {
                int stock = Integer.parseInt(txtStockInicial.getText());
                if (stock < 0) {
                    errores.append("- El stock inicial no puede ser negativo\n");
                }
            } catch (NumberFormatException e) {
                errores.append("- El stock inicial debe ser un número entero válido\n");
            }
        }

        if (errores.length() > 0) {
            AlertUtils.showError("Errores de validación", errores.toString());
            return false;
        }

        return true;
    }

    public Producto crearProductoDuplicado() {
        if (!validarDatos()) {
            return null;
        }

        try {
            Producto nuevoProducto = new Producto();

            // Datos básicos
            nuevoProducto.setNombre(txtNuevoNombre.getText().trim());
            nuevoProducto.setMarca(txtNuevaMarca.getText().trim());
            nuevoProducto.setCategoria(cmbNuevaCategoria.getValue());

            // Código de barras
            String nuevoCodigo = txtNuevoCodigo.getText().trim();
            if (nuevoCodigo.isEmpty()) {
                nuevoCodigo = generarCodigoSugerido(
                    productoOriginal.getCodigoBarras() != null ?
                    productoOriginal.getCodigoBarras() : "PROD"
                );
            }
            nuevoProducto.setCodigoBarras(nuevoCodigo);

            // Generar código interno basado en el código de barras
            nuevoProducto.setCodigoInterno(nuevoCodigo);

            // Copiar datos según las opciones seleccionadas
            if (chkCopiarPrecios.isSelected()) {
                nuevoProducto.setPrecioCompra(productoOriginal.getPrecioCompra());
                nuevoProducto.setPrecioVenta(productoOriginal.getPrecioVenta());
            } else {
                nuevoProducto.setPrecioCompra(BigDecimal.ZERO);
                nuevoProducto.setPrecioVenta(BigDecimal.ZERO);
            }

            if (chkCopiarStock.isSelected()) {
                nuevoProducto.setCantidadStock(productoOriginal.getCantidadStock());
            } else {
                nuevoProducto.setCantidadStock(Integer.parseInt(txtStockInicial.getText()));
            }

            if (chkCopiarDescripciones.isSelected()) {
                nuevoProducto.setDescripcionCorta(productoOriginal.getDescripcionCorta());
                nuevoProducto.setDescripcionLarga(productoOriginal.getDescripcionLarga());
            }

            if (chkCopiarImagen.isSelected()) {
                nuevoProducto.setRutaImagen(productoOriginal.getRutaImagen());
            }

            // Copiar otros datos del original
            nuevoProducto.setUnidadMedida(productoOriginal.getUnidadMedida());
            nuevoProducto.setStockMinimo(productoOriginal.getStockMinimo());
            nuevoProducto.setEstado("ACTIVO");
            nuevoProducto.setCreadoPor(SessionManager.getInstance().getUsuarioActual().getIdUsuario());

            return nuevoProducto;

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error creando producto duplicado: " + e.getMessage());
            return null;
        }
    }

    public String getResumenDuplicacion() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Producto duplicado:\n");
        resumen.append("- Nombre: ").append(txtNuevoNombre.getText()).append("\n");
        resumen.append("- Código: ").append(txtNuevoCodigo.getText().isEmpty() ? "Auto-generado" : txtNuevoCodigo.getText()).append("\n");
        resumen.append("- Categoría: ").append(cmbNuevaCategoria.getValue() != null ? cmbNuevaCategoria.getValue().getNombre() : "N/A").append("\n");
        resumen.append("\nDatos copiados:\n");

        if (chkCopiarPrecios.isSelected()) resumen.append("- Precios\n");
        if (chkCopiarStock.isSelected()) resumen.append("- Stock\n");
        if (chkCopiarDescripciones.isSelected()) resumen.append("- Descripciones\n");
        if (chkCopiarImagen.isSelected()) resumen.append("- Imagen\n");

        if (!chkCopiarStock.isSelected()) {
            resumen.append("\nStock inicial: ").append(txtStockInicial.getText());
        }

        return resumen.toString();
    }
}
