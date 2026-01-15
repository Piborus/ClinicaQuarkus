package br.ce.clinica.exception.mapper;

import br.ce.clinica.exception.BusinessException;
import br.ce.clinica.exception.ErrorResponse;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Provider
public class BusinessExceptionMapper implements ExceptionMapper<Throwable> {

    @Inject
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {

        Throwable root = unwrap(exception);

        if (root instanceof BusinessException be) {
            return buildResponse(be.getMessage(), Response.Status.BAD_REQUEST);
        }

        if (root instanceof ConstraintViolationException e) {
            String message = e.getConstraintViolations()
                    .stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            return buildResponse(message, Response.Status.BAD_REQUEST);
        }

        if (root instanceof BadRequestException
                || root instanceof IllegalArgumentException) {
            return buildResponse(root.getMessage(), Response.Status.BAD_REQUEST);
        }

        if (root instanceof NotFoundException
                || root instanceof NoResultException) {
            return buildResponse(root.getMessage(), Response.Status.NOT_FOUND);
        }

        return buildResponse(
                "Erro interno inesperado",
                Response.Status.INTERNAL_SERVER_ERROR
        );
    }

    private Response buildResponse(String message, Response.Status status) {
        ErrorResponse error = new ErrorResponse(
                message,
                status.getStatusCode(),
                uriInfo.getPath(),
                LocalDateTime.now()
        );

        return Response.status(status)
                .entity(error)
                .build();
    }

    private Throwable unwrap(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null
                && cause != cause.getCause()) {
            cause = cause.getCause();
        }
        return cause;
    }

}
