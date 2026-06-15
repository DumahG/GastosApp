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
    @FXML private Label lblPresupuesto;

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
        actualizarDashboard();
    }

    private void  configurarTabla(){
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colTienda.setCellValueFactory(new PropertyValueFactory<>("tienda"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("nombreCategoria"));
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
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
            // Total gastado directo
            double total = gastoDAO.obtenerTotalMes(mesActual, anioActual);
            double presupuesto = gastoDAO.obtenerPresupuesto(mesActual, anioActual);
            double ahorro = presupuesto - total;

            lblTotalGastado.setText(String.format("$ %.0f", total));
            lblPresupuesto.setText(String.format("$ %.0f", presupuesto));
            lblAhorro.setText(String.format("$ %.0f", ahorro));

            // Estimación cierre
            ResultSet rsEst = gastoDAO.estimacionCierre(mesActual, anioActual);
            if (rsEst != null && rsEst.next()) {
                lblEstimacion.setText(String.format("$ %.0f",
                        rsEst.getDouble("estimacion_cierre_mes")));
            }

            // Estado
            if (total > presupuesto) {
                lblEstado.setText("Presupuesto excedido");
            } else if (ahorro < presupuesto * 0.1) {
                lblEstado.setText("Cerca del limite");
            } else {
                lblEstado.setText("En control");
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
            return; // ← agregar return para que no siga ejecutando
        }
        gastoDAO.eliminar(seleccionado.getId()); // ← minúscula, instancia no static
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

    public void guardarPresupuesto(){
        try{
            double limite = Double.parseDouble(txtPresupuesto.getText());
            gastoDAO.guardarPresupuesto(limite, mesActual, anioActual);
            txtPresupuesto.clear();
            actualizarDashboard();
        } catch (NumberFormatException e){
            mostrarAlerta("Ingresa un monto valido para el presupuesto");
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
}
