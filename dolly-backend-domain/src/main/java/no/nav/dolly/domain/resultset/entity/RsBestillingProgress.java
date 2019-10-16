package no.nav.dolly.domain.resultset.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RsBestillingProgress {

    private Long id;

    private Long bestillingsId;

    private String ident;

    private List<String> tpsfSuccessEnv;

    private String sigrunstubStatus;

    private String krrstubStatus;

    private String aaregStatus;

    private String arenaforvalterStatus;

    private String pdlforvalterStatus;

    private List<String> feil;
}