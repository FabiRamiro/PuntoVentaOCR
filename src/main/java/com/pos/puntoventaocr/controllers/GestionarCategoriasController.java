package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.CategoriaDAO;
import com.pos.puntoventaocr.models.Categoria;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;

public class GestionarCategoriasController {
    
    @FXML private Button btnNueva;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnActualizar;
    @FXML private Button btnActivar;
    @FXML private CheckBox chkMostrarInactivas;

    @FXML private TableView<Categoria> tablaCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colDescripcion;
    @FXML private TableColumn<Categoria, Integer> colProductos;
    @FXML private TableColumn<Categoria, String> colEstado;
    
    @FXML private Label lblTotalCategorias;
    
    private CategoriaDAO categoriaDAO;
    private ObservableList<Categoria> listaCategorias;

    @FXML
    public void initialize() {
        categoriaDAO = new CategoriaDAO();
        listaCategorias = FXCollections.observableArrayList();
        
        configurarTabla();
        configurarEventos();
        cargarDatos();
    }
    
    private void configurarTabla() {
        // Configurar columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colProductos.setCellValueFactory(new PropertyValueFactory<>("cantidadProductos"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        // Aplicar estilos condicionales
        colEstado.setCellFactory(column -> new TableCell<Categoria, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("ACTIVO".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tablaCategorias.setItems(listaCategorias);
        
        // Listener para habilitar/deshabilitar botones
        tablaCategorias.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean itemSelected = newSelection != null;
                btnEditar.setDisable(!itemSelected);
                btnEliminar.setDisable(!itemSelected);

                // Configurar botón activar/desactivar
                if (newSelection != null) {
                    boolean esActivo = newSelection.isActivo();
                    btnActivar.setDisable(false);
                    btnActivar.setText(esActivo ? "Desactivar" : "Activar");
                } else {
                    btnActivar.setDisable(true);
                    btnActivar.setText("Activar");
                }
            });
    }

    private void configurarEventos() {
        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);
        btnActivar.setDisable(true);

        // Configurar checkbox para mostrar inactivas
        if (chkMostrarInactivas != null) {
            chkMostrarInactivas.selectedProperty().addListener((obs, oldVal, newVal) -> {
                cargarDatos();
            });
        }
    }

    @FXML
    private void nuevaCategoria() {
        try {
            Dialog<Categoria> dialog = crearDialogoCategoria("Nueva Categoría", null);
            Optional<Categoria> resultado = dialog.showAndWait();

            if (resultado.isPresent()) {
                Categoria categoria = resultado.get();
                categoria.setEstado("ACTIVO"); // Asegurar que nueva categoría esté activa

                if (categoriaDAO.crear(categoria)) {
                    AlertUtils.showSuccess("Éxito", "Categoría creada correctamente");
                    cargarDatos();
                } else {
                    AlertUtils.showError("Error", "No se pudo crear la categoría");
                }
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al crear categoría: " + e.getMessage());
        }
    }

    @FXML
    private void editarCategoria() {
        Categoria categoriaSeleccionada = tablaCategorias.getSelectionModel().getSelectedItem();
        if (categoriaSeleccionada == null) {
            AlertUtils.showWarning("Advertencia", "Seleccione una categoría para editar");
            return;
        }

        try {
            Dialog<Categoria> dialog = crearDialogoCategoria("Editar Categoría", categoriaSeleccionada);
            Optional<Categoria> resultado = dialog.showAndWait();

            if (resultado.isPresent()) {
                Categoria categoria = resultado.get();
                // Preservar el estado original durante la edición
                categoria.setEstado(categoriaSeleccionada.getEstado());
                categoria.setIdCategoria(categoriaSeleccionada.getIdCategoria());

                if (categoriaDAO.actualizar(categoria)) {
                    AlertUtils.showSuccess("Éxito", "Categoría actualizada correctamente");
                    cargarDatos();
                } else {
                    AlertUtils.showError("Error", "No se pudo actualizar la categoría");
                }
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al editar categoría: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarCategoria() {
        Categoria categoriaSeleccionada = tablaCategorias.getSelectionModel().getSelectedItem();
        if (categoriaSeleccionada == null) {
            AlertUtils.showWarning("Advertencia", "Seleccione una categoría para eliminar");
            return;
        }

        try {
            // Verificar si tiene productos asociados
            if (categoriaSeleccionada.getCantidadProductos() > 0) {
                AlertUtils.showWarning("Advertencia",
                    "No se puede eliminar la categoría porque tiene " +
                    categoriaSeleccionada.getCantidadProductos() + " productos asociados");
                return;
            }

            Optional<ButtonType> confirmacion = AlertUtils.showConfirmation(
                "Confirmar Eliminación",
                "¿Está seguro de eliminar la categoría '" + categoriaSeleccionada.getNombre() + "'?"
            );

            if (confirmacion.isPresent() && confirmacion.get() == ButtonType.OK) {
                if (categoriaDAO.eliminar(categoriaSeleccionada.getIdCategoria())) {
                    AlertUtils.showSuccess("Éxito", "Categoría eliminada correctamente");
                    cargarDatos();
                } else {
                    AlertUtils.showError("Error", "No se pudo eliminar la categoría");
                }
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al eliminar categoría: " + e.getMessage());
        }
    }

    @FXML
    private void activarDesactivarCategoria() {
        Categoria categoriaSeleccionada = tablaCategorias.getSelectionModel().getSelectedItem();
        if (categoriaSeleccionada == null) {
            AlertUtils.showWarning("Advertencia", "Seleccione una categoría");
            return;
        }

        try {
            boolean esActivo = categoriaSeleccionada.isActivo();
            String accion = esActivo ? "desactivar" : "activar";
            String nuevoEstado = esActivo ? "INACTIVO" : "ACTIVO";

            Optional<ButtonType> confirmacion = AlertUtils.showConfirmation(
                "Confirmar " + (esActivo ? "Desactivación" : "Activación"),
                "¿Está seguro de " + accion + " la categoría '" + categoriaSeleccionada.getNombre() + "'?"
            );

            if (confirmacion.isPresent() && confirmacion.get() == ButtonType.OK) {
                categoriaSeleccionada.setEstado(nuevoEstado);

                if (categoriaDAO.actualizar(categoriaSeleccionada)) {
                    AlertUtils.showSuccess("Éxito",
                        "Categoría " + (esActivo ? "desactivada" : "activada") + " correctamente");
                    cargarDatos();
                } else {
                    AlertUtils.showError("Error", "No se pudo " + accion + " la categoría");
                }
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al cambiar estado de categoría: " + e.getMessage());
        }
    }

    @FXML
    private void actualizarTabla() {
        cargarDatos();
    }

    private void cargarDatos() {
        try {
            List<Categoria> categorias;

            // Verificar si se deben mostrar las inactivas
            boolean mostrarInactivas = chkMostrarInactivas != null && chkMostrarInactivas.isSelected();

            if (mostrarInactivas) {
                categorias = categoriaDAO.obtenerTodas(); // Incluye activas e inactivas
            } else {
                categorias = categoriaDAO.obtenerActivas(); // Solo activas
            }

            listaCategorias.clear();
            listaCategorias.addAll(categorias);

            actualizarContadores();

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al cargar categorías: " + e.getMessage());
        }
    }

    private void actualizarContadores() {
        long activas = listaCategorias.stream().filter(Categoria::isActivo).count();
        long inactivas = listaCategorias.size() - activas;

        String texto = String.format("Total: %d categorías (%d activas, %d inactivas)",
                                   listaCategorias.size(), activas, inactivas);
        lblTotalCategorias.setText(texto);
    }

    private Dialog<Categoria> crearDialogoCategoria(String titulo, Categoria categoria) {
        Dialog<Categoria> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);

        // Configurar botones
        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        // Crear formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtNombre = new TextField();
        TextArea txtDescripcion = new TextArea();
        txtDescripcion.setPrefRowCount(3);

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(txtDescripcion, 1, 1);

        // Si es edición, cargar datos existentes
        if (categoria != null) {
            txtNombre.setText(categoria.getNombre());
            txtDescripcion.setText(categoria.getDescripcion());
        }

        dialog.getDialogPane().setContent(grid);

        // Habilitar/deshabilitar botón guardar
        Button btnGuardarNode = (Button) dialog.getDialogPane().lookupButton(btnGuardar);
        btnGuardarNode.setDisable(true);

        txtNombre.textProperty().addListener((observable, oldValue, newValue) -> {
            btnGuardarNode.setDisable(newValue.trim().isEmpty());
        });

        // Foco inicial
        Platform.runLater(() -> txtNombre.requestFocus());

        // Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                Categoria nuevaCategoria = categoria != null ? categoria : new Categoria();
                nuevaCategoria.setNombre(txtNombre.getText().trim());
                nuevaCategoria.setDescripcion(txtDescripcion.getText().trim());

                // Si es nueva categoría, establecer usuario creador
                if (categoria == null) {
                    try {
                        Integer idUsuario = SessionManager.getInstance().getCurrentUserId();
                        nuevaCategoria.setCreadoPor(idUsuario);
                    } catch (Exception e) {
                        System.err.println("Error obteniendo usuario actual: " + e.getMessage());
                    }
                } else {
                    // Si es edición, establecer usuario modificador
                    try {
                        Integer idUsuario = SessionManager.getInstance().getCurrentUserId();
                        nuevaCategoria.setModificadoPor(idUsuario);
                    } catch (Exception e) {
                        System.err.println("Error obteniendo usuario actual: " + e.getMessage());
                    }
                }

                return nuevaCategoria;
            }
            return null;
        });

        return dialog;
    }

    // Método para refrescar desde otros controladores
    public void refrescar() {
        cargarDatos();
    }
}

