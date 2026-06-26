package controller;

import com.mysql.cj.log.NullLogger;
import  dao.GastoDAO;
import dao.CategoriaDAO;
import model.Gasto;
import model.Categoria;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import javafx.scene.layout.VBox;
import dao.AhorroDAO;
import java.time.format.DateTimeFormatter;


public class DashboardController {

    // Labels del dashboard
    @FXML private Label lblTotalGastado;
    @FXML private Label lblEstimacion;
    @FXML private Label lblAhorro;
    @FXML private Label lblEstado;
    @FXML private Label lblPresupuesto;
    @FXML private ComboBox<String> cmbMes;
    @FXML private ComboBox<Integer> cmbAnio;
    @FXML private VBox panelSobrante;
    @FXML private Label lblSobrante;
    @FXML private Label lblTotalAhorrado;

    // Formulario registro
    @FXML private TextField txtMonto;
    @FXML private TextField txtTienda;
    @FXML private TextField txtUbicacion;
    @FXML private TextField txtPresupuesto;
    @FXML private ComboBox<Categoria> cmbCategoria;

    // Categorias personalizadas
    @FXML private TextField txtNuevaCategoria;
    @FXML private ComboBox<Categoria> cmbEliminarCategoria;

    // Tabla historial
    @FXML private TableView<Gasto> tablaGastos;
    @FXML private TableColumn<Gasto, Integer> colId;
    @FXML private TableColumn<Gasto, Double> colMonto;
    @FXML private TableColumn<Gasto, String> colTienda;
    @FXML private TableColumn<Gasto, String> colCategoria;
    @FXML private TableColumn<Gasto, String> colUbicacion;
    @FXML private TableColumn<Gasto, LocalDateTime> colFecha;

    private GastoDAO gastoDAO = new GastoDAO();
    private CategoriaDAO categoriaDAO = new CategoriaDAO();
    private AhorroDAO ahorroDAO = new AhorroDAO();
    private int mesActual = LocalDateTime.now().getMonthValue();
    private int anioActual = LocalDateTime.now().getYear();

    @FXML
    public  void initialize(){
        configurarTabla();
        cargarCategorias();
        cargarGastos();
        actualizarDashboard();
        cargarPresupuesto();
        cargarCategoriasPersonalizadas();
        cargarSelectorMes();
        actualizarDashboard();
    }

    private void  configurarTabla(){
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colMonto.setCellFactory(column -> new TableCell<Gasto, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$ %,.0f", item));
            }
        });
        colTienda.setCellValueFactory(new PropertyValueFactory<>("tienda"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("nombreCategoria"));
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
        colFecha.setCellFactory(column -> new TableCell<Gasto, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
    }

    public  void cargarGastos(){
        List<Gasto> lista = gastoDAO.listarPorMes(mesActual, anioActual);
        tablaGastos.setItems(FXCollections.observableArrayList(lista));
    }

    private void cargarCategorias (){
        List<Categoria> lista = categoriaDAO.obtenerTodas();
        cmbCategoria.setItems(FXCollections.observableArrayList(lista));
    }

    private void actualizarDashboard() {
        try {
            double ingreso = gastoDAO.obtenerPresupuesto(mesActual, anioActual);
            double totalGastado = gastoDAO.obtenerTotalMes(mesActual, anioActual);
            double ahorro = ingreso - totalGastado;
            double totalAhorrado = ahorroDAO.obtenerTotalAhorrado();

            lblPresupuesto.setText(String.format("$ %.0f", ingreso));
            lblTotalGastado.setText(String.format("$ %.0f", totalGastado));
            lblAhorro.setText(String.format("$ %.0f", ahorro));

            // Color según estado
            if (ahorro < 0) {
                lblAhorro.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                lblEstado.setText("Ingreso excedido");
                panelSobrante.setVisible(false);
                panelSobrante.setManaged(false);
            } else if (ahorro < ingreso * 0.1) {
                lblAhorro.setStyle("-fx-text-fill: orange;");
                lblEstado.setText("Cerca del limite");
                panelSobrante.setVisible(false);
                panelSobrante.setManaged(false);
            } else {
                lblAhorro.setStyle("-fx-text-fill: green;");
                lblEstado.setText("En control");
                // Solo mostrar panel si hay ingreso registrado Y hay sobrante real
                if (ingreso > 0 && ahorro > 0) {
                    panelSobrante.setVisible(true);
                    panelSobrante.setManaged(true);
                    lblSobrante.setText(String.format("Tienes $ %.0f disponibles", ahorro));
                } else {
                    panelSobrante.setVisible(false);
                    panelSobrante.setManaged(false);
                }
            }

            // Estimación cierre
            ResultSet rsEst = gastoDAO.estimacionCierre(mesActual, anioActual);
            if (rsEst != null && rsEst.next()) {
                lblEstimacion.setText(String.format("$ %.0f",
                        rsEst.getDouble("estimacion_cierre_mes")));
            }

            // Total ahorrado
            lblTotalAhorrado.setText(String.format("Cuenta ahorro: $ %.0f", totalAhorrado));

        } catch (Exception e) {
            System.out.println("Error dashboard: " + e.getMessage());
        }
    }

    @FXML
    public void registrarGasto(){
        try{
            double monto = Double.parseDouble(txtMonto.getText());
            String tienda = txtTienda.getText();
            String ubicacion = txtUbicacion.getText();
            Categoria cat = cmbCategoria.getValue();

            if (tienda.isEmpty() || cat == null){
                mostrarAlerta("Por favor completa todos los campos.");
                return;
            }

            gastoDAO.registrar(monto, tienda, ubicacion, cat.getId());
            limpiarFormulario();
            cargarGastos();
            actualizarDashboard();
            mostrarExito("Gasto registrado correctamente.");
        } catch (NumberFormatException e){
            mostrarAlerta("El monto debe ser un número válido.");
        }
    }

    @FXML
    public void eliminarGasto(){
        Gasto seleccionado = tablaGastos.getSelectionModel().getSelectedItem();
        if(seleccionado == null ){
            mostrarAlerta("Selecciona un gasto de la tabla.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setContentText("¿Estás seguro de eliminar el gasto de "
                + seleccionado.getTienda() + " por $" + String.format("%,.0f", seleccionado.getMonto()) + "?");

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            gastoDAO.eliminar(seleccionado.getId());
            cargarGastos();
            actualizarDashboard();
            mostrarExito("Gasto eliminado correctamente.");
        }
    }

    private void limpiarFormulario(){
        txtMonto.clear();
        txtTienda.clear();
        txtUbicacion.clear();
        cmbCategoria.setValue(null);
    }

    private void mostrarAlerta(String mensaje){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void guardarPresupuesto(){
        try {
            double nuevoMonto = Double.parseDouble(txtPresupuesto.getText());
            double montoActual = gastoDAO.obtenerPresupuesto(mesActual, anioActual);

            if (montoActual > 0) {
                Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacion.setTitle("Confirmar actualización");
                confirmacion.setContentText("Ya existe un ingreso de $"
                        + String.format("%,.0f", montoActual) + " registrado para este mes.\n¿Deseas actualizarlo a $"
                        + String.format("%,.0f", nuevoMonto) + "?");

                if (confirmacion.showAndWait().get() != ButtonType.OK) {
                    return; // cancela, no hace nada
                }
            }

            gastoDAO.guardarPresupuesto(nuevoMonto, mesActual, anioActual);
            txtPresupuesto.clear();
            actualizarDashboard();
            mostrarExito("Ingreso guardado correctamente.");
        } catch (NumberFormatException e) {
            mostrarAlerta("Ingresa un monto válido para el ingreso.");
        }
    }

    private void cargarPresupuesto (){
        try {
            double presupuesto = gastoDAO.obtenerPresupuesto(mesActual, anioActual);
            lblPresupuesto.setText("$ " + presupuesto);
        } catch (Exception e){
            System.out.println("Error cargando presupuesto: " + e.getMessage());
        }
    }

    @FXML
    public void agregarCategoria(){
        String nombre = txtNuevaCategoria.getText().trim();
        if (nombre.isEmpty()){
            mostrarAlerta("Escribe un nombre para la categoria.");
            return;
        }
        categoriaDAO.agregar(nombre);
        txtNuevaCategoria.clear();
        cargarCategorias();
        cargarCategoriasPersonalizadas();
        mostrarExito("Categoría agregada correctamente.");
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void eliminarCategoria(){
        Categoria seleccionada = cmbEliminarCategoria.getValue();
        if (seleccionada == null){
            mostrarAlerta("Selecciona una categoria para eliminar.");
            return;
        }
        categoriaDAO.eliminar(seleccionada.getId());
        cargarCategorias();
        cargarCategoriasPersonalizadas();
        cmbEliminarCategoria.setValue(null);
        mostrarExito2("Categoría Eliminada correctamente.");
    }

    private void mostrarExito2(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cargarCategoriasPersonalizadas(){
        List<Categoria> personalizadas = categoriaDAO.obtenerPersonalizadas();
        cmbEliminarCategoria.setItems(FXCollections.observableArrayList(personalizadas));
    }

    // Cargar mes
    private void cargarSelectorMes() {
        cmbMes.setItems(FXCollections.observableArrayList(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        ));
        // Cargar anios
        cmbAnio.setItems(FXCollections.observableArrayList(
                2024, 2025, 2026, 2027
        ));
        cmbAnio.getSelectionModel().select(Integer.valueOf(anioActual));
    }

    @FXML
    public void consultarMes() {
        int mes = cmbMes.getSelectionModel().getSelectedIndex() + 1;
        int anio = cmbAnio.getValue();
        mesActual = mes;
        anioActual = anio;
        cargarGastos();
        cargarPresupuesto();
        actualizarDashboard();
    }

    @FXML
    public void guardarEnAhorro() {
        double sobrante = calcularSobrante();
        if (sobrante <= 0) {
            mostrarAlerta("No hay sobrante para guardar.");
            return;
        }
        ahorroDAO.registrarAhorro(sobrante, mesActual, anioActual,
                "Ahorro mes " + mesActual + "/" + anioActual);
        mostrarExito(String.format("$ %.0f guardado en cuenta de ahorro!", sobrante));
        actualizarDashboard();
    }

    @FXML
    public void agregarAlSiguienteMes() {
        double sobrante = calcularSobrante();
        if (sobrante <= 0) {
            mostrarAlerta("No hay sobrante para agregar.");
            return;
        }
        ahorroDAO.agregarAIngresoSiguiente(sobrante, mesActual, anioActual);
        mostrarExito(String.format("$ %.0f agregado al ingreso del mes siguiente!", sobrante));
        actualizarDashboard();
    }

    private double calcularSobrante() {
        double ingreso = gastoDAO.obtenerPresupuesto(mesActual, anioActual);
        double gastado = gastoDAO.obtenerTotalMes(mesActual, anioActual);
        return ingreso - gastado;
    }
}
