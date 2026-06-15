package dao;

import model.Categoria;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {
    // Obtener todas las categorías
    public List<Categoria> obtenerTodas() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "select * from categorias";
        try (Connection conn = ConexionDB.conectar();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Categoria(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("tipo")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return lista;
    }

    // Agregar categoria personalizada
    public void agregar(String nombre) {
        String sql = "insert into categorias (nombre, tipo) values (?, 'personalizada')";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            System.out.println("Categoria agregada.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Eliminar categoria personalizada
    public void eliminar(int id) {
        String sql = "delete from categorias where id = ? and tipo = 'personalizada'";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Categoria eliminada.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Obtener solo categorias personalizadas
    public List<Categoria> obtenerPersonalizadas(){
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categorias WHERE tipo = 'personalizada'";
        try (Connection conn  = ConexionDB.conectar();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)){
            while (rs.next()){
                lista.add(new Categoria(
                   rs.getInt("id"),
                   rs.getString("nombre"),
                   rs.getString("tipo")
                ));
            }
        } catch (SQLException e ) {
            System.out.println("Error : " + e.getMessage());
        }
        return lista;
    }

}
