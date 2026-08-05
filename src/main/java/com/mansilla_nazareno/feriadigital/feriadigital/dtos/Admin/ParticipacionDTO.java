package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin;

import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Participacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoParticipacion;
import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoPago;

public class ParticipacionDTO {

    private int id;

    private Integer edicionId;
    private String nombreEdicion;
    private String nombreFeriaBase;

    private Integer standId;
    private String stand;

    // 🟢 ESTOS SON LOS 3 ATRIBUTOS QUE FALTABAN DECLARAR:
    private EstadoParticipacion estado;
    private EstadoPago estadoPago;
    private Double montoAbonado;

    private Integer espacioId;
    private String espacioNombre;
    private Double espacioPrecio;
    private Integer numeroStandPreferido;

    public ParticipacionDTO() {}

    public ParticipacionDTO(Participacion participacion) {
        this.id = participacion.getId();
        this.numeroStandPreferido = participacion.getNumeroStandPreferido();

        // MAPEO SEGURO DESDE EDICIÓN
        if (participacion.getEdicion() != null) {
            this.edicionId = participacion.getEdicion().getId();
            this.nombreEdicion = participacion.getEdicion().getNombreEdicion();

            // Traemos el nombre de la plantilla base a través de la edición
            if (participacion.getEdicion().getFeria() != null) {
                this.nombreFeriaBase = participacion.getEdicion().getFeria().getNombre();
            }
        }

        if (participacion.getStand() != null) {
            this.standId = participacion.getStand().getId();
            this.stand = participacion.getStand().getNombre();
        }

        this.estado = participacion.getEstado();
        this.estadoPago = participacion.getEstadoPago();
        this.montoAbonado = participacion.getMontoAbonado();

        if (participacion.getEspacio() != null) {
            this.espacioId = participacion.getEspacio().getId();
            this.espacioNombre = participacion.getEspacio().getNombre();
            this.espacioPrecio = participacion.getEspacio().getPrecio();
        }
    }

    // --- GETTERS Y SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getEdicionId() { return edicionId; }
    public void setEdicionId(Integer edicionId) { this.edicionId = edicionId; }

    public String getNombreEdicion() { return nombreEdicion; }
    public void setNombreEdicion(String nombreEdicion) { this.nombreEdicion = nombreEdicion; }

    public String getNombreFeriaBase() { return nombreFeriaBase; }
    public void setNombreFeriaBase(String nombreFeriaBase) { this.nombreFeriaBase = nombreFeriaBase; }

    public Integer getStandId() { return standId; }
    public void setStandId(Integer standId) { this.standId = standId; }

    public String getStand() { return stand; }
    public void setStand(String stand) { this.stand = stand; }

    public EstadoParticipacion getEstado() { return estado; }
    public void setEstado(EstadoParticipacion estado) { this.estado = estado; }

    public EstadoPago getEstadoPago() { return estadoPago; }
    public void setEstadoPago(EstadoPago estadoPago) { this.estadoPago = estadoPago; }

    public Double getMontoAbonado() { return montoAbonado; }
    public void setMontoAbonado(Double montoAbonado) { this.montoAbonado = montoAbonado; }

    public Integer getEspacioId() { return espacioId; }
    public void setEspacioId(Integer espacioId) { this.espacioId = espacioId; }

    public String getEspacioNombre() { return espacioNombre; }
    public void setEspacioNombre(String espacioNombre) { this.espacioNombre = espacioNombre; }

    public Double getEspacioPrecio() { return espacioPrecio; }
    public void setEspacioPrecio(Double espacioPrecio) { this.espacioPrecio = espacioPrecio; }

    public Integer getNumeroStandPreferido() { return numeroStandPreferido; }
    public void setNumeroStandPreferido(Integer n) { this.numeroStandPreferido = n; }
}