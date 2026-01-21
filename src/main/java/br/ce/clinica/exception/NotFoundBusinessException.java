package br.ce.clinica.exception;

public class NotFoundBusinessException extends BusinessException {
    public NotFoundBusinessException(String message) {
        super(message, 404);
    }
}
