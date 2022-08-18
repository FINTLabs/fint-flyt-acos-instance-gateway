package no.fintlabs.security.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientAuthorization {
    private boolean authorized;
    private String clientId;
    private String sourceApplicationId;
}
