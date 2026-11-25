package com.tecnicoya.api.dao.impl;

import com.tecnicoya.api.dao.IExperienciaLaboralDao;
import com.tecnicoya.api.model.ExperienciaLaboral;
import org.springframework.stereotype.Repository;

@Repository
public class ExperienciaLaboralDaoImpl extends GenericDaoImpl<ExperienciaLaboral, Long> implements IExperienciaLaboralDao {
    public ExperienciaLaboralDaoImpl() {
        super(ExperienciaLaboral.class);
    }

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
