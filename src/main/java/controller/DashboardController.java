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

public class DashboardController {

    // Labels del dashboard
    @FXML private Label lblTotalGastado;
    @FXML private Label lblEstimacion;
    @FXML private Label lblAhorro;
    @FXML private Label lblEstado;

    // Formulario registro
    @FXML private TextField txtMonto;
    @FXML private TextField txtTienda;
    @FXML private TextField txtUbicacion;
    @FXML private ComboBox<Categoria> cmbCategoria;

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
    private int mesActual = LocalDateTime.now().getMonthValue();
    private int anioActual = LocalDateTime.now().getYear();

    @FXML
    public  void initialize(){
        configurarTabla();
        cargarCategorias();
        cargarGastos();
        actualizarDashboard();
    }

    private void  configurarTabla(){
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colTienda.setCellValueFactory(new PropertyValueFactory<>("tienda"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("NombreCategoria"));
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
    }

    private void cargarCategorias (){
        List<Categoria> lista = categoriaDAO.obtenerTodas();
        cmbCategoria.setItems(FXCollections.observableArrayList(lista));
    }

    private void actualizarDashboard(){
        try {
            // Estimación cierre
            ResultSet rsEst = gastoDAO.estimacionCierre(mesActual, anioActual);
            if (rsEst != null && rsEst.next()) {
                lblTotalGastado.setText("$ " + rsEst.getDouble("gastado_hasta_hoy"));
                lblEstimacion.setText("$ " + rsEst.getDouble("estimacion_cierre_mes"));
            }
            // Ahorro
            ResultSet rsAhorro = gastoDAO.calcularAhorro(mesActual, anioActual);
            if (rsAhorro != null && rsAhorro.next()) {
                lblAhorro.setText("$ " + rsAhorro.getDouble("ahorro_posible"));
                lblEstado.setText(rsAhorro.getString("estado"));
            }
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
        } catch (NumberFormatException e){
            mostrarAlerta("El monto debe ser un número válido.");
        }
    }

    @FXML
    public void eliminarGasto(){
        Gasto seleccionado = tablaGastos.getSelectionModel().getSelectedItem();
        if(seleccionado == null ){
            mostrarAlerta("Selecciona un gasto de la tabla.");
            cargarGastos();
            actualizarDashboard();
        }
        GastoDAO.eliminar(seleccionado.getId());
        cargarGastos();
        actualizarDashboard();
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
}
