package modelo;

public class Producto {
    
    private String codigo;
    private String nombre;
    private double precio;
    private int stock;
    
    private int pasillo; // Fila
    private int estante; // Columna

    public Producto(String codigo, String nombre, double precio, int stock, int pasillo, int estante) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.pasillo = pasillo;
        this.estante = estante;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    
    public void setStock(int stock) { this.stock = stock; }

    @Override
    public String toString() {
        return codigo + "," + nombre + "," + precio + "," + stock + "," + pasillo + "," + estante;
    }
}