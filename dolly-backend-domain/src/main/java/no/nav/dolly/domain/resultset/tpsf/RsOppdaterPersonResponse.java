package no.nav.dolly.domain.resultset.tpsf;

import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RsOppdaterPersonResponse {

    private List<IdentTuple> identTupler;

    public List<IdentTuple> getIdentTupler() {
        if (isNull(identTupler)) {
            identTupler = new ArrayList<>();
        }
        return identTupler;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdentTuple {

        private String ident;
        private boolean lagtTil;
    }
}

