package br.ce.clinica.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    BAD_REQUEST(400,"Bad Request"),
    VALIDATION_ERROR(400,"Validation Error"),
    NOT_FOUND(404, "Not Found"),
    CONFLICT(409, "Conflict"),
    UNAUTHORIZED(401,"Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    INTERNAL_ERROR(500,"Internal Server Error"),
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity");

    private final int status;
    private final String title;

    public String getType() {
        return "urn:clinica:error:" + name().toLowerCase();
    }
}
