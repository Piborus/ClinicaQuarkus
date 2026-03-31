package br.ce.clinica.exception;

import br.ce.clinica.enums.ErrorCode;

import java.util.List;

public class ConstraintViolationBusinessException extends BusinessException {
    public ConstraintViolationBusinessException(String message) {
        super(
                message,
                409,
                "Conflict",
                ErrorCode.CONFLICT,
                List.of()
        );
    }
}
