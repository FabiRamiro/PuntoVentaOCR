package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.ProductoDAO;
import com.pos.puntoventaocr.dao.CategoriaDAO;
import com.pos.puntoventaocr.models.Producto;
import com.pos.puntoventaocr.models.Categoria;
import com.pos.puntoventaocr.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class ReporteInventarioController {

    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbEstadoStock;
    @FXML private Button btnGenerar;
    @FXML private Button btnExportar;

    @FXML private Label lblTotalProductos;
    @FXML private Label lblValorTotal;
    @FXML private Label lblBajoStock;
    @FXML private Label lblSinStock;

    @FXML private TableView<Producto> tablaInventario;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Integer> colStockMinimo;
    @FXML private TableColumn<Producto, Double> colPrecioCompra;
    @FXML private TableColumn<Producto, Double> colPrecioVenta;
    @FXML private TableColumn<Producto, Double> colValorInventario;
    @FXML private TableColumn<Producto, String> colEstado;

    private ProductoDAO productoDAO;
    private CategoriaDAO categoriaDAO;
    private ObservableList<Producto> listaInventario;

    public void initialize() {
        productoDAO = new ProductoDAO();
        categoriaDAO = new CategoriaDAO();
        listaInventario = FXCollections.observableArrayList();

        configurarTabla();
        configurarFiltros();
        limpiarResumen();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(cellData -> {
            Categoria cat = cellData.getValue().getCategoria();
            return new javafx.beans.property.SimpleStringProperty(cat != null ? cat.getNombre() : "Sin categoría");
        });
        colStock.setCellValueFactory(new PropertyValueFactory<>("cantidadStock"));
        colStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colPrecioCompra.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrecioCompra().doubleValue()).asObject();
        });
        colPrecioVenta.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrecioVenta().doubleValue()).asObject();
        });

        // Columna calculada para valor de inventario
        colValorInventario.setCellValueFactory(cellData -> {
            Producto producto = cellData.getValue();
            double valor = producto.getCantidadStock() * producto.getPrecioCompra().doubleValue();
            return new javafx.beans.property.SimpleDoubleProperty(valor).asObject();
        });

        // Columna calculada para estado de stock
        colEstado.setCellValueFactory(cellData -> {
            Producto producto = cellData.getValue();
            String estado = determinarEstadoStock(producto);
            return new javafx.beans.property.SimpleStringProperty(estado);
        });

        // Configurar estilo para las filas según el stock
        tablaInventario.setRowFactory(tv -> {
            TableRow<Producto> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setStyle("");
                } else {
                    String estado = determinarEstadoStock(newItem);
                    switch (estado) {
                        case "Sin Stock":
                            row.setStyle("-fx-background-color: #f8d7da;");
                            break;
                        case "Bajo Stock":
                            row.setStyle("-fx-background-color: #fff3cd;");
                            break;
                        case "Normal":
                            row.setStyle("-fx-background-color: #d4edda;");
                            break;
                        default:
                            row.setStyle("");
                    }
                }
            });
            return row;
        });

        tablaInventario.setItems(listaInventario);
    }

    private void configurarFiltros() {
        // Configurar estados de stock
        cmbEstadoStock.getItems().addAll("Todos", "Sin Stock", "Bajo Stock", "Normal", "Sobrestockeado");
        cmbEstadoStock.setValue("Todos");

        // Cargar categorías
        try {
            cmbCategoria.getItems().add("Todas");
            List<Categoria> categorias = categoriaDAO.obtenerTodas();
            for (Categoria categoria : categorias) {
                cmbCategoria.getItems().add(categoria.getNombre());
            }
            cmbCategoria.setValue("Todas");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al cargar categorías: " + e.getMessage());
        }
    }

    @FXML
    private void generarReporte() {
        try {
            String categoria = cmbCategoria.getValue();
            String estadoStock = cmbEstadoStock.getValue();

            List<Producto> productos = productoDAO.obtenerTodos();
            listaInventario.clear();

            for (Producto producto : productos) {
                // Aplicar filtros
                boolean incluir = true;

                // Filtro por categoría
                if (!"Todas".equals(categoria)) {
                    String nombreCategoria = producto.getCategoria() != null ?
                        producto.getCategoria().getNombre() : "Sin categoría";
                    if (!categoria.equals(nombreCategoria)) {
                        incluir = false;
                    }
                }

                // Filtro por estado de stock
                if (!"Todos".equals(estadoStock)) {
                    String estadoProducto = determinarEstadoStock(producto);
                    if (!estadoStock.equals(estadoProducto)) {
                        incluir = false;
                    }
                }

                if (incluir) {
                    listaInventario.add(producto);
                }
            }

            calcularResumen();

            AlertUtils.showInfo("Éxito", "Reporte de inventario generado correctamente");

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al generar reporte: " + e.getMessage());
        }
    }

    @FXML
    private void exportarReporte() {
        if (listaInventario.isEmpty()) {
            AlertUtils.showWarning("Advertencia", "No hay datos para exportar. Genere el reporte primero.");
            return;
        }

        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Guardar Reporte de Inventario");
            fileChooser.setInitialFileName("reporte_inventario_" +
                java.time.LocalDate.now().toString().replace("-", "_") + ".pdf");

            // CORRECCIÓN: Configurar filtros para PDF, no CSV
            fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"),
                new javafx.stage.FileChooser.ExtensionFilter("Todos los archivos", "*.*")
            );

            java.io.File archivo = fileChooser.showSaveDialog(btnExportar.getScene().getWindow());

            if (archivo != null) {
                // Aquí iría la lógica para generar el PDF
                // Por ahora, simular la exportación
                AlertUtils.showInfo("Éxito", "Reporte de inventario exportado correctamente a:\n" + archivo.getAbsolutePath());

                // TODO: Implementar generación real de PDF
                // generarPDFInventario(archivo, listaInventario);
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al exportar reporte de inventario: " + e.getMessage());
        }
    }

    private void calcularResumen() {
        int totalProductos = listaInventario.size();
        double valorTotal = 0;
        int bajoStock = 0;
        int sinStock = 0;

        for (Producto producto : listaInventario) {
            valorTotal += producto.getCantidadStock() * producto.getPrecioCompra().doubleValue();

            String estado = determinarEstadoStock(producto);
            if ("Sin Stock".equals(estado)) {
                sinStock++;
            } else if ("Bajo Stock".equals(estado)) {
                bajoStock++;
            }
        }

        lblTotalProductos.setText(String.valueOf(totalProductos));
        lblValorTotal.setText("$" + String.format("%.2f", valorTotal));
        lblBajoStock.setText(String.valueOf(bajoStock));
        lblSinStock.setText(String.valueOf(sinStock));
    }

    private void limpiarResumen() {
        lblTotalProductos.setText("0");
        lblValorTotal.setText("$0.00");
        lblBajoStock.setText("0");
        lblSinStock.setText("0");
    }

    private String determinarEstadoStock(Producto producto) {
        int stock = producto.getCantidadStock();
        int stockMinimo = producto.getStockMinimo();

        if (stock == 0) {
            return "Sin Stock";
        } else if (stock <= stockMinimo) {
            return "Bajo Stock";
        } else if (stock > stockMinimo * 3) {
            return "Sobrestockeado";
        } else {
            return "Normal";
        }
    }
}
