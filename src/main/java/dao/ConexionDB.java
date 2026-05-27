package dao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL = "jdbc:mysql://localhost:3306/gastosapp";
    private static final String USER = "root";
    private static final String PASSWORD = "mysql";

    public static Connection conectar(){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e){
            System.out.println("Error al conectar: " + e.getMessage());
        }
        return  conn;
    }

}
