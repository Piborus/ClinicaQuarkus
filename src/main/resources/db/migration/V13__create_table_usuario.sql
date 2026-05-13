CREATE TABLE clinica.usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    data_nascimento DATE NOT NULL,
    cpf VARCHAR(14) UNIQUE NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    crp VARCHAR(20) UNIQUE NOT NULL,
    especialidade VARCHAR(100) NOT NULL,
    tipo_usuario VARCHAR(50) NOT NULL,
    dt_criacao TIMESTAMP WITH TIME ZONE NOT NULL,
    dt_alteracao TIMESTAMP WITH TIME ZONE,
    dt_delecao TIMESTAMP WITH TIME ZONE,
    criado_por VARCHAR(100),
    atualizado_por VARCHAR(100),
    status BOOLEAN NOT NULL,
    deletado BOOLEAN NOT NULL
);

ALTER TABLE clinica.paciente
    ADD COLUMN usuario_id BIGINT,
    ADD CONSTRAINT fk_paciente_usuario
    FOREIGN KEY (usuario_id)
    REFERENCES clinica.usuario(id);