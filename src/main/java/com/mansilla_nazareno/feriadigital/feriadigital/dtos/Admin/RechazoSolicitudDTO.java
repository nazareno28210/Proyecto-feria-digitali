package com.mansilla_nazareno.feriadigital.feriadigital.dtos.Admin;

public class RechazoSolicitudDTO {
    private String motivoRechazo;

    public RechazoSolicitudDTO() {}

    public RechazoSolicitudDTO(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }
}
