package com.tecnicoya.api.dao.impl;

import com.tecnicoya.api.dao.IUsuarioDao;
import com.tecnicoya.api.model.Usuario;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UsuarioDaoImpl extends GenericDaoImpl<Usuario, Long> implements IUsuarioDao {
    public UsuarioDaoImpl() {
        super(Usuario.class);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return em.createQuery("SELECT u FROM Usuario u WHERE u.correo = :correo", Usuario.class)
                .setParameter("correo", correo)
                .getResultStream()
                .findFirst();
    }

    @Override
    public boolean existeCorreo(String correo) {
        return existe(em.createQuery("SELECT COUNT(u) FROM Usuario u WHERE u.correo = :correo", Long.class)
                .setParameter("correo", correo));
    }

    @Override
    public boolean existeCorreoEnOtroUsuario(String correo, Long idUsuario) {
        return existe(em.createQuery("""
                        SELECT COUNT(u) FROM Usuario u
                        WHERE u.correo = :correo AND u.idUsuario <> :idUsuario
                        """, Long.class)
                .setParameter("correo", correo)
                .setParameter("idUsuario", idUsuario));
    }
}
