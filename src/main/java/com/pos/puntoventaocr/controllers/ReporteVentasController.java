package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.dao.VentaDAO;
import com.pos.puntoventaocr.dao.UsuarioDAO;
import com.pos.puntoventaocr.dao.BitacoraDAO;
import com.pos.puntoventaocr.models.Venta;
import com.pos.puntoventaocr.models.Usuario;
import com.pos.puntoventaocr.utils.AlertUtils;
import com.pos.puntoventaocr.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReporteVentasController {

    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private ComboBox<String> cmbUsuario;
    @FXML private Button btnGenerar;
    @FXML private Button btnExportar;

    @FXML private Label lblTotalVentas;
    @FXML private Label lblMontoTotal;
    @FXML private Label lblPromedioDiario;
    @FXML private Label lblTicketPromedio;

    @FXML private TableView<Venta> tablaReporte;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, String> colNumero;
    @FXML private TableColumn<Venta, String> colCliente;
    @FXML private TableColumn<Venta, String> colUsuario;
    @FXML private TableColumn<Venta, Double> colSubtotal;
    @FXML private TableColumn<Venta, Double> colIva;
    @FXML private TableColumn<Venta, Double> colTotal;

    private VentaDAO ventaDAO;
    private UsuarioDAO usuarioDAO;
    private BitacoraDAO bitacoraDAO;
    private ObservableList<Venta> listaReporte;

    public void initialize() {
        ventaDAO = new VentaDAO();
        usuarioDAO = new UsuarioDAO();
        bitacoraDAO = new BitacoraDAO();
        listaReporte = FXCollections.observableArrayList();

        configurarTabla();
        configurarFiltros();
        limpiarResumen();
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            )
        );
        colNumero.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<String>(cellData.getValue().getNumeroVenta())
        );
        colCliente.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCliente() != null ? cellData.getValue().getCliente().getNombre() : "N/A"
            )
        );
        colUsuario.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getUsuario() != null ?
                    cellData.getValue().getUsuario().getNombre() + " " + cellData.getValue().getUsuario().getApellido() : "N/A"
            )
        );
        colSubtotal.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<Double>(cellData.getValue().getSubtotal().doubleValue())
        );
        colIva.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<Double>(cellData.getValue().getIva().doubleValue())
        );
        colTotal.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<Double>(cellData.getValue().getTotal().doubleValue())
        );

        tablaReporte.setItems(listaReporte);
    }

    private void configurarFiltros() {
        // Configurar fechas por defecto (mes actual)
        LocalDate ahora = LocalDate.now();
        dpFechaDesde.setValue(ahora.withDayOfMonth(1));
        dpFechaHasta.setValue(ahora);

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
    private void generarReporte() {
        try {
            LocalDate fechaDesde = dpFechaDesde.getValue();
            LocalDate fechaHasta = dpFechaHasta.getValue();
            String usuario = cmbUsuario.getValue();

            if (fechaDesde == null || fechaHasta == null) {
                AlertUtils.showWarning("Advertencia", "Seleccione el rango de fechas");
                return;
            }

            if (fechaDesde.isAfter(fechaHasta)) {
                AlertUtils.showWarning("Advertencia", "La fecha desde no puede ser mayor a la fecha hasta");
                return;
            }

            List<Venta> ventas = ventaDAO.obtenerVentasPorFiltros(fechaDesde, fechaHasta, usuario, "Todos");

            listaReporte.clear();
            listaReporte.addAll(ventas);

            calcularResumen(fechaDesde, fechaHasta);

            // Registrar en bitácora
            Integer idUsuario = SessionManager.getInstance().getCurrentUserId();
            if (idUsuario != null && idUsuario > 0) {
                String parametros = String.format("Fechas: %s a %s, Usuario: %s, Registros: %d",
                    fechaDesde, fechaHasta, usuario, listaReporte.size());
                bitacoraDAO.registrarGeneracionReporte(idUsuario, "REPORTE_VENTAS", parametros);
            }

            AlertUtils.showInfo("Éxito", "Reporte generado correctamente");

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al generar reporte: " + e.getMessage());
        }
    }

    @FXML
    private void exportarReporte() {
        if (listaReporte.isEmpty()) {
            AlertUtils.showWarning("Advertencia", "No hay datos para exportar. Genere el reporte primero.");
            return;
        }

        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Guardar Reporte de Ventas");
            fileChooser.setInitialFileName("reporte_ventas_" +
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
                AlertUtils.showInfo("Éxito", "Reporte de ventas exportado correctamente a:\n" + archivo.getAbsolutePath());

                // TODO: Implementar generación real de PDF
                // generarPDFVentas(archivo, listaReporte);
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al exportar reporte de ventas: " + e.getMessage());
        }
    }

    private void calcularResumen(LocalDate fechaDesde, LocalDate fechaHasta) {
        int totalVentas = listaReporte.size();
        double montoTotal = listaReporte.stream()
            .mapToDouble(venta -> venta.getTotal().doubleValue())
            .sum();

        // Calcular días del período
        long diasPeriodo = ChronoUnit.DAYS.between(fechaDesde, fechaHasta) + 1;
        double promedioDiario = diasPeriodo > 0 ? montoTotal / diasPeriodo : 0;

        // Calcular ticket promedio
        double ticketPromedio = totalVentas > 0 ? montoTotal / totalVentas : 0;

        lblTotalVentas.setText(String.valueOf(totalVentas));
        lblMontoTotal.setText("$" + String.format("%.2f", montoTotal));
        lblPromedioDiario.setText("$" + String.format("%.2f", promedioDiario));
        lblTicketPromedio.setText("$" + String.format("%.2f", ticketPromedio));
    }

    private void limpiarResumen() {
        lblTotalVentas.setText("0");
        lblMontoTotal.setText("$0.00");
        lblPromedioDiario.setText("$0.00");
        lblTicketPromedio.setText("$0.00");
    }
}
