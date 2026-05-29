package dao;

import model.Gasto;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GastoDAO {

    // Registrar gasto usando procedimeinto almacenado
    public void registrar(double monto, String tienda, String ubicacion, int idCategoria){
        try (Connection conn = ConexionDB.conectar();
            CallableStatement cs = conn.prepareCall("{call registrar_gasto (?,?,?,?)}")){
            cs.setDouble(1, monto);
            cs.setString(2, tienda);
            cs.setString(3, ubicacion);
            cs.setInt(4, idCategoria);
            cs.execute();
            System.out.println("Gasto registrado correctamente.");
        } catch (SQLException e){
            System.out.println("Error: " + e.getMessage());
        }
    }

// Listar gastos del mes con JOIN
public List<Gasto> listarPorMes(int mes, int anio) {
    List<Gasto> lista = new ArrayList<>();
    String sql = """
            SELECT g.id, g.monto, g.tienda, g.ubicacion,
                   g.fecha_hora, g.id_categoria, c.nombre
            FROM gastos g
            JOIN categorias c ON g.id_categoria = c.id
            WHERE MONTH(g.fecha_hora) = ? AND YEAR(g.fecha_hora) = ?
            ORDER BY g.fecha_hora DESC
            """;
    try (Connection conn = ConexionDB.conectar();
        PreparedStatement ps = conn.prepareStatement(sql)){
        ps.setInt(1, mes);
        ps.setInt(2, anio);
        ResultSet rs = ps.executeQuery();
        while (rs.next()){
            lista.add(new Gasto(
                    rs.getInt("id"),
                    rs.getDouble("monto"),
                    rs.getString("tienda"),
                    rs.getString("ubicacion"),
                    rs.getTimestamp("fecha_hora").toLocalDateTime(),
                    rs.getInt("id_categoria"),
                    rs.getString("nombre")
            ));
        }
    } catch (SQLException e){
        System.out.println("Error : " + e.getMessage());
    }
    return lista;
    }

    // Eliminar gasto
    public void eliminar(int id){
        String sql = "delete from gastos where id = ?";
        try (Connection conn = ConexionDB.conectar();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Resumen mensual usando procedimiento almacenado
    public ResultSet resumenMensual(int mes, int anio){
        try{
            Connection conn = ConexionDB.conectar();
            CallableStatement cs = conn.prepareCall("{call resumen_mensual(?, ?)}");
            cs.setInt(1, mes);
            cs.setInt(2, anio);
            cs.execute();
            return cs.getResultSet();
        } catch (SQLException e){
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    //Estimacion cierre de mes usando procedimiento almacenado
    public ResultSet estimacionCierre(int mes, int anio){
        try{
            Connection conn = ConexionDB.conectar();
            CallableStatement cs = conn.prepareCall("{call estimacion_cierre(?, ?)}");
            cs.setInt(1, mes);
            cs.setInt(2, anio);
            return cs.getResultSet();
        } catch (SQLException e){
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    // Calcular ahorro usando procedimiento almacenado
    public ResultSet calcularAhorro(int mes, int anio){
        try{
            Connection conn = ConexionDB.conectar();
            CallableStatement cs = conn.prepareCall("{call calcular_ahorro(?, ?)}");
            cs.setInt(1, mes);
            cs.setInt(2, anio);
            cs.execute();
            return cs.getResultSet();
        } catch (SQLException e){
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    // Guardar o actualizar presupuesto mensual
    public void guardarPresupuesto(double limite, int mes, int anio) {
        String sqlCheck = "SELECT id FROM presupuesto WHERE mes = ? AND anio = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
            ps.setInt(1, mes);
            ps.setInt(2, anio);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Ya existe — actualizar
                try (PreparedStatement psUpdate = conn.prepareStatement(
                        "UPDATE presupuesto SET monto_limite = ? WHERE mes = ? AND anio = ?")) {
                    psUpdate.setDouble(1, limite);
                    psUpdate.setInt(2, mes);
                    psUpdate.setInt(3, anio);
                    psUpdate.executeUpdate();
                }
            } else {
                // No existe — insertar
                try (PreparedStatement psInsert = conn.prepareStatement(
                        "INSERT INTO presupuesto (monto_limite, mes, anio) VALUES (?, ?, ?)")) {
                    psInsert.setDouble(1, limite);
                    psInsert.setInt(2, mes);
                    psInsert.setInt(3, anio);
                    psInsert.executeUpdate();
                }
            }
            System.out.println("Presupuesto guardado correctamente.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    //Obtener presupuesto del mes
    public double obtenerPresupuesto (int mes, int anio){
        String sql = "select monto_limite from presupuesto where mes = ? and anio = ?";
        try(Connection conn = ConexionDB.conectar();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, mes);
            ps.setInt(2, anio);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return rs.getDouble("monto_limite");
            }
        }catch (SQLException e){
            System.out.println("Error: " + e.getMessage());
        }
        return 0;
    }

    // Obtener total gastado en el mes
    public double obtenerTotalMes(int mes, int anio) {
        String sql = "SELECT COALESCE(SUM(monto), 0) as total FROM gastos " +
                "WHERE MONTH(fecha_hora) = ? AND YEAR(fecha_hora) = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mes);
            ps.setInt(2, anio);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return 0;
    }
}
