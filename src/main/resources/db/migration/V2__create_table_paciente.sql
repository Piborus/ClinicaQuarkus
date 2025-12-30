-- Tabela de Paciente no schema clinica
CREATE TABLE clinica.paciente (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255),
    idade INTEGER,
    sexo VARCHAR(20),
    data_nascimento DATE,
    cpf VARCHAR(14) UNIQUE,
    rg VARCHAR(20) UNIQUE,
    telefone VARCHAR(20),
    email VARCHAR(255),
    -- Campos de BaseAuditEntity
    dt_criacao TIMESTAMP WITH TIME ZONE NOT NULL,
    dt_alteracao TIMESTAMP WITH TIME ZONE,
    dt_delecao TIMESTAMP WITH TIME ZONE,
    criado_por VARCHAR(100),
    atualizado_por VARCHAR(100),
    status BOOLEAN NOT NULL,
    deletado BOOLEAN NOT NULL
);

