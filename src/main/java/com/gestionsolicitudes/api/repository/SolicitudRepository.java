package com.gestionsolicitudes.api.repository;

import com.gestionsolicitudes.api.model.Solicitud;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SolicitudRepository extends MongoRepository<Solicitud, String> {
}
