package co.escorelab.scorelabbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Obtener el encabezado "Authorization"
        String authHeader = request.getHeader("Authorization");

        // 2. Si no trae Token o no empieza por "Bearer ", ignoramos y seguimos
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token puro
        String token = authHeader.substring(7);

        try {
            // 4. Extraer el correo del token usando el servicio
            String email = jwtService.extraerCorreo(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 5. Creamos la credencial de autenticación para Spring
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

                // 6. Guardamos al usuario en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Si el token es falso o expiró, simplemente no lo autenticamos
            System.out.println("Error validando el token: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
