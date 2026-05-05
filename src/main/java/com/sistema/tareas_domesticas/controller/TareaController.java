package com.sistema.tareas_domesticas.controller;

import com.sistema.tareas_domesticas.model.CreateTareaRequest;
import com.sistema.tareas_domesticas.model.Tarea;
import com.sistema.tareas_domesticas.model.TareaResponse;
import com.sistema.tareas_domesticas.service.TareaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tareas")
@CrossOrigin(origins = "*")
public class TareaController {

    @Autowired
    private TareaService tareaService;

    /**
     * HU-06: Crear una nueva tarea
     * Este endpoint recibe los datos del formulario del front y delega al service.
     */
    @PostMapping("/crear")
    public TareaResponse crearTarea(@RequestBody CreateTareaRequest request) {
        // Llamada al servicio con los campos del request
        Tarea tarea = tareaService.crearTarea(
                request.getUsuarioId(),
                request.getNombre(),
                request.getDescripcion(),
                request.getPrioridad(),
                request.getFechaLimite()
        );

        // Retornamos la respuesta mapeada al DTO TareaResponse
        return new TareaResponse(
                tarea.getId(),
                tarea.getNombre(),
                tarea.getDescripcion(),
                tarea.getPrioridad(),
                tarea.getFechaLimite(),
                tarea.getEstado(),
                tarea.getHogarId()
        );
    }

    /**
     * HU-07: Listar tareas del hogar (Adelanto para dejar el controller listo)
     * GET /api/tareas/hogar/{hogarId}
     */
    @GetMapping("/hogar/{hogarId}")
    public List<TareaResponse> listarTareasPorHogar(@PathVariable Long hogarId) {
        return tareaService.listarTareasPorHogar(hogarId).stream()
                .map(t -> new TareaResponse(
                        t.getId(),
                        t.getNombre(),
                        t.getDescripcion(),
                        t.getPrioridad(),
                        t.getFechaLimite(),
                        t.getEstado(),
                        t.getHogarId()
                ))
                .collect(Collectors.toList());
    }
}