package com.tecnicoya.api.dao.impl;

import com.tecnicoya.api.dao.IExperienciaLaboralDao;
import com.tecnicoya.api.model.ExperienciaLaboral;
import org.springframework.stereotype.Repository;

/** Implementación JPA de {@link IExperienciaLaboralDao}. */
@Repository
public class ExperienciaLaboralDaoImpl extends GenericDaoImpl<ExperienciaLaboral, Long> implements IExperienciaLaboralDao {

    public ExperienciaLaboralDaoImpl() {
        super(ExperienciaLaboral.class);
    }

    /** Trae el técnico y su usuario en la misma consulta (evita el problema N+1). */
    @Override
    protected String consultaListar() {
        return """
                SELECT e FROM ExperienciaLaboral e
                JOIN FETCH e.tecnico t
                JOIN FETCH t.usuario
                ORDER BY e.idExperiencia
                """;
    }
}
