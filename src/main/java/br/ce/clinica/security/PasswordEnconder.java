package br.ce.clinica.security;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class PasswordEnconder {

    public String hash(String senha) {
        return BcryptUtil.bcryptHash(senha);
    }

    public boolean matches(String senha, String hash) {
        return BcryptUtil.matches(senha, hash);
    }

}
