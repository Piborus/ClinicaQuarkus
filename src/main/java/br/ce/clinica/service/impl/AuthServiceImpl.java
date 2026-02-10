package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.UsuarioRequest;
import br.ce.clinica.dto.response.TokenResponse;
import br.ce.clinica.dto.response.UsuarioResponse;
import br.ce.clinica.entity.Usuario;
import br.ce.clinica.exception.BadRequestBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.repository.UsuarioRepository;
import br.ce.clinica.security.GenerateToken;
import br.ce.clinica.security.PasswordEnconder;
import br.ce.clinica.service.AuthService;
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


    @Override
    public Uni<TokenResponse> login(UsuarioRequest request) {
        return usuarioRepository.findByEmail(request.getEmail())
                .onItem().ifNull().failWith(
                        new NotFoundBusinessException("Usuário ou senha inválidos")
                )
                .flatMap(usuario -> {

                    if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
                        return Uni.createFrom().failure(
                                new NotFoundBusinessException("Usuário ou senha inválidos")
                        );
                    }

                    String token = generateToken.generateToken(usuario);

                    return Uni.createFrom().item(
                            TokenResponse.tokenResponse(token)
                    );
                });
    }


    @Override
    public Uni<UsuarioResponse> cadastra(UsuarioRequest request) {
        return usuarioRepository.findByEmail(request.getEmail())
                .onItem().ifNotNull().failWith(
                        new BadRequestBusinessException("Email já cadastrado")
                )
                .onItem().ifNull().switchTo(() -> {

                    Usuario usuario = Usuario.builder()
                            .nome(request.getNome())
                            .email(request.getEmail())
                            .senha(passwordEncoder.hash(request.getSenha()))
                            .tipoUsuario(request.getTipoUsuario())
                            .build();

                    return usuarioRepository.persist(usuario);
                })
                .map(UsuarioResponse::toResponse);
    }
}
