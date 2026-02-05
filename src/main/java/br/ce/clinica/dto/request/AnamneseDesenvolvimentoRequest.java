package br.ce.clinica.dto.request;

import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnamneseDesenvolvimentoRequest {

    @Schema(name = "gravidezParto", description = "Gravidez e parto do paciente", examples = {"Gravidez tranquila, parto normal"})
    private String gravidezParto;

    @Schema(name = "memoriasInfancia", description = "Memórias da infância do paciente", examples = {"Infância feliz, com muitos amigos"})
    private String memoriasInfancia;

    @Schema(name = "memoriasAdolescencia", description = "Memórias da adolescência do paciente", examples = {"Adolescência difícil, com problemas de relacionamento"})
    private String memoriasAdolescencia;

    @Schema(name = "faseAdulta", description = "Fase adulta do paciente", examples = {"Fase adulta, com muitos amigos"})
    private String faseAdulta;

    @Schema(name = "faseAtual", description = "Fase atual do paciente", examples = {"Fase atual, com muitos amigos"})
    private String faseAtual;

    @Schema(name = "moraComQuem", description = "Mora com quem?", examples = {"Mora com meu pai"})
    private String moraComQuem;

    @Schema(name = "numeroFilhos", description = "Número de filhos do paciente", examples = {"2"})
    private Integer numeroFilhos;

    @Schema(name = "numeroIrmaos", description = "Número de irmãos do paciente", examples = {"3"})
    private Integer numeroIrmaos;

    @Schema(name = "ordemNascimento", description = "Ordem de nascimento do paciente", examples = {"Primeiro a nascer"})
    private String ordemNascimento;

    @Schema(name = "fumante",
            description = "Fumante?",
            examples = {"false"},
            defaultValue = "false"
    )
    private Boolean fumante;

    @Schema(name = "etilista",
            description = "Etilista?",
            examples = {"false"},
            defaultValue = "false"
    )
    private Boolean etilista;

    @Schema(name = "usoMedicamento",
            description = "Usa Medicamento?",
            examples = {"false"},
            defaultValue = "false"
    )
    private Boolean usoMedicamento;

    @Schema(name = "descricaoMedicamentos", description = "Descrição dos medicamentos que o paciente faz uso", examples = {"Paracetamol"})
    private String descricaoMedicamentos;

    @Schema(
            name = "rotina",
            description = "Rotina do paciente (sono, alimentação, socialização, sexualidade e saúde). " +
                    "Caso não informado, será registrado automaticamente como 'Rotina não informada.'",
            examples = {"Rotina normal"}

    )
    private String rotina;
}
