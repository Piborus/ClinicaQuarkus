package br.ce.clinica.dto.response;

import br.ce.clinica.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private Integer status;

    private OffsetDateTime timestamp;

    private String title;

    private String detail;

    private String errorCode;

    private String path;

    private List<ErrorObject> messages;

    public static ErrorResponse from(BusinessException e) {
        return from(e, null);
    }

    public static ErrorResponse from(BusinessException e, String path) {
        List<ErrorObject> messages = e.getMessages() == null ? List.of() : e.getMessages();
        return new ErrorResponse(
                e.getStatus(),
                OffsetDateTime.now(),
                e.getTitle(),
                e.getMessage(),
                e.getErrorCode().getCode(),
                path,
                messages
        );
    }

}
