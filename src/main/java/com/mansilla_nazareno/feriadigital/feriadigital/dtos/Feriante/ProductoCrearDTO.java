package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante;

public class ProductoCrearDTO {
    private String nombre;
    private String descripcion;
    private double precio;
    private String imagen;
    private String tipoVenta; // Recibe "PESO", "LONGITUD" o "UNIDAD"
    private String unidadMedida;

    // Getters
    public String getTipoVenta() { return tipoVenta; }
    public String getUnidadMedida() { return unidadMedida; }
    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public String getImagen() {
        return imagen;
    }


}
