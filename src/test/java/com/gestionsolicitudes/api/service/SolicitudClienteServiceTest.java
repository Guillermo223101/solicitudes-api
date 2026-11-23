
package com.gestionsolicitudes.api.service;

import com.gestionsolicitudes.api.model.Solicitud;
import com.gestionsolicitudes.api.repository.SolicitudRepository;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SolicitudClienteServiceTest {

    @Test
    void debeConsultarSolicitudesDelCliente() {

        SolicitudRepository repository = mock(SolicitudRepository.class);

        SolicitudService service = new SolicitudService(repository);

        Solicitud solicitud = new Solicitud();
        solicitud.setTitulo("Solicitud del cliente");
        solicitud.setClienteCorreo(
            "cliente.api@example.com"
        );

        when(repository.findByClienteCorreo(
            "cliente.api@example.com"
        )).thenReturn(List.of(solicitud));

        List<Solicitud> resultado =
            service.listarPorClienteCorreo(
                "CLIENTE.API@EXAMPLE.COM"
            );

        assertEquals(1, resultado.size());

        assertEquals(
            "cliente.api@example.com",
            resultado.get(0).getClienteCorreo()
        );

        verify(repository).findByClienteCorreo(
            "cliente.api@example.com"
        );
    }

    @Test
    void debeRechazarCorreoVacio() {

        SolicitudRepository repository = mock(SolicitudRepository.class);

        SolicitudService service = new SolicitudService(repository);

        assertThrows(
            IllegalArgumentException.class,
            () -> service.listarPorClienteCorreo(" ")
        );

        verifyNoInteractions(repository);
    }

    @Test
    void debeCrearSolicitudConPropietarioCorrecto() {

        SolicitudRepository repository =
            mock(SolicitudRepository.class);

        SolicitudService service =
            new SolicitudService(repository);

        when(repository.save(any(Solicitud.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        Solicitud resultado = service.crearParaCliente(
            "CLIENTE.API@EXAMPLE.COM",
            "Problema electrico",
            "Revisar instalacion"
        );

        assertEquals(
            "cliente.api@example.com",
            resultado.getClienteCorreo()
        );

        assertEquals(
            "Problema electrico",
            resultado.getTitulo()
        );

        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals("MEDIA", resultado.getPrioridad());
        assertNull(resultado.getTecnicoCorreo());

        verify(repository).save(any(Solicitud.class));
    }

    @Test
    void debeActualizarSolicitudPropia() {

        SolicitudRepository repository = mock(SolicitudRepository.class);

        SolicitudService service = new SolicitudService(repository);

        Solicitud solicitud = new Solicitud();
        solicitud.setClienteCorreo("cliente.api@example.com");
        solicitud.setTitulo("Titulo antiguo");
        solicitud.setDescripcion("Descripcion antigua");
        solicitud.setEstado("PENDIENTE");
        solicitud.setPrioridad("MEDIA");

        when(repository.findById("id-1")).thenReturn(Optional.of(solicitud));

        when(repository.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = service.actualizarContenidoDeCliente(
            "id-1",
            "CLIENTE.API@EXAMPLE.COM",
            "Titulo nuevo",
            "Descripcion nueva"
        );

        assertTrue(resultado.isPresent());
        assertEquals("Titulo nuevo", resultado.get().getTitulo());
        assertEquals("Descripcion nueva", resultado.get().getDescripcion());
        assertEquals("PENDIENTE", resultado.get().getEstado());
        assertEquals("MEDIA", resultado.get().getPrioridad());
        assertEquals("cliente.api@example.com", resultado.get().getClienteCorreo());

        verify(repository).save(solicitud);
    }

    @Test
    void noDebeActualizarSolicitudAjena() {

        SolicitudRepository repository = mock(SolicitudRepository.class);

        SolicitudService service = new SolicitudService(repository);

        Solicitud solicitud = new Solicitud();
        solicitud.setClienteCorreo("otro.cliente@example.com");

        when(repository.findById("id-ajeno"))
            .thenReturn(Optional.of(solicitud));

        var resultado = service.actualizarContenidoDeCliente(
            "id-ajeno",
            "cliente.api@example.com",
            "Titulo cambiado",
            "Descripcion cambiada"
        );

        assertTrue(resultado.isEmpty());
        verify(repository, never()).save(any(Solicitud.class));
    }

    @Test
    void debeEliminarSolicitudPropia() {

        SolicitudRepository repository = mock(SolicitudRepository.class);

        SolicitudService service = new SolicitudService(repository);

        Solicitud solicitud = new Solicitud();
        solicitud.setClienteCorreo("cliente.api@example.com");

        when(repository.findById("id-1")).thenReturn(Optional.of(solicitud));

        boolean eliminada = service.eliminarDeCliente(
            "id-1",
            "cliente.api@example.com"
        );

        assertTrue(eliminada);
        verify(repository).delete(solicitud);
    }

    @Test
    void noDebeEliminarSolicitudAjena() {

        SolicitudRepository repository = mock(SolicitudRepository.class);

        SolicitudService service = new SolicitudService(repository);

        Solicitud solicitud = new Solicitud();
        solicitud.setClienteCorreo("otro.cliente@example.com");

        when(repository.findById("id-ajeno")).thenReturn(Optional.of(solicitud));

        boolean eliminada = service.eliminarDeCliente(
            "id-ajeno",
            "cliente.api@example.com"
        );

        assertFalse(eliminada);
        verify(repository, never()).delete(any(Solicitud.class));
    }

}
