package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.RolDAO;
import com.pos.puntoventaocr.models.Rol;
import com.pos.puntoventaocr.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;

public class GestionarRolesController {

    @FXML private TextField txtNombreRol;
    @FXML private TextArea txtDescripcion;
    @FXML private CheckBox chkActivo;
    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnEliminar;

    // Permisos
    @FXML private CheckBox chkVentas;
    @FXML private CheckBox chkProductos;
    @FXML private CheckBox chkInventario;
    @FXML private CheckBox chkReportes;
    @FXML private CheckBox chkUsuarios;
    @FXML private CheckBox chkConfiguracion;
    @FXML private CheckBox chkOCR;
    @FXML private CheckBox chkDevoluciones;

    @FXML private TableView<Rol> tablaRoles;
    @FXML private TableColumn<Rol, String> colNombre;
    @FXML private TableColumn<Rol, String> colDescripcion;
    @FXML private TableColumn<Rol, String> colEstado;
    @FXML private TableColumn<Rol, String> colPermisos;

    private RolDAO rolDAO;
    private ObservableList<Rol> listaRoles;
    private Rol rolSeleccionado;

    public void initialize() {
        rolDAO = new RolDAO();
        listaRoles = FXCollections.observableArrayList();

        configurarTabla();
        configurarEventos();
        cargarRoles();
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreRol"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colEstado.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().isActivo() ? "Activo" : "Inactivo"
            )
        );
        colPermisos.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                obtenerResumenPermisos(cellData.getValue())
            )
        );

        // Formatear columna de estado con colores
        colEstado.setCellFactory(column -> new TableCell<Rol, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Activo".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tablaRoles.setItems(listaRoles);
    }

    private void configurarEventos() {
        // Listener para selección en tabla
        tablaRoles.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    cargarRolEnFormulario(newSelection);
                }
            }
        );
    }

    private String obtenerResumenPermisos(Rol rol) {
        StringBuilder permisos = new StringBuilder();

        if (rol.isPermisoVentas()) permisos.append("Ventas, ");
        if (rol.isPermisoProductos()) permisos.append("Productos, ");
        if (rol.isPermisoInventario()) permisos.append("Inventario, ");
        if (rol.isPermisoReportes()) permisos.append("Reportes, ");
        if (rol.isPermisoUsuarios()) permisos.append("Usuarios, ");
        if (rol.isPermisoConfiguracion()) permisos.append("Configuración, ");
        if (rol.isPermisoOCR()) permisos.append("OCR, ");
        if (rol.isPermisoDevoluciones()) permisos.append("Devoluciones, ");

        String resultado = permisos.toString();
        if (resultado.endsWith(", ")) {
            resultado = resultado.substring(0, resultado.length() - 2);
        }

        return resultado.isEmpty() ? "Sin permisos" : resultado;
    }

    @FXML
    private void guardarRol() {
        try {
            if (!validarFormulario()) {
                return;
            }

            Rol rol = rolSeleccionado != null ? rolSeleccionado : new Rol();

            rol.setNombreRol(txtNombreRol.getText().trim());
            rol.setDescripcion(txtDescripcion.getText().trim());
            rol.setActivo(chkActivo.isSelected());

            // Establecer permisos
            rol.setPermisoVentas(chkVentas.isSelected());
            rol.setPermisoProductos(chkProductos.isSelected());
            rol.setPermisoInventario(chkInventario.isSelected());
            rol.setPermisoReportes(chkReportes.isSelected());
            rol.setPermisoUsuarios(chkUsuarios.isSelected());
            rol.setPermisoConfiguracion(chkConfiguracion.isSelected());
            rol.setPermisoOCR(chkOCR.isSelected());
            rol.setPermisoDevoluciones(chkDevoluciones.isSelected());

            boolean resultado;
            if (rolSeleccionado == null) {
                resultado = rolDAO.crear(rol);
            } else {
                resultado = rolDAO.actualizar(rol);
            }

            if (resultado) {
                AlertUtils.showInfo("Éxito",
                    rolSeleccionado == null ? "Rol creado correctamente" : "Rol actualizado correctamente");
                limpiarFormulario();
                cargarRoles();
            } else {
                AlertUtils.showError("Error", "No se pudo guardar el rol");
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al guardar rol: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarRol() {
        if (rolSeleccionado == null) {
            AlertUtils.showWarning("Advertencia", "Seleccione un rol para eliminar");
            return;
        }

        Optional<ButtonType> resultado = AlertUtils.showConfirmation("Confirmar Eliminación",
            "¿Está seguro de eliminar el rol '" + rolSeleccionado.getNombreRol() + "'?");

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                if (rolDAO.eliminar(rolSeleccionado.getIdRol())) {
                    AlertUtils.showInfo("Éxito", "Rol eliminado correctamente");
                    limpiarFormulario();
                    cargarRoles();
                } else {
                    AlertUtils.showError("Error", "No se pudo eliminar el rol");
                }
            } catch (Exception e) {
                AlertUtils.showError("Error", "Error al eliminar rol: " + e.getMessage());
            }
        }
    }

    @FXML
    private void limpiarFormulario() {
        rolSeleccionado = null;

        txtNombreRol.clear();
        txtDescripcion.clear();
        chkActivo.setSelected(true);

        // Limpiar permisos
        chkVentas.setSelected(false);
        chkProductos.setSelected(false);
        chkInventario.setSelected(false);
        chkReportes.setSelected(false);
        chkUsuarios.setSelected(false);
        chkConfiguracion.setSelected(false);
        chkOCR.setSelected(false);
        chkDevoluciones.setSelected(false);

        btnEliminar.setDisable(true);
        tablaRoles.getSelectionModel().clearSelection();
    }

    private void cargarRolEnFormulario(Rol rol) {
        rolSeleccionado = rol;

        txtNombreRol.setText(rol.getNombreRol());
        txtDescripcion.setText(rol.getDescripcion());
        chkActivo.setSelected(rol.isActivo());

        // Cargar permisos
        chkVentas.setSelected(rol.isPermisoVentas());
        chkProductos.setSelected(rol.isPermisoProductos());
        chkInventario.setSelected(rol.isPermisoInventario());
        chkReportes.setSelected(rol.isPermisoReportes());
        chkUsuarios.setSelected(rol.isPermisoUsuarios());
        chkConfiguracion.setSelected(rol.isPermisoConfiguracion());
        chkOCR.setSelected(rol.isPermisoOCR());
        chkDevoluciones.setSelected(rol.isPermisoDevoluciones());

        btnEliminar.setDisable(false);
    }

    private void cargarRoles() {
        try {
            List<Rol> roles = rolDAO.obtenerTodos();
            listaRoles.clear();
            listaRoles.addAll(roles);
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al cargar roles: " + e.getMessage());
        }
    }

    private boolean validarFormulario() {
        if (txtNombreRol.getText().trim().isEmpty()) {
            AlertUtils.showWarning("Validación", "El nombre del rol es obligatorio");
            txtNombreRol.requestFocus();
            return false;
        }

        if (txtDescripcion.getText().trim().isEmpty()) {
            AlertUtils.showWarning("Validación", "La descripción del rol es obligatoria");
            txtDescripcion.requestFocus();
            return false;
        }

        // Verificar que al menos un permiso esté seleccionado
        if (!chkVentas.isSelected() && !chkProductos.isSelected() && !chkInventario.isSelected() &&
            !chkReportes.isSelected() && !chkUsuarios.isSelected() && !chkConfiguracion.isSelected() &&
            !chkOCR.isSelected() && !chkDevoluciones.isSelected()) {

            AlertUtils.showWarning("Validación", "Debe seleccionar al menos un permiso para el rol");
            return false;
        }

        return true;
    }
}
