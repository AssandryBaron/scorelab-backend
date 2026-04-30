package co.escorelab.scorelabbackend.service;

import co.escorelab.scorelabbackend.dto.EquipoRequest;
import co.escorelab.scorelabbackend.dto.EquipoResponse;
import co.escorelab.scorelabbackend.model.Equipo;
import co.escorelab.scorelabbackend.model.Torneo;
import co.escorelab.scorelabbackend.model.Usuario;
import co.escorelab.scorelabbackend.repository.EquipoRepository;
import co.escorelab.scorelabbackend.repository.TorneoRepository;
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
    private final TorneoRepository torneoRepository;

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
                .estado("PENDIENTE")
                .motivoRechazo(null)
                .build();

        return convertirAResponse(equipoRepository.save(nuevoEquipo));
    }

    public List<EquipoResponse> listarMisEquipos(String correoUsuario) {
        Usuario delegado = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return equipoRepository.findByDelegado(delegado).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    public List<EquipoResponse> listarPendientes() {
        return equipoRepository.findByEstado("PENDIENTE").stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
public void cambiarEstado(Long equipoId, String nuevoEstado, String motivo) {

    Equipo equipo = equipoRepository.findById(equipoId)
            .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

    if ("APROBADO".equalsIgnoreCase(nuevoEstado)) {

        Torneo torneo = torneoRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No hay torneos"));

        equipoRepository.vincularATorneoYAprobar(equipoId, torneo, "APROBADO");

        torneo.agregarEquipo(equipo);
        torneoRepository.save(torneo);

    } else if ("RECHAZADO".equalsIgnoreCase(nuevoEstado)) {

        equipo.setEstado("RECHAZADO");
        // opcional: guardar motivo si tienes campo
        equipo.setMotivoRechazo(motivo);

        equipoRepository.save(equipo);
    }
}

    private EquipoResponse convertirAResponse(Equipo equipo) {
        return EquipoResponse.builder()
                .id(equipo.getId())
                .nombre(equipo.getNombre())
                .ciudad(equipo.getCiudad())
                .fechaCreacion(equipo.getFechaCreacion())
                .nombreDelegado(
                        equipo.getDelegado() != null
                                ? equipo.getDelegado().getNombre()
                                : "Sin delegado"
                )
                .estado(equipo.getEstado())
                .nombreTorneo(
                        equipo.getTorneo() != null
                                ? equipo.getTorneo().getNombre()
                                : "Sin Torneo"
                )
                .build();
    }
}