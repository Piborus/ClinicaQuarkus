CREATE TABLE clinica.antecedente_familiar_tipo (
    antecedente_familiar_id BIGINT NOT NULL,
    tipo_antecedente_familiar VARCHAR(50) NOT NULL,

    CONSTRAINT pk_antecedente_familiar_tipo
        PRIMARY KEY (antecedente_familiar_id, tipo_antecedente_familiar),

    CONSTRAINT fk_antecedente_familiar_tipo
        FOREIGN KEY (antecedente_familiar_id)
        REFERENCES clinica.antecedente_familiar(id)
        ON DELETE CASCADE
);