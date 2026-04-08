package co.escorelab.scorelabbackend.service;

import co.escorelab.scorelabbackend.dto.EquipoRequest;
import co.escorelab.scorelabbackend.dto.EquipoResponse;
import co.escorelab.scorelabbackend.model.Equipo;
import co.escorelab.scorelabbackend.model.Usuario;
import co.escorelab.scorelabbackend.repository.EquipoRepository;
import co.escorelab.scorelabbackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final UsuarioRepository usuarioRepository;

    public EquipoResponse crearEquipo(EquipoRequest request, String correoUsuario) {
        // 1. Buscamos al usuario dueño del token
        Usuario delegado = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Validamos que el nombre del equipo no esté en uso
        if (equipoRepository.findByNombre(request.getNombre()).isPresent()) {
            throw new RuntimeException("Ya existe un equipo registrado con ese nombre");
        }

        // 3. Creamos la entidad Equipo
        Equipo nuevoEquipo = Equipo.builder()
                .nombre(request.getNombre())
                .ciudad(request.getCiudad())
                .fechaCreacion(LocalDate.now()) // Fecha automática del servidor
                .delegado(delegado)
                .build();

        // 4. Lo guardamos en la base de datos
        Equipo equipoGuardado = equipoRepository.save(nuevoEquipo);

        // 5. Devolvemos la respuesta mapeada
        return convertirAResponse(equipoGuardado);
    }

    public List<EquipoResponse> listarMisEquipos(String correoUsuario) {
        Usuario delegado = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return equipoRepository.findByDelegado(delegado).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    // Método auxiliar para transformar el modelo al DTO de respuesta
    private EquipoResponse convertirAResponse(Equipo equipo) {
        return EquipoResponse.builder()
                .id(equipo.getId())
                .nombre(equipo.getNombre())
                .ciudad(equipo.getCiudad())
                .fechaCreacion(equipo.getFechaCreacion())
                .nombreDelegado(equipo.getDelegado().getNombre())
                .build();
    }
}
