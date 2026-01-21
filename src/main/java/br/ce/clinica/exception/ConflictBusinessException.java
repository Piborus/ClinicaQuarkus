package br.ce.clinica.exception;

public class ConflictBusinessException extends BusinessException {
    public ConflictBusinessException(String message) {
        super(message, 409);
    }
}
