    package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin;

    import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.Participacion;
    import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoParticipacion;
    import com.mansilla_nazareno.feriadigital.feriadigital.models.Admin.EstadoPago;

    public class ParticipacionDTO {

        private int id;

        private Integer edicionId;       // 🟢 Cambiado: Apunta al ID de la edición
        private String nombreEdicion;    // 🟢 Cambiado: Nombre de la edición (Ej: "Invierno 2026")
        private String nombreFeriaBase;  // 🟢 Nuevo: Nombre de la feria madre (Ej: "Feria de Emprendedores RG")

        private Integer standId;
        private String stand;            // Nombre del emprendimiento

        private Integer numeroStand;     // Mesa o lugar físico
        private EstadoParticipacion estado;

        private EstadoPago estadoPago;
        private Double montoAbonado;
        private Integer numeroStandPreferido;

        public ParticipacionDTO() {}

        public ParticipacionDTO(Participacion participacion) {
            this.id = participacion.getId();

            // 🟢 MAPEO SEGURO DESDE EDICIÓN
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

            this.numeroStand = participacion.getNumeroStand();
            this.estado = participacion.getEstado();
            this.estadoPago = participacion.getEstadoPago();
            this.montoAbonado = participacion.getMontoAbonado();
            this.numeroStandPreferido = participacion.getNumeroStandPreferido();
        }

        // --- GETTERS Y SETTERS (Indispensables para Jackson y Axios) ---

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

        public Integer getNumeroStand() { return numeroStand; }
        public void setNumeroStand(Integer numeroStand) { this.numeroStand = numeroStand; }

        public EstadoParticipacion getEstado() { return estado; }
        public void setEstado(EstadoParticipacion estado) { this.estado = estado; }

        public EstadoPago getEstadoPago() { return estadoPago; }
        public void setEstadoPago(EstadoPago estadoPago) { this.estadoPago = estadoPago; }

        public Double getMontoAbonado() { return montoAbonado; }
        public void setMontoAbonado(Double montoAbonado) { this.montoAbonado = montoAbonado; }

        public Integer getNumeroStandPreferido() { return numeroStandPreferido; }
        public void setNumeroStandPreferido(Integer numeroStandPreferido) { this.numeroStandPreferido = numeroStandPreferido; }
    }