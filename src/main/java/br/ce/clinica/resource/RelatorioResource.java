package br.ce.clinica.resource;

import br.ce.clinica.dto.request.RelatorioRequest;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.dto.response.RelatorioDetalhadoResponse;
import br.ce.clinica.dto.response.RelatorioResponse;
import br.ce.clinica.service.RelatorioService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
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
public class RelatorioResource {

    @Inject
    RelatorioService relatorioService;

    @POST
    @Operation(summary = "Cria um relatório do paciente",
            description = "Cria um novo relatório do paciente no sistema")
    public Uni<RestResponse<RelatorioResponse>> salvar(
            @RequestBody RelatorioRequest relatorioRequest
    ) {
        return relatorioService.save(relatorioRequest)
                .onItem()
                .transform(RestResponse::ok)
                .onFailure().recoverWithItem(RestResponse.serverError());
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca o relatorio por id",
            description = "Busca um relatorio do paciente pelo id no sistema")
    public Uni<RestResponse<RelatorioResponse>> buscarPorId(
            @PathParam("id") Long id
    ) {
        return relatorioService.findById(id)
                .onItem()
                .transform(RestResponse::ok)
                .onFailure().recoverWithItem(RestResponse.notFound());
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
    public Uni<RestResponse<RelatorioResponse>> atualizar(
            @PathParam("id") Long id,
            @RequestBody RelatorioRequest relatorioRequest
    ) {
        return relatorioService.update(id, relatorioRequest)
                .onItem().transform(relatorio -> RestResponse.ok(relatorio));
    }

    @GET
    @Path("/{id}/paciente")
    @Operation()
    public Uni<RestResponse<RelatorioDetalhadoResponse>> findByIdWithPaciente(
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

        return relatorioService.findPaginated(panachePage, sort, filterFields, filterValues)
                .onItem().transform(RestResponse::ok);
    }
}
