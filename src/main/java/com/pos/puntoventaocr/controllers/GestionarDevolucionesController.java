package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.DevolucionDAO;
import com.pos.puntoventaocr.dao.VentaDAO;
import com.pos.puntoventaocr.models.Devolucion;
import com.pos.puntoventaocr.models.Venta;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class GestionarDevolucionesController {

    @FXML private TextField txtBuscarVenta;
    @FXML private Button btnBuscarVenta;
    @FXML private Label lblVentaInfo;

    @FXML private TextArea txtMotivo;
    @FXML private TextField txtMontoDevolucion;
    @FXML private Button btnCrearDevolucion;

    // Filtros
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Button btnFiltrar;

    // Tabla de devoluciones
    @FXML private TableView<Devolucion> tablaDevoluciones;
    @FXML private TableColumn<Devolucion, String> colNumero;
    @FXML private TableColumn<Devolucion, String> colVenta;
    @FXML private TableColumn<Devolucion, String> colFecha;
    @FXML private TableColumn<Devolucion, String> colMotivo;
    @FXML private TableColumn<Devolucion, Double> colMonto;
    @FXML private TableColumn<Devolucion, String> colEstado;
    @FXML private TableColumn<Devolucion, String> colProcesadoPor;
    @FXML private TableColumn<Devolucion, String> colAcciones;

    // Información de devolución seleccionada
    @FXML private TextArea txtDetalleDevolucion;
    @FXML private Button btnAprobar;
    @FXML private Button btnRechazar;

    private DevolucionDAO devolucionDAO;
    private VentaDAO ventaDAO;
    private SessionManager sessionManager;
    private ObservableList<Devolucion> listaDevoluciones;
    private Venta ventaSeleccionada;
    private Devolucion devolucionSeleccionada;

    public void initialize() {
        devolucionDAO = new DevolucionDAO();
        ventaDAO = new VentaDAO();
        sessionManager = SessionManager.getInstance();
        listaDevoluciones = FXCollections.observableArrayList();

        configurarTabla();
        configurarFiltros();
        configurarEventos();
        cargarDevoluciones();
    }

    private void configurarTabla() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroDevolucion"));
        colVenta.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getVentaOriginal() != null ?
                cellData.getValue().getVentaOriginal().getNumeroVenta() : "N/A"
            )
        );
        colFecha.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFechaCreacion() != null ?
                cellData.getValue().getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"
            )
        );
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colMonto.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<Double>(
                cellData.getValue().getMontoTotal() != null ?
                cellData.getValue().getMontoTotal().doubleValue() : 0.0
            )
        );
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colProcesadoPor.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getProcesadoPor() != null ?
                cellData.getValue().getProcesadoPor().getNombre() + " " + cellData.getValue().getProcesadoPor().getApellido() : "N/A"
            )
        );

        // Configurar columna de acciones
        colAcciones.setCellFactory(col -> new TableCell<Devolucion, String>() {
            private final Button btnVer = new Button("Ver");

            {
                btnVer.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
                btnVer.setOnAction(event -> {
                    Devolucion devolucion = getTableView().getItems().get(getIndex());
                    seleccionarDevolucion(devolucion);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnVer);
                }
            }
        });

        // Formatear columna de monto
        colMonto.setCellFactory(column -> new TableCell<Devolucion, Double>() {
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
        colEstado.setCellFactory(column -> new TableCell<Devolucion, String>() {
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
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                        case "APROBADA":
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case "RECHAZADA":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        tablaDevoluciones.setItems(listaDevoluciones);
    }

    private void configurarFiltros() {
        // Configurar fechas por defecto (mes actual)
        LocalDate ahora = LocalDate.now();
        dpFechaDesde.setValue(ahora.withDayOfMonth(1));
        dpFechaHasta.setValue(ahora);

        // Configurar estados
        cmbEstado.getItems().addAll("Todos", "PENDIENTE", "APROBADA", "RECHAZADA");
        cmbEstado.setValue("Todos");
    }

    private void configurarEventos() {
        // Listener para selección en tabla
        tablaDevoluciones.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    seleccionarDevolucion(newSelection);
                }
            }
        );
    }

    @FXML
    private void buscarVenta() {
        String numeroVenta = txtBuscarVenta.getText().trim();
        if (numeroVenta.isEmpty()) {
            AlertUtils.showWarning("Advertencia", "Ingrese el número de venta");
            return;
        }

        try {
            // Buscar venta directamente por número
            ventaSeleccionada = ventaDAO.obtenerPorNumero(numeroVenta);

            if (ventaSeleccionada != null) {
                lblVentaInfo.setText("Venta encontrada: " + ventaSeleccionada.getNumeroVenta() +
                                   " - Total: $" + ventaSeleccionada.getTotal() +
                                   " - Fecha: " + ventaSeleccionada.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                lblVentaInfo.setStyle("-fx-text-fill: green;");
                txtMontoDevolucion.setText(ventaSeleccionada.getTotal().toString());
                btnCrearDevolucion.setDisable(false);
            } else {
                lblVentaInfo.setText("Venta no encontrada");
                lblVentaInfo.setStyle("-fx-text-fill: red;");
                ventaSeleccionada = null;
                btnCrearDevolucion.setDisable(true);
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error buscando venta: " + e.getMessage());
        }
    }

    @FXML
    private void crearDevolucion() {
        if (ventaSeleccionada == null) {
            AlertUtils.showWarning("Advertencia", "Primero debe buscar y seleccionar una venta");
            return;
        }

        String motivo = txtMotivo.getText().trim();
        String montoTexto = txtMontoDevolucion.getText().trim();

        if (motivo.isEmpty()) {
            AlertUtils.showWarning("Advertencia", "Ingrese el motivo de la devolución");
            return;
        }

        try {
            BigDecimal monto = new BigDecimal(montoTexto);

            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                AlertUtils.showWarning("Advertencia", "El monto debe ser mayor a cero");
                return;
            }

            if (monto.compareTo(ventaSeleccionada.getTotal()) > 0) {
                AlertUtils.showWarning("Advertencia", "El monto de devolución no puede ser mayor al total de la venta");
                return;
            }

            // Crear nueva devolución
            Devolucion devolucion = new Devolucion(
                ventaSeleccionada,
                motivo,
                monto,
                sessionManager.getUsuarioActual()
            );

            if (devolucionDAO.guardar(devolucion)) {
                AlertUtils.showInfo("Éxito", "Devolución creada correctamente");
                limpiarFormulario();
                cargarDevoluciones();
            } else {
                AlertUtils.showError("Error", "No se pudo crear la devolución");
            }

        } catch (NumberFormatException e) {
            AlertUtils.showWarning("Advertencia", "Ingrese un monto válido");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error creando devolución: " + e.getMessage());
        }
    }

    @FXML
    private void filtrarDevoluciones() {
        cargarDevoluciones();
    }

    private void cargarDevoluciones() {
        try {
            List<Devolucion> devoluciones;
            String estado = cmbEstado.getValue();
            LocalDate fechaDesde = dpFechaDesde.getValue();
            LocalDate fechaHasta = dpFechaHasta.getValue();

            if (fechaDesde != null && fechaHasta != null) {
                devoluciones = devolucionDAO.obtenerPorRangoFechas(fechaDesde, fechaHasta);

                // Filtrar por estado si no es "Todos"
                if (!"Todos".equals(estado)) {
                    devoluciones.removeIf(d -> !estado.equals(d.getEstado()));
                }
            } else if (!"Todos".equals(estado)) {
                devoluciones = devolucionDAO.obtenerPorEstado(estado);
            } else {
                devoluciones = devolucionDAO.obtenerTodas();
            }

            listaDevoluciones.clear();
            listaDevoluciones.addAll(devoluciones);

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error cargando devoluciones: " + e.getMessage());
        }
    }

    private void seleccionarDevolucion(Devolucion devolucion) {
        devolucionSeleccionada = devolucion;

        // Mostrar información detallada
        StringBuilder detalle = new StringBuilder();
        detalle.append("INFORMACIÓN DE LA DEVOLUCIÓN\n");
        detalle.append("================================\n");
        detalle.append("Número: ").append(devolucion.getNumeroDevolucion()).append("\n");
        detalle.append("Venta Original: ").append(devolucion.getVentaOriginal().getNumeroVenta()).append("\n");
        detalle.append("Monto: $").append(devolucion.getMontoTotal()).append("\n");
        detalle.append("Estado: ").append(devolucion.getEstado()).append("\n");
        detalle.append("Motivo: ").append(devolucion.getMotivo()).append("\n");
        detalle.append("Procesado por: ").append(
            devolucion.getProcesadoPor().getNombre() + " " + devolucion.getProcesadoPor().getApellido()
        ).append("\n");

        if (devolucion.getAutorizadoPor() != null) {
            detalle.append("Autorizado por: ").append(
                devolucion.getAutorizadoPor().getNombre() + " " + devolucion.getAutorizadoPor().getApellido()
            ).append("\n");
        }

        if (devolucion.getObservaciones() != null && !devolucion.getObservaciones().isEmpty()) {
            detalle.append("Observaciones: ").append(devolucion.getObservaciones()).append("\n");
        }

        txtDetalleDevolucion.setText(detalle.toString());

        // Habilitar/deshabilitar botones según el estado y permisos
        boolean puedeAutorizar = devolucion.puedeSerProcesada() &&
                               sessionManager.getUsuarioActual().getRol().getNombreRol().equals("GERENTE") ||
                               sessionManager.getUsuarioActual().getRol().getNombreRol().equals("ADMINISTRADOR");

        btnAprobar.setDisable(!puedeAutorizar);
        btnRechazar.setDisable(!puedeAutorizar);
    }

    @FXML
    private void aprobarDevolucion() {
        if (devolucionSeleccionada == null) {
            AlertUtils.showWarning("Advertencia", "Seleccione una devolución");
            return;
        }

        Optional<String> observaciones = AlertUtils.showInputDialog(
            "Aprobar Devolución",
            "Ingrese observaciones (opcional):",
            ""
        );

        if (observaciones.isPresent()) {
            try {
                devolucionSeleccionada.aprobar(sessionManager.getUsuarioActual(), observaciones.get());

                if (devolucionDAO.actualizar(devolucionSeleccionada)) {
                    AlertUtils.showInfo("Éxito", "Devolución aprobada correctamente");
                    cargarDevoluciones();
                    seleccionarDevolucion(devolucionSeleccionada);
                } else {
                    AlertUtils.showError("Error", "No se pudo aprobar la devolución");
                }
            } catch (Exception e) {
                AlertUtils.showError("Error", "Error aprobando devolución: " + e.getMessage());
            }
        }
    }

    @FXML
    private void rechazarDevolucion() {
        if (devolucionSeleccionada == null) {
            AlertUtils.showWarning("Advertencia", "Seleccione una devolución");
            return;
        }

        Optional<String> motivo = AlertUtils.showInputDialog(
            "Rechazar Devolución",
            "Ingrese el motivo del rechazo:",
            ""
        );

        if (motivo.isPresent() && !motivo.get().trim().isEmpty()) {
            try {
                devolucionSeleccionada.rechazar(sessionManager.getUsuarioActual(), motivo.get());

                if (devolucionDAO.actualizar(devolucionSeleccionada)) {
                    AlertUtils.showInfo("Información", "Devolución rechazada");
                    cargarDevoluciones();
                    seleccionarDevolucion(devolucionSeleccionada);
                } else {
                    AlertUtils.showError("Error", "No se pudo rechazar la devolución");
                }
            } catch (Exception e) {
                AlertUtils.showError("Error", "Error rechazando devolución: " + e.getMessage());
            }
        } else {
            AlertUtils.showWarning("Advertencia", "Debe ingresar un motivo para el rechazo");
        }
    }

    private void limpiarFormulario() {
        txtBuscarVenta.clear();
        txtMotivo.clear();
        txtMontoDevolucion.clear();
        lblVentaInfo.setText("");
        ventaSeleccionada = null;
        btnCrearDevolucion.setDisable(true);
    }
}
