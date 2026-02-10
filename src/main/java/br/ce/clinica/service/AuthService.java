package br.ce.clinica.service;

import br.ce.clinica.dto.request.UsuarioRequest;
import br.ce.clinica.dto.response.TokenResponse;
import br.ce.clinica.dto.response.UsuarioResponse;
import io.smallrye.mutiny.Uni;

public interface AuthService {

    Uni<TokenResponse> login(UsuarioRequest request);

    Uni<UsuarioResponse> cadastra(UsuarioRequest request);
}
