CREATE TABLE clinica.carteira (
    id BIGSERIAL PRIMARY KEY,
    dt_criacao TIMESTAMP WITH TIME ZONE NOT NULL,
    dt_alteracao TIMESTAMP WITH TIME ZONE,
    dt_delecao TIMESTAMP WITH TIME ZONE,
    criado_por VARCHAR(100),
    atualizado_por VARCHAR(100),
    status BOOLEAN NOT NULL,
    deletado BOOLEAN NOT NULL,
    valor NUMERIC(19,2) NOT NULL,
    tipo_movimento VARCHAR(50) NOT NULL,
    descricao VARCHAR(255),
    tipo_pagamento VARCHAR(50) NOT NULL,
    paciente_id BIGINT NOT NULL,
    CONSTRAINT fk_transacao_paciente
        FOREIGN KEY (paciente_id)
        REFERENCES clinica.paciente(id)
);
