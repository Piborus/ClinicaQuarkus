package br.ce.clinica.enums;

import lombok.Getter;

@Getter
public enum Especialidade {

    AVALIACAO_PSICOLOGICA("Avaliação Psicológica"),
    NEUROPSICOLOGIA("Neuropsicologia"),
    PSICOLOGIA_CLINICA("Psicologia Clinica"),
    PSICOLOGIA_TRAFEGO("Psicologia Trafego"),
    PSICOLOGIA_ESPORTE("Psicologia Esporte"),
    PSICOLOGIA_SAUDE("Psicologia Saúde"),
    PSICOLOGIA_ESCOLAR_EDUCACIONAL("Psicologia Escolar/Educacional"),
    PSICOLOGIA_HOSPITALAR("Psicologia Hospitalar"),
    PSICOLOGIA_JURIDICA("Psicologia Jurídica"),
    PSICOLOGIA_ORGANIZACIONAL_TRABALHO("Psicologia Organizacional/Trabalho"),
    PSICOLOGIA_SOCIAL("Psicologia Social"),
    PSICOMOTRICIDADE("Psicomotricidade"),
    PSICOPEDAGOGIA("Psicopedagogia");

    private final String valor;

    Especialidade(String valor) {
        this.valor = valor;
    }

    public static Especialidade fromString(String valor) {
        for (Especialidade especialidade : Especialidade.values()) {
            if (especialidade.valor.equalsIgnoreCase(valor)) {
                return especialidade;
            }
        }
        throw new IllegalArgumentException("Especialidade inválida");
    }
}
