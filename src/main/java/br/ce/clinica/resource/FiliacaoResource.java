package br.ce.clinica.resource;


import br.ce.clinica.dto.request.FiliacaoRequest;
import br.ce.clinica.dto.response.FiliacaoResponse;
import br.ce.clinica.openapi.ApiDocumentation;
import br.ce.clinica.service.FiliacaoService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
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
@Tag(name = "Filiacoes", description = "Controlador para gerenciar Filiações no sistema")
@ApplicationScoped
@ApiDocumentation
@WithSession
@Path("/filiacoes")
public class FiliacaoResource {

    @Inject
    FiliacaoService filiacaoService;

    @GET
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Path("paciente/{id}")
    @Operation(summary = "Filiacões por paciente id"
            , description = "Retorna as filiações pelo id do paciente")
    public Uni<RestResponse<List<FiliacaoResponse>>> buscarFiliacoesPorPacienteId(
            @PathParam("id") Long id
    ) {
        return filiacaoService.findByPacienteId(id)
                .onItem()
                .transform(RestResponse::ok);
    }

    @PUT
    @Path("/{id}")
    public Uni<RestResponse<FiliacaoResponse>> atualizar(
            @PathParam("id") Long id,
            @Valid FiliacaoRequest filiacaoRequest
            )
    {
        return filiacaoService.update(id, filiacaoRequest)
                .onItem().transform(RestResponse::ok);
    }

    @DELETE
    @Path("/{id}")
    public Uni<RestResponse<Void>> deletaPorId(
            @PathParam("id") Long id
    ) {
        return filiacaoService.deleteById(id)
                .onItem().transform(filiacao -> RestResponse.noContent());
    }

}
