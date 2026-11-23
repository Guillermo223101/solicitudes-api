package com.gestionsolicitudes.api.repository;

import com.gestionsolicitudes.api.model.Solicitud;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SolicitudRepository extends MongoRepository<Solicitud, String> {
    List<Solicitud> findByTecnicoCorreo(String tecnicoCorreo);
}
