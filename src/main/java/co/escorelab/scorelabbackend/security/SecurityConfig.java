package co.escorelab.scorelabbackend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:3000"));
                    // 🌟 Aseguramos que PATCH esté presente para la aprobación
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Endpoints Públicos
                        .requestMatchers("/api/auth/**").permitAll()

                        // 2. Torneos
                        .requestMatchers(HttpMethod.GET, "/api/torneos/todos").authenticated()
                        .requestMatchers("/api/torneos/**").authenticated()

                        // 3. Equipos (Reglas específicas de arriba a abajo)
                        // 🌟 SOLO el Organizador puede ver pendientes y aprobar
                        // Cambia esto:
                        .requestMatchers(HttpMethod.GET, "/api/equipos/pendientes").hasAuthority("ORGANIZADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/equipos/*/aprobar").hasAuthority("ORGANIZADOR")

                        // El resto de acciones de equipos para cualquier usuario autenticado (Delegados)
                        .requestMatchers("/api/equipos/**").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}