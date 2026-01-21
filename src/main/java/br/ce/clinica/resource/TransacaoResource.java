package br.ce.clinica.resource;

import br.ce.clinica.dto.request.TransacaoRequest;
import br.ce.clinica.dto.response.TransacaoResponse;
import br.ce.clinica.service.TransacaoService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
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
    public Uni<RestResponse<TransacaoResponse>> salvar (
            @RequestBody TransacaoRequest transacaoRequest
    ){
        return transacaoService.save(transacaoRequest)
                .onItem()
                .transform(transacao-> RestResponse.ok(transacao))
                .onFailure().recoverWithItem(RestResponse.serverError());
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca Transação por id",
            description = "Busca uma transação pelo id no sistema")
    public Uni<RestResponse<TransacaoResponse>> buscarPorId(
            @PathParam("id") Long id
    ) {
        return transacaoService.findById(id)
                .onItem().transform(RestResponse::ok);

    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Deleta uma transação pelo id",
            description = "Deleta uma transação pelo id no sistema")
    public Uni<RestResponse<Boolean>> deletarPorId(
            @PathParam("id") Long id
    ){
        return transacaoService.deleteById(id)
                .onItem()
                .transform(RestResponse::ok)
                .onFailure().recoverWithItem(RestResponse.notFound());
    }

    @PUT
    @Path("/{id}")
    public Uni<RestResponse<TransacaoResponse>> atualizar(
            @PathParam("id") Long id,
            @RequestBody TransacaoRequest transacaoRequest
    ) {
        return transacaoService.update(id, transacaoRequest)
                .onItem()
                .transform(RestResponse::ok)
                .onFailure().recoverWithItem(RestResponse.notFound());
    }
}
