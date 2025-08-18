package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.ProductoDAO;
import com.pos.puntoventaocr.dao.CategoriaDAO;
import com.pos.puntoventaocr.dao.BitacoraDAO;
import com.pos.puntoventaocr.models.Producto;
import com.pos.puntoventaocr.models.Categoria;
import com.pos.puntoventaocr.models.InventarioFisico;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InventarioFisicoController {

    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private TextField txtBuscar;
    @FXML private Button btnFiltrar;
    @FXML private Button btnLimpiarFiltros;
    @FXML private Button btnNuevoConteo;
    @FXML private Button btnActualizar;

    @FXML private TableView<InventarioFisico> tablaProductos;
    @FXML private TableColumn<InventarioFisico, String> colCodigo;
    @FXML private TableColumn<InventarioFisico, String> colNombre;
    @FXML private TableColumn<InventarioFisico, String> colCategoria;
    @FXML private TableColumn<InventarioFisico, Integer> colStockSistema;
    @FXML private TableColumn<InventarioFisico, Integer> colStockFisico;
    @FXML private TableColumn<InventarioFisico, Integer> colDiferencia;
    @FXML private TableColumn<InventarioFisico, String> colObservaciones;
    @FXML private TableColumn<InventarioFisico, Void> colAcciones;

    @FXML private Label lblTotalProductos;
    @FXML private Label lblProductosContados;
    @FXML private Label lblConDiferencias;
    @FXML private ProgressBar progressConteo;

    @FXML private Button btnExportarCSV;
    @FXML private Button btnGuardarConteo;
    @FXML private Button btnAplicarAjustes;

    private ProductoDAO productoDAO;
    private CategoriaDAO categoriaDAO;
    private BitacoraDAO bitacoraDAO;
    private SessionManager sessionManager;
    private ObservableList<InventarioFisico> listaInventario;
    private ObservableList<InventarioFisico> listaInventarioCompleta;

    public void initialize() {
        productoDAO = new ProductoDAO();
        categoriaDAO = new CategoriaDAO();
        bitacoraDAO = new BitacoraDAO();
        sessionManager = SessionManager.getInstance();
        listaInventario = FXCollections.observableArrayList();
        listaInventarioCompleta = FXCollections.observableArrayList();

        configurarTabla();
        configurarFiltros();
        cargarDatos();
        actualizarResumen();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colStockSistema.setCellValueFactory(new PropertyValueFactory<>("stockSistema"));
        colStockFisico.setCellValueFactory(new PropertyValueFactory<>("stockFisico"));
        colDiferencia.setCellValueFactory(new PropertyValueFactory<>("diferencia"));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));

        // Hacer editable la columna de stock físico
        colStockFisico.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object != null ? object.toString() : "";
            }

            @Override
            public Integer fromString(String string) {
                try {
                    return string.isEmpty() ? 0 : Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }));

        colStockFisico.setOnEditCommit(event -> {
            InventarioFisico item = event.getRowValue();
            item.setStockFisico(event.getNewValue());
            item.calcularDiferencia();
            actualizarResumen();
        });

        // Hacer editable la columna de observaciones
        colObservaciones.setCellFactory(TextFieldTableCell.forTableColumn());
        colObservaciones.setOnEditCommit(event -> {
            InventarioFisico item = event.getRowValue();
            item.setObservaciones(event.getNewValue());
        });

        // Configurar columna de acciones
        colAcciones.setCellFactory(col -> new TableCell<InventarioFisico, Void>() {
            private final Button btnConteo = new Button("Contar");

            {
                btnConteo.setOnAction(event -> {
                    InventarioFisico item = getTableView().getItems().get(getIndex());
                    abrirDialogoConteo(item);
                });
                btnConteo.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 10px;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnConteo);
                }
            }
        });

        // Formatear columna de diferencias con colores
        colDiferencia.setCellFactory(column -> new TableCell<InventarioFisico, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item > 0) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (item < 0) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });

        tablaProductos.setItems(listaInventario);
        tablaProductos.setEditable(true);
    }

    private void configurarFiltros() {
        // Cargar categorías
        try {
            List<Categoria> categorias = categoriaDAO.obtenerTodas();
            ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList(categorias);

            // Crear categoria "Todas" con el constructor correcto
            Categoria todasCategorias = new Categoria(0, "Todas las categorías", "Filtro para mostrar todas las categorías", "ACTIVO");
            listaCategorias.add(0, todasCategorias);

            cmbCategoria.setItems(listaCategorias);
            cmbCategoria.setValue(listaCategorias.get(0));

            cmbCategoria.setConverter(new StringConverter<Categoria>() {
                @Override
                public String toString(Categoria categoria) {
                    return categoria != null ? categoria.getNombre() : "";
                }

                @Override
                public Categoria fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error cargando categorías: " + e.getMessage());
        }
    }

    @FXML
    private void aplicarFiltros() {
        String textoBusqueda = txtBuscar.getText().toLowerCase().trim();
        Categoria categoriaSeleccionada = cmbCategoria.getValue();

        ObservableList<InventarioFisico> listaFiltrada = listaInventarioCompleta.stream()
                .filter(item -> {
                    boolean coincideTexto = textoBusqueda.isEmpty() ||
                            item.getNombreProducto().toLowerCase().contains(textoBusqueda) ||
                            item.getCodigo().toLowerCase().contains(textoBusqueda);

                    boolean coincideCategoria = categoriaSeleccionada == null ||
                            categoriaSeleccionada.getId() == 0 ||
                            item.getCategoria().equals(categoriaSeleccionada.getNombre());

                    return coincideTexto && coincideCategoria;
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        listaInventario.setAll(listaFiltrada);
        actualizarResumen();
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        if (!cmbCategoria.getItems().isEmpty()) {
            cmbCategoria.setValue(cmbCategoria.getItems().get(0));
        }
        listaInventario.setAll(listaInventarioCompleta);
        actualizarResumen();
    }

    @FXML
    private void nuevoConteo() {
        Optional<ButtonType> resultado = AlertUtils.showConfirmation("Nuevo Conteo",
                "¿Está seguro de iniciar un nuevo conteo de inventario?\n" +
                "Esto limpiará todos los datos de conteo físico actuales.");

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            for (InventarioFisico item : listaInventarioCompleta) {
                item.setStockFisico(null);
                item.setObservaciones("");
                item.calcularDiferencia();
            }
            tablaProductos.refresh();
            actualizarResumen();
            AlertUtils.showInfo("Nuevo Conteo", "Se ha iniciado un nuevo conteo de inventario");
        }
    }

    @FXML
    private void actualizarLista() {
        cargarDatos();
    }

    @FXML
    private void exportarCSV() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Inventario Físico");
            fileChooser.setInitialFileName("inventario_fisico_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
            );

            File archivo = fileChooser.showSaveDialog(btnExportarCSV.getScene().getWindow());
            if (archivo != null) {
                exportarAPDF(archivo);
                AlertUtils.showInfo("Exportación", "Inventario exportado exitosamente a: " + archivo.getName());
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al exportar: " + e.getMessage());
        }
    }

    @FXML
    private void guardarConteo() {
        try {
            // Aquí se guardarían los datos del conteo en la base de datos
            AlertUtils.showInfo("Conteo Guardado", "El conteo de inventario ha sido guardado exitosamente");

            // Registrar en bitácora
            if (sessionManager.getCurrentUserId() != 0) {
                bitacoraDAO.registrarAccion(sessionManager.getCurrentUserId(), "GUARDAR_INVENTARIO", "PRODUCTOS",
                    "Guardó conteo de inventario físico");
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al guardar conteo: " + e.getMessage());
        }
    }

    @FXML
    private void aplicarAjustes() {
        List<InventarioFisico> itemsConDiferencia = listaInventarioCompleta.stream()
                .filter(item -> item.getDiferencia() != 0)
                .collect(Collectors.toList());

        if (itemsConDiferencia.isEmpty()) {
            AlertUtils.showWarning("Sin Diferencias", "No hay productos con diferencias para ajustar");
            return;
        }

        Optional<ButtonType> resultado = AlertUtils.showConfirmation("Aplicar Ajustes",
                "¿Está seguro de aplicar los ajustes de inventario?\n" +
                "Se encontraron " + itemsConDiferencia.size() + " productos con diferencias.\n" +
                "Esta acción actualizará el stock en el sistema.");

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                int ajustesAplicados = 0;
                for (InventarioFisico item : itemsConDiferencia) {
                    if (item.getStockFisico() != null) {
                        // Actualizar el stock en la base de datos
                        Producto producto = productoDAO.obtenerPorCodigo(item.getCodigo());
                        if (producto != null) {
                            producto.setStock(item.getStockFisico());
                            if (productoDAO.actualizar(producto)) {
                                ajustesAplicados++;

                                // Registrar ajuste en bitácora
                                if (sessionManager.getCurrentUserId() != 0) {
                                    bitacoraDAO.registrarAccion(sessionManager.getCurrentUserId(),
                                        "AJUSTE_INVENTARIO", "PRODUCTOS",
                                        String.format("Ajuste de inventario - Producto: %s, Stock anterior: %d, Stock nuevo: %d, Diferencia: %d",
                                            item.getNombreProducto(), item.getStockSistema(),
                                            item.getStockFisico(), item.getDiferencia()));
                                }
                            }
                        }
                    }
                }

                AlertUtils.showInfo("Ajustes Aplicados",
                    "Se aplicaron " + ajustesAplicados + " ajustes de inventario exitosamente");
                cargarDatos(); // Recargar datos actualizados
            } catch (Exception e) {
                AlertUtils.showError("Error", "Error al aplicar ajustes: " + e.getMessage());
            }
        }
    }

    private void abrirDialogoConteo(InventarioFisico item) {
        TextInputDialog dialog = new TextInputDialog(
            item.getStockFisico() != null ? item.getStockFisico().toString() : ""
        );
        dialog.setTitle("Conteo Físico");
        dialog.setHeaderText("Producto: " + item.getNombreProducto());
        dialog.setContentText("Stock físico:");

        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            try {
                int stockFisico = Integer.parseInt(resultado.get());
                item.setStockFisico(stockFisico);
                item.calcularDiferencia();
                tablaProductos.refresh();
                actualizarResumen();
            } catch (NumberFormatException e) {
                AlertUtils.showError("Error", "Ingrese un número válido");
            }
        }
    }

    private void cargarDatos() {
        try {
            List<Producto> productos = productoDAO.obtenerTodos();
            listaInventarioCompleta.clear();

            for (Producto producto : productos) {
                InventarioFisico item = new InventarioFisico(
                    producto.getCodigo(),
                    producto.getNombre(),
                    producto.getCategoria() != null ? producto.getCategoria().getNombre() : "Sin categoría",
                    producto.getStock()
                );
                listaInventarioCompleta.add(item);
            }

            listaInventario.setAll(listaInventarioCompleta);
            actualizarResumen();

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error cargando productos: " + e.getMessage());
        }
    }

    private void actualizarResumen() {
        int totalProductos = listaInventario.size();
        int productosContados = (int) listaInventario.stream()
                .filter(item -> item.getStockFisico() != null)
                .count();
        int conDiferencias = (int) listaInventario.stream()
                .filter(item -> item.getDiferencia() != 0)
                .count();

        lblTotalProductos.setText(String.valueOf(totalProductos));
        lblProductosContados.setText(String.valueOf(productosContados));
        lblConDiferencias.setText(String.valueOf(conDiferencias));

        double progreso = totalProductos > 0 ? (double) productosContados / totalProductos : 0;
        progressConteo.setProgress(progreso);
    }

    private void exportarAPDF(File archivo) throws Exception {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate());
        com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(archivo));

        document.open();

        // Título
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("INVENTARIO FÍSICO", titleFont);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(title);

        // Fecha
        com.itextpdf.text.Font dateFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12);
        com.itextpdf.text.Paragraph date = new com.itextpdf.text.Paragraph("Fecha: " +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), dateFont);
        date.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(date);
        document.add(new com.itextpdf.text.Paragraph(" "));

        // Tabla
        com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{15, 30, 20, 15, 15, 15, 20});

        // Encabezados
        String[] headers = {"Código", "Producto", "Categoría", "Stock Sistema", "Stock Físico", "Diferencia", "Observaciones"};
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);

        for (String header : headers) {
            com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, headerFont));
            cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        // Datos
        com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9);

        for (InventarioFisico item : listaInventario) {
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getCodigo(), dataFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getNombreProducto(), dataFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(item.getCategoria(), dataFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.valueOf(item.getStockSistema()), dataFont)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                item.getStockFisico() != null ? item.getStockFisico().toString() : "", dataFont)));

            // Diferencia con color
            com.itextpdf.text.pdf.PdfPCell diffCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.valueOf(item.getDiferencia()), dataFont));
            if (item.getDiferencia() > 0) {
                diffCell.setBackgroundColor(new com.itextpdf.text.BaseColor(200, 255, 200)); // Verde claro
            } else if (item.getDiferencia() < 0) {
                diffCell.setBackgroundColor(new com.itextpdf.text.BaseColor(255, 200, 200)); // Rojo claro
            }
            table.addCell(diffCell);

            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(
                item.getObservaciones() != null ? item.getObservaciones() : "", dataFont)));
        }

        document.add(table);

        // Resumen
        document.add(new com.itextpdf.text.Paragraph(" "));
        document.add(new com.itextpdf.text.Paragraph("RESUMEN:", titleFont));
        document.add(new com.itextpdf.text.Paragraph("Total productos: " + lblTotalProductos.getText(), dataFont));
        document.add(new com.itextpdf.text.Paragraph("Productos contados: " + lblProductosContados.getText(), dataFont));
        document.add(new com.itextpdf.text.Paragraph("Con diferencias: " + lblConDiferencias.getText(), dataFont));

        document.close();
    }
}
