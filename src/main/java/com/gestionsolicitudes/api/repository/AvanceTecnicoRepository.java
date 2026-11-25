package com.gestionsolicitudes.api.repository;

import com.gestionsolicitudes.api.model.AvanceTecnico;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AvanceTecnicoRepository extends MongoRepository<AvanceTecnico, String> {
    List<AvanceTecnico> findBySolicitudIdOrderByFechaHoraAsc(String solicitudId);
}
