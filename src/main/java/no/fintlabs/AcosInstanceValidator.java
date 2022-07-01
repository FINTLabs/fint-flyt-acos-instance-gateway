package no.fintlabs;

import no.fintlabs.model.acos.AcosInstance;
import no.fintlabs.model.acos.AcosInstanceElement;
import org.springframework.stereotype.Service;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Service
public class AcosInstanceValidator {

    private final Validator fieldValidator;

    public AcosInstanceValidator(ValidatorFactory validatorFactory) {
        this.fieldValidator = validatorFactory.getValidator();
    }

    public Optional<List<String>> validate(AcosInstance acosInstance) {
        List<String> errors = fieldValidator.validate(acosInstance)
                .stream()
                .map(constraintViolation ->
                        constraintViolation.getPropertyPath() + " " + constraintViolation.getMessage())
                .sorted()
                .collect(Collectors.toList());

        validateElementIds(acosInstance).ifPresent(errors::add);

        return errors.isEmpty()
                ? Optional.empty()
                : Optional.of(errors);
    }

    private Optional<String> validateElementIds(AcosInstance acosInstance) {
        List<String> duplicateElementIds = findDuplicateElementIds(getElements(acosInstance));
        return duplicateElementIds.isEmpty()
                ? Optional.empty()
                : Optional.of("Duplicate element ID(s): " + duplicateElementIds);
    }

    private List<AcosInstanceElement> getElements(AcosInstance acosInstance) {
        return Optional.ofNullable(acosInstance.getElements()).orElse(emptyList())
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<String> findDuplicateElementIds(List<AcosInstanceElement> acosInstanceElements) {
        Set<String> items = new HashSet<>();
        return acosInstanceElements.stream()
                .map(AcosInstanceElement::getId)
                .filter(Objects::nonNull)
                .filter(n -> !items.add(n))
                .collect(Collectors.toList());
    }

}
