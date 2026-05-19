package br.ce.clinica.enums;

import lombok.Getter;

@Getter
public enum GrauParentesco {

    CONJUGE("Conjuge"),
    GENITOR("Genitor"),
    FILHO("Filho/Filha"),
    ENTEADO("Enteado"),
    IRMAO("Irmão/Irmã"),
    AVO("Avô/Avó"),
    NETO("Neto"),
    TIO("Tio/Tia"),
    SOBRINHO("Sobrinho/Sobrinha"),
    PRIMO("Primo/Prima"),
    SOGRO("Sogro/Sogra"),
    CUNHADO("Cunhado/Cunhada"),
    GENRO_NORA("Genro/Nora"),
    TUTOR("Tutor"),
    CURADOR("Curador"),
    OUTRO("Outro");

    private final String valor;

    GrauParentesco(String valor) {
        this.valor = valor;
    }

    public static GrauParentesco fromString(String valor) {
        for (GrauParentesco grau : GrauParentesco.values()) {
            if (grau.valor.equalsIgnoreCase(valor)) {
                return grau;
            }
        }
        throw new IllegalArgumentException("Grau de parentesco inválido");
    }

}
