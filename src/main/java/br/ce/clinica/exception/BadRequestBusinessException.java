package br.ce.clinica.exception;

public class BadRequestBusinessException extends BusinessException {
    public BadRequestBusinessException(String message) {
        super(message, 400);
    }
}
