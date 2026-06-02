package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Feriante;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Feriante.Producto;
import java.util.List;
import java.util.stream.Collectors;

public class ProductoDTO {
    private int id;
    private String nombre;
    private String descripcion;
    private double precio;
    private boolean activo;
    private String categoriaNombre;
    private String imagenUrl;
    private int categoriaId;
    private String tipoVenta;
    private String unidadMedida;
    private String feriaNombre;
    private String standNombre;
    private int usuarioDueñoId;
    private Double promedioEstrellas;
    private int cantidadResenas;
    private String contactoTelefono;

    private List<ImagenDetalleDTO> galeria;

    public ProductoDTO(Producto producto) {
        this.id = producto.getId();
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.precio = producto.getPrecio();
        this.activo = producto.isActivo();
        this.imagenUrl = producto.getImagenUrl();

        if (producto.getImagenes() != null) {
            this.galeria = producto.getImagenes().stream()
                    .map(img -> new ImagenDetalleDTO((long) img.getId(), img.getUrl()))
                    .collect(Collectors.toList());
        }

        if (producto.getTipoVenta() != null) {
            this.tipoVenta = producto.getTipoVenta().name();
        }
        this.unidadMedida = producto.getUnidadMedida();

        if (producto.getCategoria() != null) {
            this.categoriaNombre = producto.getCategoria().getNombre();
            this.categoriaId = producto.getCategoria().getId();
        } else {
            this.categoriaNombre = "Sin categoría";
            this.categoriaId = 0;
        }

        // 🟢 RELACIÓN CORREGIDA: Stand -> Participaciones -> Edición -> Feria
        if (producto.getStand() != null &&
                producto.getStand().getParticipaciones() != null &&
                !producto.getStand().getParticipaciones().isEmpty()) {

            // Pasamos de forma segura por la edición cronológica antes de pedir el nombre de la feria base
            this.feriaNombre = producto.getStand().getParticipaciones().get(0).getEdicion().getFeria().getNombre();
        } else {
            this.feriaNombre = "Feria General";
        }

        if (producto.getStand() != null) {
            this.standNombre = producto.getStand().getNombre();
        } else {
            this.standNombre = "Stand General";
        }

        if (producto.getStand() != null && producto.getStand().getFeriante() != null) {
            this.usuarioDueñoId = producto.getStand().getFeriante().getUsuario().getId();
            this.contactoTelefono = producto.getStand().getFeriante().getTelefono();
        }
    }

    public static class ImagenDetalleDTO {
        public long id;
        public String url;

        public ImagenDetalleDTO(long id, String url) {
            this.id = id;
            this.url = url;
        }
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio() { return precio; }
    public boolean isActivo() { return activo; }
    public String getImagenUrl() { return imagenUrl; }
    public String getCategoriaNombre() { return categoriaNombre; }
    public int getCategoriaId() { return categoriaId; }
    public String getTipoVenta() { return tipoVenta; }
    public String getUnidadMedida() { return unidadMedida; }
    public String getFeriaNombre() { return feriaNombre; }
    public String getStandNombre() { return standNombre; }
    public int getUsuarioDueñoId() { return usuarioDueñoId; }
    public int getCantidadResenas() { return cantidadResenas; }
    public Double getPromedioEstrellas() { return promedioEstrellas; }
    public void setCantidadResenas(int cantidadResenas) { this.cantidadResenas = cantidadResenas; }
    public void setPromedioEstrellas(Double promedioEstrellas) { this.promedioEstrellas = promedioEstrellas; }
    public List<ImagenDetalleDTO> getGaleria() { return galeria; }
    public String getContactoTelefono() { return contactoTelefono; }
}