package co.escorelab.scorelabbackend.service;

import co.escorelab.scorelabbackend.dto.RegistroRequest;
import co.escorelab.scorelabbackend.model.Usuario;
import co.escorelab.scorelabbackend.repository.UsuarioRepository;
import co.escorelab.scorelabbackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Usuario registrar(RegistroRequest request) {
        // 1. Verificar si el correo ya existe
        if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        // 2. Crear el nuevo usuario
        Usuario nuevoUsuario = Usuario.builder()
                .nombre(request.getNombre())
                .correo(request.getCorreo())
                .contrasena(passwordEncoder.encode(request.getContrasena())) // Encriptamos!
                .rol(request.getRol())
                .build();

        return usuarioRepository.save(nuevoUsuario);
    }

    public String login(String correo, String contrasena) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            // Si la clave es correcta, generamos el token
            return new JwtService().generarToken(usuario.getCorreo());
        } else {
            throw new RuntimeException("Contraseña incorrecta");
        }
    }
}
