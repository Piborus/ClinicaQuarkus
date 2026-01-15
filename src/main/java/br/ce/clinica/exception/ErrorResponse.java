package br.ce.clinica.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {

    private String mensagem;
    private Integer codigo;
    private String caminho;
    private LocalDateTime dataHora;
}
