package no.nav.dolly.domain.resultset.aareg;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class RsPeriode {

    @Schema(description = "Dato fra-og-med",
            type = "LocalDateTime",
            required = true)
    private LocalDateTime fom;

    @Schema(description = "Dato til-og-med",
            type = "LocalDateTime")
    private LocalDateTime tom;
}
