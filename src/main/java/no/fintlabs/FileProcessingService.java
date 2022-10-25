package no.fintlabs;

import no.fintlabs.model.acos.AcosDocument;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FileProcessingService {

    public UUID processFile(AcosDocument acosDocument) {
        return UUID.randomUUID();
    }
}
