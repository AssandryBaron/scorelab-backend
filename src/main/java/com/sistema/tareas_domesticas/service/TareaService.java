package com.sistema.tareas_domesticas.service;

import com.sistema.tareas_domesticas.model.Tarea;
import com.sistema.tareas_domesticas.model.Usuario;
import com.sistema.tareas_domesticas.repository.TareaRepository;
import com.sistema.tareas_domesticas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class TareaService {

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * HU-06: Crear una nueva tarea.
     * Valida que el creador sea ADMINISTRADOR y pertenezca a un hogar.
     */
    public Tarea crearTarea(Long usuarioId, String nombre, String descripcion, String prioridad, LocalDate fechaLimite) {
        // 1. Validar campos obligatorios
        if (nombre == null || nombre.isBlank()) {
            throw new RuntimeException("El nombre de la tarea es obligatorio");
        }
        if (prioridad == null || prioridad.isBlank()) {
            throw new RuntimeException("La prioridad de la tarea es obligatoria");
        }
        if (fechaLimite == null) {
            throw new RuntimeException("La fecha límite de la tarea es obligatoria");
        }

        // 2. Validar fecha límite no anterior a hoy
        if (fechaLimite.isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha límite no puede ser anterior a hoy");
        }

        // 3. Validar que el usuario exista y tenga permisos
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!"ADMINISTRADOR".equals(usuario.getRol())) {
            throw new RuntimeException("Solo los administradores pueden crear tareas");
        }

        if (usuario.getFamiliaId() == null) {
            throw new RuntimeException("El usuario no pertenece a ningún hogar");
        }

        // 4. Mapear y guardar tarea
        Tarea tarea = new Tarea();
        tarea.setNombre(nombre);
        tarea.setDescripcion(descripcion);
        tarea.setPrioridad(prioridad.toUpperCase());
        tarea.setFechaLimite(fechaLimite);
        tarea.setEstado("PENDIENTE"); // Estado inicial
        tarea.setHogarId(usuario.getFamiliaId()); // Vinculación automática al hogar

        return tareaRepository.save(tarea);
    }

    /**
     * HU-07: Listar tareas del hogar.
     * Este método es indispensable para el endpoint en TareaController.
     */
    public List<Tarea> listarTareasPorHogar(Long hogarId) {
        if (hogarId == null) {
            throw new RuntimeException("El ID del hogar es obligatorio para listar tareas");
        }
        return tareaRepository.findByHogarId(hogarId);
    }
}