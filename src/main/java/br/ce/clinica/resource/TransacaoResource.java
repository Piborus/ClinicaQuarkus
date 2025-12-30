package br.ce.clinica.resource;

import br.ce.clinica.dto.response.TransacaoResponse;
import br.ce.clinica.service.TransacaoService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/transacao")
@Consumes("application/json")
@Produces("application/json")
@ApplicationScoped
@WithSession
@Tag(name = "Transaçoes"
        , description = "Controlador para gerenciar " +
        "as transaçoes dos paciente no sistema")
public class TransacaoResource {

    @Inject
    TransacaoService transacaoService;

    @POST
    @Operation(summary = "Cria uma Transação", description = "Cria uma transação para um paciente no sistema")
    public Uni<RestResponse<TransacaoResponse>> salvar (){
        return null;
    }
}
