package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.ComprobanteOCRDAO;
import com.pos.puntoventaocr.dao.UsuarioDAO;
import com.pos.puntoventaocr.models.ComprobanteOCR;
import com.pos.puntoventaocr.models.Usuario;
import com.pos.puntoventaocr.models.Venta;
import com.pos.puntoventaocr.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class HistorialOCRController {

    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ComboBox<String> cmbUsuario;
    @FXML private Button btnFiltrar;
    @FXML private Button btnLimpiarFiltros;

    @FXML private TableView<ComprobanteOCR> tablaHistorialOCR;
    @FXML private TableColumn<ComprobanteOCR, Integer> colId;
    @FXML private TableColumn<ComprobanteOCR, String> colFecha;
    @FXML private TableColumn<ComprobanteOCR, Integer> colVenta;
    @FXML private TableColumn<ComprobanteOCR, String> colBanco;
    @FXML private TableColumn<ComprobanteOCR, String> colReferencia;
    @FXML private TableColumn<ComprobanteOCR, Double> colMonto;
    @FXML private TableColumn<ComprobanteOCR, String> colEstado;
    @FXML private TableColumn<ComprobanteOCR, String> colUsuario;
    @FXML private TableColumn<ComprobanteOCR, Void> colAcciones;

    @FXML private Label lblTotalValidaciones;
    @FXML private Label lblValidadas;
    @FXML private Label lblRechazadas;

    private ComprobanteOCRDAO comprobanteDAO;
    private UsuarioDAO usuarioDAO;
    private ObservableList<ComprobanteOCR> listaComprobantes;

    public void initialize() {
        comprobanteDAO = new ComprobanteOCRDAO();
        usuarioDAO = new UsuarioDAO();
        listaComprobantes = FXCollections.observableArrayList();

        configurarTabla();
        configurarFiltros();
        cargarDatos();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idComprobante"));
        colFecha.setCellValueFactory(cellData -> {
            LocalDateTime fecha = cellData.getValue().getFechaProcesamiento();
            if (fecha != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colVenta.setCellValueFactory(cellData -> {
            Venta venta = cellData.getValue().getVenta();
            if (venta != null) {
                return new javafx.beans.property.SimpleObjectProperty<>(venta.getIdVenta());
            }
            return new javafx.beans.property.SimpleObjectProperty<>(null);
        });
        colBanco.setCellValueFactory(new PropertyValueFactory<>("bancoEmisor"));
        colReferencia.setCellValueFactory(new PropertyValueFactory<>("referenciaOperacion"));
        colMonto.setCellValueFactory(cellData -> {
            java.math.BigDecimal monto = cellData.getValue().getMontoDetectado();
            if (monto != null) {
                return new javafx.beans.property.SimpleObjectProperty<>(monto.doubleValue());
            }
            return new javafx.beans.property.SimpleObjectProperty<>(0.0);
        });
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoValidacion"));
        colUsuario.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue().getUsuarioValidador();
            if (usuario != null) {
                String nombreCompleto = usuario.getNombre() + " " + usuario.getApellido();
                return new javafx.beans.property.SimpleStringProperty(nombreCompleto);
            }
            return new javafx.beans.property.SimpleStringProperty("Sin asignar");
        });

        // Formatear columna de monto
        colMonto.setCellFactory(column -> new TableCell<ComprobanteOCR, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + String.format("%.2f", item));
                }
            }
        });

        // Formatear columna de estado con colores
        colEstado.setCellFactory(column -> new TableCell<ComprobanteOCR, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toUpperCase()) {
                        case "VALIDADO":
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case "RECHAZADO":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        case "PENDIENTE":
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Configurar columna de acciones
        colAcciones.setCellFactory(col -> new TableCell<ComprobanteOCR, Void>() {
            private final Button btnVer = new Button("Ver");

            {
                btnVer.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-size: 10px;");
                btnVer.setOnAction(event -> {
                    ComprobanteOCR comprobante = getTableView().getItems().get(getIndex());
                    mostrarDetalles(comprobante);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnVer);
                }
            }
        });

        tablaHistorialOCR.setItems(listaComprobantes);
    }

    private void configurarFiltros() {
        // Configurar fechas por defecto (último mes)
        dpFechaHasta.setValue(LocalDate.now());
        dpFechaDesde.setValue(LocalDate.now().minusMonths(1));

        // Configurar estados
        cmbEstado.getItems().addAll("Todos", "Validado", "Rechazado", "Pendiente");
        cmbEstado.setValue("Todos");

        // Cargar usuarios
        try {
            cmbUsuario.getItems().add("Todos");
            List<Usuario> usuarios = usuarioDAO.obtenerTodos();
            for (Usuario usuario : usuarios) {
                cmbUsuario.getItems().add(usuario.getNombreCompleto());
            }
            cmbUsuario.setValue("Todos");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al cargar usuarios: " + e.getMessage());
        }
    }

    @FXML
    private void aplicarFiltros() {
        try {
            LocalDate fechaDesde = dpFechaDesde.getValue();
            LocalDate fechaHasta = dpFechaHasta.getValue();
            String estado = cmbEstado.getValue();
            String usuario = cmbUsuario.getValue();

            List<ComprobanteOCR> comprobantesFiltrados = comprobanteDAO.obtenerPorFiltros(
                fechaDesde, fechaHasta, estado, usuario);

            listaComprobantes.clear();
            listaComprobantes.addAll(comprobantesFiltrados);

            actualizarEstadisticas();

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al aplicar filtros: " + e.getMessage());
        }
    }

    @FXML
    private void limpiarFiltros() {
        dpFechaHasta.setValue(LocalDate.now());
        dpFechaDesde.setValue(LocalDate.now().minusMonths(1));
        cmbEstado.setValue("Todos");
        cmbUsuario.setValue("Todos");
        cargarDatos();
    }

    private void cargarDatos() {
        try {
            List<ComprobanteOCR> comprobantes = comprobanteDAO.obtenerTodos();
            listaComprobantes.clear();
            listaComprobantes.addAll(comprobantes);
            actualizarEstadisticas();
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al cargar historial OCR: " + e.getMessage());
        }
    }

    private void actualizarEstadisticas() {
        int totalValidaciones = listaComprobantes.size();
        int validadas = 0;
        int rechazadas = 0;

        for (ComprobanteOCR comprobante : listaComprobantes) {
            switch (comprobante.getEstadoValidacion().toLowerCase()) {
                case "validado":
                    validadas++;
                    break;
                case "rechazado":
                    rechazadas++;
                    break;
            }
        }

        lblTotalValidaciones.setText("Total validaciones: " + totalValidaciones);
        lblValidadas.setText("Validadas: " + validadas);
        lblRechazadas.setText("Rechazadas: " + rechazadas);
    }

    private void mostrarDetalles(ComprobanteOCR comprobante) {
        if (comprobante == null) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles del Comprobante OCR");
        alert.setHeaderText("Información detallada del comprobante");

        StringBuilder detalles = new StringBuilder();
        detalles.append("ID: ").append(comprobante.getIdComprobante()).append("\n");
        detalles.append("Estado: ").append(comprobante.getEstadoValidacion()).append("\n");
        detalles.append("Fecha de procesamiento: ");
        if (comprobante.getFechaProcesamiento() != null) {
            detalles.append(comprobante.getFechaProcesamiento().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        } else {
            detalles.append("No disponible\n");
        }

        if (comprobante.getVenta() != null) {
            detalles.append("Venta asociada: ").append(comprobante.getVenta().getNumeroVenta()).append("\n");
            detalles.append("Total venta: $").append(comprobante.getVenta().getTotal()).append("\n");
        }

        detalles.append("Banco emisor: ").append(comprobante.getBancoEmisor() != null ? comprobante.getBancoEmisor() : "No especificado").append("\n");
        detalles.append("Referencia: ").append(comprobante.getReferenciaOperacion() != null ? comprobante.getReferenciaOperacion() : "No disponible").append("\n");
        detalles.append("Monto detectado: $").append(comprobante.getMontoDetectado()).append("\n");

        if (comprobante.getFechaTransferencia() != null) {
            detalles.append("Fecha transferencia: ").append(comprobante.getFechaTransferencia().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        }

        detalles.append("Beneficiario: ").append(comprobante.getNombreBeneficiario() != null ? comprobante.getNombreBeneficiario() : "No especificado").append("\n");

        if (comprobante.getUsuarioValidador() != null) {
            detalles.append("Validado por: ").append(comprobante.getUsuarioValidador().getNombre())
                    .append(" ").append(comprobante.getUsuarioValidador().getApellido()).append("\n");
        }

        if (comprobante.getObservaciones() != null && !comprobante.getObservaciones().isEmpty()) {
            detalles.append("Observaciones: ").append(comprobante.getObservaciones()).append("\n");
        }

        if (comprobante.getDatosExtraidos() != null && !comprobante.getDatosExtraidos().isEmpty()) {
            detalles.append("\nDatos extraídos:\n").append(comprobante.getDatosExtraidos());
        }

        TextArea textArea = new TextArea(detalles.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(15);
        textArea.setPrefColumnCount(50);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }
}
