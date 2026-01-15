package br.ce.clinica.exception;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

//    private Response.Status status;

    public BusinessException(String message) {
        super(message);
    }
}
