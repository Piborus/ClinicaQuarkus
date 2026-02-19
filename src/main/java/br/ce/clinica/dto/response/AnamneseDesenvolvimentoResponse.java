package br.ce.clinica.dto.response;

import br.ce.clinica.entity.AnamneseDesenvolvimento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnamneseDesenvolvimentoResponse {

    private Long id;

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

    public static AnamneseDesenvolvimentoResponse toResponse(AnamneseDesenvolvimento anamneseDesenvolvimento) {
        return AnamneseDesenvolvimentoResponse.builder()
                .id(anamneseDesenvolvimento.getId())
                .gravidezParto(anamneseDesenvolvimento.getGravidezParto())
                .memoriasInfancia(anamneseDesenvolvimento.getMemoriasInfancia())
                .memoriasAdolescencia(anamneseDesenvolvimento.getMemoriasAdolescencia())
                .faseAdulta(anamneseDesenvolvimento.getFaseAdulta())
                .faseAtual(anamneseDesenvolvimento.getFaseAtual())
                .moraComQuem(anamneseDesenvolvimento.getMoraComQuem())
                .numeroFilhos(anamneseDesenvolvimento.getNumeroFilhos())
                .numeroIrmaos(anamneseDesenvolvimento.getNumeroIrmaos())
                .ordemNascimento(anamneseDesenvolvimento.getOrdemNascimento())
                .fumante(anamneseDesenvolvimento.getFumante())
                .etilista(anamneseDesenvolvimento.getEtilista())
                .usoMedicamento(anamneseDesenvolvimento.getUsoMedicamento())
                .descricaoMedicamentos(anamneseDesenvolvimento.getDescricaoMedicamentos())
                .rotina(anamneseDesenvolvimento.getRotina())
                .build();
    }
}
