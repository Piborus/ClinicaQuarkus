package br.ce.clinica.service.impl;

import br.ce.clinica.repository.TransacaoRepository;
import br.ce.clinica.service.TransacaoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TransacaoServiceImpl implements TransacaoService {

    @Inject
    TransacaoRepository transacaoRepository;
}
