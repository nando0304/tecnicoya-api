package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.Tecnico;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {

    @Override
    @EntityGraph(attributePaths = "usuario")
    List<Tecnico> findAll();

    boolean existsByUsuario_IdUsuario(Long usuarioId);

    boolean existsByUsuario_IdUsuarioAndIdTecnicoNot(Long usuarioId, Long idTecnico);
}
