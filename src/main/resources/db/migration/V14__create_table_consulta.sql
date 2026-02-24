CREATE TABLE clinica.consulta (
    id BIGSERIAL PRIMARY KEY,
    dt_criacao TIMESTAMP WITH TIME ZONE NOT NULL,
    dt_alteracao TIMESTAMP WITH TIME ZONE,
    dt_delecao TIMESTAMP WITH TIME ZONE,
    criado_por VARCHAR(100),
    atualizado_por VARCHAR(100),
    status BOOLEAN NOT NULL,
    deletado BOOLEAN NOT NULL,
    data_inicio TIMESTAMP NOT NULL,
    data_fim TIMESTAMP NOT NULL,
    status_consulta VARCHAR(50),
    status_confirmacao VARCHAR(50),
    observacao VARCHAR(255),
    paciente_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT fk_consulta_paciente
        FOREIGN KEY (paciente_id)
        REFERENCES clinica.paciente(id),
    CONSTRAINT fk_consulta_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES clinica.usuario(id)
);