package no.fintlabs.validation;

import lombok.Getter;

import java.util.List;

public class InstanceValidationException extends RuntimeException {

    @Getter
    private final List<AcosInstanceValidationService.Error> validationErrors;

    public InstanceValidationException(List<AcosInstanceValidationService.Error> validationErrors) {
        super("Instance validation error(s): " + validationErrors);
        this.validationErrors = validationErrors;
    }
}
