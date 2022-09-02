package no.fintlabs.validation;

import lombok.Builder;
import lombok.Data;
import no.fintlabs.model.acos.AcosInstance;
import org.springframework.stereotype.Service;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AcosInstanceValidationService {

    @Data
    @Builder
    public static class Error {
        private final String fieldPath;
        private final String errorMessage;
    }

    private final Validator fieldValidator;

    public AcosInstanceValidationService(ValidatorFactory validatorFactory) {
        this.fieldValidator = validatorFactory.getValidator();
    }

    public Optional<List<Error>> validate(AcosInstance acosInstance) {
        List<Error> errors = fieldValidator.validate(acosInstance)
                .stream()
                .map(constraintViolation -> Error
                        .builder()
                        .fieldPath(constraintViolation.getPropertyPath().toString())
                        .errorMessage(constraintViolation.getMessage())
                        .build()
                )
                .sorted(Comparator.comparing(Error::getFieldPath))
                .collect(Collectors.toList());

        return errors.isEmpty()
                ? Optional.empty()
                : Optional.of(errors);
    }

}
