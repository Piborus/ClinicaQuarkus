package br.ce.clinica.dto.response;

import br.ce.clinica.exception.BusinessException;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ErrorReponse {

    private Integer status;

    private OffsetDateTime timestamp;

    private String title;

    private String detail;

    public List<ErrorObject> messages;


    public static ErrorReponse from(BusinessException e) {
        return new ErrorReponse(
                e.getStatus(),
                OffsetDateTime.now(),
                "Business error",
                e.getMessage(),
                List.of()
        );
    }

    public static ErrorReponse fromValidation(ResteasyReactiveViolationException e) {
        List<ErrorObject> messages = e.getConstraintViolations().stream()
                .map(v -> new ErrorObject(
                        v.getPropertyPath().toString(),
                        v.getMessage()
                ))
                .toList();

        return new ErrorReponse(
                400,
                OffsetDateTime.now(),
                "Invalid data",
                "Dados inválidos",
                messages
        );
    }

    public static ErrorReponse internalError(Throwable e) {
        return new ErrorReponse(
                500,
                OffsetDateTime.now(),
                "Internal Server Error",
                "Erro interno inesperado",
                List.of()
        );
    }
}


