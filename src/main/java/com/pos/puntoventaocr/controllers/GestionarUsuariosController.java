package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.UsuarioDAO;
import com.pos.puntoventaocr.dao.BitacoraDAO;
import com.pos.puntoventaocr.models.Usuario;
import com.pos.puntoventaocr.models.Rol;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;

public class GestionarUsuariosController {

    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnResetPassword;
    @FXML private Button btnActualizar;

    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colApellido;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colEstado;
    @FXML private TableColumn<Usuario, String> colUltimoAcceso;

    @FXML private Label lblTotalUsuarios;
    @FXML private Label lblUsuariosActivos;

    private UsuarioDAO usuarioDAO;
    private BitacoraDAO bitacoraDAO;
    private ObservableList<Usuario> listaUsuarios;

    public void initialize() {
        usuarioDAO = new UsuarioDAO();
        bitacoraDAO = new BitacoraDAO();
        listaUsuarios = FXCollections.observableArrayList();

        configurarTabla();
        configurarEventos();
        cargarDatos();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("nombreRol"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colUltimoAcceso.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            if (usuario.getUltimoAcceso() != null) {
                // Convertir Timestamp a LocalDateTime para formatear
                java.time.LocalDateTime fechaHora = usuario.getUltimoAcceso().toLocalDateTime();
                return new javafx.beans.property.SimpleStringProperty(
                    fechaHora.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            } else {
                return new javafx.beans.property.SimpleStringProperty("Nunca");
            }
        });

        tablaUsuarios.setItems(listaUsuarios);

        // Listener para habilitar/deshabilitar botones
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean itemSelected = newSelection != null;
                btnEditar.setDisable(!itemSelected);
                btnEliminar.setDisable(!itemSelected);
                btnResetPassword.setDisable(!itemSelected);
            });
    }

    private void configurarEventos() {
        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);
        btnResetPassword.setDisable(true);
    }

    @FXML
    private void nuevoUsuario() {
        try {
            Dialog<Usuario> dialog = crearDialogoUsuario("Nuevo Usuario", null);
            Optional<Usuario> resultado = dialog.showAndWait();

            if (resultado.isPresent()) {
                Usuario usuario = resultado.get();
                if (usuarioDAO.crear(usuario)) {
                    // Registrar en bitácora
                    int idUsuarioActual = SessionManager.getInstance().getUsuarioActual().getIdUsuario();
                    bitacoraDAO.registrarCreacionUsuario(idUsuarioActual, usuario.getNombreUsuario(), "127.0.0.1");

                    AlertUtils.showInfo("Éxito", "Usuario creado correctamente");
                    cargarDatos();
                } else {
                    AlertUtils.showError("Error", "No se pudo crear el usuario");
                }
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al crear usuario: " + e.getMessage());
        }
    }

    @FXML
    private void editarUsuario() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado == null) {
            AlertUtils.showWarning("Advertencia", "Seleccione un usuario para editar");
            return;
        }

        try {
            // Guardar datos anteriores para la bitácora
            String datosAnteriores = String.format("Nombre: %s, Email: %s, Rol: %s, Estado: %s",
                usuarioSeleccionado.getNombre(), usuarioSeleccionado.getEmail(),
                usuarioSeleccionado.getNombreRol(), usuarioSeleccionado.getEstado());

            Dialog<Usuario> dialog = crearDialogoUsuario("Editar Usuario", usuarioSeleccionado);
            Optional<Usuario> resultado = dialog.showAndWait();

            if (resultado.isPresent()) {
                Usuario usuario = resultado.get();
                if (usuarioDAO.actualizar(usuario)) {
                    // Registrar en bitácora
                    int idUsuarioActual = SessionManager.getInstance().getUsuarioActual().getIdUsuario();
                    String datosNuevos = String.format("Nombre: %s, Email: %s, Rol: %s, Estado: %s",
                        usuario.getNombre(), usuario.getEmail(),
                        usuario.getNombreRol(), usuario.getEstado());

                    bitacoraDAO.registrarModificacionUsuario(idUsuarioActual, usuario.getNombreUsuario(),
                        "Usuario modificado: " + datosNuevos);

                    AlertUtils.showInfo("Éxito", "Usuario actualizado correctamente");
                    cargarDatos();
                } else {
                    AlertUtils.showError("Error", "No se pudo actualizar el usuario");
                }
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al editar usuario: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarUsuario() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado == null) {
            AlertUtils.showWarning("Advertencia", "Seleccione un usuario para eliminar");
            return;
        }

        Optional<ButtonType> resultado = AlertUtils.confirmDialog(
            "Confirmar Eliminación",
            "¿Está seguro de eliminar el usuario '" + usuarioSeleccionado.getNombreCompleto() + "'?"
        );

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                if (usuarioDAO.eliminar(usuarioSeleccionado.getIdUsuario())) {
                    // Registrar en bitácora
                    int idUsuarioActual = SessionManager.getInstance().getUsuarioActual().getIdUsuario();
                    bitacoraDAO.registrarEliminacionUsuario(idUsuarioActual, usuarioSeleccionado.getNombreUsuario(), "127.0.0.1");

                    AlertUtils.showInfo("Éxito", "Usuario eliminado correctamente");
                    cargarDatos();
                } else {
                    AlertUtils.showError("Error", "No se pudo eliminar el usuario");
                }
            } catch (Exception e) {
                AlertUtils.showError("Error", "Error al eliminar usuario: " + e.getMessage());
            }
        }
    }

    @FXML
    private void resetearPassword() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado == null) {
            AlertUtils.showWarning("Advertencia", "Seleccione un usuario para resetear contraseña");
            return;
        }

        Optional<ButtonType> resultado = AlertUtils.confirmDialog(
            "Confirmar Reset",
            "¿Está seguro de resetear la contraseña del usuario '" + usuarioSeleccionado.getNombreCompleto() + "'?\nLa nueva contraseña será: 123456"
        );

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                if (usuarioDAO.resetearPassword(usuarioSeleccionado.getIdUsuario(), "123456")) {
                    // Registrar en bitácora
                    int idUsuarioActual = SessionManager.getInstance().getUsuarioActual().getIdUsuario();
                    bitacoraDAO.registrarAccion(idUsuarioActual, "RESET_PASSWORD", "USUARIOS",
                        "Reseteó la contraseña del usuario: " + usuarioSeleccionado.getNombreUsuario());

                    AlertUtils.showInfo("Éxito", "Contraseña reseteada correctamente");
                } else {
                    AlertUtils.showError("Error", "No se pudo resetear la contraseña");
                }
            } catch (Exception e) {
                AlertUtils.showError("Error", "Error al resetear contraseña: " + e.getMessage());
            }
        }
    }

    @FXML
    private void actualizarLista() {
        cargarDatos();
    }

    private void cargarDatos() {
        try {
            List<Usuario> usuarios = usuarioDAO.obtenerTodos();
            listaUsuarios.clear();
            listaUsuarios.addAll(usuarios);
            actualizarEstadisticas();
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al cargar usuarios: " + e.getMessage());
        }
    }

    private void actualizarEstadisticas() {
        int totalUsuarios = listaUsuarios.size();
        int usuariosActivos = (int) listaUsuarios.stream()
            .filter(u -> "Activo".equals(u.getEstado()))
            .count();

        lblTotalUsuarios.setText("Total usuarios: " + totalUsuarios);
        lblUsuariosActivos.setText("Activos: " + usuariosActivos);
    }

    private Dialog<Usuario> crearDialogoUsuario(String titulo, Usuario usuario) {
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);

        ButtonType btnAceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAceptar, ButtonType.CANCEL);

        // Crear formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtNombre = new TextField();
        TextField txtApellido = new TextField();
        TextField txtUsuario = new TextField();
        TextField txtEmail = new TextField();
        PasswordField txtPassword = new PasswordField();
        ComboBox<String> cmbRol = new ComboBox<>();
        CheckBox chkActivo = new CheckBox();

        // Configurar roles disponibles
        cmbRol.getItems().addAll("ADMINISTRADOR", "GERENTE", "CAJERO");
        cmbRol.setValue("CAJERO"); // Valor por defecto

        if (usuario != null) {
            txtNombre.setText(usuario.getNombre());
            txtApellido.setText(usuario.getApellido());
            txtUsuario.setText(usuario.getNombreUsuario());
            txtEmail.setText(usuario.getEmail());

            // Mapear el rol correctamente desde la base de datos
            String rolActual = usuario.getNombreRol();
            System.out.println("🔍 DEBUG: Rol actual del usuario: " + rolActual);

            // Mapear roles de BD a nombres de interfaz
            switch (rolActual.toUpperCase()) {
                case "ADMINISTRADOR":
                    cmbRol.setValue("ADMINISTRADOR");
                    break;
                case "GERENTE":
                case "SUPERVISOR":
                    cmbRol.setValue("GERENTE");
                    break;
                case "CAJERO":
                case "VENDEDOR":
                    cmbRol.setValue("CAJERO");
                    break;
                default:
                    cmbRol.setValue("CAJERO");
                    break;
            }

            chkActivo.setSelected("ACTIVO".equals(usuario.getEstado()));
            txtPassword.setVisible(false);
            grid.add(new Label("Contraseña:"), 0, 5);
            grid.add(new Label("(Sin cambios)"), 1, 5);
        } else {
            chkActivo.setSelected(true);
            grid.add(new Label("Contraseña:"), 0, 5);
            grid.add(txtPassword, 1, 5);
        }

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Apellido:"), 0, 1);
        grid.add(txtApellido, 1, 1);
        grid.add(new Label("Usuario:"), 0, 2);
        grid.add(txtUsuario, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(txtEmail, 1, 3);
        grid.add(new Label("Rol:"), 0, 4);
        grid.add(cmbRol, 1, 4);
        grid.add(new Label("Activo:"), 0, 6);
        grid.add(chkActivo, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Validaciones
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(btnAceptar);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (txtNombre.getText().trim().isEmpty()) {
                AlertUtils.showWarning("Advertencia", "El nombre es requerido");
                event.consume();
                return;
            }
            if (txtApellido.getText().trim().isEmpty()) {
                AlertUtils.showWarning("Advertencia", "El apellido es requerido");
                event.consume();
                return;
            }
            if (txtUsuario.getText().trim().isEmpty()) {
                AlertUtils.showWarning("Advertencia", "El nombre de usuario es requerido");
                event.consume();
                return;
            }
            if (txtEmail.getText().trim().isEmpty()) {
                AlertUtils.showWarning("Advertencia", "El email es requerido");
                event.consume();
                return;
            }
            if (cmbRol.getValue() == null) {
                AlertUtils.showWarning("Advertencia", "Debe seleccionar un rol");
                event.consume();
                return;
            }
            if (usuario == null && txtPassword.getText().trim().isEmpty()) {
                AlertUtils.showWarning("Advertencia", "La contraseña es requerida para nuevos usuarios");
                event.consume();
                return;
            }
        });

        // Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnAceptar) {
                Usuario result = usuario != null ? usuario : new Usuario();
                result.setNombre(txtNombre.getText().trim());
                result.setApellido(txtApellido.getText().trim());
                result.setNombreUsuario(txtUsuario.getText().trim());
                result.setEmail(txtEmail.getText().trim());
                result.setNombreRol(cmbRol.getValue());
                result.setEstado(chkActivo.isSelected() ? "ACTIVO" : "INACTIVO");

                if (usuario == null) {
                    result.setPassword(txtPassword.getText());
                    result.setCreadoPor(SessionManager.getInstance().getUsuarioActual().getIdUsuario());
                } else {
                    result.setModificadoPor(SessionManager.getInstance().getUsuarioActual().getIdUsuario());
                }

                return result;
            }
            return null;
        });

        return dialog;
    }
}
