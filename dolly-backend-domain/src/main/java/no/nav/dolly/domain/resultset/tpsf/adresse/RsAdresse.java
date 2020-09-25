package no.nav.dolly.domain.resultset.tpsf.adresse;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "adressetype")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RsGateadresse.class, name = "GATE"),
        @JsonSubTypes.Type(value = RsMatrikkeladresse.class, name = "MATR")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class RsAdresse {

    public enum TilleggType {CO_NAVN, LEILIGHET_NR, SEKSJON_NR, BOLIG_NR}

    @Schema(required = true,
            description = "Angir adressetype, GATE eller MATR.\n" +
                    "For gateadresse, inkluder: \n" +
                    " \"gateadresse\": \"string\" Forstås som gatenavn\n" +
                    " \"husnummer\": \"string\" * \n" +
                    " \"gatekode\": \"string\" * Hentes fra adressesøk\n" +
                    "For matrikkeladresse inkluder:\n" +
                    " \"mellomnavn\": \"string\" Forståes som gårdsnavn\n" +
                    " \"gardsnr\": \"string\" *\n" +
                    " \"bruksnr\": \"string\" *\n" +
                    " \"festenr\": \"string\"\n" +
                    " \"undernr\": \"string\"")
    private String adressetype;

    @Schema(description = "Postnummer på adresse")
    private String postnr;

    @Schema(required = true,
            description = "Kommunenummer for adresse")
    private String kommunenr;

    @Schema(type = "LocalDateTime",
            description = "Flyttedato for adresse. Hvis tomt blir flyttedato identisk med fødselsdato"
    )
    private LocalDateTime flyttedato;

    private TilleggAdressetype tilleggsadresse;

    public abstract String getAdressetype();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TilleggAdressetype {

        @Schema(description = "Tilleggstype, ved CO_NAVN benyttes ikke \"nummer\" feltet")
        private TilleggType tilleggType;

        @Schema(description = "Nummer for leilighet/seksjon/bolig")
        private Integer nummer;
    }
}
