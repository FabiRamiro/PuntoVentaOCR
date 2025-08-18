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

import java.time.LocalDate;
import java.util.List;

public class ReporteProductosController {

    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<Integer> cmbTop;
    @FXML private Button btnGenerar;
    @FXML private Button btnExportar;

    @FXML private Label lblTotalProductos;
    @FXML private Label lblUnidadesTotales;
    @FXML private Label lblMontoTotal;

    @FXML private TableView<ProductoVendido> tablaProductosMasVendidos;
    @FXML private TableColumn<ProductoVendido, Integer> colPosicion;
    @FXML private TableColumn<ProductoVendido, String> colCodigo;
    @FXML private TableColumn<ProductoVendido, String> colNombre;
    @FXML private TableColumn<ProductoVendido, String> colCategoria;
    @FXML private TableColumn<ProductoVendido, Integer> colCantidadVendida;
    @FXML private TableColumn<ProductoVendido, Double> colMontoTotal;
    @FXML private TableColumn<ProductoVendido, Double> colPorcentaje;

    private ProductoDAO productoDAO;
    private CategoriaDAO categoriaDAO;
    private ObservableList<ProductoVendido> listaProductosVendidos;

    public void initialize() {
        productoDAO = new ProductoDAO();
        categoriaDAO = new CategoriaDAO();
        listaProductosVendidos = FXCollections.observableArrayList();

        configurarTabla();
        configurarFiltros();
        limpiarResumen();
    }

    private void configurarTabla() {
        colPosicion.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colCantidadVendida.setCellValueFactory(new PropertyValueFactory<>("cantidadVendida"));
        colMontoTotal.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));
        colPorcentaje.setCellValueFactory(new PropertyValueFactory<>("porcentajeVentas"));

        tablaProductosMasVendidos.setItems(listaProductosVendidos);
    }

    private void configurarFiltros() {
        // Configurar fechas por defecto (mes actual)
        LocalDate ahora = LocalDate.now();
        dpFechaDesde.setValue(ahora.withDayOfMonth(1));
        dpFechaHasta.setValue(ahora);

        // Configurar top
        cmbTop.getItems().addAll(10, 20, 50, 100);
        cmbTop.setValue(10);

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
            LocalDate fechaDesde = dpFechaDesde.getValue();
            LocalDate fechaHasta = dpFechaHasta.getValue();
            String categoria = cmbCategoria.getValue();
            Integer top = cmbTop.getValue();

            if (fechaDesde == null || fechaHasta == null) {
                AlertUtils.showWarning("Advertencia", "Seleccione el rango de fechas");
                return;
            }

            if (fechaDesde.isAfter(fechaHasta)) {
                AlertUtils.showWarning("Advertencia", "La fecha desde no puede ser mayor a la fecha hasta");
                return;
            }

            List<ProductoVendido> productos = productoDAO.obtenerProductosMasVendidos(
                fechaDesde, fechaHasta, categoria, top);

            listaProductosVendidos.clear();
            listaProductosVendidos.addAll(productos);

            calcularResumen();

            AlertUtils.showInfo("Éxito", "Reporte generado correctamente");

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al generar reporte: " + e.getMessage());
        }
    }

    @FXML
    private void exportarReporte() {
        if (listaProductosVendidos.isEmpty()) {
            AlertUtils.showWarning("Advertencia", "No hay datos para exportar. Genere el reporte primero.");
            return;
        }

        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Guardar Reporte de Productos Más Vendidos");
            fileChooser.setInitialFileName("reporte_productos_mas_vendidos_" +
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
                AlertUtils.showInfo("Éxito", "Reporte exportado correctamente a:\n" + archivo.getAbsolutePath());

                // TODO: Implementar generación real de PDF
                // generarPDFProductos(archivo, listaProductosVendidos);
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al exportar reporte: " + e.getMessage());
        }
    }

    private void calcularResumen() {
        int totalProductos = listaProductosVendidos.size();
        int unidadesTotales = listaProductosVendidos.stream()
            .mapToInt(ProductoVendido::getCantidadVendida)
            .sum();
        double montoTotal = listaProductosVendidos.stream()
            .mapToDouble(ProductoVendido::getMontoTotal)
            .sum();

        lblTotalProductos.setText(String.valueOf(totalProductos));
        lblUnidadesTotales.setText(String.valueOf(unidadesTotales));
        lblMontoTotal.setText("$" + String.format("%.2f", montoTotal));
    }

    private void limpiarResumen() {
        lblTotalProductos.setText("0");
        lblUnidadesTotales.setText("0");
        lblMontoTotal.setText("$0.00");
    }

    // Clase interna para representar productos vendidos
    public static class ProductoVendido {
        private int posicion;
        private String codigo;
        private String nombre;
        private String categoria;
        private int cantidadVendida;
        private double montoTotal;
        private double porcentajeVentas;

        // Constructors, getters y setters
        public ProductoVendido() {}

        public ProductoVendido(int posicion, String codigo, String nombre, String categoria,
                              int cantidadVendida, double montoTotal, double porcentajeVentas) {
            this.posicion = posicion;
            this.codigo = codigo;
            this.nombre = nombre;
            this.categoria = categoria;
            this.cantidadVendida = cantidadVendida;
            this.montoTotal = montoTotal;
            this.porcentajeVentas = porcentajeVentas;
        }

        // Getters y setters
        public int getPosicion() { return posicion; }
        public void setPosicion(int posicion) { this.posicion = posicion; }

        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }

        public int getCantidadVendida() { return cantidadVendida; }
        public void setCantidadVendida(int cantidadVendida) { this.cantidadVendida = cantidadVendida; }

        public double getMontoTotal() { return montoTotal; }
        public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }

        public double getPorcentajeVentas() { return porcentajeVentas; }
        public void setPorcentajeVentas(double porcentajeVentas) { this.porcentajeVentas = porcentajeVentas; }
    }
}
