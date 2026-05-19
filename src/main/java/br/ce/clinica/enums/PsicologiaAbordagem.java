package br.ce.clinica.enums;

import lombok.Getter;

@Getter
public enum PsicologiaAbordagem {

    ANALISE_DO_COMPORTAMENTO("Analise do Comportamento"),
    TERAPIA_COGNITIVO_COMPORTAMENTAL("Terapia Cognitiva Comportamental (TCC)"),
    TERAPIA_COMPORTAMENTAL_DIALETICA("Terapia Comportamental Dialetica (DBT)"),
    TERAPIA_ACEITACAO_COMPROMISSO("Terapia de Aceitação e Compromisso (ACT)"),
    PSICANALISE("Psicanalise"),
    PSICOLOGIA_ANALITICA_JUNGUIANA("Psicologia Analitica Junguiana"),
    ABORDAGEM_CENTRADA_NA_PESSOA("Abordagem Centrada na Pessoa (ACP)"),
    TERAPIA_GESTALT("Terapia Gestalt"),
    PSICOLOGIA_EXISTENCIAL("Psicologia Existencial"),
    FENOMENOLOGIA("Fenomenologia"),
    TERAPIA_SISTEMICA("Terapia Sistemica"),
    PSICODRAMA("Psicodrama"),
    INTEGRATIVA("Integrativa"),
    DESSENSIBILIZACAO_E_REPROCESSAMENTO_POR_MOVIMENTO_OCULAR("Dessensibilização e Reprocessamento por Movimento Ocular (EMDR)");

    private final String valor;

    PsicologiaAbordagem(String valor) {
        this.valor = valor;
    }

    public static PsicologiaAbordagem fromString(String valor) {
        for (PsicologiaAbordagem abordagem : PsicologiaAbordagem.values()) {
            if (abordagem.valor.equalsIgnoreCase(valor)) {
                return abordagem;
            }
        }
        throw new IllegalArgumentException("Abordagem psicológica inválida");
    }



}
