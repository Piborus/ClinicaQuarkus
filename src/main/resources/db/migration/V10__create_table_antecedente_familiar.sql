CREATE TABLE clinica.antecedente_familiar (
    id BIGSERIAL PRIMARY KEY,
    descricao TEXT,
    dt_criacao TIMESTAMP WITH TIME ZONE NOT NULL,
    dt_alteracao TIMESTAMP WITH TIME ZONE,
    dt_delecao TIMESTAMP WITH TIME ZONE,
    criado_por VARCHAR(100),
    atualizado_por VARCHAR(100),
    status BOOLEAN NOT NULL,
    deletado BOOLEAN NOT NULL,
    anamnese_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_antecedente_familiar_anamnese
        FOREIGN KEY (anamnese_id)
        REFERENCES clinica.anamnese(id)
        ON DELETE CASCADE
);