ALTER TABLE clinica.antecedente_familiar_tipo
ADD CONSTRAINT chk_tipo_antecedencia_familiar
CHECK (
    tipo_antecedente_familiar IN (
        'TRANSTORNO_MENTAL',
        'SUICIDIO',
        'ALCOOLISMO',
        'HOMICIDIO',
        'OUTROS'
    )
);