package br.ce.clinica;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class MockedTestProfile implements QuarkusTestProfile {
    
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            "quarkus.flyway.migrate-at-start", "false",
            "quarkus.flyway.active", "false",
            "quarkus.datasource.devservices.enabled", "true",
            "quarkus.hibernate-orm.database.generation", "drop-and-create",
            "quarkus.hibernate-orm.log.sql", "false"
        );
    }
}
