ALTER TABLE clinica.usuario
    ADD COLUMN sobrenome VARCHAR(100),
    ADD COLUMN data_nascimento DATE,
    ADD COLUMN cpf VARCHAR(14) UNIQUE,
    ADD COLUMN telefone VARCHAR(20),
    ADD COLUMN crp VARCHAR(20) UNIQUE,
    ADD COLUMN especialidade VARCHAR(100);
