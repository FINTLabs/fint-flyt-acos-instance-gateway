package no.fintlabs;

import no.fintlabs.model.fint.Integration;

public class IntegrationDeactivatedException extends RuntimeException {

    public IntegrationDeactivatedException(Integration integration) {
        super("Integration is disabled: " + integration);
    }

}
