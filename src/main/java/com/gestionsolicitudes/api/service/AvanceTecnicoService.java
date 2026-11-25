package com.gestionsolicitudes.api.service;

import com.gestionsolicitudes.api.model.AvanceTecnico;
import com.gestionsolicitudes.api.repository.AvanceTecnicoRepository;
import com.gestionsolicitudes.api.repository.SolicitudRepository;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service 
public class AvanceTecnicoService {
    private final AvanceTecnicoRepository avanceRepository;
    private final SolicitudRepository solicitudRepository;

    public AvanceTecnicoService(
            AvanceTecnicoRepository avanceRepository,
            SolicitudRepository solicitudRepository) {

        this.avanceRepository = avanceRepository;
        this.solicitudRepository = solicitudRepository;
    }

    // Comprobar que la solicitud pertenece al tecnico autenticado
    private boolean tieneAsignacion(
            String identificadorSolicitud,
            String correoTecnico) {

        if (identificadorSolicitud == null ||
                identificadorSolicitud.isBlank() ||
                correoTecnico == null ||
                correoTecnico.isBlank()) {

            throw new IllegalArgumentException(
                "La solicitud y el tecnico son obligatorios"
            );
        }

        String correoNormalizado = correoTecnico
            .trim()
            .toLowerCase(Locale.ROOT);

        return solicitudRepository.findById(identificadorSolicitud)
            .filter(solicitud ->
                solicitud.getTecnicoCorreo() != null &&
                correoNormalizado.equals(
                    solicitud.getTecnicoCorreo()
                        .trim()
                        .toLowerCase(Locale.ROOT)
                )
            )
            .isPresent();
    }

    // Registrar un comentario sobre una solicitud asignada
    public Optional<AvanceTecnico> registrarAvance(
            String identificadorSolicitud,
            String correoTecnico,
            String comentario) {

        if (comentario == null ||
                comentario.isBlank() ||
                comentario.length() > 1000) {

            throw new IllegalArgumentException(
                "El comentario debe contener entre 1 y 1000 caracteres"
            );
        }

        if (!tieneAsignacion(identificadorSolicitud, correoTecnico)) {
            return Optional.empty();
        }

        AvanceTecnico avance = new AvanceTecnico();

        avance.setSolicitudId(identificadorSolicitud);

        avance.setTecnicoCorreo(
            correoTecnico.trim().toLowerCase(Locale.ROOT)
        );

        avance.setComentario(comentario.trim());

        // La fecha la establece el servidor
        avance.setFechaHora(Instant.now());

        return Optional.of(avanceRepository.save(avance));
    }

    // Consultar el historial de una solicitud asignada
    public Optional<List<AvanceTecnico>> consultarAvances(
            String identificadorSolicitud,
            String correoTecnico) {

        if (!tieneAsignacion(identificadorSolicitud, correoTecnico)) {
            return Optional.empty();
        }

        List<AvanceTecnico> avances =
            avanceRepository.findBySolicitudIdOrderByFechaHoraAsc(
                identificadorSolicitud
            );

        return Optional.of(avances);
    }
}
