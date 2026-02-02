ALTER TABLE clinica.transacao
    RENAME TO carteira;

ALTER TABLE clinica.paciente
  ADD COLUMN religiao VARCHAR(100),
  ADD COLUMN naturalidade VARCHAR(100),
  ADD COLUMN escolaridade VARCHAR(100),
  ADD COLUMN profissao VARCHAR(100),
  ADD COLUMN estado_civil VARCHAR(100);
