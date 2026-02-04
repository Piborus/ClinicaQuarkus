CREATE TABLE clinica.anamnese (
    id BIGSERIAL PRIMARY KEY,
    tipo_anamnese VARCHAR(50),
    encaminhamento TEXT,
    historico_acompanhamento TEXT,
    psicodinamica_familiar TEXT,
    observacao TEXT,
    dt_criacao TIMESTAMP WITH TIME ZONE NOT NULL,
    dt_alteracao TIMESTAMP WITH TIME ZONE,
    dt_delecao TIMESTAMP WITH TIME ZONE,
    criado_por VARCHAR(100),
    atualizado_por VARCHAR(100),
    status BOOLEAN NOT NULL,
    deletado BOOLEAN NOT NULL,
    paciente_id BIGINT NOT NULL,
    CONSTRAINT fk_anamnese_paciente
        FOREIGN KEY (paciente_id)
        REFERENCES clinica.paciente(id)
);