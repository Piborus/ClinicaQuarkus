package br.ce.clinica.exception.mapper;

import br.ce.clinica.dto.response.ErrorObject;
import br.ce.clinica.dto.response.ErrorResponse;
import br.ce.clinica.enums.ErrorCode;
import br.ce.clinica.exception.BusinessException;
import br.ce.clinica.exception.UnprocessableEntityBusinessException;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Provider
public class GlobalExceptionHandler {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);

    @Inject
    UriInfo uriInfo;

    @ServerExceptionMapper(BusinessException.class)
    public Response handleBusinessException(BusinessException e) {
        if (e instanceof UnprocessableEntityBusinessException) {
            LOG.warnf("API Error [%s] at %s: %s",
                    e.getErrorCode().name(),
                    uriInfo.getPath(),
                    e.getMessage());
        }

        ErrorResponse error = ErrorResponse.from(e, uriInfo.getPath());

        return Response.status(e.getStatus())
                .type("application/problem+json")
                .entity(error)
                .build();
    }

    @ServerExceptionMapper(ResteasyReactiveViolationException.class)
    public Response handleValidation(ResteasyReactiveViolationException e) {
        return buildValidationResponse(e.getConstraintViolations());
    }

    @ServerExceptionMapper(ConstraintViolationException.class)
    public Response handleConstraintViolation(ConstraintViolationException e) {
        return buildValidationResponse(e.getConstraintViolations());
    }

    private Response buildValidationResponse(Set<? extends ConstraintViolation<?>> violations) {
        List<ErrorObject> errors = violations.stream()
                .map(v -> new ErrorObject(v.getPropertyPath().toString(), v.getMessage()))
                .toList();

        ErrorResponse error = ErrorResponse.builder()
                .status(400)
                .timestamp(OffsetDateTime.now())
                .title("Bad Request")
                .detail("Erro de validação")
                .errorCode("VALIDATION_ERROR")
                .instance(uriInfo.getPath())
                .errors(errors)
                .build();

        return Response.status(400)
                .entity(error)
                .build();
    }

    @ServerExceptionMapper(Throwable.class)
    public Response handleGeneric(Throwable e) {
        LOG.error("Unexpected error", e);

        ErrorResponse error = ErrorResponse.builder()
                .status(500)
                .timestamp(OffsetDateTime.now())
                .title("Internal Server Error")
                .detail("Erro interno inesperado")
                .errorCode(ErrorCode.INTERNAL_ERROR.name())
                .instance(uriInfo.getPath())
                .errors(List.of())
                .build();

        return Response.status(500)
                .entity(error)
                .build();
    }

    @ServerExceptionMapper(AuthenticationFailedException.class)
    public Response handleAuthFailed(AuthenticationFailedException e) {

        ErrorResponse error = ErrorResponse.builder()
                .status(401)
                .timestamp(OffsetDateTime.now())
                .title("Unauthorized")
                .detail("Token inválido ou expirado")
                .errorCode("AUTHENTICATION_FAILED")
                .instance(uriInfo.getPath())
                .errors(List.of())
                .build();

        return Response.status(401)
                .entity(error)
                .build();
    }

    @ServerExceptionMapper(ForbiddenException.class)
    public Response handleForbidden(ForbiddenException e) {
        throw new ForbiddenException("Acesso negado");
    }

    @ServerExceptionMapper(UnauthorizedException.class)
    public Response handleUnauthorized(UnauthorizedException e) {
        throw new UnauthorizedException("Acesso não autorizado");
    }

}
