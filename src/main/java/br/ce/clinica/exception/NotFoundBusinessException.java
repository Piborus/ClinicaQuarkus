package br.ce.clinica.exception;

import br.ce.clinica.enums.ErrorCode;

import java.util.List;

public class NotFoundBusinessException extends BusinessException {
    public NotFoundBusinessException(String message) {
        super(
                message,
                404,
                "Not Found",
                ErrorCode.NOT_FOUND,
                List.of()
        );
    }
}
