package no.nav.dolly.domain.resultset.aareg;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RsArbeidsavtale {

    private Integer antallKonverterteTimer;

    @Schema(description = "Gyldige verdier finnes i kodeverk 'Arbeidstidsordninger'",
            type = "String",
            required = true)
    private String arbeidstidsordning;

    private Double avtaltArbeidstimerPerUke;

    private String avloenningstype;

    @Schema(type = "LocalDateTime")
    private LocalDateTime endringsdatoStillingsprosent;

    private LocalDateTime sisteLoennsendringsdato;

    private Double stillingsprosent;

    @Schema(description = "Gyldige verdier finnes i kodeverk 'Yrker'",
            type = "String",
            required = true)
    private String yrke;

    @Schema(description = "Gyldige verdier finnes i kodeverk 'AnsettelsesformAaareg'",
            type = "String")
    private String ansettelsesform;

    @Schema(type = "LocalDateTime")
    private LocalDateTime endringsdatoLoenn;
}
