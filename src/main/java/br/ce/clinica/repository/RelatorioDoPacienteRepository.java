package br.ce.clinica.repository;

import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.entity.RelatorioDoPaciente;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RelatorioDoPacienteRepository implements PanacheRepository<RelatorioDoPaciente> {

    public Uni<RelatorioDoPaciente> findByIdWithPaciente(Long id) {
        return find("""
            SELECT r
            FROM RelatorioDoPaciente r
            JOIN FETCH r.paciente
            WHERE r.id = ?1
        """, id).firstResult();
    }

    public PanacheQuery<RelatorioDoPaciente> findPaginated(
            Sort sort,
            List<String> fields,
            List<String> values
    ) {
        StringBuilder query = new StringBuilder("1 = 1");
        List<Object> params = new ArrayList<>();

        if (fields != null && values != null) {

            if (fields.size() != values.size()) {
                throw new IllegalArgumentException(
                        "filterFields e filterValues devem ter o mesmo tamanho"
                );
            }

            for (int i = 0; i < fields.size(); i++) {
                String field = fields.get(i);
                String value = values.get(i);

                if (isStringValue(value)) {
                    query.append(" AND LOWER(")
                            .append(field)
                            .append(") LIKE ?")
                            .append(i + 1);

                    params.add("%" + value.toLowerCase() + "%");

                } else {
                    query.append(" AND ")
                            .append(field)
                            .append(" = ?")
                            .append(i + 1);

                    params.add(formatValue(value));
                }
            }
        }

        Object[] paramArray = params.toArray();

        if (sort != null) {
            return find(query.toString(), sort, paramArray);
        }

        return find(query.toString(), paramArray);
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
}
