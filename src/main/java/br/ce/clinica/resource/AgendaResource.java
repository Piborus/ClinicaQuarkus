package br.ce.clinica.resource;

import br.ce.clinica.openapi.ApiDocumentation;
import br.ce.clinica.service.AgendaService;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/consulta")
@Consumes("application/json")
@Produces("application/json")
@Tag(name = "Consulta", description = "Endpoints relacionados a consultas")
@ApplicationScoped
@ApiDocumentation
public class AgendaResource {

    @Inject
    AgendaService agendaService;

    @POST
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Operation(summary = "Cadastra consulta", description = "Cadastra uma nova consulta no sistema")
    public Uni<Void> cadastrar(){
        return null;
    }
}
