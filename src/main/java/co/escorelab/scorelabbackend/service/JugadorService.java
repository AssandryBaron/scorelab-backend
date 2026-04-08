package co.escorelab.scorelabbackend.service;

import co.escorelab.scorelabbackend.dto.JugadorRequest;
import co.escorelab.scorelabbackend.model.Equipo;
import co.escorelab.scorelabbackend.model.Jugador;
import co.escorelab.scorelabbackend.repository.EquipoRepository;
import co.escorelab.scorelabbackend.repository.JugadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JugadorService {

    private final JugadorRepository jugadorRepository;
    private final EquipoRepository equipoRepository;

    public Jugador registrarJugador(Long equipoId, JugadorRequest request, String correoDelegado) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        // 1. Validar que sea el dueño del equipo
        if (!equipo.getDelegado().getCorreo().equals(correoDelegado)) {
            throw new RuntimeException("No tienes permisos para gestionar este equipo");
        }

        // 2. Validar documento único global
        if (jugadorRepository.findByDocumento(request.getDocumento()).isPresent()) {
            throw new RuntimeException("Ya existe un jugador con ese documento");
        }

        // 3. Validar dorsal único en el equipo
        if (jugadorRepository.existsByEquipoAndNumeroCamiseta(equipo, request.getNumeroCamiseta())) {
            throw new RuntimeException("El número " + request.getNumeroCamiseta() + " ya está ocupado en este equipo");
        }

        Jugador nuevoJugador = Jugador.builder()
                .nombre(request.getNombre())
                .documento(request.getDocumento())
                .posicion(request.getPosicion())
                .numeroCamiseta(request.getNumeroCamiseta())
                .equipo(equipo)
                .build();

        return jugadorRepository.save(nuevoJugador);
    }

    public List<Jugador> listarJugadoresPorEquipo(Long equipoId) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        return jugadorRepository.findByEquipo(equipo);
    }
}
