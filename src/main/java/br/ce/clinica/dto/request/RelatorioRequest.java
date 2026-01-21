package br.ce.clinica.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelatorioRequest {

    @Schema(name = "texto",
            description = "Relatório do paciente",
            examples = { "O processo terapêutico de João da Silva apresenta uma trajetória de nítida transição " +
                    "entre um estado de reatividade emocional para um estágio de maior autorregulação e percepção reflexiva. " +
                    "No início do acompanhamento, o paciente manifestava um funcionamento psíquico marcado por mecanismos " +
                    "de defesa de isolamento e racionalização, o que dificultava o contato com demandas afetivas " +
                    "e a identificação de necessidades subjetivas. A queixa principal, centrada em níveis elevados de " +
                    "ansiedade e sintomas somáticos, estava intrinsecamente ligada a uma dificuldade em estabelecer " +
                    "fronteiras entre o ego e as pressões do ambiente externo. Ao longo das sessões, " +
                    "observou-se uma flexibilização dessas defesas. João passou a demonstrar maior capacidade de insight, " +
                    "conseguindo correlacionar padrões de comportamento repetitivos com crenças nucleares de insuficiência. " +
                    "A evolução é notável na forma como o paciente processa conflitos: a resposta impulsiva deu lugar " +
                    "a um intervalo de análise, permitindo escolhas mais conscientes e alinhadas com seus valores pessoais. " +
                    "Houve um ganho significativo na fluidez do discurso e na expressão das emoções, reduzindo a " +
                    "psicossomatização anteriormente relatada. Atualmente, o paciente apresenta-se mais integrado e " +
                    "demonstra autonomia no manejo de crises, utilizando ferramentas cognitivas e comportamentais " +
                    "desenvolvidas em terapia. Embora ainda existam núcleos de resistência no que tange à vulnerabilidade " +
                    "emocional, o prognóstico é positivo, com indicação de continuidade para consolidação da identidade " +
                    "e fortalecimento da resiliência psicológica diante de novos estressores." })
    @NotBlank
    private String texto;

    @Schema(name = "pacienteId", description = "Id do paciente", examples = { "1" })
    @NotNull
    private Long pacienteId;

}
