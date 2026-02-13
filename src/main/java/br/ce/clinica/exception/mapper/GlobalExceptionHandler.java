package br.ce.clinica.exception.mapper;

import br.ce.clinica.enums.ErrorCode;
import br.ce.clinica.exception.BusinessException;
import br.ce.clinica.dto.response.ErrorObject;
import br.ce.clinica.dto.response.ErrorResponse;
import br.ce.clinica.exception.UnprocessableEntityBusinessException;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import jakarta.validation.ConstraintViolationException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.OffsetDateTime;
import java.util.List;

@Provider
public class GlobalExceptionHandler {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);

    @Inject
    UriInfo uriInfo;

    @ServerExceptionMapper(BusinessException.class)
    public Response handleBusinessException(BusinessException e) {
        LOG.warnf("API Error [%s] at %s: %s",
                e.getErrorCode().getCode(),
                uriInfo.getPath(),
                e.getMessage());

        List<ErrorObject> messages = e.getMessages() == null ? List.of() : e.getMessages();
        ErrorResponse error = ErrorResponse.builder()
                .status(e.getStatus())
                .timestamp(OffsetDateTime.now())
                .title(e.getTitle())
                .detail(e.getMessage())
                .errorCode(e.getErrorCode().getCode())
                .path(uriInfo.getPath())
                .messages(messages)
                .build();

        return Response.status(e.getStatus())
                .entity(error)
                .build();
    }

    @ServerExceptionMapper(ResteasyReactiveViolationException.class)
    public Response handleValidation(ResteasyReactiveViolationException e) {
        List<ErrorObject> messages = e.getConstraintViolations().stream()
                .map(v -> new ErrorObject(v.getPropertyPath().toString(), v.getMessage()))
                .toList();

        ErrorResponse error = ErrorResponse.builder()
                .status(400)
                .timestamp(OffsetDateTime.now())
                .title("Bad Request")
                .detail("Erro de validação")
                .errorCode("VALIDATION_ERROR")
                .path(uriInfo.getPath())
                .messages(messages)
                .build();

        return Response.status(400)
                .entity(error)
                .build();
    }

    @ServerExceptionMapper(ConstraintViolationException.class)
    public Response handleConstraintViolation(ConstraintViolationException e){
        List<ErrorObject> messages = e.getConstraintViolations().stream()
                .map(v -> new ErrorObject(v.getPropertyPath().toString(), v.getMessage()))
                .toList();

        ErrorResponse error = ErrorResponse.builder()
                .status(400)
                .timestamp(OffsetDateTime.now())
                .title("Bad Request")
                .detail("Erro de validação")
                .errorCode("VALIDATION_ERROR")
                .path(uriInfo.getPath())
                .messages(messages)
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
                .errorCode(ErrorCode.INTERNAL_ERROR.getCode())
                .path(uriInfo.getPath())
                .messages(List.of())
                .build();

        return Response.status(500)
                .entity(error)
                .build();
    }

    @ServerExceptionMapper(AuthenticationFailedException.class)
    public Response handleAuthFailed(AuthenticationFailedException e) {
        throw new UnauthorizedException("Token inválido ou expirado");
    }

    @ServerExceptionMapper(ForbiddenException.class)
    public Response handleForbidden(ForbiddenException e) {
        throw new ForbiddenException("Acesso negado");
    }

    @ServerExceptionMapper(UnauthorizedException.class)
    public Response handleUnauthorized(UnauthorizedException e) {
        throw new UnauthorizedException("Acesso não autorizado");
    }

    @ServerExceptionMapper(UnprocessableEntityBusinessException.class)
    public Response handleUnprocessableEntity(UnprocessableEntityBusinessException e) {
        LOG.warnf("API Error [%s] at %s: %s",
                e.getErrorCode().getCode(),
                uriInfo.getPath(),
                e.getMessage());

        List<ErrorObject> messages = e.getMessages() == null ? List.of() : e.getMessages();
        ErrorResponse error = ErrorResponse.builder()
                .status(e.getStatus())
                .timestamp(OffsetDateTime.now())
                .title(e.getTitle())
                .detail(e.getMessage())
                .errorCode(e.getErrorCode().getCode())
                .path(uriInfo.getPath())
                .messages(messages)
                .build();

        return Response.status(e.getStatus())
                .entity(error)
                .build();
    }



}
