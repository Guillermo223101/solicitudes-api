package com.gestionsolicitudes.api.service;

import com.gestionsolicitudes.api.model.Solicitud;
import com.gestionsolicitudes.api.repository.SolicitudRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Locale;

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

    //Consultar solicitudes de un cliente
    public List<Solicitud> listarPorClienteCorreo(String clienteCorreo){
        if(clienteCorreo == null || clienteCorreo.isBlank()){
            throw new IllegalArgumentException("El correo del cliente es obligatorio");
        }

        String correoNormalizado = clienteCorreo
            .trim()
            .toLowerCase(Locale.ROOT);

        return repository.findByClienteCorreo(correoNormalizado);
    }

    //Crear una solicitud asociada al cliente autenticado
    public Solicitud crearParaCliente(
        String correo,
        String titulo,
        String descripcion){
            if(correo == null || correo.isBlank() || titulo == null || titulo.isBlank()
                || descripcion == null || descripcion.isBlank()){
                throw new IllegalArgumentException(
                "Los datos de la solicitud son obligatorios"
                );
            }

            Solicitud solicitud = new Solicitud();

            solicitud.setTitulo(titulo.trim());
            solicitud.setDescripcion(descripcion.trim());

            //Valores iniciales controlados por el servidor
            solicitud.setEstado("PENDIENTE");
            solicitud.setPrioridad("MEDIA");

            //Propietario identificado mediante JWT
            solicitud.setClienteCorreo(correo.trim().toLowerCase(java.util.Locale.ROOT));

            //El tecnico se asignara posteriormente
            solicitud.setTecnicoCorreo(null);

            return repository.save(solicitud);
    }
    
    
    public Optional<Solicitud> actualizarContenidoDeCliente(
            String id,
            String clienteCorreo,
            String titulo,
            String descripcion) {

        if (id == null || id.isBlank() || clienteCorreo == null || clienteCorreo.isBlank()
                || titulo == null || titulo.isBlank() || descripcion == null || descripcion.isBlank()) {

            throw new IllegalArgumentException("Los datos de actualizacion son obligatorios");
        }

        String correoNormalizado = clienteCorreo
            .trim()
            .toLowerCase(Locale.ROOT);

        // Buscar la solicitud
        Optional<Solicitud> encontrada = repository.findById(id);

        if (encontrada.isEmpty()) {
            return Optional.empty();
        }

        Solicitud solicitud = encontrada.get();

        // Comprobar su propietario
        if (solicitud.getClienteCorreo() == null
                || !correoNormalizado.equals(
                    solicitud.getClienteCorreo()
                        .trim()
                        .toLowerCase(Locale.ROOT))) {

            return Optional.empty();
        }

        // Modificar solamente campos permitidos
        solicitud.setTitulo(titulo.trim());
        solicitud.setDescripcion(descripcion.trim());

        return Optional.of(repository.save(solicitud));
    }

    public boolean eliminarDeCliente(
            String id,
            String clienteCorreo) {

        if (id == null || id.isBlank() || clienteCorreo == null || clienteCorreo.isBlank()) {
            throw new IllegalArgumentException("El ID y el correo son obligatorios");
        }

        String correoNormalizado = clienteCorreo
            .trim()
            .toLowerCase(Locale.ROOT);

        Optional<Solicitud> encontrada = repository.findById(id);

        if (encontrada.isEmpty()) {
            return false;
        }

        Solicitud solicitud = encontrada.get();

        // Impedir eliminar solicitudes ajenas
        if (solicitud.getClienteCorreo() == null
                || !correoNormalizado.equals(
                    solicitud.getClienteCorreo()
                        .trim()
                        .toLowerCase(Locale.ROOT))) {

            return false;
        }

        repository.delete(solicitud);

        return true;
    }

}
