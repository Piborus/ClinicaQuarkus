package br.ce.clinica.resource;

import br.ce.clinica.dto.request.RelatorioRequest;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.dto.response.RelatorioResponse;
import br.ce.clinica.dto.response.RelatorioResumeResponse;
import br.ce.clinica.openapi.ApiDocumentation;
import br.ce.clinica.service.RelatorioService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
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
@Path("/relatorio-do-paciente")
@WithSession
@ApplicationScoped
@Tag(name = "RelatorioDoPaciente",
        description = "Controlador para gerenciar RelatorioDoPaciente no sistema")
@ApiDocumentation
public class RelatorioResource {

    @Inject
    RelatorioService relatorioService;

    @POST
    @Operation(summary = "Cria um relatório do paciente",
            description = "Cria um novo relatório do paciente no sistema")
    public Uni<RestResponse<RelatorioResponse>> salvar(
            @Valid RelatorioRequest relatorioRequest
    ) {
        return relatorioService.save(relatorioRequest)
                .onItem()
                .transform(relatorioResponse -> RestResponse
                        .ResponseBuilder.create(RestResponse.Status.CREATED, relatorioResponse).build());
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca o relatorio por id",
            description = "Busca um relatorio do paciente pelo id no sistema")
    public Uni<RestResponse<RelatorioResumeResponse>> buscarPorId(
            @PathParam("id") Long id
    ) {
        return relatorioService.findById(id)
                .onItem()
                .transform(RestResponse::ok);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Deleta um relatorio do paciente pelo id",
            description = "Deleta um relatorio do paciente pelo id no sistema")
    public Uni<RestResponse<Boolean>> deletarPorId(
            @PathParam("id") Long id
    ) {
        return relatorioService.deleteById(id)
                .onItem().transform(relatorio -> RestResponse.noContent());
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualiza um relatorio do paciente pelo id",
            description = "Atualiza um relatorio do paciente pelo id no sistema")
    public Uni<RestResponse<RelatorioResumeResponse>> atualizar(
            @PathParam("id") Long id,
            @Valid RelatorioRequest relatorioRequest
    ) {
        return relatorioService.update(id, relatorioRequest)
                .onItem().transform(relatorio -> RestResponse.ok(relatorio));
    }

    @GET
    @Path("/{id}/paciente")
    @Operation(summary = "Buscar o relatorio com o paciente", description = "Busca um relatorio com o paciente pelo id")
    public Uni<RestResponse<RelatorioResponse>> findByIdWithPaciente(
            @PathParam("id") Long id
    ) {
        return relatorioService.findByIdWithPaciente(id)
                .onItem().transform(RestResponse::ok);

    }

    @GET
    @Operation(summary = "Busca relatórios do paciente paginados",
            description = "Busca relatórios do paciente com paginação, ordenação e filtros")
    public Uni<RestResponse<PanachePage<RelatorioResponse>>> listarRelatorios(
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("20") Integer size,
            @QueryParam("sort") String sort,
            @QueryParam("filterFields") List<String> filterFields,
            @QueryParam("filterValues") List<String> filterValues
    ) {
        Page panachePage  = Page.of(page - 1, size);
        return relatorioService.findPaginated(
                panachePage,
                sort,
                filterFields,
                filterValues
        ).onItem().transform(RestResponse::ok);
    }
}
