package br.ce.clinica.resource;

import br.ce.clinica.dto.request.ProntuarioRequest;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.dto.response.ProntuarioResponse;
import br.ce.clinica.dto.response.ProntuarioResumeResponse;
import br.ce.clinica.openapi.ApiDocumentation;
import br.ce.clinica.service.ProntuarioService;
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

@Consumes("application/json")
@Produces("application/json")
@Path("/prontuario-do-paciente")
@WithSession
@ApplicationScoped
@Tag(name = "ProntuarioDoPaciente",
        description = "Controlador para gerenciar prontuario do paciente no sistema")
@ApiDocumentation
public class ProntuarioResource {

    @Inject
    ProntuarioService prontuarioService;

    @POST
    @Operation(summary = "Cria um prontuario do paciente",
            description = "Cria um novo prontuario do paciente no sistema")
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    public Uni<RestResponse<ProntuarioResponse>> salvar(
            @Valid ProntuarioRequest prontuarioRequest
    ) {
        return prontuarioService.save(prontuarioRequest)
                .onItem()
                .transform(prontuarioResponse -> RestResponse
                        .ResponseBuilder.create(RestResponse.Status.CREATED, prontuarioResponse).build());
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca o prontuario por id",
            description = "Busca um prontuario do paciente pelo id no sistema")
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    public Uni<RestResponse<ProntuarioResumeResponse>> buscarPorId(
            @PathParam("id") Long id
    ) {
        return prontuarioService.findById(id)
                .onItem()
                .transform(RestResponse::ok);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Deleta um prontuario do paciente pelo id",
            description = "Deleta um prontuario do paciente pelo id no sistema")
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    public Uni<RestResponse<Boolean>> deletarPorId(
            @PathParam("id") Long id
    ) {
        return prontuarioService.deleteById(id)
                .onItem().transform(prontuario -> RestResponse.noContent());
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualiza um prontuario do paciente pelo id",
            description = "Atualiza um prontuario do paciente pelo id no sistema")
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    public Uni<RestResponse<ProntuarioResumeResponse>> atualizar(
            @PathParam("id") Long id,
            @Valid ProntuarioRequest prontuarioRequest
    ) {
        return prontuarioService.update(id, prontuarioRequest)
                .onItem().transform(prontuario -> RestResponse.ok(prontuario));
    }

    @GET
    @Path("/{id}/paciente")
    @Operation(summary = "Buscar o prontuario com o paciente",
            description = "Busca um prontuario com o paciente pelo id")
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    public Uni<RestResponse<ProntuarioResponse>> findByIdWithPaciente(
            @PathParam("id") Long id
    ) {
        return prontuarioService.findByIdWithPaciente(id)
                .onItem().transform(RestResponse::ok);

    }

    @GET
    @Operation(summary = "Busca prontuarios do paciente paginados",
            description = "Busca prontuarios do paciente com paginacao, ordenacao e filtros")
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    public Uni<RestResponse<PanachePage<ProntuarioResponse>>> listarProntuarios(
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("20") Integer size,
            @QueryParam("sort") String sort,
            @QueryParam("filterFields") List<String> filterFields,
            @QueryParam("filterValues") List<String> filterValues
    ) {
        Page panachePage  = Page.of(page - 1, size);
        return prontuarioService.findPaginated(
                panachePage,
                sort,
                filterFields,
                filterValues
        ).onItem().transform(RestResponse::ok);
    }
}
