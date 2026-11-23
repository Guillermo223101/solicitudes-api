package com.gestionsolicitudes.api.service;

import com.gestionsolicitudes.api.model.Solicitud;
import com.gestionsolicitudes.api.repository.SolicitudRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service 
public class SolicitudService {
    private final SolicitudRepository repository;

    //Constructor
    public SolicitudService(SolicitudRepository repository){
        this.repository = repository;
    }

    //Obtener todas las solicitudes
    public List<Solicitud> listarSolicitudes(){
        return repository.findAll();
    }

    //Buscar una solicitud por ID
    public Optional<Solicitud> buscarPorId(String id){
        return repository.findById(id);
    }

    // Crear una nueva solicitud
    public Solicitud crearSolicitud(Solicitud solicitud) {
        solicitud.setId(null);
        return repository.save(solicitud);
    }

    // Actualizar una solicitud
    public Optional<Solicitud> actualizarSolicitud(
            String id, Solicitud datosActualizados) {

        return repository.findById(id)
                .map(solicitud -> {

                    solicitud.setTitulo(datosActualizados.getTitulo());
                    solicitud.setDescripcion(datosActualizados.getDescripcion());
                    solicitud.setEstado(datosActualizados.getEstado());
                    solicitud.setPrioridad(datosActualizados.getPrioridad());

                    return repository.save(solicitud);
                });
    }

    // Eliminar una solicitud
    public boolean eliminarSolicitud(String id) {

        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }


    // Asignar un tecnico a una solicitud
    public Optional<Solicitud> asignarTecnico(
            String id, String tecnicoCorreo) {

        // Validar correo del tecnico
        if (tecnicoCorreo == null || tecnicoCorreo.isBlank()) {
            throw new IllegalArgumentException(
                "El correo del tecnico es obligatorio"
            );
        }

        // Buscar la solicitud en MongoDB
        return repository.findById(id)
            .map(solicitud -> {

                // Asignar tecnico responsable
                solicitud.setTecnicoCorreo(
                    tecnicoCorreo.trim()
                );

                // Guardar cambios en MongoDB
                return repository.save(solicitud);
            });
    }


    // Obtener solicitudes asignadas a un tecnico
    public List<Solicitud> listarAsignadasATecnico(
            String tecnicoCorreo) {

        // Validar identificador del tecnico
        if (tecnicoCorreo == null ||
                tecnicoCorreo.isBlank()) {

            throw new IllegalArgumentException(
                "El correo del tecnico es obligatorio"
            );
        }

        // Consultar solicitudes asignadas
        return repository.findByTecnicoCorreo(
            tecnicoCorreo.trim()
        );
    }


}
