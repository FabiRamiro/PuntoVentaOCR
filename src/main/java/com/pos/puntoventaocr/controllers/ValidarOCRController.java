package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.ComprobanteOCRDAO;
import com.pos.puntoventaocr.dao.VentaDAO;
import com.pos.puntoventaocr.models.ComprobanteOCR;
import com.pos.puntoventaocr.models.ComprobanteOCR.EstadoOCR;
import com.pos.puntoventaocr.models.MotorOCR;
import com.pos.puntoventaocr.models.Venta;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ValidarOCRController implements Initializable {

    // Controles de la tabla
    @FXML private TableView<ComprobanteOCR> tableComprobantes;
    @FXML private TableColumn<ComprobanteOCR, String> colNumeroVenta;
    @FXML private TableColumn<ComprobanteOCR, String> colReferencia;
    @FXML private TableColumn<ComprobanteOCR, BigDecimal> colMonto;
    @FXML private TableColumn<ComprobanteOCR, String> colBanco;
    @FXML private TableColumn<ComprobanteOCR, String> colEstado;
    @FXML private TableColumn<ComprobanteOCR, String> colFecha;

    // Controles del formulario OCR
    @FXML private ImageView imgComprobante;
    @FXML private TextField txtNumeroVenta;
    @FXML private TextField txtBancoEmisor;
    @FXML private TextField txtCuentaRemitente;
    @FXML private TextField txtMonto;
    @FXML private DatePicker dpFechaTransferencia;
    @FXML private TextField txtReferencia;
    @FXML private TextField txtBeneficiario;
    @FXML private TextArea txtObservaciones;
    @FXML private TextArea txtTextoExtraido;

    // Información de la venta
    @FXML private Label lblTotalVenta;
    @FXML private Label lblFechaVenta;
    @FXML private Label lblUsuarioVenta;
    @FXML private Label lblEstadoValidacion;

    // Controles de filtros
    @FXML private ComboBox<EstadoOCR> cmbFiltroEstado;
    @FXML private TextField txtFiltroReferencia;

    // Botones
    @FXML private Button btnCargarImagen;
    @FXML private Button btnProcesarOCR;
    @FXML private Button btnValidar;
    @FXML private Button btnRechazar;
    @FXML private Button btnGuardarCambios;
    @FXML private Button btnNuevoComprobante;

    private ComprobanteOCRDAO comprobanteDAO;
    private VentaDAO ventaDAO;
    private SessionManager sessionManager;
    private MotorOCR motorOCR;
    private ObservableList<ComprobanteOCR> comprobantesData;
    private ComprobanteOCR comprobanteSeleccionado;
    private boolean modoEdicion = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comprobanteDAO = new ComprobanteOCRDAO();
        ventaDAO = new VentaDAO();
        sessionManager = SessionManager.getInstance();
        motorOCR = MotorOCR.getInstance();
        comprobantesData = FXCollections.observableArrayList();

        configurarTabla();
        configurarFormulario();
        configurarEventos();
        cargarComprobantes();
        limpiarFormulario();
    }

    private void configurarTabla() {
        // Configurar columnas
        colNumeroVenta.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getVenta().getNumeroVenta()));
        colReferencia.setCellValueFactory(new PropertyValueFactory<>("referenciaOperacion"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("montoDetectado"));
        colBanco.setCellValueFactory(new PropertyValueFactory<>("bancoEmisor"));
        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEstadoValidacion().name()));
        colFecha.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFechaProcesamiento()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

        // Personalizar celdas de estado con colores
        colEstado.setCellFactory(column -> new TableCell<ComprobanteOCR, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "PENDIENTE":
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                            break;
                        case "VALIDADO":
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                            break;
                        case "RECHAZADO":
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                            break;
                        case "ERROR_PROCESAMIENTO":
                            setStyle("-fx-background-color: #f5c6cb; -fx-text-fill: #721c24;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Configurar selección
        tableComprobantes.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    comprobanteSeleccionado = newSelection;
                    if (newSelection != null) {
                        cargarComprobanteEnFormulario(newSelection);
                    }
                });

        tableComprobantes.setItems(comprobantesData);
    }

    private void configurarFormulario() {
        // Configurar filtros
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                EstadoOCR.values()
        ));
        cmbFiltroEstado.getItems().add(0, null); // Opción "Todos"
        cmbFiltroEstado.setValue(EstadoOCR.PENDIENTE); // Mostrar pendientes por defecto

        // Deshabilitar campos inicialmente
        habilitarFormulario(false);
    }

    private void configurarEventos() {
        // Eventos de filtros
        cmbFiltroEstado.setOnAction(e -> filtrarComprobantes());
        txtFiltroReferencia.textProperty().addListener((obs, oldText, newText) -> filtrarComprobantes());

        // Validaciones en tiempo real
        txtMonto.textProperty().addListener((obs, oldText, newText) ->
                validarNumero(txtMonto, newText));
    }

    private void cargarComprobantes() {
        try {
            List<ComprobanteOCR> comprobantes = comprobanteDAO.listarTodos();
            comprobantesData.setAll(comprobantes);
        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "No se pudieron cargar los comprobantes: " + e.getMessage());
        }
    }

    // === MÉTODOS DE ACCIÓN ===

    @FXML
    private void handleNuevoComprobante(ActionEvent event) {
        modoEdicion = false;
        limpiarFormulario();
        habilitarFormulario(true);
        btnCargarImagen.setDisable(false);
        sessionManager.registrarActividad("Iniciado registro de nuevo comprobante OCR");
    }

    @FXML
    private void handleCargarImagen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Comprobante");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf")
        );

        File file = fileChooser.showOpenDialog(btnCargarImagen.getScene().getWindow());
        if (file != null) {
            try {
                // Cargar y mostrar la imagen
                if (file.getName().toLowerCase().endsWith(".pdf")) {
                    // Para PDF, mostrar ícono genérico
                    imgComprobante.setImage(new Image(getClass().getResourceAsStream("/images/pdf_icon.png")));
                } else {
                    Image image = new Image(file.toURI().toString());
                    imgComprobante.setImage(image);
                }

                // Habilitar procesamiento OCR
                btnProcesarOCR.setDisable(false);

                sessionManager.registrarActividad("Imagen cargada para OCR: " + file.getName());

            } catch (Exception e) {
                AlertUtils.mostrarError("Error", "No se pudo cargar la imagen: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleProcesarOCR(ActionEvent event) {
        if (imgComprobante.getImage() == null) {
            AlertUtils.mostrarAdvertencia("Error", "Debe cargar una imagen primero");
            return;
        }

        // Solicitar número de venta
        String numeroVenta = AlertUtils.mostrarDialogoTexto(
                "Número de Venta",
                "Ingrese el número de venta asociada:"
        ).orElse("");

        if (numeroVenta.trim().isEmpty()) {
            AlertUtils.mostrarAdvertencia("Error", "Debe ingresar un número de venta");
            return;
        }

        try {
            // Buscar la venta
            Venta venta = ventaDAO.buscarPorNumero(numeroVenta);
            if (venta == null) {
                AlertUtils.mostrarError("Error", "No se encontró la venta: " + numeroVenta);
                return;
            }

            // Verificar que la venta sea por transferencia
            if (!"TRANSFERENCIA".equals(venta.getMetodoPago())) {
                AlertUtils.mostrarError("Error", "La venta debe ser por transferencia");
                return;
            }

            // Mostrar progreso
            ProgressIndicator progress = new ProgressIndicator();
            Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
            progressAlert.setTitle("Procesando OCR");
            progressAlert.setHeaderText("Procesando comprobante...");
            progressAlert.getDialogPane().setContent(progress);
            progressAlert.show();

            // Procesar en hilo separado
            new Thread(() -> {
                try {
                    // Simular ruta de imagen (en implementación real obtendrías la ruta del FileChooser)
                    String rutaImagen = "/temp/comprobante_temp.jpg";

                    // Procesar con OCR
                    ComprobanteOCR comprobante = motorOCR.procesarComprobante(rutaImagen, venta);

                    javafx.application.Platform.runLater(() -> {
                        progressAlert.close();

                        if (comprobante != null) {
                            // Cargar datos extraídos en el formulario
                            cargarComprobanteEnFormulario(comprobante);
                            habilitarFormulario(true);

                            AlertUtils.mostrarExito("OCR Procesado",
                                    "Comprobante procesado exitosamente. Revise los datos extraídos.");

                            sessionManager.registrarActividad("OCR procesado para venta: " + numeroVenta);
                        } else {
                            AlertUtils.mostrarError("Error OCR", "No se pudo procesar el comprobante");
                        }
                    });

                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        progressAlert.close();
                        AlertUtils.mostrarError("Error", "Error al procesar OCR: " + e.getMessage());
                    });
                }
            }).start();

        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "Error al procesar comprobante: " + e.getMessage());
        }
    }

    @FXML
    private void handleValidar(ActionEvent event) {
        if (comprobanteSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Selección", "Debe seleccionar un comprobante");
            return;
        }

        if (AlertUtils.mostrarConfirmacion("Confirmar Validación",
                "¿Está seguro que desea validar este comprobante?\n" +
                        "Referencia: " + comprobanteSeleccionado.getReferenciaOperacion())) {

            try {
                // IMPORTANTE: Actualizar datos del formulario antes de validar
                actualizarComprobanteDesdeFormulario(comprobanteSeleccionado);
                
                // Primero actualizar el comprobante con los datos del formulario
                if (!comprobanteDAO.actualizar(comprobanteSeleccionado)) {
                    AlertUtils.mostrarError("Error", "No se pudieron guardar los datos del comprobante");
                    return;
                }
                
                // Luego aprobar
                if (comprobanteDAO.aprobar(comprobanteSeleccionado.getIdComprobante(),
                        sessionManager.getUsuarioActual().getIdUsuario())) {

                    AlertUtils.mostrarExito("Éxito", "Comprobante validado correctamente");
                    sessionManager.registrarActividad("Comprobante OCR validado: " +
                            comprobanteSeleccionado.getReferenciaOperacion());

                    cargarComprobantes();
                    limpiarFormulario();
                } else {
                    AlertUtils.mostrarError("Error", "No se pudo validar el comprobante");
                }
            } catch (Exception e) {
                AlertUtils.mostrarError("Error", "Error al validar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRechazar(ActionEvent event) {
        if (comprobanteSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Selección", "Debe seleccionar un comprobante");
            return;
        }

        String motivo = AlertUtils.mostrarDialogoTexto(
                "Motivo de Rechazo",
                "Ingrese el motivo del rechazo:"
        ).orElse("");

        if (motivo.trim().isEmpty()) {
            AlertUtils.mostrarAdvertencia("Error", "Debe ingresar un motivo de rechazo");
            return;
        }

        try {
            // IMPORTANTE: Actualizar datos del formulario antes de rechazar
            actualizarComprobanteDesdeFormulario(comprobanteSeleccionado);
            
            // Primero actualizar el comprobante con los datos del formulario
            if (!comprobanteDAO.actualizar(comprobanteSeleccionado)) {
                AlertUtils.mostrarError("Error", "No se pudieron guardar los datos del comprobante");
                return;
            }
            
            // Luego rechazar
            if (comprobanteDAO.rechazar(comprobanteSeleccionado.getIdComprobante(),
                    sessionManager.getUsuarioActual().getIdUsuario(), motivo)) {

                AlertUtils.mostrarExito("Éxito", "Comprobante rechazado");
                sessionManager.registrarActividad("Comprobante OCR rechazado: " +
                        comprobanteSeleccionado.getReferenciaOperacion() + " - Motivo: " + motivo);

                cargarComprobantes();
                limpiarFormulario();
            } else {
                AlertUtils.mostrarError("Error", "No se pudo rechazar el comprobante");
            }
        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "Error al rechazar: " + e.getMessage());
        }
    }

    @FXML
    private void handleGuardarCambios(ActionEvent event) {
        if (comprobanteSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Error", "No hay comprobante seleccionado");
            return;
        }

        if (validarFormulario()) {
            try {
                // Actualizar datos del comprobante
                actualizarComprobanteDesdeFormulario(comprobanteSeleccionado);

                if (comprobanteDAO.actualizar(comprobanteSeleccionado)) {
                    AlertUtils.mostrarExito("Éxito", "Comprobante actualizado correctamente");
                    sessionManager.registrarActividad("Comprobante OCR modificado: " +
                            comprobanteSeleccionado.getReferenciaOperacion());

                    cargarComprobantes();
                } else {
                    AlertUtils.mostrarError("Error", "No se pudo actualizar el comprobante");
                }
            } catch (Exception e) {
                AlertUtils.mostrarError("Error", "Error al guardar cambios: " + e.getMessage());
            }
        }
    }

    // === MÉTODOS AUXILIARES ===

    private void filtrarComprobantes() {
        EstadoOCR estadoFiltro = cmbFiltroEstado.getValue();
        String referenciaFiltro = txtFiltroReferencia.getText().toLowerCase().trim();

        try {
            List<ComprobanteOCR> comprobantes;
            if (estadoFiltro != null) {
                comprobantes = comprobanteDAO.listarPorEstado(estadoFiltro);
            } else {
                comprobantes = comprobanteDAO.listarTodos();
            }

            // Filtrar por referencia si se especifica
            if (!referenciaFiltro.isEmpty()) {
                comprobantes = comprobantes.stream()
                        .filter(c -> c.getReferenciaOperacion() != null &&
                                c.getReferenciaOperacion().toLowerCase().contains(referenciaFiltro))
                        .toList();
            }

            comprobantesData.setAll(comprobantes);

        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "Error al filtrar comprobantes: " + e.getMessage());
        }
    }

    private void cargarComprobanteEnFormulario(ComprobanteOCR comprobante) {
        // Cargar datos del comprobante
        txtNumeroVenta.setText(comprobante.getVenta().getNumeroVenta());
        txtBancoEmisor.setText(comprobante.getBancoEmisor());
        txtCuentaRemitente.setText(comprobante.getCuentaRemitente());
        txtMonto.setText(comprobante.getMontoDetectado() != null ?
                comprobante.getMontoDetectado().toString() : "");

        if (comprobante.getFechaTransferencia() != null) {
            dpFechaTransferencia.setValue(comprobante.getFechaTransferencia().toLocalDate());
        }

        txtReferencia.setText(comprobante.getReferenciaOperacion());
        txtBeneficiario.setText(comprobante.getNombreBeneficiario());
        txtObservaciones.setText(comprobante.getObservaciones());
        
        // Mostrar texto extraído por OCR
        if (comprobante.getDatosExtraidos() != null && !comprobante.getDatosExtraidos().trim().isEmpty()) {
            txtTextoExtraido.setText(comprobante.getDatosExtraidos());
        } else {
            txtTextoExtraido.setText("No hay texto extraído disponible.");
        }

        // Cargar información de la venta
        Venta venta = comprobante.getVenta();
        lblTotalVenta.setText("$" + venta.getTotal().toString());
        if (venta.getFechaVenta() != null) {
            lblFechaVenta.setText(venta.getFechaVenta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        if (venta.getUsuario() != null) {
            lblUsuarioVenta.setText(venta.getUsuario().getNombreCompleto());
        }
        lblEstadoValidacion.setText(comprobante.getEstadoValidacion().name());

        // Cargar imagen si existe
        if (comprobante.getImagenOriginal() != null) {
            try {
                File imageFile = new File(comprobante.getImagenOriginal());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imgComprobante.setImage(image);
                }
            } catch (Exception e) {
                System.err.println("Error al cargar imagen: " + e.getMessage());
            }
        }

        // Configurar botones según estado
        configurarBotonesSegunEstado(comprobante.getEstadoValidacion());
    }

    private void configurarBotonesSegunEstado(EstadoOCR estado) {
        boolean esPendiente = EstadoOCR.PENDIENTE.equals(estado);

        btnValidar.setDisable(!esPendiente);
        btnRechazar.setDisable(!esPendiente);
        btnGuardarCambios.setDisable(!esPendiente);

        // Solo permitir edición si está pendiente
        habilitarFormulario(esPendiente && comprobanteSeleccionado != null);
    }

    private void actualizarComprobanteDesdeFormulario(ComprobanteOCR comprobante) {
        comprobante.setBancoEmisor(txtBancoEmisor.getText().trim());
        comprobante.setCuentaRemitente(txtCuentaRemitente.getText().trim());

        if (!txtMonto.getText().trim().isEmpty()) {
            comprobante.setMontoDetectado(new BigDecimal(txtMonto.getText().trim()));
        }

        if (dpFechaTransferencia.getValue() != null) {
            comprobante.setFechaTransferencia(dpFechaTransferencia.getValue().atStartOfDay());
        }

        comprobante.setReferenciaOperacion(txtReferencia.getText().trim());
        comprobante.setNombreBeneficiario(txtBeneficiario.getText().trim());
        comprobante.setObservaciones(txtObservaciones.getText().trim());
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        if (txtBancoEmisor.getText().trim().isEmpty()) {
            errores.append("- Banco emisor requerido\n");
        }

        if (txtMonto.getText().trim().isEmpty()) {
            errores.append("- Monto requerido\n");
        } else {
            try {
                new BigDecimal(txtMonto.getText().trim());
            } catch (NumberFormatException e) {
                errores.append("- Monto debe ser un número válido\n");
            }
        }

        if (txtReferencia.getText().trim().isEmpty()) {
            errores.append("- Referencia de operación requerida\n");
        }

        if (errores.length() > 0) {
            AlertUtils.mostrarErrorValidacion("Errores de Validación", "", errores.toString());
            return false;
        }

        return true;
    }

    private void limpiarFormulario() {
        txtNumeroVenta.clear();
        txtBancoEmisor.clear();
        txtCuentaRemitente.clear();
        txtMonto.clear();
        dpFechaTransferencia.setValue(null);
        txtReferencia.clear();
        txtBeneficiario.clear();
        txtObservaciones.clear();
        txtTextoExtraido.clear();

        lblTotalVenta.setText("$0.00");
        lblFechaVenta.setText("-");
        lblUsuarioVenta.setText("-");
        lblEstadoValidacion.setText("-");

        imgComprobante.setImage(null);

        btnProcesarOCR.setDisable(true);
        btnValidar.setDisable(true);
        btnRechazar.setDisable(true);
        btnGuardarCambios.setDisable(true);
    }

    private void habilitarFormulario(boolean habilitar) {
        txtBancoEmisor.setDisable(!habilitar);
        txtCuentaRemitente.setDisable(!habilitar);
        txtMonto.setDisable(!habilitar);
        dpFechaTransferencia.setDisable(!habilitar);
        txtReferencia.setDisable(!habilitar);
        txtBeneficiario.setDisable(!habilitar);
        txtObservaciones.setDisable(!habilitar);

        btnGuardarCambios.setDisable(!habilitar);
    }

    private void validarNumero(TextField campo, String nuevoTexto) {
        if (!nuevoTexto.matches("\\d*\\.?\\d*")) {
            campo.setText(nuevoTexto.replaceAll("[^\\d.]", ""));
        }
    }
}