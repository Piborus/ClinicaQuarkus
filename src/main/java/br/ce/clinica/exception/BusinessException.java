package br.ce.clinica.exception;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException{

    private final int status;

    public BusinessException(String message, int status){
        super(message);
        this.status = status;
    }

}