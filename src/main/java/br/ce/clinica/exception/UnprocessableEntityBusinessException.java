package br.ce.clinica.exception;

import br.ce.clinica.enums.ErrorCode;

import java.util.List;

public class UnprocessableEntityBusinessException extends BusinessException{
    public UnprocessableEntityBusinessException(String message) {
        super(
                message,
                422,
                "Unprocessable Entity",
                ErrorCode.UNPROCESSABLE_ENTITY,
                List.of()
        );
    }
}
