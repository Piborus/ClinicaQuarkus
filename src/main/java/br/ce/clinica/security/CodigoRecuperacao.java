package br.ce.clinica.security;

import jakarta.enterprise.context.ApplicationScoped;

import java.security.SecureRandom;

@ApplicationScoped
public class CodigoRecuperacao {

    private static final SecureRandom SECURE_RANDOM  = new SecureRandom();

    public String gerarCodigoRecuperacao() {
        int codigo = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", codigo);
    }
}
