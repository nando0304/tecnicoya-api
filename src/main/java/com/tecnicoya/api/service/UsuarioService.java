package com.tecnicoya.api.service;

import com.tecnicoya.api.dto.request.LoginRequest;
import com.tecnicoya.api.dto.request.UsuarioRequest;
import com.tecnicoya.api.dto.response.UsuarioResponse;
import com.tecnicoya.api.exception.CredencialesInvalidasException;

/**
 * Lógica de negocio de usuarios (clientes, técnicos y administradores).
 *
 * <p>Reglas:
 * <ul>
 *   <li>El correo se guarda en minúsculas y sin espacios, y debe ser único.</li>
 *   <li>La contraseña es obligatoria al crear y se guarda cifrada con BCrypt; al actualizar, si no se envía, se conserva la actual.</li>
 *   <li>El estado por defecto es {@code ACTIVO}; al actualizar, si no se envía, se conserva.</li>
 *   <li>Un usuario con perfil de técnico no puede cambiar su tipo.</li>
 *   <li>La fecha de registro la asigna el sistema.</li>
 *   <li>No se puede eliminar un usuario con servicios registrados; su perfil de técnico se elimina en cascada.</li>
 * </ul>
 */
public interface UsuarioService extends CrudService<UsuarioRequest, UsuarioResponse> {

    /**
     * Valida las credenciales de inicio de sesión.
     *
     * <p>Responde con el mismo mensaje si el correo no existe o si la contraseña es incorrecta,
     * para no revelar qué correos están registrados.
     *
     * @param request correo y contraseña en texto plano
     * @return los datos del usuario autenticado, sin la contraseña
     * @throws CredencialesInvalidasException si las credenciales son incorrectas o la cuenta no está {@code ACTIVO}
     */
    UsuarioResponse autenticar(LoginRequest request);
}
