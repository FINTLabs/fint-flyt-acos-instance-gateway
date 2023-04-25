package no.fintlabs.model.acos;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder

public class AcosInstanceElement {

    @NotBlank
    private String id;

    private String value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AcosInstanceElement that = (AcosInstanceElement) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
