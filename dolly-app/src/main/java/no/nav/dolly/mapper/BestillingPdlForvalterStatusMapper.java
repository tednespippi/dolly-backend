package no.nav.dolly.mapper;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static no.nav.dolly.mapper.BestillingMeldingStatusIdentMapper.prepareResult;
import static no.nav.dolly.mapper.BestillingMeldingStatusIdentMapper.resolveStatus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import no.nav.dolly.domain.jpa.BestillingProgress;
import no.nav.dolly.domain.resultset.RsMeldingStatusIdent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BestillingPdlForvalterStatusMapper {

    public static List<RsMeldingStatusIdent> buildPdldataStatusMap(List<BestillingProgress> progressList) {

        //  melding     status      ident
        Map<String, Map<String, List<String>>> msgStatusIdents = new HashMap<>();

        progressList.forEach(progress -> {
            if (nonNull(progress.getPdlforvalterStatus())) {
                asList(progress.getPdlforvalterStatus().split("\\$")).forEach(
                        resolveStatus(msgStatusIdents, progress)
                );
            }
        });

        return prepareResult(msgStatusIdents);
    }
}