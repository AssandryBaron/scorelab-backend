package co.escorelab.scorelabbackend.repository;

import co.escorelab.scorelabbackend.model.Equipo;
import co.escorelab.scorelabbackend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    // Para validar que no existan dos equipos con el mismo nombre
    Optional<Equipo> findByNombre(String nombre);

    // Para que un usuario delegado vea los equipos que ha creado
    List<Equipo> findByDelegado(Usuario delegado);

    // 🌟 NUEVO: Para que el organizador encuentre todos los equipos que esperan aprobación
    List<Equipo> findByEstado(String estado);
}
