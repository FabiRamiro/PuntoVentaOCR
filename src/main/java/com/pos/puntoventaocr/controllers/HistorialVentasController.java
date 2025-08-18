package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.VentaDAO;
import com.pos.puntoventaocr.models.Venta;
import com.pos.puntoventaocr.utils.AlertUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class HistorialVentasController {

    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private ComboBox<String> cmbUsuario;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Button btnFiltrar;
    @FXML private Button btnLimpiarFiltros;

    @FXML private TableView<Venta> tablaVentas;
    @FXML private TableColumn<Venta, String> colNumero;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, String> colCliente;
    @FXML private TableColumn<Venta, String> colUsuario;
    @FXML private TableColumn<Venta, String> colTotal;
    @FXML private TableColumn<Venta, String> colEstado;
    @FXML private TableColumn<Venta, Void> colAcciones;

    @FXML private Label lblTotalVentas;
    @FXML private Label lblMontoTotal;

    private VentaDAO ventaDAO;
    private ObservableList<Venta> listaVentas;

    public void initialize() {
        ventaDAO = new VentaDAO();
        listaVentas = FXCollections.observableArrayList();

        configurarTabla();
        configurarFiltros();
        cargarDatos();
    }

    private void configurarTabla() {
        // Configurar columnas de la tabla
        colNumero.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getNumeroVenta()));

        colFecha.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getFecha()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

        colCliente.setCellValueFactory(cellData -> {
            String cliente = cellData.getValue().getCliente() != null ?
                cellData.getValue().getCliente().getNombre() : "Cliente General";
            return new SimpleStringProperty(cliente);
        });

        colUsuario.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getUsuario().getNombreCompleto()));

        colTotal.setCellValueFactory(cellData ->
            new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getTotal())));

        // Configurar columna de estado con colores
        colEstado.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getEstado()));

        colEstado.setCellFactory(col -> new TableCell<Venta, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    switch (estado) {
                        case "COMPLETADA":
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case "ANULADA":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });

        // Configurar columna de acciones con botones
        colAcciones.setCellFactory(col -> new TableCell<Venta, Void>() {
            private final Button btnAnular = new Button("Anular");
            private final Button btnVer = new Button("Ver");
            private final HBox hbox = new HBox(5);

            {
                btnAnular.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");
                btnVer.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-size: 10px;");

                btnAnular.setOnAction(event -> {
                    Venta venta = getTableView().getItems().get(getIndex());
                    anularVenta(venta);
                });

                btnVer.setOnAction(event -> {
                    Venta venta = getTableView().getItems().get(getIndex());
                    verDetalleVenta(venta);
                });

                hbox.getChildren().addAll(btnVer, btnAnular);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Venta venta = getTableView().getItems().get(getIndex());

                    // Solo mostrar botón anular si la venta está completada
                    if ("COMPLETADA".equals(venta.getEstado())) {
                        btnAnular.setVisible(true);
                        btnAnular.setManaged(true);
                    } else {
                        btnAnular.setVisible(false);
                        btnAnular.setManaged(false);
                    }

                    setGraphic(hbox);
                }
            }
        });

        tablaVentas.setItems(listaVentas);
    }

    private void configurarFiltros() {
        // Configurar fechas por defecto (último mes)
        dpFechaHasta.setValue(LocalDate.now());
        dpFechaDesde.setValue(LocalDate.now().minusMonths(1));

        // Configurar combo de estados (sin "Pendiente")
        cmbEstado.getItems().addAll("Todos", "COMPLETADA", "ANULADA");
        cmbEstado.setValue("Todos");

        // Cargar usuarios desde la base de datos
        cargarUsuarios();
    }

    private void cargarUsuarios() {
        try {
            List<String> usuarios = ventaDAO.obtenerUsuarios();
            cmbUsuario.getItems().clear();
            cmbUsuario.getItems().add("Todos");
            cmbUsuario.getItems().addAll(usuarios);
            cmbUsuario.setValue("Todos");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error cargando usuarios: " + e.getMessage());
        }
    }

    @FXML
    private void aplicarFiltros() {
        try {
            LocalDate fechaDesde = dpFechaDesde.getValue();
            LocalDate fechaHasta = dpFechaHasta.getValue();
            String usuario = cmbUsuario.getValue();
            String estado = cmbEstado.getValue();

            // Validar fechas
            if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
                AlertUtils.showWarning("Fechas inválidas", "La fecha desde no puede ser posterior a la fecha hasta");
                return;
            }

            List<Venta> ventasFiltradas = ventaDAO.obtenerVentasPorFiltros(
                fechaDesde, fechaHasta, usuario, estado);

            listaVentas.clear();
            listaVentas.addAll(ventasFiltradas);

            actualizarEstadisticas();

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al aplicar filtros: " + e.getMessage());
        }
    }

    @FXML
    private void limpiarFiltros() {
        dpFechaHasta.setValue(LocalDate.now());
        dpFechaDesde.setValue(LocalDate.now().minusMonths(1));
        cmbUsuario.setValue("Todos");
        cmbEstado.setValue("Todos");
        cargarDatos();
    }

    private void cargarDatos() {
        try {
            List<Venta> ventas = ventaDAO.obtenerTodas();
            listaVentas.clear();
            listaVentas.addAll(ventas);
            actualizarEstadisticas();
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al cargar historial de ventas: " + e.getMessage());
        }
    }

    private void actualizarEstadisticas() {
        int totalVentas = listaVentas.size();

        // Calcular solo ventas completadas para el monto total
        BigDecimal montoTotal = listaVentas.stream()
            .filter(v -> "COMPLETADA".equals(v.getEstado()))
            .map(Venta::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotalVentas.setText("Total ventas: " + totalVentas);
        lblMontoTotal.setText(String.format("Monto total: $%.2f", montoTotal));
    }

    private void anularVenta(Venta venta) {
        if (!"COMPLETADA".equals(venta.getEstado())) {
            AlertUtils.showWarning("Venta no válida", "Solo se pueden anular ventas completadas");
            return;
        }

        // Mostrar diálogo para ingresar motivo de anulación
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Anular Venta");
        dialog.setHeaderText("Anulación de venta: " + venta.getNumeroVenta());
        dialog.setContentText("Ingrese el motivo de anulación:");

        Optional<String> resultado = dialog.showAndWait();

        if (resultado.isPresent()) {
            String motivo = resultado.get().trim();

            if (motivo.isEmpty()) {
                AlertUtils.showWarning("Motivo requerido", "Debe ingresar un motivo para anular la venta");
                return;
            }

            // Confirmar anulación
            Optional<ButtonType> confirmacion = AlertUtils.showConfirmation(
                "Confirmar Anulación",
                "¿Está seguro de anular la venta " + venta.getNumeroVenta() + "?\n\n" +
                "Motivo: " + motivo + "\n\n" +
                "Esta acción no se puede deshacer."
            );

            if (confirmacion.isPresent() && confirmacion.get() == ButtonType.OK) {
                try {
                    boolean exitoso = ventaDAO.anularVenta(venta.getIdVenta(), motivo);

                    if (exitoso) {
                        AlertUtils.showInfo("Éxito", "Venta anulada correctamente");
                        // Actualizar la venta en la tabla
                        venta.setEstado("ANULADA");
                        venta.setMotivoAnulacion(motivo);
                        tablaVentas.refresh();
                        actualizarEstadisticas();
                    } else {
                        AlertUtils.showError("Error", "No se pudo anular la venta");
                    }
                } catch (Exception e) {
                    AlertUtils.showError("Error", "Error anulando venta: " + e.getMessage());
                }
            }
        }
    }

    private void verDetalleVenta(Venta venta) {
        // Crear un diálogo para mostrar los detalles de la venta
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle de Venta");
        alert.setHeaderText("Venta: " + venta.getNumeroVenta());

        StringBuilder detalles = new StringBuilder();
        detalles.append("Fecha: ").append(venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        detalles.append("Usuario: ").append(venta.getUsuario().getNombreCompleto()).append("\n");

        if (venta.getCliente() != null) {
            detalles.append("Cliente: ").append(venta.getCliente().getNombre()).append("\n");
        }

        detalles.append("Método de pago: ").append(venta.getMetodoPago()).append("\n");
        detalles.append("Estado: ").append(venta.getEstado()).append("\n");

        if ("ANULADA".equals(venta.getEstado()) && venta.getMotivoAnulacion() != null) {
            detalles.append("Motivo anulación: ").append(venta.getMotivoAnulacion()).append("\n");
        }

        detalles.append("\n--- TOTALES ---\n");
        detalles.append(String.format("Subtotal: $%.2f\n", venta.getSubtotal()));
        detalles.append(String.format("IVA: $%.2f\n", venta.getIva()));
        detalles.append(String.format("TOTAL: $%.2f", venta.getTotal()));

        alert.setContentText(detalles.toString());
        alert.setResizable(true);
        alert.showAndWait();
    }
}
