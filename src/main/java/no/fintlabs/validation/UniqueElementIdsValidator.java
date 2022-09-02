package no.fintlabs.validation;

import no.fintlabs.model.acos.AcosInstanceElement;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.util.CollectionHelper;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class UniqueElementIdsValidator implements ConstraintValidator<UniqueElementIds, List<AcosInstanceElement>> {

    @Override
    public boolean isValid(List<AcosInstanceElement> value, ConstraintValidatorContext constraintValidatorContext) {
        List<String> duplicateElementIds = findDuplicateElementIds(value);
        if (duplicateElementIds.isEmpty()) {
            return true;
        }
        if (constraintValidatorContext instanceof HibernateConstraintValidatorContext) {
            constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class)
                    .addMessageParameter("duplicateElementIds", String.join(", ", duplicateElementIds))
                    .withDynamicPayload(CollectionHelper.toImmutableList(duplicateElementIds));
        }
        return false;
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
