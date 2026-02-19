package br.ce.clinica.resource;

import br.ce.clinica.dto.request.LoginRequest;
import br.ce.clinica.dto.request.UsuarioRequest;
import br.ce.clinica.dto.response.TokenResponse;
import br.ce.clinica.dto.response.UsuarioResponse;
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
    public Uni<RestResponse<UsuarioResponse>> cadastrar(
            @Valid UsuarioRequest request)
    {
        return authService.save(request)
                .onItem().transform(usuario -> RestResponse
                        .ResponseBuilder.create(RestResponse.Status.CREATED, usuario).build());
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
}
