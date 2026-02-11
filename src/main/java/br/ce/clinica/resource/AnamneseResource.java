package br.ce.clinica.resource;

import br.ce.clinica.dto.request.AnamneseRequest;
import br.ce.clinica.dto.response.AnamneseResponse;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.openapi.ApiDocumentation;
import br.ce.clinica.service.AnamneseService;
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
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
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
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Path("/{id}")
    @Operation(summary = "Atualiza anamnese", description = "Atualiza uma anamnese no sistema")
    public Uni<RestResponse<AnamneseResponse>> atualizar(
            @PathParam("id") Long id,
            @Valid AnamneseRequest anamneseRequest
    ){
        return anamneseService.updade(id, anamneseRequest)
                .onItem()
                .transform(RestResponse::ok);
    }

    @GET
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Path("/{id}")
    @Operation(summary = "Busca anamnese por id", description = "Busca uma anamnese pelo id no sistema")
    public Uni<RestResponse<AnamneseResponse>> buscarPorId(
            @PathParam("id") Long id
    ) {
        return anamneseService.findById(id)
                .onItem().transform(RestResponse::ok);
    }

    @DELETE
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Path("/{id}")
    @Operation(summary = "Deleta anamnese", description = "Deleta uma anamnese pelo id")
    public Uni<RestResponse<Void>> deletaPorId(
            @PathParam("id") Long id
    ) {
        return anamneseService.deleteById(id)
                .onItem().transform(deleted -> RestResponse.noContent());
    }

    @GET
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Path("/paciente/{pacienteId}")
    @Operation(summary = "Busca anamnese por id do paciente", description = "Busca uma anamnese pelo id do paciente no sistema")
    public Uni<RestResponse<AnamneseResponse>> buscarPorPacienteId(
            @PathParam("pacienteId") Long pacienteId
    ) {
        return anamneseService.findByPacienteId(pacienteId)
                .onItem().transform(RestResponse::ok);
    }

    @GET
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Operation(summary = "Busca anamnese paginada",
            description = "Busca uma lista paginada de anamnese no sistema")
    public Uni<RestResponse<PanachePage<AnamneseResponse>>> buscarPaginado(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("sort") String sort,
            @QueryParam("filterFields") List<String> filterFields,
            @QueryParam("filterValues") List<String> filterValues
    ){
        Page panachePage = Page.of(page - 1,size);

        return anamneseService.findPaginated(
                panachePage,
                sort,
                filterFields,
                filterValues
        ).onItem().transform(RestResponse::ok);
    }

}
