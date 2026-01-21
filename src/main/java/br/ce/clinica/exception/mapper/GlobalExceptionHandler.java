package br.ce.clinica.exception.mapper;

import br.ce.clinica.exception.BusinessException;
import br.ce.clinica.dto.response.ErrorResponse;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import jakarta.validation.ConstraintViolationException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@Provider
public class GlobalExceptionHandler {

    @Inject
    UriInfo uriInfo;

    @ServerExceptionMapper(BusinessException.class)
    public Response handleBusinessException(BusinessException e) {
        return Response.status(e.getStatus())
                .entity(ErrorResponse.from(e))
                .build();
    }

    @ServerExceptionMapper(ResteasyReactiveViolationException.class)
    public Response handleValidation(ResteasyReactiveViolationException e) {
        ErrorResponse error = ErrorResponse.fromValidation(e);
        return Response
                 .status(error.getStatus())
                 .entity(error)
                 .build();
    }

    @ServerExceptionMapper(ConstraintViolationException.class)
    public Response handleConstraintViolation(ConstraintViolationException e) {
        ErrorResponse errorResponse = ErrorResponse.fromConstraint(e);
        return Response
                .status(errorResponse.getStatus())
                .entity(errorResponse)
                .build();
    }

    @ServerExceptionMapper(Throwable.class)
    public Response handleGeneric(Throwable e) {
        ErrorResponse error = ErrorResponse.internalError(e);
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .entity(ErrorResponse.internalError(e))
                .build();
    }
}
