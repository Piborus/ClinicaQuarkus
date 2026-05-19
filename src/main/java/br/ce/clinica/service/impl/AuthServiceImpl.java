package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.LoginRequest;
import br.ce.clinica.dto.request.RedefinirSenhaRequest;
import br.ce.clinica.dto.request.RefreshTokenRequest;
import br.ce.clinica.dto.request.UsuarioRequest;
import br.ce.clinica.dto.response.TokenResponse;
import br.ce.clinica.dto.response.UsuarioResponse;
import br.ce.clinica.entity.Usuario;
import br.ce.clinica.enums.TipoUsuario;
import br.ce.clinica.exception.BadRequestBusinessException;
import br.ce.clinica.exception.UnauthorizedBusinessException;
import br.ce.clinica.repository.UsuarioRepository;
import br.ce.clinica.security.*;
import br.ce.clinica.service.AuthService;
import br.ce.clinica.service.EmailService;
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

    @Inject
    RecuperacaoSenhaRedisService recuperacaoSenhaRedisService;

    @Inject
    CodigoRecuperacao codigoRecuperacao;

    @Inject
    EmailService emailService;

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
                            .dataNascimento(request.getDataNascimento())
                            .email(request.getEmail())
                            .senha(passwordEncoder.hash(request.getSenha()))
                            .telefone(request.getTelefone())
                            .cpf(request.getCpf())
                            .crp(request.getCrp())
                            .tipoUsuario(request.getCrp() != null ? TipoUsuario.PSICOLOGO : null)
                            .especialidade(request.getEspecialidade())
                            .psicologiaAbordagem(request.getPsicologiaAbordagem())
                            .build();

                    return usuarioRepository.persist(usuario)
                            .onItem().transformToUni(email -> emailService.enviarEmailBemVindo(
                                            usuario.getEmail(),
                                            usuario.getNome(),
                                            usuario.getDataCriacao().toLocalDate()
                                    )
                                    .onItem().transformToUni(ignored -> Uni.createFrom().item(usuario))
                            );
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


    @Override
    public Uni<Void> esqueciSenha(String email) {
        String emailNormalizado = email.trim().toLowerCase();


        return usuarioRepository.findByEmail(emailNormalizado)
                .onItem().transformToUni
                        (usuario -> {
                                    if (usuario == null) {
                                        return Uni.createFrom().voidItem();
                                    }

                                    String codigo = codigoRecuperacao.gerarCodigoRecuperacao();

                                    return recuperacaoSenhaRedisService.salvarCodigo(usuario.getId(), codigo)
                                            .onItem()
                                            .transformToUni(result -> emailService.enviarEmailRecuperacaoSenha(
                                                            usuario.getEmail(),
                                                            usuario.getNome(),
                                                            codigo
                                                    )
                                            );

                                }
                        );

    }

    @Override
    public Uni<Void> redefinirSenha(RedefinirSenhaRequest request) {
        String emailNormalizado = request.getEmail().trim().toLowerCase();

        return Panache.withTransaction(() -> usuarioRepository.findByEmail(emailNormalizado)
                .onItem().ifNull().failWith(() -> new BadRequestBusinessException("Codigo expirado ou inválido")
                ).onItem().ifNotNull().transformToUni(usuario -> recuperacaoSenhaRedisService.tentativasExcedidas(usuario.getId())
                        .onItem().transformToUni(excedeu -> {
                            if (Boolean.TRUE.equals(excedeu)) {
                                return Uni.createFrom().failure(new BadRequestBusinessException("Codigo expirado ou inválido"));
                            }
                            return recuperacaoSenhaRedisService.codigoValido(usuario.getId(), request.getCodigo())
                                    .onItem().transformToUni(codigoValido -> {
                                        if (!Boolean.TRUE.equals(codigoValido)) {
                                            return recuperacaoSenhaRedisService.incrementarTentativas(usuario.getId())
                                                    .onItem().transformToUni(totalTentativas ->
                                                            Uni.createFrom().failure(new BadRequestBusinessException("Codigo expirado ou inválido")));
                                        }
                                        usuario.setSenha(passwordEncoder.hash(request.getNovaSenha()));
                                        return usuarioRepository.persist(usuario)
                                                .onItem().transformToUni(ignored ->
                                                        recuperacaoSenhaRedisService.removerCodigo(usuario.getId())
                                                );
                                    });
                        })
                )
        );
    }


}
