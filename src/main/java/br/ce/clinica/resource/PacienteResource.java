package br.ce.clinica.resource;

import br.ce.clinica.dto.request.PacienteRequest;
import br.ce.clinica.dto.response.PacienteResponse;
import br.ce.clinica.service.PacienteService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;



@ApplicationScoped
@Path("/pacientes")
@Consumes("application/json")
@Produces("application/json")
@WithSession
@Tag(name = "Pacientes", description = "Controlador para gerenciar Pacientes no sistema")
public class PacienteResource {

    @Inject
    PacienteService pacienteService;

    @POST
    @Operation(summary = "Cria paciente", description = "Cria um novo paciente no sistema")
    public Uni<RestResponse<PacienteResponse>> salvar(
            @RequestBody PacienteRequest pacienteRequest
    ) {
      return pacienteService.save(pacienteRequest)
              .onItem()
              .transform( pessoa -> RestResponse.ok(pessoa))
              .onFailure().invoke(t -> {
                  System.err.println("Erro ao salvar paciente: " + t.getMessage());
                  t.printStackTrace();
              })
              .onFailure().recoverWithItem( RestResponse.serverError());
    }

    @GET
    @Operation(summary = "Paciente por id", description = "Retorna um paciente pelo id")
    @Path("/{id}")
    public Uni<RestResponse<PacienteResponse>> buscarPorId(
            @PathParam("id") Long id
    ) {
        return pacienteService.findById(id)
                .onItem().transform( pessoa -> RestResponse.ok(pessoa))
                .onFailure().recoverWithItem( RestResponse.notFound());
    }

    @DELETE
    @Operation(summary = "Deleta Paciente", description = "Deleta um paciente pelo id")
    @Path("/{id}")
    public Uni<RestResponse<Boolean>> deletarPorId(
            @PathParam("id") Long id
    ) {
        return pacienteService.deleteById(id)
                .onItem().transform( pessoa -> RestResponse.noContent());
    }

    @PUT
    @Operation(summary = "Atualiza Paciente", description = "Atualiza um paciente pelo id")
    @Path("/{id}")
    public Uni<RestResponse<PacienteResponse>> atualizar(
            @PathParam("id") Long id,
            @RequestBody PacienteRequest pacienteRequest
    ){
        return pacienteService.update(id, pacienteRequest)
                .onItem().transform( pessoa -> RestResponse.ok(pessoa));
    }

}
