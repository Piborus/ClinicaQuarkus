package br.ce.clinica.exception.mapper;

import br.ce.clinica.exception.BusinessException;
import br.ce.clinica.exception.ErrorObject;
import br.ce.clinica.exception.ErrorReponse;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.OffsetDateTime;
import java.util.ArrayList;

@Provider
public class GlobalExceptionHandler {

    @Inject
    UriInfo uriInfo;

    /* ================= BUSINESS ================= */

    @ServerExceptionMapper
    public Response handleBusinessException(BusinessException e) {
        ErrorReponse error = new ErrorReponse(
                422,
                OffsetDateTime.now(),
                "Business error",
                e.getMessage(),
                null
        );

        return Response.status(422).entity(error).build();
    }

    /* ================= VALIDATION ================= */

    @ServerExceptionMapper
    public Response handleValidationException(ConstraintViolationException e) {
        ErrorReponse error = new ErrorReponse(
                400,
                OffsetDateTime.now(),
                "Invalid data",
                "Dados inválidos",
                new ArrayList<>()
        );

        for (ConstraintViolation<?> v : e.getConstraintViolations()) {
            error.getMessages().add(
                    new ErrorObject(
                            v.getPropertyPath().toString(),
                            v.getMessage()
                    )
            );
        }

        return Response.status(400).entity(error).build();
    }

    /* ================= DATABASE (23505) ================= */

    @ServerExceptionMapper
    public Response handleDatabaseException(Throwable e) throws Throwable {

        Throwable cause = e.getCause();

        if (cause instanceof org.hibernate.exception.ConstraintViolationException hibernateEx
                && "23505".equals(hibernateEx.getSQLState())) {

            ErrorReponse error = new ErrorReponse(
                    409,
                    OffsetDateTime.now(),
                    "Conflict",
                    "Registro já existente",
                    null
            );

            return Response.status(409).entity(error).build();
        }

        throw e;
    }

    /* ================= NOT FOUND ================= */

    @ServerExceptionMapper
    public Response handleNotFoundException(NotFoundException e) {
        ErrorReponse error = new ErrorReponse(
                404,
                OffsetDateTime.now(),
                "Not found",
                e.getMessage(),
                null
        );

        return Response.status(404).entity(error).build();
    }

    /* ================= Illegal Argument ================= */

    @ServerExceptionMapper
    public Response handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorReponse error = new ErrorReponse(
                400,
                OffsetDateTime.now(),
                "Invalid argument",
                e.getMessage(),
                null
        );
        return Response.status(400).entity(error).build();
    }

    /* ================= FALLBACK ================= */

    @ServerExceptionMapper
    public Response handleGenericException(Throwable e) {
        ErrorReponse error = new ErrorReponse(
                500,
                OffsetDateTime.now(),
                "Internal Server Error",
                "Erro interno inesperado",
                null
        );

        return Response.status(500).entity(error).build();
    }
}
