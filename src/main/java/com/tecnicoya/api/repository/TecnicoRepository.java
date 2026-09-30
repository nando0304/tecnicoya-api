package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.Tecnico;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a datos de {@link Tecnico}. */
public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {

    /** Lista todos los registros cargando su usuario en la misma consulta (evita el problema N+1). */
    @Override
    @EntityGraph(attributePaths = "usuario")
    List<Tecnico> findAll();

    /** Indica si el usuario ya tiene un perfil de técnico. */
    boolean existsByUsuario_IdUsuario(Long usuarioId);

    /** Indica si el usuario tiene otro perfil de técnico distinto de {@code idTecnico}. */
    boolean existsByUsuario_IdUsuarioAndIdTecnicoNot(Long usuarioId, Long idTecnico);
}
