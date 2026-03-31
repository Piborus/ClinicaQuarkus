package br.ce.clinica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

    private String type;
    private String title;
    private Integer status;
    private String detail;
    private String instance;
    private OffsetDateTime timestamp;

    // campos extras permitidos pelo RFC
    private String errorCode;
    private List<ErrorObject> errors;

    public static ErrorResponse from(br.ce.clinica.exception.BusinessException e, String path) {
        return ErrorResponse.builder()
                .type(e.getErrorCode().getType())
                .title(e.getTitle())
                .status(e.getStatus())
                .detail(e.getMessage())
                .instance(path)
                .timestamp(OffsetDateTime.now())
                .errorCode(e.getErrorCode().name())
                .errors(e.getDetails())
                .build();
    }

}
