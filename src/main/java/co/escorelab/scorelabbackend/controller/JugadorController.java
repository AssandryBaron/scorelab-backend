package co.escorelab.scorelabbackend.controller;

import co.escorelab.scorelabbackend.dto.ApiResponse;
import co.escorelab.scorelabbackend.dto.JugadorRequest;
import co.escorelab.scorelabbackend.model.Jugador;
import co.escorelab.scorelabbackend.service.JugadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/equipos")
@RequiredArgsConstructor
public class JugadorController {

    private final JugadorService jugadorService;

    @PostMapping("/{equipoId}/jugadores")
    public ResponseEntity<ApiResponse<Jugador>> registrarJugador(
            @PathVariable Long equipoId,
            @Valid @RequestBody JugadorRequest request,
            Principal principal) {

        String correoDelegado = principal.getName();
        Jugador jugador = jugadorService.registrarJugador(equipoId, request, correoDelegado);
        return ResponseEntity.ok(ApiResponse.ok("¡Jugador registrado con éxito!", jugador));
    }

    @GetMapping("/{equipoId}/jugadores")
    public ResponseEntity<ApiResponse<List<Jugador>>> listarJugadores(@PathVariable Long equipoId) {
        List<Jugador> jugadores = jugadorService.listarJugadoresPorEquipo(equipoId);
        return ResponseEntity.ok(ApiResponse.ok("Lista de jugadores obtenida", jugadores));
    }
}