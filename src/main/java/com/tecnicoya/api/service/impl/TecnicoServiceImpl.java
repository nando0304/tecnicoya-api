package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dao.ITecnicoDao;
import com.tecnicoya.api.dao.IUsuarioDao;
import com.tecnicoya.api.dto.request.TecnicoRequest;
import com.tecnicoya.api.dto.response.TecnicoResponse;
import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.model.enums.EstadoVerificacion;
import com.tecnicoya.api.model.enums.TipoUsuario;
import com.tecnicoya.api.model.Tecnico;
import com.tecnicoya.api.model.Usuario;
import com.tecnicoya.api.service.TecnicoService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Implementación de {@link TecnicoService}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TecnicoServiceImpl implements TecnicoService {

    private final ITecnicoDao tecnicoDao;
    private final IUsuarioDao usuarioDao;

    @Override
    public List<TecnicoResponse> listar() {
        return tecnicoDao.listar().stream().map(TecnicoResponse::desde).toList();
    }

    @Override
    public TecnicoResponse obtenerPorId(Long id) {
        return TecnicoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public TecnicoResponse crear(TecnicoRequest request) {
        Usuario usuario = buscarUsuarioTecnico(request.usuarioId());
        if (tecnicoDao.existePerfilDeUsuario(usuario.getIdUsuario())) {
            throw new ConflictoException("El usuario con id " + usuario.getIdUsuario()
                    + " ya tiene un perfil de técnico registrado");
        }

        Tecnico tecnico = new Tecnico();
        tecnico.setUsuario(usuario);
        copiarDatos(request, tecnico);
        tecnico.setEstadoVerificacion(request.estadoVerificacion() != null
                ? request.estadoVerificacion() : EstadoVerificacion.PENDIENTE);
        return TecnicoResponse.desde(tecnicoDao.guardar(tecnico));
    }

    @Override
    @Transactional
    public TecnicoResponse actualizar(Long id, TecnicoRequest request) {
        Tecnico tecnico = buscar(id);
        if (!tecnico.getUsuario().getIdUsuario().equals(request.usuarioId())) {
            Usuario usuario = buscarUsuarioTecnico(request.usuarioId());
            if (tecnicoDao.existePerfilDeUsuarioEnOtroTecnico(usuario.getIdUsuario(), id)) {
                throw new ConflictoException("El usuario con id " + usuario.getIdUsuario()
                        + " ya tiene otro perfil de técnico registrado");
            }
            tecnico.setUsuario(usuario);
        }

        copiarDatos(request, tecnico);
        if (request.estadoVerificacion() != null) {
            tecnico.setEstadoVerificacion(request.estadoVerificacion());
        }
        return TecnicoResponse.desde(tecnicoDao.guardar(tecnico));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        tecnicoDao.eliminar(buscar(id));
    }

    private Tecnico buscar(Long id) {
        return tecnicoDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el técnico", id));
    }

    private Usuario buscarUsuarioTecnico(Long usuarioId) {
        Usuario usuario = usuarioDao.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("el usuario", usuarioId));
        if (usuario.getTipoUsuario() != TipoUsuario.TECNICO) {
            throw new ReglaNegocioException("El usuario con id " + usuarioId
                    + " no es de tipo TECNICO; solo esos usuarios pueden tener perfil de técnico");
        }
        return usuario;
    }

    private static void copiarDatos(TecnicoRequest request, Tecnico tecnico) {
        tecnico.setEspecialidad(StringUtils.normalizeSpace(request.especialidad()));
        tecnico.setDescripcion(StringUtils.stripToNull(request.descripcion()));
    }
}
