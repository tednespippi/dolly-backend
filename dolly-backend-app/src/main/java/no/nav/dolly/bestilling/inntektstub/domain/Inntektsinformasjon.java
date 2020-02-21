package no.nav.dolly.bestilling.inntektstub.domain;

import java.time.LocalDate;
import java.util.List;

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
public class Inntektsinformasjon {

    private String norskIdent;

    private String aarMaaned;
    private String opplysningspliktig;
    private String virksomhet;

    private List<Inntekt> inntektsliste;
    private List<Fradrag> fradragsliste;
    private List<Forskuddstrekk> forskuddstrekksliste;
    private List<Arbeidsforhold> arbeidsforholdsliste;
    private Integer versjon;

    private String feilmelding;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Fradrag {

        private Long id;
        private Double beloep;
        private String beskrivelse;
        private String feilmelding;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Forskuddstrekk {

        private Long id;
        private Double beloep;
        private String beskrivelse;
        private String feilmelding;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Arbeidsforhold {

        private Long id;
        private Double antallTimerPerUkeSomEnFullStillingTilsvarer;
        private String arbeidsforholdstype;
        private String arbeidstidsordning;
        private String avloenningstype;
        private String feilmelding;

        private LocalDate sisteDatoForStillingsprosentendring;
        private LocalDate sisteLoennsendringsdato;
        private LocalDate sluttdato;
        private LocalDate startdato;

        private Double stillingsprosent;
        private String yrke;
    }
}