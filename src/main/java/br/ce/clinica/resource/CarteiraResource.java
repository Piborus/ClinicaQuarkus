package br.ce.clinica.resource;

import br.ce.clinica.dto.request.CarteiraRequest;
import br.ce.clinica.dto.response.CarteiraResumeResponse;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.openapi.ApiDocumentation;
import br.ce.clinica.service.CarteiraService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

@Path("/carteira")
@Consumes("application/json")
@Produces("application/json")
@ApplicationScoped
@WithSession
@Tag(name = "Carteira"
        , description = "Controlador para gerenciar " +
        "as transaçoes dos paciente no sistema")
@ApiDocumentation
public class CarteiraResource {

    @Inject
    CarteiraService carteiraService;

    @POST
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Operation(summary = "Cria uma Transação", description = "Cria uma transação para um paciente no sistema")
    public Uni<RestResponse<CarteiraResumeResponse>> salvar (
            @Valid CarteiraRequest carteiraRequest
    ){
        return carteiraService.save(carteiraRequest)
                .onItem()
                .transform(transacaoResumeResponse -> RestResponse
                        .ResponseBuilder.create(RestResponse.Status.CREATED, transacaoResumeResponse).build());
    }

    @GET
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Path("/{id}")
    @Operation(summary = "Busca Transação por id",
            description = "Busca uma transação pelo id no sistema")
    public Uni<RestResponse<CarteiraResumeResponse>> buscarPorId(
            @PathParam("id") Long id
    ) {
        return carteiraService.findById(id)
                .onItem().transform(RestResponse::ok);

    }

    @DELETE
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Path("/{id}")
    @Operation(summary = "Deleta uma transação pelo id",
            description = "Deleta uma transação pelo id no sistema")
    public Uni<RestResponse<Boolean>> deletarPorId(
            @PathParam("id") Long id
    ){
        return carteiraService.deleteById(id)
                .onItem()
                .transform(transacao -> RestResponse.noContent());
    }

    @PUT
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Path("/{id}")
    public Uni<RestResponse<CarteiraResumeResponse>> atualizar(
            @PathParam("id") Long id,
            @Valid CarteiraRequest carteiraRequest
    ) {
        return carteiraService.update(id, carteiraRequest)
                .onItem()
                .transform(RestResponse::ok);
    }

    @GET
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Operation(summary = "Lista transações paginadas",
            description = "Lista as transações com paginação, ordenação e filtros opcionais")
    public Uni<RestResponse<PanachePage<CarteiraResumeResponse>>> listarRegistrosPag(
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size,
            @QueryParam("sort") String sort,
            @QueryParam("filterFields") List<String> filterFields,
            @QueryParam("filterValues") List<String> filterValues
    ) {
        Page panachePage = Page.of(page - 1,size);
        return carteiraService.findPaginated(
                panachePage,
                sort,
                filterFields,
                filterValues
        ).onItem().transform(RestResponse :: ok);
    }
}
