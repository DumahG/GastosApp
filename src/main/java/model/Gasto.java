package model;

import java.time.LocalDateTime;

public class Gasto {
    private int id;
    private double monto;
    private String tienda;
    private String ubicacion;
    private LocalDateTime fechaHora;
    private int idCategoria;
    private String nombreCategoria;

    public Gasto(int id, double monto, String tienda, String ubicacion, LocalDateTime fechaHora, int idCategoria, String nombreCategoria){
        this.id = id;
        this.monto = monto;
        this.tienda = tienda;
        this.ubicacion = ubicacion;
        this.fechaHora = fechaHora;
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
    }

    public int getId() {return id;}
    public double getMonto(){return monto;}
    public String getTienda(){return tienda;}
    public String getUbicacion(){return ubicacion;}
    public LocalDateTime getFechaHora(){return fechaHora;}
    public int getIdCategoria(){return idCategoria;}
    public String getNombreCategoria(){return nombreCategoria;}
}
