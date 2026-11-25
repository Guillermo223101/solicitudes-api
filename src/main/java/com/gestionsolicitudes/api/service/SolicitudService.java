package com.gestionsolicitudes.api.service;

import com.gestionsolicitudes.api.model.Solicitud;
import com.gestionsolicitudes.api.repository.SolicitudRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Locale;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

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

    // Mantener compatibilidad con las llamadas anteriores
    public Solicitud crearParaCliente(
            String correo,
            String titulo,
            String descripcion) {

        return crearParaCliente(correo, titulo, descripcion, null);
    }

    // Compatibilidad con creacion sin fotografia
    public Solicitud crearParaCliente(
            String correo,
            String titulo,
            String descripcion,
            String categoria) {

        return crearParaCliente(
            correo, titulo, descripcion, categoria, null
        );
    }

    // Crear una solicitud con categoria y fotografia opc
    public Solicitud crearParaCliente(
            String correo,
            String titulo,
            String descripcion,
            String categoria,
            String evidenciaBase64) {

        if (correo == null || correo.isBlank() ||
                titulo == null || titulo.isBlank() ||
                descripcion == null || descripcion.isBlank()) {

            throw new IllegalArgumentException(
                "Los datos de la solicitud son obligatorios"
            );
        }

        String categoriaValidada = validarCategoria(categoria);

        String evidenciaValidada = validarEvidencia(evidenciaBase64);

        Solicitud solicitud = new Solicitud();

        solicitud.setTitulo(titulo.trim());
        solicitud.setDescripcion(descripcion.trim());
        solicitud.setCategoria(categoriaValidada);
        solicitud.setEvidenciaBase64(evidenciaValidada);

        // Valores iniciales controlados por el servidor
        solicitud.setEstado("PENDIENTE");
        solicitud.setPrioridad("MEDIA");

        // Propietario identificado por JWT
        solicitud.setClienteCorreo(
            correo.trim().toLowerCase(Locale.ROOT)
        );

        solicitud.setTecnicoCorreo(null);

        return repository.save(solicitud);
    }
    
    
    
    // Mantener compatibilidad con las llamadas anteriores
    public Optional<Solicitud> actualizarContenidoDeCliente(
            String id,
            String clienteCorreo,
            String titulo,
            String descripcion) {

        return actualizarContenidoDeCliente(
            id, clienteCorreo, titulo, descripcion, null
        );
    }

    // Actualizar titulo, descripcion y categoria del cliente
    public Optional<Solicitud> actualizarContenidoDeCliente(
            String id,
            String clienteCorreo,
            String titulo,
            String descripcion,
            String categoria) {

        if (id == null || id.isBlank() ||
                clienteCorreo == null || clienteCorreo.isBlank() ||
                titulo == null || titulo.isBlank() ||
                descripcion == null || descripcion.isBlank()) {

            throw new IllegalArgumentException(
                "Los datos de actualizacion son obligatorios"
            );
        }

        String categoriaValidada = validarCategoria(categoria);

        String correoNormalizado = clienteCorreo
            .trim()
            .toLowerCase(Locale.ROOT);

        Optional<Solicitud> encontrada = repository.findById(id);

        if (encontrada.isEmpty()) {
            return Optional.empty();
        }

        Solicitud solicitud = encontrada.get();

        // Impedir modificar solicitudes ajenas
        if (solicitud.getClienteCorreo() == null ||
                !correoNormalizado.equals(
                    solicitud.getClienteCorreo()
                        .trim()
                        .toLowerCase(Locale.ROOT))) {

            return Optional.empty();
        }

        solicitud.setTitulo(titulo.trim());
        solicitud.setDescripcion(descripcion.trim());

        // Si el cliente no envio categoria, conservar la actual
        if (categoriaValidada != null) {
            solicitud.setCategoria(categoriaValidada);
        }

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

    // Modificar prioridad de una solicitud
    public Optional<Solicitud> actualizarPrioridad(
            String identificadorSolicitud,
            String prioridadNueva) {

        if (identificadorSolicitud == null ||
                identificadorSolicitud.isBlank() ||
                prioridadNueva == null ||
                prioridadNueva.isBlank()) {

            throw new IllegalArgumentException(
                "El ID y la prioridad son obligatorios"
            );
        }

        String prioridadNormalizada = prioridadNueva
            .trim()
            .toUpperCase(Locale.ROOT);

        // Validar las prioridades admitidas
        if (!List.of("BAJA", "MEDIA", "ALTA")
                .contains(prioridadNormalizada)) {

            throw new IllegalArgumentException(
                "La prioridad debe ser BAJA, MEDIA o ALTA"
            );
        }

        return repository.findById(identificadorSolicitud)
            .map(solicitud -> {

                solicitud.setPrioridad(prioridadNormalizada);

                return repository.save(solicitud);
            });
    }

    // Actualizar el estado de una solicitud asignada al tecnico
    public Optional<Solicitud> actualizarEstadoDeTecnico(
            String identificadorSolicitud,
            String correoTecnico,
            String estadoNuevo) {

        if (identificadorSolicitud == null ||
                identificadorSolicitud.isBlank() ||
                correoTecnico == null ||
                correoTecnico.isBlank() ||
                estadoNuevo == null ||
                estadoNuevo.isBlank()) {

            throw new IllegalArgumentException(
                "Los datos de actualizacion son obligatorios"
            );
        }

        String estadoNormalizado = estadoNuevo
            .trim()
            .toUpperCase(Locale.ROOT);

        if (!List.of("EN_PROCESO", "RESUELTA")
                .contains(estadoNormalizado)) {

            throw new IllegalArgumentException(
                "El estado debe ser EN_PROCESO o RESUELTA"
            );
        }

        String correoNormalizado = correoTecnico
            .trim()
            .toLowerCase(Locale.ROOT);

        return repository.findById(identificadorSolicitud)
            .filter(solicitud ->
                solicitud.getTecnicoCorreo() != null &&
                correoNormalizado.equals(
                    solicitud.getTecnicoCorreo()
                        .trim()
                        .toLowerCase(Locale.ROOT)
                )
            )
            .map(solicitud -> {
                solicitud.setEstado(estadoNormalizado);
                return repository.save(solicitud);
            });
    }

    // Validar las categorias admitidas
    private String validarCategoria(String categoria) {

        if (categoria == null) {
            return null;
        }

        String categoriaNormalizada = categoria.trim();

        if (!List.of("Hardware", "Software", "Redes")
                .contains(categoriaNormalizada)) {

            throw new IllegalArgumentException(
                "La categoria debe ser Hardware, Software o Redes"
            );
        }

        return categoriaNormalizada;
    }

    private String validarEvidencia(String evidenciaBase64) {

        if (evidenciaBase64 == null || evidenciaBase64.isBlank()) {
            return null;
        }

        String textoBase64 = evidenciaBase64.trim();

        if (textoBase64.length() > 1_400_000) {
            throw new IllegalArgumentException(
                "La fotografia supera el limite permitido"
            );
        }

        byte[] imagenDecodificada;

        try {
            imagenDecodificada = Base64.getDecoder().decode(textoBase64);
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException(
                "La fotografia no tiene un formato Base64 valido"
            );
        }

        if (imagenDecodificada.length == 0 ||
                imagenDecodificada.length > 1_048_576) {
            throw new IllegalArgumentException(
                "La fotografia debe ocupar como maximo 1 MB"
            );
        }

        boolean esJpeg =
            imagenDecodificada.length >= 3 &&
            (imagenDecodificada[0] & 0xFF) == 0xFF &&
            (imagenDecodificada[1] & 0xFF) == 0xD8 &&
            (imagenDecodificada[2] & 0xFF) == 0xFF;

        byte[] firmaPng = {
            (byte) 137, 80, 78, 71, 13, 10, 26, 10
        };

        boolean esPng =
            imagenDecodificada.length >= firmaPng.length;

        if (esPng) {
            for (int indice = 0; indice < firmaPng.length; indice++) {
                if (imagenDecodificada[indice] != firmaPng[indice]) {
                    esPng = false;
                    break;
                }
            }
        }

        if (!esJpeg && !esPng) {
            throw new IllegalArgumentException(
                "Solo se permiten fotografias JPEG o PNG"
            );
        }

        try {
            var imagen = ImageIO.read(
                new ByteArrayInputStream(imagenDecodificada)
            );

            if (imagen == null) {
                throw new IllegalArgumentException(
                    "No se pudo interpretar la fotografia"
                );
            }
        } catch (IOException error) {
            throw new IllegalArgumentException(
                "La fotografia esta danada o no es valida"
            );
        }

        return textoBase64;
    }
}
