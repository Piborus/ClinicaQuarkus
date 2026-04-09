CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE INDEX idx_consulta_usuario_data
    ON clinica.consulta (usuario_id, data_inicio);

ALTER TABLE clinica.consulta
    ADD CONSTRAINT check_intervalo_valido
        CHECK (data_fim > data_inicio);


ALTER TABLE clinica.consulta
    ADD CONSTRAINT no_conflito_horario
        EXCLUDE USING GIST (
        usuario_id WITH =,
        tsrange(data_inicio, data_fim, '[)') WITH &&
        )
        WHERE (status_consulta <> 'CANCELADA');