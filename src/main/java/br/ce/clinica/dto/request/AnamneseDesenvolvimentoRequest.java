package br.ce.clinica.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnamneseDesenvolvimentoRequest {

    private String gravidezParto;

    private String memoriasInfancia;

    private String memoriasAdolescencia;

    private String faseAdulta;

    private String faseAtual;

    private String moraComQuem;

    private Integer numeroFilhos;

    private Integer numeroIrmaos;

    private String ordemNascimento;

    private Boolean fumante;

    private Boolean etilista;

    private Boolean usoMedicamento;

    private String descricaoMedicamentos;

    private String rotina;
}
