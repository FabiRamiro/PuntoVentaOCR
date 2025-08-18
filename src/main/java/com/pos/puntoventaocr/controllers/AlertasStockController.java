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

public class AlertasStockController {

    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbNivelAlerta;
    @FXML private Button btnActualizar;

    @FXML private TableView<Producto> tablaAlertas;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Integer> colStockActual;
    @FXML private TableColumn<Producto, Integer> colStockMinimo;
    @FXML private TableColumn<Producto, String> colNivelAlerta;
    @FXML private TableColumn<Producto, Void> colAcciones;

    @FXML private Label lblTotalAlertas;
    @FXML private Label lblCriticos;
    @FXML private Label lblBajos;

    private ProductoDAO productoDAO;
    private CategoriaDAO categoriaDAO;
    private ObservableList<Producto> listaAlertas;

    public void initialize() {
        productoDAO = new ProductoDAO();
        categoriaDAO = new CategoriaDAO();
        listaAlertas = FXCollections.observableArrayList();

        configurarTabla();
        configurarFiltros();
        cargarDatos();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("nombreCategoria"));
        colStockActual.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        // Configurar columna de nivel de alerta
        colNivelAlerta.setCellValueFactory(cellData -> {
            Producto producto = cellData.getValue();
            String nivel = determinarNivelAlerta(producto);
            return new javafx.beans.property.SimpleStringProperty(nivel);
        });

        // Configurar estilo para las filas según el nivel de alerta
        tablaAlertas.setRowFactory(tv -> {
            TableRow<Producto> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setStyle("");
                } else {
                    String nivel = determinarNivelAlerta(newItem);
                    switch (nivel) {
                        case "Crítico":
                            row.setStyle("-fx-background-color: #f8d7da;");
                            break;
                        case "Bajo":
                            row.setStyle("-fx-background-color: #fff3cd;");
                            break;
                        default:
                            row.setStyle("");
                    }
                }
            });
            return row;
        });

        tablaAlertas.setItems(listaAlertas);
    }

    private void configurarFiltros() {
        try {
            // Cargar categorías
            cmbCategoria.getItems().add("Todas");
            List<Categoria> categorias = categoriaDAO.obtenerTodas();
            for (Categoria categoria : categorias) {
                cmbCategoria.getItems().add(categoria.getNombre());
            }
            cmbCategoria.setValue("Todas");

            // Configurar niveles de alerta
            cmbNivelAlerta.getItems().addAll("Todos", "Crítico", "Bajo", "Normal");
            cmbNivelAlerta.setValue("Todos");

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al configurar filtros: " + e.getMessage());
        }
    }

    @FXML
    private void actualizarAlertas() {
        cargarDatos();
    }

    private void cargarDatos() {
        try {
            String categoriaFiltro = cmbCategoria.getValue();
            String nivelFiltro = cmbNivelAlerta.getValue();

            List<Producto> productos = productoDAO.obtenerProductosConBajoStock();
            listaAlertas.clear();

            for (Producto producto : productos) {
                // Aplicar filtros
                boolean incluir = true;

                if (!"Todas".equals(categoriaFiltro) &&
                    !categoriaFiltro.equals(producto.getNombreCategoria())) {
                    incluir = false;
                }

                if (!"Todos".equals(nivelFiltro)) {
                    String nivelProducto = determinarNivelAlerta(producto);
                    if (!nivelFiltro.equals(nivelProducto)) {
                        incluir = false;
                    }
                }

                if (incluir) {
                    listaAlertas.add(producto);
                }
            }

            actualizarEstadisticas();

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al cargar alertas de stock: " + e.getMessage());
        }
    }

    private void actualizarEstadisticas() {
        int totalAlertas = listaAlertas.size();
        int criticos = 0;
        int bajos = 0;

        for (Producto producto : listaAlertas) {
            String nivel = determinarNivelAlerta(producto);
            if ("Crítico".equals(nivel)) {
                criticos++;
            } else if ("Bajo".equals(nivel)) {
                bajos++;
            }
        }

        lblTotalAlertas.setText("Productos en alerta: " + totalAlertas);
        lblCriticos.setText("Críticos: " + criticos);
        lblBajos.setText("Bajos: " + bajos);
    }

    private String determinarNivelAlerta(Producto producto) {
        int stock = producto.getStock();
        int stockMinimo = producto.getStockMinimo();

        if (stock <= 0) {
            return "Crítico";
        } else if (stock <= stockMinimo) {
            return "Bajo";
        } else if (stock <= stockMinimo * 1.5) {
            return "Advertencia";
        } else {
            return "Normal";
        }
    }
}
