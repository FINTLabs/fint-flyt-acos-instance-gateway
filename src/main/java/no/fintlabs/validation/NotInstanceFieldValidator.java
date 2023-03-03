package no.fintlabs.validation;

import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

@Service
public class NotInstanceFieldValidator implements ConstraintValidator<NotInstanceField, String> {
    private static final Pattern instanceFieldKeyPattern = Pattern.compile("(?:(?!\\$if\\{).)+");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isEmpty()) {
            return true;
        }

        return instanceFieldKeyPattern.matcher(value).matches();
    }
}
