package br.ce.clinica.exception.mapper;

import br.ce.clinica.exception.BusinessException;
import br.ce.clinica.dto.response.ErrorReponse;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@Provider
public class GlobalExceptionHandler {

    @Inject
    UriInfo uriInfo;

    @ServerExceptionMapper(BusinessException.class)
    public Response handleBusinessException(BusinessException e) {
        return Response.status(e.getStatus())
                .entity(ErrorReponse.from(e))
                .build();
    }

    @ServerExceptionMapper(ResteasyReactiveViolationException.class)
    public Response handleValidation(ResteasyReactiveViolationException e) {
        ErrorReponse error = ErrorReponse.fromValidation(e);
        return Response
                 .status(error.getStatus())
                .entity(error)
                 .build();
    }

    @ServerExceptionMapper(Throwable.class)
    public Response handleGeneric(Throwable e) {
        ErrorReponse error = ErrorReponse.internalError(e);
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .entity(ErrorReponse.internalError(e))
                .build();
    }
}
