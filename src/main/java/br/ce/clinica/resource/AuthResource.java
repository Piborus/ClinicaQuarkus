package br.ce.clinica.resource;

import br.ce.clinica.dto.request.*;
import br.ce.clinica.dto.response.ApiResponse;
import br.ce.clinica.dto.response.TokenResponse;
import br.ce.clinica.openapi.ApiDocumentation;
import br.ce.clinica.service.AuthService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
@Path("/auth")
@Tag(name = "Auth",
        description = "Controlador para gerenciar autenticação no sistema")
@ApiDocumentation
@WithSession
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Operation(summary = "Cadastra usuário", description = "Cadastra um novo usuário no sistema")
    public Uni<RestResponse<ApiResponse>> cadastrar(
            @Valid UsuarioRequest request
    ){
        return authService.save(request)
                .onItem().transform(usuario -> RestResponse
                        .status(RestResponse.Status.CREATED,
                                ApiResponse.builder()
                                        .message("Usuario cadastrado com sucesso")
                                        .build()
                                ));
    }

    @POST
    @Path("/login")
    @Operation(summary = "Login", description = "Realiza o login do usuário no sistema")
    public Uni<RestResponse<TokenResponse>> login(
            @Valid LoginRequest request
    ){
        return authService.login(request)
                .onItem().transform(usuario -> RestResponse
                        .ResponseBuilder.create(RestResponse.Status.CREATED, usuario).build());
    }

    @POST
    @Path("/refresh")
    @Operation(summary = "Refresh Token", description = "Atualiza o token de acesso")
    public Uni<RestResponse<TokenResponse>> refreshToken(
            @Valid RefreshTokenRequest request
    ) {
        return authService.refreshToken(request.getToken())
                .onItem().transform(usuario -> RestResponse
                        .ResponseBuilder.create(RestResponse.Status.CREATED, usuario).build());
    }

    @POST
    @Path("/logout")
    @Operation(summary = "Logout", description = "Realiza o logout do usuário no sistema")
    public Uni<RestResponse<Void>> logout(
            @Valid RefreshTokenRequest token
    ) {
        return authService.logout(token)
                .onItem().transform(refreshToken -> RestResponse.noContent());
    }

    @POST
    @Path("/esqueci-senha")
    @Operation(summary = "Envia um email para recuperar a senha", description = "Envia um email para recuperar a senha")
    public Uni<RestResponse<ApiResponse>> enviarEmailEsqueciSenha(
            @Valid EsqueciSenhaRequest request
    ) {
        return authService.esqueciSenha(request.getEmail())
                .onItem()
                .transform(esqueciSenha -> RestResponse.ok(
                        ApiResponse.builder()
                                .message("Se o e-mail estiver cadastrado, enviaremos um código para redefinir sua senha.")
                                .build()
                ));
    }

    @POST
    @Path("/redefinir-senha")
    @Operation(summary = "Redefinir senha", description = "Redefine a senha do usuário utilizando um código de verificação enviado por email")
    public Uni<RestResponse<ApiResponse>> redefinirSenha(
            @Valid RedefinirSenhaRequest request
    ){
        return authService.redefinirSenha(request)
                .onItem()
                .transform(redefinirSenha -> RestResponse.ok(
                        ApiResponse.builder()
                                .message("Senha redefinida com sucesso")
                                .build()
                ));
    }
}
