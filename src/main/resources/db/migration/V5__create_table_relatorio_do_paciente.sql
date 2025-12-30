CREATE TABLE clinica.relatorio_do_paciente (
    id BIGSERIAL PRIMARY KEY,
    dt_criacao TIMESTAMP WITH TIME ZONE NOT NULL,
    dt_alteracao TIMESTAMP WITH TIME ZONE,
    dt_delecao TIMESTAMP WITH TIME ZONE,
    criado_por VARCHAR(100),
    atualizado_por VARCHAR(100),
    status BOOLEAN NOT NULL,
    deletado BOOLEAN NOT NULL,
    paciente_id BIGSERIAL NOT NULL REFERENCES clinica.paciente(id),
    relatorio VARCHAR(1000)
)