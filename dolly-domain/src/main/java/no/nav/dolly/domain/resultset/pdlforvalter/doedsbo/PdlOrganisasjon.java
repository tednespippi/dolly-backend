package no.nav.dolly.domain.resultset.pdlforvalter.doedsbo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PdlOrganisasjon {

    private PdlPersonnavn kontaktperson;
    private String organisasjonsnavn;
    private String organisasjonsnummer;
}