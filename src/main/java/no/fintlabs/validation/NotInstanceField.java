package no.fintlabs.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = NotInstanceFieldValidator.class)
public @interface NotInstanceField {

    String message() default "must not contain \"$if{\"";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
