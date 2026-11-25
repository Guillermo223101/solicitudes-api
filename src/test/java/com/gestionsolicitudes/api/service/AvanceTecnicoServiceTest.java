package com.gestionsolicitudes.api.service;

import com.gestionsolicitudes.api.model.AvanceTecnico;
import com.gestionsolicitudes.api.model.Solicitud;
import com.gestionsolicitudes.api.repository.AvanceTecnicoRepository;
import com.gestionsolicitudes.api.repository.SolicitudRepository;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AvanceTecnicoServiceTest {

    @Test
    void debeRegistrarAvanceDeTecnicoAsignado() {

        AvanceTecnicoRepository repositorioAvances =
            mock(AvanceTecnicoRepository.class);

        SolicitudRepository repositorioSolicitudes =
            mock(SolicitudRepository.class);

        AvanceTecnicoService servicio =
            new AvanceTecnicoService(
                repositorioAvances,
                repositorioSolicitudes
            );

        Solicitud solicitud = new Solicitud();
        solicitud.setId("sol-001");
        solicitud.setTecnicoCorreo("tecnico.prueba@example.com");

        when(repositorioSolicitudes.findById("sol-001"))
            .thenReturn(Optional.of(solicitud));

        when(repositorioAvances.save(any(AvanceTecnico.class)))
            .thenAnswer(respuesta -> respuesta.getArgument(0));

        Optional<AvanceTecnico> resultado =
            servicio.registrarAvance(
                "sol-001",
                "tecnico.prueba@example.com",
                "Se reviso el equipo"
            );

        assertTrue(resultado.isPresent());

        assertEquals(
            "Se reviso el equipo",
            resultado.get().getComentario()
        );

        assertNotNull(resultado.get().getFechaHora());

        verify(repositorioAvances).save(
            any(AvanceTecnico.class)
        );
    }

    @Test
    void noDebeRegistrarAvanceDeOtroTecnico() {

        AvanceTecnicoRepository repositorioAvances =
            mock(AvanceTecnicoRepository.class);

        SolicitudRepository repositorioSolicitudes =
            mock(SolicitudRepository.class);

        AvanceTecnicoService servicio =
            new AvanceTecnicoService(
                repositorioAvances,
                repositorioSolicitudes
            );

        Solicitud solicitud = new Solicitud();
        solicitud.setTecnicoCorreo("otro.tecnico@example.com");

        when(repositorioSolicitudes.findById("sol-001"))
            .thenReturn(Optional.of(solicitud));

        Optional<AvanceTecnico> resultado =
            servicio.registrarAvance(
                "sol-001",
                "tecnico.prueba@example.com",
                "Intento sin permiso"
            );

        assertTrue(resultado.isEmpty());

        verify(repositorioAvances, never()).save(
            any(AvanceTecnico.class)
        );
    }

    @Test
    void debeConsultarHistorialDeSolicitudAsignada() {

        AvanceTecnicoRepository repositorioAvances =
            mock(AvanceTecnicoRepository.class);

        SolicitudRepository repositorioSolicitudes =
            mock(SolicitudRepository.class);

        AvanceTecnicoService servicio =
            new AvanceTecnicoService(
                repositorioAvances,
                repositorioSolicitudes
            );

        Solicitud solicitud = new Solicitud();
        solicitud.setTecnicoCorreo("tecnico.prueba@example.com");

        AvanceTecnico avance = new AvanceTecnico();
        avance.setComentario("Equipo revisado");

        when(repositorioSolicitudes.findById("sol-001"))
            .thenReturn(Optional.of(solicitud));

        when(repositorioAvances
            .findBySolicitudIdOrderByFechaHoraAsc("sol-001"))
            .thenReturn(List.of(avance));

        var resultado = servicio.consultarAvances(
            "sol-001",
            "tecnico.prueba@example.com"
        );

        assertTrue(resultado.isPresent());
        assertEquals(1, resultado.get().size());
        assertEquals(
            "Equipo revisado",
            resultado.get().get(0).getComentario()
        );
    }

    @Test
    void debeRechazarComentarioVacio() {

        AvanceTecnicoRepository repositorioAvances =
            mock(AvanceTecnicoRepository.class);

        SolicitudRepository repositorioSolicitudes =
            mock(SolicitudRepository.class);

        AvanceTecnicoService servicio =
            new AvanceTecnicoService(
                repositorioAvances,
                repositorioSolicitudes
            );

        assertThrows(
            IllegalArgumentException.class,
            () -> servicio.registrarAvance(
                "sol-001",
                "tecnico.prueba@example.com",
                ""
            )
        );

        verifyNoInteractions(
            repositorioAvances,
            repositorioSolicitudes
        );
    }
}
