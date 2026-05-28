package dao;

import com.mysql.cj.protocol.Resultset;
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
    public void eliminar (int id){
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
}
