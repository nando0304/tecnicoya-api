package com.tecnicoya.api.dao.impl;

import com.tecnicoya.api.dao.IGenericDao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.metamodel.EntityType;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class GenericDaoImpl<T, ID> implements IGenericDao<T, ID> {
    @PersistenceContext
    protected EntityManager em;

    private final Class<T> claseEntidad;

    protected GenericDaoImpl(Class<T> claseEntidad) {
        this.claseEntidad = checkNotNull(claseEntidad, "La clase de la entidad es obligatoria");
    }

    @Override
    public List<T> listar() {
        return em.createQuery(consultaListar(), claseEntidad).getResultList();
    }

    protected String consultaListar() {
        return "SELECT e FROM " + nombreEntidad() + " e ORDER BY e." + nombreId();
    }

    @Override
    public Optional<T> buscarPorId(ID id) {
        checkNotNull(id, "El id no puede ser nulo");
        return Optional.ofNullable(em.find(claseEntidad, id));
    }

    @Override
    public T guardar(T entidad) {
        checkNotNull(entidad, "La entidad a guardar no puede ser nula");
        if (esNueva(entidad)) {
            em.persist(entidad);
            return entidad;
        }
        return em.merge(entidad);
    }

    @Override
    public void eliminar(T entidad) {
        checkNotNull(entidad, "La entidad a eliminar no puede ser nula");
        em.remove(em.contains(entidad) ? entidad : em.merge(entidad));
        em.flush();
    }

    @Override
    public long contar() {
        return em.createQuery("SELECT COUNT(e) FROM " + nombreEntidad() + " e", Long.class).getSingleResult();
    }

    protected static boolean existe(TypedQuery<Long> consulta) {
        return consulta.getSingleResult() > 0;
    }

    private boolean esNueva(T entidad) {
        return em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entidad) == null;
    }

    private String nombreEntidad() {
        return tipoEntidad().getName();
    }

    private String nombreId() {
        EntityType<T> tipo = tipoEntidad();
        return tipo.getId(tipo.getIdType().getJavaType()).getName();
    }

    private EntityType<T> tipoEntidad() {
        return em.getMetamodel().entity(claseEntidad);
    }
}
