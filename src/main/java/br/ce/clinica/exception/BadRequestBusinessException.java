package br.ce.clinica.exception;

import br.ce.clinica.enums.ErrorCode;

import java.util.List;

public class BadRequestBusinessException extends BusinessException {
    public BadRequestBusinessException(String message) {
        super(
                message,
                400,
                "Bad Request",
                ErrorCode.BAD_REQUEST,
                List.of()
        );
    }
}
