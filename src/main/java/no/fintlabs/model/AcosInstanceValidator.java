package no.fintlabs.model;

import no.fintlabs.model.acos.AcosInstance;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AcosInstanceValidator {

    public static class Error {
    }

    public Optional<Error> validate(AcosInstance acosInstance) {
        // TODO: 28/06/2022 Implement
        // Check elements have unique ids
        // Check all required fields have value
        return Optional.empty();
    }
}
