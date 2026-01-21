package br.ce.clinica.dto.response;

import br.ce.clinica.exception.BusinessException;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;



import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {

    private Integer status;

    private OffsetDateTime timestamp;

    private String title;

    private String detail;

    public List<ErrorObject> messages;


    public static ErrorResponse from(BusinessException e) {
        return new ErrorResponse(
                e.getStatus(),
                OffsetDateTime.now(),
                "Business error",
                e.getMessage(),
                List.of()
        );
    }

    public static ErrorResponse fromValidation(ResteasyReactiveViolationException e) {
        List<ErrorObject> messages = e.getConstraintViolations().stream()
                .map(v -> new ErrorObject(
                        v.getPropertyPath().toString(),
                        v.getMessage()
                ))
                .toList();

        return new ErrorResponse(
                400,
                OffsetDateTime.now(),
                "Invalid data",
                "Dados inválidos",
                messages
        );
    }


    public static ErrorResponse fromConstraint(ConstraintViolationException e) {
        List<ErrorObject> messages = e.getConstraintViolations()
                .stream()
                .map(v -> new ErrorObject(
                        v.getPropertyPath().toString(),
                        v.getMessage()
                ))
                .toList();

        return new ErrorResponse(
                400,
                OffsetDateTime.now(),
                "Invalid data",
                "Dados inválidos",
                messages
        );
    }


    public static ErrorResponse internalError(Throwable e) {
        return new ErrorResponse(
                500,
                OffsetDateTime.now(),
                "Internal Server Error",
                "Erro interno inesperado",
                List.of()
        );
    }
}


