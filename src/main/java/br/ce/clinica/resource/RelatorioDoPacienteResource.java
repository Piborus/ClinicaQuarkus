package br.ce.clinica.resource;

import br.ce.clinica.dto.request.RelatorioDoPacienteRequest;
import br.ce.clinica.dto.response.RelatorioDoPacienteResponse;
import br.ce.clinica.service.RelatorioDoPacienteService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;

@Consumes("application/json")
@Produces("application/json")
@Path("/relatorio-do-paciente")
@WithSession
@ApplicationScoped
@Tag(name = "RelatorioDoPaciente",
        description = "Controlador para gerenciar RelatorioDoPaciente no sistema")
public class RelatorioDoPacienteResource {

    @Inject
    RelatorioDoPacienteService relatorioDoPacienteService;

    @POST
    @Operation(summary = "Cria um relatório do paciente",
            description = "Cria um novo relatório do paciente no sistema")
    public Uni<RestResponse<RelatorioDoPacienteResponse>> salvar(
            @RequestBody RelatorioDoPacienteRequest relatorioDoPacienteRequest
    ) {
        return relatorioDoPacienteService.save(relatorioDoPacienteRequest)
                .onItem()
                .transform(RestResponse::ok)
                .onFailure().recoverWithItem(RestResponse.serverError());
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca o relatorio por id",
            description = "Busca um relatorio do paciente pelo id no sistema")
    public Uni<RestResponse<RelatorioDoPacienteResponse>> buscarPorId(
            @PathParam("id") Long id
    ) {
        return relatorioDoPacienteService.findById(id)
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
        return relatorioDoPacienteService.deleteById(id)
                .onItem().transform(relatorio -> RestResponse.noContent());
    }
}
