CREATE TABLE clinica.anamnese_desenvolvimento(
    id BIGSERIAL PRIMARY KEY,
    gravidez_parto TEXT,
    memorias_infancia TEXT,
    memorias_adolescencia TEXT,
    fase_adulta TEXT,
    fase_atual TEXT,
    mora_com_quem TEXT,
    numero_filhos INTEGER,
    numero_irmaos INTEGER,
    ordem_nascimento VARCHAR(50),
    fumante BOOLEAN,
    etilista BOOLEAN,
    uso_medicamento BOOLEAN,
    descricao_medicamentos TEXT,
    rotina TEXT,
    dt_criacao TIMESTAMP WITH TIME ZONE NOT NULL,
    dt_alteracao TIMESTAMP WITH TIME ZONE,
    dt_delecao TIMESTAMP WITH TIME ZONE,
    criado_por VARCHAR(100),
    atualizado_por VARCHAR(100),
    status BOOLEAN NOT NULL,
    deletado BOOLEAN NOT NULL,
    anamnese_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_desenvolvimento_anamnese
        FOREIGN KEY (anamnese_id)
        REFERENCES clinica.anamnese(id)
        ON DELETE CASCADE
);