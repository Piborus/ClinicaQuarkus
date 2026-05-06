package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.LoginRequest;
import br.ce.clinica.dto.request.RefreshTokenRequest;
import br.ce.clinica.dto.request.UsuarioRequest;
import br.ce.clinica.dto.response.TokenResponse;
import br.ce.clinica.dto.response.UsuarioResponse;
import br.ce.clinica.entity.Usuario;
import br.ce.clinica.enums.TipoUsuario;
import br.ce.clinica.exception.BadRequestBusinessException;
import br.ce.clinica.exception.UnauthorizedBusinessException;
import br.ce.clinica.repository.UsuarioRepository;
import br.ce.clinica.security.GenerateToken;
import br.ce.clinica.security.PasswordEnconder;
import br.ce.clinica.security.RefreshToken;
import br.ce.clinica.service.AuthService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthServiceImpl implements AuthService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    GenerateToken generateToken;

    @Inject
    PasswordEnconder passwordEncoder;

    @Inject
    RefreshToken refreshToken;



    @Override
    public Uni<TokenResponse> login(LoginRequest request) {
        return Panache.withTransaction(() -> usuarioRepository.findByEmail(request.getEmail())
                .onItem().ifNull().failWith(
                        new UnauthorizedBusinessException("Usuário ou senha inválidos")
                )
                .flatMap(usuario -> {

                    if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
                        return Uni.createFrom().failure(
                                new UnauthorizedBusinessException("Usuário ou senha inválidos")
                        );
                    }

                    String accessToken = generateToken.generateToken(usuario);
                    return refreshToken.generateRefreshToken(usuario.getId())
                            .map(refresh -> TokenResponse.tokenResponse(accessToken, refresh));
                }));
    }


    @Override
    public Uni<UsuarioResponse> save(UsuarioRequest request) {
        return Panache.withTransaction(() -> usuarioRepository.findByEmail(request.getEmail())
                .onItem().ifNotNull().failWith(
                        new BadRequestBusinessException("Email já cadastrado")
                )
                .onItem().ifNull().switchTo(() -> {

                    Usuario usuario = Usuario.builder()
                            .nome(request.getNome())
                            .sobrenome(request.getSobrenome())
                            .dataNascimento(request.getDataNascimento())
                            .email(request.getEmail())
                            .senha(passwordEncoder.hash(request.getSenha()))
                            .telefone(request.getTelefone())
                            .cpf(request.getCpf())
                            .crp(request.getCrp())
                            .tipoUsuario(request.getCrp() != null ? TipoUsuario.PSICOLOGO : null)
                            .especialidade(request.getEspecialidade())
                            .build();

                    return usuarioRepository.persist(usuario);
                })
                .map(UsuarioResponse::toResponse));
    }

    @Override
    public Uni<TokenResponse> refreshToken(String refreshTokenValue) {
        return Panache.withTransaction(() -> refreshToken.validateRefreshToken(refreshTokenValue)
                .flatMap(userId -> usuarioRepository.findById(userId))
                .onItem().ifNull().failWith(
                        new UnauthorizedBusinessException("Usuário não encontrado")
                )
                .map(usuario -> {
                    String accessToken = generateToken.generateToken(usuario);
                    return TokenResponse.tokenResponse(accessToken, refreshTokenValue);
                }));
    }

    @Override
    public Uni<Void> logout(RefreshTokenRequest token) {
        return refreshToken.revokeRefreshToken(token.getToken());
    }

}
