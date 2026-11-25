package com.tecnicoya.api.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Auditoria {
    public static final Logger LOG = LoggerFactory.getLogger("AUDITORIA");

    public static String limpiar(Object valor) {
        if (valor == null) {
            return null;
        }

        return StringUtils.abbreviate(String.valueOf(valor), 200).replace("\r", "\\r").replace("\n", "\\n");
    }

    private Auditoria() {
    }
}
