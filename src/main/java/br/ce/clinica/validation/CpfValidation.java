package br.ce.clinica.validation;

import br.com.caelum.stella.validation.CPFValidator;
import org.apache.commons.lang3.StringUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class CpfValidation implements ConstraintValidator<ValidCpf, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(cpf)) return false;

        CPFValidator cpfValidator = new CPFValidator();

        return cpfValidator.isEligible(cpf);
    }
}
