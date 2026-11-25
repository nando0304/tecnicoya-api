package com.tecnicoya.api.dao.impl;

import com.tecnicoya.api.dao.IServicioDao;
import com.tecnicoya.api.model.Servicio;
import org.springframework.stereotype.Repository;

@Repository
public class ServicioDaoImpl extends GenericDaoImpl<Servicio, Long> implements IServicioDao {
    public ServicioDaoImpl() {
        super(Servicio.class);
    }

    @Override
    protected String consultaListar() {
        return """
                SELECT e FROM Servicio e
                JOIN FETCH e.cliente
                LEFT JOIN FETCH e.tecnico t
                LEFT JOIN FETCH t.usuario
                ORDER BY e.idServicio
                """;
    }
}
