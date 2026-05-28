package model;

import java.util.stream.Stream;

public class Categoria {
    private int id;
    private String nombre;
    private String tipo;

    public Categoria(int id, String nombre, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public int getId() {return id;}
    public String getNombre() {return nombre;}
    public String getTipo() {return tipo;}

    @Override
    public String toString() {return nombre;}
}
