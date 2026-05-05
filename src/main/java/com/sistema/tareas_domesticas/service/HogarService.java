package com.sistema.tareas_domesticas.service;

import com.sistema.tareas_domesticas.model.Hogar;
import com.sistema.tareas_domesticas.model.Usuario;
import com.sistema.tareas_domesticas.repository.HogarRepository;
import com.sistema.tareas_domesticas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HogarService {

    @Autowired
    private HogarRepository hogarRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Hogar crearHogar(Long usuarioId, String nombreHogar) {
        if (nombreHogar == null || nombreHogar.isBlank()) {
            throw new RuntimeException("Nombre de hogar inválido");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getFamiliaId() != null) {
            throw new RuntimeException("Ya perteneces a un hogar");
        }

        Hogar hogar = new Hogar();
        hogar.setNombre(nombreHogar);
        hogar.setCodigoInvitacion(generarCodigoUnico());

        Hogar hogarGuardado = hogarRepository.save(hogar);

        usuario.setFamiliaId(hogarGuardado.getId());
        usuario.setRol("ADMINISTRADOR");
        usuarioRepository.save(usuario);

        return hogarGuardado;
    }

    public Hogar unirseAHogar(Long usuarioId, String codigoInvitacion) {
        // 1. Validar que el código no sea nulo
        if (codigoInvitacion == null || codigoInvitacion.isBlank()) {
            throw new RuntimeException("El código de invitación es obligatorio");
        }

        // 2. Buscar el hogar por código de invitación
        Hogar hogar = hogarRepository.findByCodigoInvitacion(codigoInvitacion)
                .orElseThrow(() -> new RuntimeException("Hogar no encontrado con el código proporcionado"));

        // 3. Buscar al usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 4. Verificar si el usuario ya tiene un hogar
        if (usuario.getFamiliaId() != null) {
            throw new RuntimeException("El usuario ya pertenece a un hogar");
        }

        // 5. Vincular usuario al hogar y asignar rol de MIEMBRO
        usuario.setFamiliaId(hogar.getId());
        usuario.setRol("MIEMBRO");
        usuarioRepository.save(usuario);

        return hogar;
    }

    private String generarCodigoUnico() {
        String codigo;
        do {
            codigo = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (hogarRepository.findByCodigoInvitacion(codigo).isPresent());
        return codigo;
    }
}
