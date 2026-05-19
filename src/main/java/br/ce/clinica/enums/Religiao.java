package br.ce.clinica.enums;

import lombok.Getter;

@Getter
public enum Religiao {

    CATOLICISMO("Catolicismo"),
    EVANGELICA("Evangélica"),
    ESPIRITISMO("Espiritismo"),
    MATRIZ_AFRICANA("Matriz Africana"),
    JUDAISMO("Judaismo"),
    ISLAMISMO("Islamismo"),
    BUDISMO("Budismo"),
    HINDUISMO("Hinduismo"),
    TRADICOES_INDIGENAS("Tradições Indígenas"),
    OUTRA("Outra"),
    ATEU("Atéu"),
    NAO_INFORMA("Não Informar");

    private final String valor;

    Religiao(String valor) {
        this.valor = valor;
    }

    public static Religiao fromString(String valor) {
        for (Religiao religiao : Religiao.values()) {
            if (religiao.valor.equalsIgnoreCase(valor)) {
                return religiao;
            }
        }
         throw new IllegalArgumentException("Religião inválida");
    }

}
