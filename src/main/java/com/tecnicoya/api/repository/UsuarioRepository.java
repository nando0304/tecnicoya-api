package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Acceso a datos de {@link Usuario}. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Busca un usuario por su correo (debe llegar normalizado en minúsculas). */
    Optional<Usuario> findByCorreo(String correo);

    /** Indica si algún usuario ya usa el correo. */
    boolean existsByCorreo(String correo);

    /** Indica si otro usuario, distinto de {@code idUsuario}, ya usa el correo (validación al actualizar). */
    boolean existsByCorreoAndIdUsuarioNot(String correo, Long idUsuario);
}
