package com.gestionsolicitudes.api.service;

import com.gestionsolicitudes.api.model.Solicitud;
import com.gestionsolicitudes.api.repository.SolicitudRepository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

class SolicitudServiceTest {

    @Test
    void debeCrearSolicitudCorrectamente(){
        //1- Simular el repositorio
        SolicitudRepository repository = mock(SolicitudRepository.class);
        SolicitudService service = new SolicitudService(repository);

        //2- Preparar una solicitud nueva
        Solicitud solicitud = new Solicitud();
        solicitud.setTitulo("Problema de internet");
        solicitud.setDescripcion("Sin conexion");
        solicitud.setEstado("Recibida");
        solicitud.setPrioridad("Media");

        //3- Simular la respuesta de MongoDB
        Solicitud guardada = new Solicitud();
        guardada.setId("mongo-001");
        guardada.setTitulo("Problema de internet");
        when(repository.save(solicitud)).thenReturn(guardada);

        //4- Ejecutar el metodo del Service
        Solicitud resultado = service.crearSolicitud(solicitud);

        //5- Comprobar los resultados
        assertNull(solicitud.getId());
        assertEquals("mongo-001", resultado.getId());

        verify(repository, times(1)).save(solicitud);
    }

    @Test
    void debeListarSolicitudesCorrectamente(){
        //1- Simular el repositorio
        SolicitudRepository repository = mock(SolicitudRepository.class);
        SolicitudService service = new SolicitudService(repository);

        //2- Preparar una solicitud nueva
        Solicitud solicitud = new Solicitud();
        solicitud.setId("mongo-001");
        solicitud.setTitulo("Problema de internet");

        //3- Simular la respuesta de MongoDB
        when(repository.findAll()).thenReturn(List.of(solicitud));

        //4- Ejecutar el metodo del Service
        List<Solicitud> resultado = service.listarSolicitudes();

        //5- Comprobar los resultados
        assertEquals(1, resultado.size());
        assertEquals("Problema de internet", resultado.get(0).getTitulo());

        verify(repository, times(1)).findAll();
    }


    @Test
    void debeBuscarSolicitudPorId() {
        //1- Simular el repositorio
        SolicitudRepository repository = mock(SolicitudRepository.class);
        SolicitudService service = new SolicitudService(repository);

        //2- Preparar una solicitud nueva
        Solicitud solicitud = new Solicitud();
        solicitud.setId("mongo-001");
        solicitud.setTitulo("Problema de internet");

        //3- Simular la respuesta de MongoDB
        when(repository.findById("mongo-001")).thenReturn(Optional.of(solicitud));

        //4- Ejecutar el metodo del Service
        Optional<Solicitud> resultado = service.buscarPorId("mongo-001");

        //5- Comprobar los resultados
        assertTrue(resultado.isPresent());
        assertEquals("Problema de internet", resultado.get().getTitulo());

        verify(repository).findById("mongo-001");
    }


    @Test
    void debeDevolverVacioSiNoExisteSolicitud() {
        //1- Simular el repositorio
        SolicitudRepository repository = mock(SolicitudRepository.class);
        SolicitudService service = new SolicitudService(repository);

        //3- Simular la respuesta de MongoDB
        when(repository.findById("no-existe")).thenReturn(Optional.empty());

        //4- Ejecutar el metodo del Service
        Optional<Solicitud> resultado = service.buscarPorId("no-existe");

        //5- Comprobar los resultados
        assertTrue(resultado.isEmpty());
        verify(repository).findById("no-existe");
    }



    @Test
    void debeActualizarSolicitudCorrectamente() {

        //1- Simular el repositorio
        SolicitudRepository repository = mock(SolicitudRepository.class);
        SolicitudService service = new SolicitudService(repository);

        //2- Solicitud que ya existe en MongoDB
        Solicitud existente = new Solicitud();
        existente.setId("mongo-001");
        existente.setTitulo("Computador no enciende");
        existente.setDescripcion("Equipo con problemas");
        existente.setEstado("Recibida");
        existente.setPrioridad("Media");

        //3- Nuevos datos
        Solicitud nuevosDatos = new Solicitud();
        nuevosDatos.setTitulo("Computador reparado");
        nuevosDatos.setDescripcion("Fuente reemplazada");
        nuevosDatos.setEstado("Resuelta");
        nuevosDatos.setPrioridad("Baja");

        //4- Simular busqueda y guardado
        when(repository.findById("mongo-001")).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        //5- Ejecutar actualizacion
        Optional<Solicitud> resultado = service.actualizarSolicitud("mongo-001", nuevosDatos);

        //6- Comprobar resultados
        assertTrue(resultado.isPresent());
        assertEquals("mongo-001", resultado.get().getId());
        assertEquals("Computador reparado", resultado.get().getTitulo());
        assertEquals("Fuente reemplazada", resultado.get().getDescripcion());
        assertEquals("Resuelta", resultado.get().getEstado());
        assertEquals("Baja", resultado.get().getPrioridad());
        verify(repository).save(existente);
    }


    @Test
    void noDebeActualizarSolicitudInexistente() {

        SolicitudRepository repository = mock(SolicitudRepository.class);
        SolicitudService service = new SolicitudService(repository);
        Solicitud nuevosDatos = new Solicitud();
        nuevosDatos.setTitulo("Nuevo titulo");

        // Simular solicitud inexistente
        when(repository.findById("no-existe")).thenReturn(Optional.empty());

        // Intentar actualizar
        Optional<Solicitud> resultado = service.actualizarSolicitud("no-existe", nuevosDatos);

        // Comprobar resultado
        assertTrue(resultado.isEmpty());

        // Verificar que no se guarde nada
        verify(repository, never()).save(any(Solicitud.class));
    }


    @Test
    void debeEliminarSolicitudCorrectamente() {

        SolicitudRepository repository = mock(SolicitudRepository.class);
        SolicitudService service = new SolicitudService(repository);

        // Simular que existe
        when(repository.existsById("mongo-001")).thenReturn(true);

        // Ejecutar eliminacion
        boolean resultado = service.eliminarSolicitud("mongo-001");

        // Comprobar resultado
        assertTrue(resultado);

        // Verificar eliminacion
        verify(repository, times(1)).deleteById("mongo-001");
    }


    @Test
    void noDebeEliminarSolicitudInexistente() {

        SolicitudRepository repository = mock(SolicitudRepository.class);
        SolicitudService service = new SolicitudService(repository);

        // Simular que no existe
        when(repository.existsById("no-existe")).thenReturn(false);

        // Intentar eliminar
        boolean resultado = service.eliminarSolicitud("no-existe");

        // Comprobar resultado
        assertFalse(resultado);

        // Verificar que no se elimine nada
        verify(repository, never()).deleteById(anyString());
    }

}
