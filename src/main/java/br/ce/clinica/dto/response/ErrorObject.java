package br.ce.clinica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorObject {
    private String name;
    private String message;
}
