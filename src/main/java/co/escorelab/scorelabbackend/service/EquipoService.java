package co.escorelab.scorelabbackend.service;

import co.escorelab.scorelabbackend.dto.EquipoRequest;
import co.escorelab.scorelabbackend.dto.EquipoResponse;
import co.escorelab.scorelabbackend.model.Equipo;
import co.escorelab.scorelabbackend.model.Usuario;
import co.escorelab.scorelabbackend.repository.EquipoRepository;
import co.escorelab.scorelabbackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final UsuarioRepository usuarioRepository;

    public EquipoResponse crearEquipo(EquipoRequest request, String correoUsuario) {
        Usuario delegado = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (equipoRepository.findByNombre(request.getNombre()).isPresent()) {
            throw new RuntimeException("Ya existe un equipo registrado con ese nombre");
        }

        Equipo nuevoEquipo = Equipo.builder()
                .nombre(request.getNombre())
                .ciudad(request.getCiudad())
                .fechaCreacion(LocalDate.now())
                .delegado(delegado)
                .estado("PENDIENTE") // 🌟 Todo equipo nuevo nace PENDIENTE
                .build();

        Equipo equipoGuardado = equipoRepository.save(nuevoEquipo);
        return convertirAResponse(equipoGuardado);
    }

    public List<EquipoResponse> listarMisEquipos(String correoUsuario) {
        Usuario delegado = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return equipoRepository.findByDelegado(delegado).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    // 🌟 NUEVO: Para que el Organizador vea los que esperan aprobación
    public List<EquipoResponse> listarPendientes() {
        return equipoRepository.findByEstado("PENDIENTE").stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    // 🌟 NUEVO: El método que te faltaba para aprobar
    @Transactional
    public void cambiarEstado(Long equipoId, String nuevoEstado) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        equipo.setEstado(nuevoEstado);
        equipoRepository.save(equipo);
    }

    private EquipoResponse convertirAResponse(Equipo equipo) {
        return EquipoResponse.builder()
                .id(equipo.getId())
                .nombre(equipo.getNombre())
                .ciudad(equipo.getCiudad())
                .fechaCreacion(equipo.getFechaCreacion())
                .nombreDelegado(equipo.getDelegado().getNombre())
                .estado(equipo.getEstado()) // 🌟 Mapeamos el estado a la respuesta
                // Si tu modelo Equipo tiene relación con Torneo, añade esto:
                .nombreTorneo(equipo.getTorneo() != null ? equipo.getTorneo().getNombre() : "Sin Torneo")
                .build();
    }
}