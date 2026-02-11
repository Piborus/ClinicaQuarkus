package br.ce.clinica.exception;

import br.ce.clinica.enums.ErrorCode;

import java.util.List;

public class ConflictBusinessException extends BusinessException {
    public ConflictBusinessException(String message) {
        super(
                message,
                409,
                "Conflict",
                ErrorCode.CONFLICT,
                List.of()
        );
    }
}
