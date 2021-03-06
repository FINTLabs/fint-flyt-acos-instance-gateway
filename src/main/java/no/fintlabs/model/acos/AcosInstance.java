package no.fintlabs.model.acos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcosInstance {

    @NotNull
    @Valid
    private AcosInstanceMetadata metadata;

    @NotEmpty
    @Valid
    private List<@NotNull AcosInstanceElement> elements;

    @Valid
    private List<@NotNull AcosDocument> documents = new ArrayList<>();

}
