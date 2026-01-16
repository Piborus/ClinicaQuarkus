package br.ce.clinica.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ErrorReponse {

    private Integer status;

    private OffsetDateTime timestamp;

    private String title;

    private String detail;

    public List<ErrorObject> messages;

    public ErrorReponse(BusinessException e){
        this.status = 422;
        this.timestamp = OffsetDateTime.now();
        this.title = "Business";
        this.detail = e.getLocalizedMessage();
    }

    public ErrorReponse(ConstraintViolationException e){
        this.status = 400;
        this.timestamp = OffsetDateTime.now();
        this.title = "Invalid data";
        this.detail  = "Dados inválidos";
        this.messages = new ArrayList<>();
    }

    public ErrorReponse(NotFoundException e){
        this.status = 404;
        this.timestamp = OffsetDateTime.now();
        this.title = "Not Found";
        this.detail  = e.getLocalizedMessage();
        this.messages = new ArrayList<>();
    }
}
