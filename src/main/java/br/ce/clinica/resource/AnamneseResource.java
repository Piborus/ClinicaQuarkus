package br.ce.clinica.resource;

import br.ce.clinica.dto.request.AnamneseRequest;
import br.ce.clinica.dto.response.AnamneseResponse;
import br.ce.clinica.openapi.ApiDocumentation;
import br.ce.clinica.service.AnamneseService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
@Path("/anamnese")
@Tag(name = "Anamnese",
        description = "Controlador para gerenciar Anamnese no sistema")
@ApiDocumentation
@WithSession
public class AnamneseResource {

    @Inject
    AnamneseService anamneseService;

    @POST
    @Operation(summary = "Salva anamnese", description = "Salva uma nova anamnese no sistema")
    public Uni<RestResponse<AnamneseResponse>> salvar(
           @Valid AnamneseRequest anamneseRequest
    ){
        return anamneseService.save(anamneseRequest)
                .onItem()
                .transform(anamnese -> RestResponse
                        .ResponseBuilder.create(RestResponse.Status.CREATED, anamnese).build());
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualiza anamnese", description = "Atualiza uma anamnese no sistema")
    public Uni<RestResponse<AnamneseResponse>> atualizar(
            @PathParam("id") Long id,
            @Valid AnamneseRequest anamneseRequest
    ){
        return anamneseService.update(id, anamneseRequest)
                .onItem()
                .transform(RestResponse::ok);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca anamnese por id", description = "Busca uma anamnese pelo id no sistema")
    public Uni<RestResponse<AnamneseResponse>> buscarPorId(
            @PathParam("id") Long id
    ) {
        return anamneseService.findById(id)
                .onItem().transform(RestResponse::ok);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Deleta anamnese", description = "Deleta uma anamnese pelo id")
    public Uni<RestResponse<Void>> deletaPorId(
            @PathParam("id") Long id
    ) {
        return anamneseService.deleteById(id)
                .onItem().transform(deleted -> RestResponse.noContent());
    }

}
