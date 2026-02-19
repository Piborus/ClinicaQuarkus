CREATE TABLE clinica.filiacao (
    id BIGSERIAL PRIMARY KEY,
    dt_criacao TIMESTAMP WITH TIME ZONE NOT NULL,
    dt_alteracao TIMESTAMP WITH TIME ZONE,
    dt_delecao TIMESTAMP WITH TIME ZONE,
    criado_por VARCHAR(100),
    atualizado_por VARCHAR(100),
    status BOOLEAN NOT NULL,
    deletado BOOLEAN NOT NULL,
    nome VARCHAR(255),
    idade INTEGER,
    cpf VARCHAR(14) UNIQUE,
    telefone VARCHAR(20),
    email VARCHAR(255),
    grau_parentesco VARCHAR(255),
    paciente_id BIGINT NOT NULL,
    CONSTRAINT fk_filiacao_paciente
        FOREIGN KEY (paciente_id)
        REFERENCES clinica.paciente(id)
);
