package br.ce.clinica.exception;

import br.ce.clinica.enums.ErrorCode;

import java.util.List;

public class ForbiddenBusinessException extends BusinessException {
    public ForbiddenBusinessException(String message) {
        super(
                message,
                403,
                "Forbidden",
                ErrorCode.FORBIDDEN,
                List.of()
        );
    }
}
