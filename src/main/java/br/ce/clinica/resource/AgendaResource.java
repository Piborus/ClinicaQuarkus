package br.ce.clinica.resource;

import br.ce.clinica.dto.request.AgendaRequest;
import br.ce.clinica.dto.response.ApiResponse;
import br.ce.clinica.dto.response.ConsultaResponse;
import br.ce.clinica.openapi.ApiDocumentation;
import br.ce.clinica.service.AgendaService;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Path("/consulta")
@Consumes("application/json")
@Produces("application/json")
@Tag(name = "Consulta", description = "Endpoints relacionados a consultas")
@ApplicationScoped
@ApiDocumentation
@WithSession
public class AgendaResource {

    @Inject
    AgendaService agendaService;

    @POST
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Operation(summary = "Cadastra consulta", description = "Cadastra uma nova consulta no sistema")
    public Uni<RestResponse<ApiResponse>> cadastrar(
            @Valid AgendaRequest agendaRequest
    ){
        return agendaService.scheduleConsultation(agendaRequest)
                .onItem()
                .transform(consulta ->
                        RestResponse.status(RestResponse.Status.CREATED,
                                ApiResponse
                                        .builder()
                                        .message("Consulta Registrada com Sucesso")
                                        .build())
                        );
    }

    @PATCH
    @Path("/cancelar/{id}")
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Operation(summary = "Cancela consulta", description = "Cancela uma consulta no sistema")
    public Uni<RestResponse<ApiResponse>> cancelar(
            @PathParam("id") Long id
//            @Valid AgendaCancelamentoRequest agendaCancelamentoRequest
    ) {
        return agendaService.cancelConsultation(id)
                .onItem().transform(consulta -> RestResponse.ok(
                ApiResponse.builder()
                        .message("Consulta cancelado com sucesso")
                        .build()
                        ));
    }

    @GET
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Path("/usuarios/{usuarioId}/horarios-disponiveis")
    public Uni<RestResponse<List<LocalTime>>> horariosDisponiveis(
            @PathParam("usuarioId") Long id,
            @QueryParam("data") String dataStr
    ) {
        LocalDate data = LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return agendaService.findAvailableTimes(id, data)
                .onItem().transform(RestResponse::ok);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Operation(summary = "Busca consulta por id", description = "Busca uma consulta pelo id no sistema")
    public Uni<RestResponse<ConsultaResponse>> buscarPorId(
            @PathParam("id") Long id
    ) {
        return agendaService.findById(id)
                .onItem().transform(RestResponse::ok);
    }
}
