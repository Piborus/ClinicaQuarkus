package br.ce.clinica.exception.mapper;

import br.ce.clinica.exception.BusinessException;
import br.ce.clinica.exception.ErrorObject;
import br.ce.clinica.exception.ErrorReponse;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.BadRequestException;
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


    @ServerExceptionMapper({BusinessException.class})
    public Response handleBusinessException(BusinessException e) {
        ErrorReponse error = new ErrorReponse(
                422,
                OffsetDateTime.now(),
                "Business error",
                "Erro de negócio",
                new ArrayList<>()
        );

        error.getMessages().add(new ErrorObject(null, e.getMessage()));

        return Response.status(422).entity(error).build();
    }


    @ServerExceptionMapper({BadRequestException.class})
    public Response handleValidationException(BadRequestException e) {
        ErrorReponse error = new ErrorReponse(
                400,
                OffsetDateTime.now(),
                "Invalid data",
                "Dados inválidos",
               null
        );

        return Response.status(400).entity(error).build();
    }

    @ServerExceptionMapper({ConstraintViolationException.class})
    public Response handleDatabaseException(ConstraintViolationException e) throws Throwable {

        Throwable cause = e.getCause();

        if (cause instanceof org.hibernate.exception.ConstraintViolationException hibernateEx
                && "23505".equals(hibernateEx.getSQLState())) {

            ErrorReponse error = new ErrorReponse(
                    409,
                    OffsetDateTime.now(),
                    "Conflict",
                    "Registro já existente",
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

            return Response.status(409).entity(error).build();
        }

        throw e;
    }

    @ServerExceptionMapper({NotFoundException.class})
    public Response handleNotFoundException(NotFoundException e) {
        ErrorReponse error = new ErrorReponse(
                404,
                OffsetDateTime.now(),
                "Not found",
                "Recurso não encontrado",
                new ArrayList<>()
        );

        error.getMessages().add(new ErrorObject(null, e.getMessage()));

        return Response.status(404).entity(error).build();
    }

    @ServerExceptionMapper({IllegalArgumentException.class})
    public Response handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorReponse error = new ErrorReponse(
                400,
                OffsetDateTime.now(),
                "Invalid argument",
                "Argumento Invalido",
                new ArrayList<>()
        );
        error.getMessages().add(new ErrorObject("argument", e.getMessage()));


        return Response.status(400).entity(error).build();
    }


    @ServerExceptionMapper({ResteasyReactiveViolationException.class})
    public Response handleResteasyReactiveViolationException(ResteasyReactiveViolationException e) {
        ErrorReponse error = new ErrorReponse(
                400,
                OffsetDateTime.now(),
                "Invalid data",
                "Dados Invalido",
                new ArrayList<>()

        );
        e.getConstraintViolations().forEach(violation -> {
            String message = violation.getPropertyPath().toString();

            if (message.contains(".")){
                message = message.substring(message.lastIndexOf('.')+1);
            }
            error.getMessages().add(new ErrorObject(message, violation.getMessage()));
        });
        return Response.status(400).entity(error).build();
    }

//    @ServerExceptionMapper
//    public Response handleGenericException(Throwable e) {
//        ErrorReponse error = new ErrorReponse(
//                500,
//                OffsetDateTime.now(),
//                "Internal Server Error",
//                "Erro interno inesperado",
//                null
//        );
//
//        return Response.status(500).entity(error).build();
//    }
}
