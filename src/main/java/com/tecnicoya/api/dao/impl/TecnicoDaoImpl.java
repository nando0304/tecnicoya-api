package com.tecnicoya.api.dao.impl;

import com.tecnicoya.api.dao.ITecnicoDao;
import com.tecnicoya.api.model.Tecnico;
import org.springframework.stereotype.Repository;

@Repository
public class TecnicoDaoImpl extends GenericDaoImpl<Tecnico, Long> implements ITecnicoDao {
    public TecnicoDaoImpl() {
        super(Tecnico.class);
    }

    @Override
    protected String consultaListar() {
        return "SELECT e FROM Tecnico e JOIN FETCH e.usuario ORDER BY e.idTecnico";
    }

    @Override
    public boolean existePerfilDeUsuario(Long usuarioId) {
        return existe(em.createQuery(
                        "SELECT COUNT(t) FROM Tecnico t WHERE t.usuario.idUsuario = :usuarioId", Long.class)
                .setParameter("usuarioId", usuarioId));
    }

    @Override
    public boolean existePerfilDeUsuarioEnOtroTecnico(Long usuarioId, Long idTecnico) {
        return existe(em.createQuery("""
                        SELECT COUNT(t) FROM Tecnico t
                        WHERE t.usuario.idUsuario = :usuarioId AND t.idTecnico <> :idTecnico
                        """, Long.class)
                .setParameter("usuarioId", usuarioId)
                .setParameter("idTecnico", idTecnico));
    }
}
