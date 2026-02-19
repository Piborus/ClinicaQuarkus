package br.ce.clinica.exception;

import br.ce.clinica.enums.ErrorCode;

import java.util.List;

public class UnauthorizedBusinessException extends BusinessException {
    public UnauthorizedBusinessException(String message) {
        super(
                message,
                401,
                "Unauthorized",
                ErrorCode.UNAUTHORIZED,
                List.of()
        );
    }
}

