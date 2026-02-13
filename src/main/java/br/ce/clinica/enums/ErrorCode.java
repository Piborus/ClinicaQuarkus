package br.ce.clinica.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    BAD_REQUEST("CLN-400"),
    VALIDATION_ERROR("CLN-400-VAL"),
    NOT_FOUND("CLN-404"),
    CONFLICT("CLN-409"),
    UNAUTHORIZED("CLN-401"),
    FORBIDDEN("CLN-403"),
    INTERNAL_ERROR("CLN-500"),
    UNPROCESSABLE_ENTITY("CLN-422");

    private final String code;
}
