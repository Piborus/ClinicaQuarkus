package br.ce.clinica.exception;

import br.ce.clinica.dto.response.ErrorObject;
import br.ce.clinica.enums.ErrorCode;
import lombok.Getter;

import java.util.List;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final int status;
    private final String title;
    private final ErrorCode errorCode;
    private final List<ErrorObject> details;

    protected BusinessException(String message,
                                int status,
                                String title,
                                ErrorCode errorCode,
                                List<ErrorObject> details) {
        super(message);
        this.status = status;
        this.title = title;
        this.errorCode = errorCode;
        this.details = details;
    }

}