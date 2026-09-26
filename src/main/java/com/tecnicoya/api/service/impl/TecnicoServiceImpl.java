package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dto.request.TecnicoRequest;
import com.tecnicoya.api.dto.response.TecnicoResponse;
import com.tecnicoya.api.entity.Tecnico;
import com.tecnicoya.api.entity.Usuario;
import com.tecnicoya.api.entity.enums.EstadoVerificacion;
import com.tecnicoya.api.entity.enums.TipoUsuario;
import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.repository.TecnicoRepository;
import com.tecnicoya.api.repository.UsuarioRepository;
import com.tecnicoya.api.service.TecnicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TecnicoServiceImpl implements TecnicoService {

    private final TecnicoRepository tecnicoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public List<TecnicoResponse> listar() {
        return tecnicoRepository.findAll().stream().map(TecnicoResponse::desde).toList();
    }

    @Override
    public TecnicoResponse obtenerPorId(Long id) {
        return TecnicoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public TecnicoResponse crear(TecnicoRequest request) {
        Usuario usuario = buscarUsuarioTecnico(request.usuarioId());
        if (tecnicoRepository.existsByUsuario_IdUsuario(usuario.getIdUsuario())) {
            throw new ConflictoException("El usuario con id " + usuario.getIdUsuario()
                    + " ya tiene un perfil de técnico registrado");
        }

        Tecnico tecnico = new Tecnico();
        tecnico.setUsuario(usuario);
        copiarDatos(request, tecnico);
        tecnico.setEstadoVerificacion(request.estadoVerificacion() != null
                ? request.estadoVerificacion() : EstadoVerificacion.PENDIENTE);
        return TecnicoResponse.desde(tecnicoRepository.save(tecnico));
    }

    @Override
    @Transactional
    public TecnicoResponse actualizar(Long id, TecnicoRequest request) {
        Tecnico tecnico = buscar(id);
        if (!tecnico.getUsuario().getIdUsuario().equals(request.usuarioId())) {
            Usuario usuario = buscarUsuarioTecnico(request.usuarioId());
            if (tecnicoRepository.existsByUsuario_IdUsuarioAndIdTecnicoNot(usuario.getIdUsuario(), id)) {
                throw new ConflictoException("El usuario con id " + usuario.getIdUsuario()
                        + " ya tiene otro perfil de técnico registrado");
            }
            tecnico.setUsuario(usuario);
        }

        copiarDatos(request, tecnico);
        if (request.estadoVerificacion() != null) {
            tecnico.setEstadoVerificacion(request.estadoVerificacion());
        }
        return TecnicoResponse.desde(tecnicoRepository.save(tecnico));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        tecnicoRepository.delete(buscar(id));
        tecnicoRepository.flush();
    }

    private Tecnico buscar(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el técnico", id));
    }

    private Usuario buscarUsuarioTecnico(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("el usuario", usuarioId));
        if (usuario.getTipoUsuario() != TipoUsuario.TECNICO) {
            throw new ReglaNegocioException("El usuario con id " + usuarioId
                    + " no es de tipo TECNICO; solo esos usuarios pueden tener perfil de técnico");
        }
        return usuario;
    }

    private static void copiarDatos(TecnicoRequest request, Tecnico tecnico) {
        tecnico.setEspecialidad(request.especialidad().trim());
        tecnico.setDescripcion(request.descripcion());
    }
}
