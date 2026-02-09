package br.ce.clinica.repository;

import br.ce.clinica.entity.Anamnese;
import br.ce.clinica.exception.BadRequestBusinessException;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Set;

@ApplicationScoped
public class AnamneseRepository implements PanacheRepository<Anamnese> {

    private static final Set<String> ALLOWED_FILTER_FIELDS = Set.of(
            "id",
            "tipoAnamnese",
            "encaminhamento",
            "historicoAcompanhamento",
            "psicodinamicaFamiliar",
            "observacao",
            "dataCriacao",
            "dataAtualizacao",
            "dataDelecao",
            "criadoPor",
            "atualizadoPor",
            "status",
            "deletado",
            "paciente.id"
    );

    private static final String JPQL_FIND_BY_ID = """
    SELECT DISTINCT a FROM Anamnese a
    LEFT JOIN FETCH a.paciente p
    LEFT JOIN FETCH p.responsaveis
    LEFT JOIN FETCH p.transacao
    LEFT JOIN FETCH p.prontuarioDoPaciente
    LEFT JOIN FETCH a.desenvolvimento d
    LEFT JOIN FETCH a.antecedenteFamiliar af
    LEFT JOIN FETCH af.tiposAntecedentes
    WHERE a.id = ?1
    """;

    private static final String JPQL_FIND_BY_PACIENTE_ID = """
    SELECT DISTINCT a FROM Anamnese a
    LEFT JOIN FETCH a.paciente p
    LEFT JOIN FETCH p.responsaveis
    LEFT JOIN FETCH p.transacao
    LEFT JOIN FETCH p.prontuarioDoPaciente
    LEFT JOIN FETCH a.desenvolvimento d
    LEFT JOIN FETCH a.antecedenteFamiliar af
    LEFT JOIN FETCH af.tiposAntecedentes
    WHERE p.id = ?1
    """;

    private static final String JPQL_BASE = """
    SELECT DISTINCT a FROM Anamnese a
    LEFT JOIN FETCH a.paciente p
    LEFT JOIN FETCH p.responsaveis
    LEFT JOIN FETCH p.transacao
    LEFT JOIN FETCH p.prontuarioDoPaciente
    LEFT JOIN FETCH a.desenvolvimento d
    LEFT JOIN FETCH a.antecedenteFamiliar af
    LEFT JOIN FETCH af.tiposAntecedentes
    WHERE 1 = 1
    """;

    public Uni<Anamnese> findByIdWithCollections(Long pacienteId) {
        return find(JPQL_FIND_BY_ID, pacienteId).firstResult();
    }

    public Uni<Anamnese> findByPacienteIdWithCollections(Long pacienteId) {
        return find(JPQL_FIND_BY_PACIENTE_ID, pacienteId).firstResult();
    }

    public PanacheQuery<Anamnese> findPaginated(
            Sort sort,
            List<String> fields,
            List<String> values
    ) {
        StringBuilder query = new StringBuilder(JPQL_BASE);
        List<Object> params = new java.util.ArrayList<>();

        if (fields != null && values != null) {

            if (fields.size() != values.size()) {
                throw new BadRequestBusinessException(
                        "fields e values devem ter o mesmo tamanho"
                );
            }

            for (int i = 0; i < fields.size(); i++) {
                String field = normalizeField(fields.get(i));

                if (!isAllowedFilterField(field)) {
                    throw new BadRequestBusinessException(
                            "Campo de filtro inválido: " + field
                    );
                }

                String value = values.get(i);

                if (isStringValue(value)) {
                    query.append(" AND LOWER(a.")
                            .append(field)
                            .append(") LIKE ?")
                            .append(i + 1);
                    params.add("%" + value.toLowerCase() + "%");
                } else {
                    query.append(" AND a.")
                            .append(field)
                            .append(" = ?")
                            .append(i + 1);
                    params.add(value);
                }
            }
        }

        Object[] paramsArray = params.toArray();

        if (sort != null) {
            return find(query.toString(), sort, paramsArray);
        } else {
            return find(query.toString(), paramsArray);
        }
    }

    public String formatValue(String value) {

        if (value == null) return "null";

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
            return value.toLowerCase();

        try {
            Double.parseDouble(value);
            return value;
        } catch (NumberFormatException e) {
            return "'%" + value.toLowerCase().replace("'", "''") + "%'";
        }
    }

    public boolean isStringValue(String value) {

        if (value == null) return false;
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) return false;

        try {
            Double.parseDouble(value);
            return false;
        } catch (NumberFormatException ex) {
            return true;
        }
    }

    private boolean isAllowedFilterField(String field) {
        if (field == null || field.isBlank()) {
            return false;
        }
        return ALLOWED_FILTER_FIELDS.contains(field);
    }

    private String normalizeField(String field) {
        return field == null ? null : field.trim();
    }
}
