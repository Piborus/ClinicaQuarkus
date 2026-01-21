package br.ce.clinica.exception;

public class ForbiddenBusinessException extends BusinessException {
    public ForbiddenBusinessException(String message) {
        super(message, 403);
    }
}
