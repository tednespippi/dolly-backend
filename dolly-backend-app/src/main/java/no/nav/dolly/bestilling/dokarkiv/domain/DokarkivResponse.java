package no.nav.dolly.bestilling.dokarkiv.domain;

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
public class DokarkivResponse {

    private String journalpostId;
    private boolean journalpostferdigstilt;
    private DokumentInfo dokumenter;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DokumentInfo {

        private String dokumentInfoId;
        private String brevkode;
        private String tittel;
    }
}
