package no.nav.dolly.aareg;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import no.nav.dolly.bestilling.ClientRegister;
import no.nav.dolly.domain.jpa.BestillingProgress;
import no.nav.dolly.domain.resultset.RsDollyBestilling;
import no.nav.dolly.domain.resultset.aareg.RsAaregOpprettRequest;
import no.nav.dolly.domain.resultset.aareg.RsAktoer;
import no.nav.dolly.domain.resultset.aareg.RsAktoerPerson;
import no.nav.dolly.domain.resultset.aareg.RsArbeidsforhold;
import no.nav.dolly.domain.resultset.aareg.RsOrganisasjon;
import no.nav.dolly.domain.resultset.aareg.RsPersonAareg;
import no.nav.dolly.domain.resultset.tpsf.TpsPerson;

@Slf4j
@Service
public class AaregClient extends AaregAbstractClient implements ClientRegister {

    @Autowired
    private AaregWsConsumer aaregWsConsumer;

    @Autowired
    private AaregRestConsumer aaregRestConsumer;

    @Autowired
    private AaregReleaseIdentClient aaregReleaseIdentClient;

    @Override public void gjenopprett(RsDollyBestilling bestilling, TpsPerson tpsPerson, BestillingProgress progress) {

        StringBuilder result = new StringBuilder();

        if (!bestilling.getAareg().isEmpty()) {

            bestilling.getEnvironments().forEach(env -> {

                ResponseEntity<Object[]> response = ResponseEntity.ok(new Object[] {});
                try {
                    response = aaregRestConsumer.readArbeidsforhold(tpsPerson.getHovedperson(), env);
                } catch (RuntimeException e) {
                    log.error("Lesing av aareg i {} feilet, {}", env, e.getLocalizedMessage());
                }

                for (int i = 0; i < bestilling.getAareg().size(); i++) {
                    RsArbeidsforhold arbfInput = bestilling.getAareg().get(i);
                    arbfInput.setArbeidsforholdID(Integer.toString(i + 1));
                    arbfInput.setArbeidstaker(RsPersonAareg.builder().ident(tpsPerson.getHovedperson()).build());

                    boolean found = false;
                    for (int j = 0; j < response.getBody().length; j++) {
                        Map arbfFraAareg = (Map) response.getBody()[j];

                        if ((isMatchArbgivOrgnummer(arbfInput.getArbeidsgiver(), getIdentifyingNumber(arbfFraAareg)) ||
                                isMatchArbgivPersonnummer(arbfInput.getArbeidsgiver(), getIdentifyingNumber(arbfFraAareg))) &&
                                arbfInput.getArbeidsforholdID().equals(getArbforholdId(arbfFraAareg))) {

                            arbfInput.setArbeidsforholdIDnav(getNavArbfholdId(arbfFraAareg));
                            appendResult(aaregWsConsumer.oppdaterArbeidsforhold(buildRequest(arbfInput, env)), arbfInput.getArbeidsforholdID(), result);

                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        appendResult(aaregWsConsumer.opprettArbeidsforhold(RsAaregOpprettRequest.builder()
                                .arbeidsforhold(arbfInput)
                                .environments(singletonList(env))
                                .build()), arbfInput.getArbeidsforholdID(), result);
                    }
                }
            });
        }

        progress.setAaregStatus(result.length() > 1 ? result.substring(1) : null);
    }

    @Override public void release(List<String> identer) {

        identer.forEach(ident ->
                aaregReleaseIdentClient.deleteArbeidsforhold(ident));
    }

    private static String getIdentifyingNumber(Map arbfFraAareg) {
        return "Organisasjon".equals(getArbeidsgiverType(arbfFraAareg)) ? getOrgnummer(arbfFraAareg) : getPersonnummer(arbfFraAareg);
    }

    private static boolean isMatchArbgivOrgnummer(RsAktoer arbeidsgiver, String orgnummer) {
        return arbeidsgiver instanceof RsOrganisasjon &&
                ((RsOrganisasjon) arbeidsgiver).getOrgnummer().equals(orgnummer);
    }

    private static boolean isMatchArbgivPersonnummer(RsAktoer arbeidsgiver, String ident) {
        return arbeidsgiver instanceof RsAktoerPerson &&
                ((RsAktoerPerson) arbeidsgiver).getIdent().equals(ident);
    }

    private static StringBuilder appendResult(Map<String, String> result, String arbeidsforholdId, StringBuilder builder) {
        for (Map.Entry<String, String> entry : result.entrySet()) {
            builder.append(',')
                    .append(entry.getKey())
                    .append(": arbforhold=")
                    .append(arbeidsforholdId)
                    .append('$')
                    .append(entry.getValue().replaceAll(",", "&").replaceAll(":", "="));
        }
        return builder;
    }
}
