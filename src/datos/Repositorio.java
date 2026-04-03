package datos;

import java.io.*;
import java.util.ArrayList;
import modelo.Producto;
import java.time.LocalDate;

public class Repositorio {
    private final String ARCHIVO = "inventario.txt";

    public ArrayList<Producto> cargarProductos() {
        
        ArrayList<Producto> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length == 6) {
                   
                    Producto p = new Producto(
                        datos[0], datos[1], //código y nombre
                        Double.parseDouble(datos[2]), //precio
                        Integer.parseInt(datos[3]), //stock
                        Integer.parseInt(datos[4]), //pasillo
                        Integer.parseInt(datos[5])  //estante
                    );
                    lista.add(p);
                }
            }
        } catch (Exception e) { //archivo no existe
            System.out.println("Error leyendo: " + e.getMessage());
        }
        return lista;
    }

    // --- 2. GUARDAR (ESCRIBIR) ---
    public void guardarProductos(ArrayList<Producto> lista) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            //borra todo el contenido del archivo par reescribirlo desde 0 
            for (Producto p : lista) {
                bw.write(p.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error guardando: " + e.getMessage());
        }
    }

    // --- 3. AGREGAR NUEVO ---
    public void agregarProducto(Producto nuevo) {
        ArrayList<Producto> lista = cargarProductos();
        lista.add(nuevo);
        guardarProductos(lista);
    }

    // --- 4. ACTUALIZAR STOCK  ---
    public void actualizarStock(String nombre, int nuevoStock) {
        ArrayList<Producto> lista = cargarProductos();//sube todo a memoria
        for (Producto p : lista) {//busca uno por uno
            if (p.getNombre().equals(nombre)) {//verifica el nombre del producto
                p.setStock(nuevoStock); //cambia el número de stock en la memoria
                break;
            }
        }
        guardarProductos(lista); //guarda los cambios en el archivo
    }

    // --- 5. ELIMINAR PRODUCTO  ---
    public void eliminarProducto(String nombre) {
        ArrayList<Producto> lista = cargarProductos();
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getNombre().equals(nombre)) {
                lista.remove(i);
                break;
            }
        }
        guardarProductos(lista);
    }

    // --- 6. HISTORIAL VENTAS (REGISTRO INDIVIDUAL) ---
    public void registrarVentaDiaria(String boleta) {   
        try {
            String fecha = LocalDate.now().toString();//obtiene la fecha, día y hora
            File carpeta = new File("Registro_Ventas");//prepara una carpeta nueva
            if (!carpeta.exists()) carpeta.mkdir();//si la carpeta no existe la crea automaticamente con .mkdir
            
            File archivo = new File(carpeta, fecha + ".txt");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, true))) {//agrega la nueva boleta al final
                bw.write(boleta);
                bw.newLine();
                bw.write("-----------------------------------");//separata de boletas
                bw.newLine();
            }
        } catch (Exception e) {
            System.out.println("Error historial: " + e.getMessage());
        }
    }

    
    
    
    
    // --- 7. CIERRE DE CAJA (NUEVO: RESUMEN DEL DÍA) ---
    public void generarCierreCaja() {
        String fechaHoy = LocalDate.now().toString();
        
        File carpeta = new File("Registro_Ventas");//crae un objeto que represente a la carpeta(directorio)
        File archivoVentas = new File(carpeta, fechaHoy + ".txt"); //esto es la ruta completo "Registro_Ventas/2025-12-19.txt"
        
        if (!archivoVentas.exists()) {
            javax.swing.JOptionPane.showMessageDialog(null, "No hay ventas registradas hoy.");
            return;
        }

        // Variables de Dinero
        double totalGeneral = 0;
        double totalEfectivo = 0;
        double totalYape = 0;
        double totalVisa = 0;
        
        // Variable para contar Productos: Mapa <NombreProducto, CantidadTotal>
        java.util.HashMap<String, Integer> conteoProductos = new java.util.HashMap<>();
        
        String ultimoMetodoDetectado = ""; 

        try (BufferedReader br = new BufferedReader(new FileReader(archivoVentas))) {
            String linea;   
            
            while ((linea = br.readLine()) != null) {
                
                // 1. Detectar Método de Pago
                if (linea.contains("METODO PAGO:")) {
                    ultimoMetodoDetectado = linea.split(":")[1].trim(); //se guarda que tipo de moneda se usó para pagar 
                }
                
                // 2. Detectar y Sumar Totales (Dinero)
                if (linea.contains("TOTAL PAGADO: S/")) {
                    String numeroTexto = linea.split("S/ ")[1].trim();
                    double monto = Double.parseDouble(numeroTexto);
                    totalGeneral += monto;
                    
                    if (ultimoMetodoDetectado.equals("EFECTIVO")) totalEfectivo += monto;
                    else if (ultimoMetodoDetectado.contains("YAPE") || ultimoMetodoDetectado.contains("PLIN")) totalYape += monto;
                    else if (ultimoMetodoDetectado.contains("TARJETA") || ultimoMetodoDetectado.contains("VISA")) totalVisa += monto;
                }
                
                // 3. DETECTAR PRODUCTOS (La línea empieza con guion " - ")
                // Ejemplo de línea: " - Cemento Sol (x2) : S/ 57.0"
                if (linea.trim().startsWith("-")) {
                    try {
                        // Magia para sacar el nombre y la cantidad del texto
                        int inicioNombre = linea.indexOf("-") + 1;
                        int finNombre = linea.indexOf("(x");
                        
                        // Extraemos el nombre (Ej: "Cemento Sol")
                        String nombreProd = linea.substring(inicioNombre, finNombre).trim();
                        
                        // Extraemos la cantidad (Ej: "2")
                        int inicioCant = finNombre + 2; // Saltamos el "(x"
                        int finCant = linea.indexOf(")");
                        String cantTexto = linea.substring(inicioCant, finCant);
                        int cantidad = Integer.parseInt(cantTexto);
                        
                        // AGREGAMOS AL MAPA (Si ya existe, sumamos la cantidad nueva)
                        if (conteoProductos.containsKey(nombreProd)) {
                            int cantidadActual = conteoProductos.get(nombreProd);
                            conteoProductos.put(nombreProd, cantidadActual + cantidad);
                        } else {
                            conteoProductos.put(nombreProd, cantidad);
                        }
                        
                    } catch (Exception e) {
                        // Si una línea tiene formato raro, la ignoramos para no romper el reporte
                    }
                }
            }
            
            // 4. GENERAR EL REPORTE FINAL
            File archivoCierre = new File(carpeta, "CIERRE_" + fechaHoy + ".txt");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivoCierre))) {
                bw.write("======================================="); bw.newLine();
                bw.write("       REPORTE DE CIERRE DE CAJA       "); bw.newLine();
                bw.write("       FECHA: " + fechaHoy); bw.newLine();
                bw.write("======================================="); bw.newLine();
                bw.write(" VENTAS EN EFECTIVO : S/ " + totalEfectivo); bw.newLine();
                bw.write(" VENTAS CON YAPE    : S/ " + totalYape); bw.newLine();
                bw.write(" VENTAS CON TARJETA : S/ " + totalVisa); bw.newLine();
                bw.write("---------------------------------------"); bw.newLine();
                bw.write(" GANANCIA TOTAL     : S/ " + totalGeneral); bw.newLine();
                bw.write("======================================="); bw.newLine();
                bw.write("       DETALLE DE PRODUCTOS VENDIDOS   "); bw.newLine();
                bw.write("======================================="); bw.newLine();
                
                // Escribimos la lista de productos acumulados
                // Escribimos la lista de productos acumulados CON ALINEACIÓN
                for (String nombre : conteoProductos.keySet()) {
                    int cantidadFinal = conteoProductos.get(nombre);
                    String lineaBonita = String.format(" * %-25s : %5d und.", nombre, cantidadFinal);
                    bw.write(lineaBonita);
                    bw.newLine();
                }
                bw.write("======================================="); bw.newLine();
            }
            
            javax.swing.JOptionPane.showMessageDialog(null, 
                    "¡Cierre de Caja Exitoso!\nArchivo: CIERRE_" + fechaHoy + ".txt");

        } catch (Exception e) { 
            System.out.println("Error al cerrar caja: " + e.getMessage());
        }   
    }
}