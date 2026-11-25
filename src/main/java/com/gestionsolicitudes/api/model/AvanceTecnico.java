package com.gestionsolicitudes.api.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document (collection = "avances_tecnico")
public class AvanceTecnico {

    @Id
    private String id;

    private String solicitudId;
    private String tecnicoCorreo;
    private String comentario;
    private Instant fechaHora;


    public AvanceTecnico() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(String solicitudId) {
        this.solicitudId = solicitudId;
    }

    public String getTecnicoCorreo() {
        return tecnicoCorreo;
    }

    public void setTecnicoCorreo(String tecnicoCorreo) {
        this.tecnicoCorreo = tecnicoCorreo;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Instant fechaHora) {
        this.fechaHora = fechaHora;
    }
}
