package com.gestionsolicitudes.api.controller;

import com.gestionsolicitudes.api.model.Solicitud;
import com.gestionsolicitudes.api.service.SolicitudService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService service;
    //Consturctor
    public SolicitudController(SolicitudService service){
        this.service = service;
    }

    //Obtener todas las solicitudes
    @GetMapping
    public List<Solicitud> listarSolicitudes() {
        return service.listarSolicitudes();
    }

    //Buscar por id
    @GetMapping("/{id}")
    public ResponseEntity<Solicitud> buscarSolicitud(
        @PathVariable String id) {
        return service.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElseGet(() ->
                ResponseEntity.notFound().build());
    }
    

    //Crear nueva solicitud
    @PostMapping 
    public ResponseEntity<Solicitud> crearSolicitud(
        @RequestBody Solicitud solicitud){
            
            Solicitud guardada = service.crearSolicitud(solicitud);

            return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    //Actualizar una solicitud existente
    @PutMapping("/{id}")
    public ResponseEntity<Solicitud> updateSolicitud(
        @PathVariable String id,
        @RequestBody Solicitud datosActualizado){
            return service.actualizarSolicitud(id, datosActualizado)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                    ResponseEntity.notFound().build());
    }

    // Eliminar una solicitud por ID
    @DeleteMapping ("/{id}")
    public ResponseEntity<Void> eliminarSolicitud(
        @PathVariable String id){

            boolean eliminada = service.eliminarSolicitud(id);
            // Verificar existenca
            if (!eliminada) {
                return ResponseEntity.notFound().build();
            }

            // Eliminar la solicitud MongoDB
            //repository.deleteById(id);

            // Confirmar exito
            return ResponseEntity.noContent().build();
        }
}
