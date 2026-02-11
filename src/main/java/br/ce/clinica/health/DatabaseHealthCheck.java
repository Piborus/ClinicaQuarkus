package br.ce.clinica.health;


import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.hibernate.reactive.mutiny.Mutiny;

@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements AsyncHealthCheck {

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @Override
    public Uni<HealthCheckResponse> call() {
        return sessionFactory.withSession(session ->
                        session.createNativeQuery("SELECT 1")
                                .getSingleResult())
                .onItem().transform(result ->
                        HealthCheckResponse.up("Database connection is healthy")
                )
                .onFailure().recoverWithItem(throwable ->
                        HealthCheckResponse.down("Database connection is unhealthy: " + throwable.getMessage()));
    }
}
