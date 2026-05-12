package br.ce.clinica.openapi;

import br.ce.clinica.dto.response.ErrorResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@APIResponses({
        @APIResponse(
                responseCode = "200",
                description = "Requisição bem sucedida"
        ),
        @APIResponse(
                responseCode = "201",
                description = "Recurso criado com sucesso"
        ),
        @APIResponse(
                responseCode = "204",
                description = "Recurso excluído com sucesso"
        ),
        @APIResponse(
                responseCode = "400",
                description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
                responseCode = "401",
                description = "Não autorizado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
                responseCode = "404",
                description = "Recurso não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
                responseCode = "409",
                description = "Conflito de dados",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
                responseCode = "422",
                description = "Entidade não processável",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
                responseCode = "500",
                description = "Erro interno do servidor",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
})
public @interface ApiDocumentation {
}
