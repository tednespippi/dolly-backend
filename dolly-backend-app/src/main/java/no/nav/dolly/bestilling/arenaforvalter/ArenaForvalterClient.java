package no.nav.dolly.bestilling.arenaforvalter;

import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import no.nav.dolly.bestilling.ClientRegister;
import no.nav.dolly.domain.jpa.BestillingProgress;
import no.nav.dolly.domain.resultset.RsDollyUtvidetBestilling;
import no.nav.dolly.domain.resultset.arenaforvalter.ArenaArbeidssokerBruker;
import no.nav.dolly.domain.resultset.arenaforvalter.ArenaDagpenger;
import no.nav.dolly.domain.resultset.arenaforvalter.ArenaNyBruker;
import no.nav.dolly.domain.resultset.arenaforvalter.ArenaNyeBrukere;
import no.nav.dolly.domain.resultset.arenaforvalter.ArenaNyeBrukereResponse;
import no.nav.dolly.domain.resultset.arenaforvalter.ArenaNyeDagpengerResponse;
import no.nav.dolly.domain.resultset.tpsf.DollyPerson;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArenaForvalterClient implements ClientRegister {

    private final ArenaForvalterConsumer arenaForvalterConsumer;
    private final MapperFacade mapperFacade;

    @Override
    public void gjenopprett(RsDollyUtvidetBestilling bestilling, DollyPerson dollyPerson, BestillingProgress progress, boolean isOpprettEndre) {

        if (nonNull(bestilling.getArenaforvalter())) {

            StringBuilder status = new StringBuilder();

            ResponseEntity<List> envResponse = arenaForvalterConsumer.getEnvironments();
            List<String> environments = envResponse.hasBody() ? envResponse.getBody() : emptyList();

            List<String> availEnvironments = new ArrayList<>(environments);

            availEnvironments.retainAll(bestilling.getEnvironments());

            if (!availEnvironments.isEmpty()) {

                if (!isOpprettEndre) {
                    deleteServicebruker(dollyPerson.getHovedperson(), availEnvironments);
                }

                ArenaNyeBrukere arenaNyeBrukere = new ArenaNyeBrukere();
                List<ArenaDagpenger> dagpengerListe = new ArrayList<>();
                availEnvironments.forEach(environment -> {
                    ArenaNyBruker arenaNyBruker = mapperFacade.map(bestilling.getArenaforvalter(), ArenaNyBruker.class);
                    arenaNyBruker.setPersonident(dollyPerson.getHovedperson());
                    arenaNyBruker.setMiljoe(environment);
                    arenaNyeBrukere.getNyeBrukere().add(arenaNyBruker);

                    if (!bestilling.getArenaforvalter().getDagpenger().isEmpty()) {
                        ArenaDagpenger arenaDagpenger = mapperFacade.map(bestilling.getArenaforvalter(), ArenaDagpenger.class);
                        arenaDagpenger.setPersonident(dollyPerson.getHovedperson());
                        arenaDagpenger.setMiljoe(environment);
                        dagpengerListe.add(arenaDagpenger);
                    }
                });

                sendArenadata(arenaNyeBrukere, status);
                dagpengerListe.forEach(dagpenger -> sendArenadagpenger(dagpenger, status));

            }

            List<String> notSupportedEnvironments = new ArrayList<>(bestilling.getEnvironments());
            notSupportedEnvironments.removeAll(environments);
            notSupportedEnvironments.forEach(environment ->
                    status.append(',')
                            .append(environment)
                            .append("$Feil: Miljø ikke støttet"));

            if (status.length() > 1) {
                progress.setArenaforvalterStatus(status.substring(1));
            }
        }
    }

    @Override
    public void release(List<String> identer) {

        identer.forEach(ident -> {
            ResponseEntity<ArenaArbeidssokerBruker> existingServicebruker = arenaForvalterConsumer.getIdent(ident);
            if (existingServicebruker.hasBody()) {
                existingServicebruker.getBody().getArbeidsokerList().forEach(list -> {
                    if (nonNull(list.getMiljoe())) {
                        List.of(list.getMiljoe().split(",")).forEach(
                                environment -> arenaForvalterConsumer.deleteIdent(ident, environment));
                    }
                });
            }
        });
    }

    private void deleteServicebruker(String ident, List<String> availEnvironments) {

        try {
            availEnvironments.forEach(environment ->
                    arenaForvalterConsumer.deleteIdent(ident, environment));

        } catch (RuntimeException e) {

            log.error("Feilet å inaktivere testperson: {} i ArenaForvalter: ", ident, e);
        }

    }

    private void sendArenadagpenger(ArenaDagpenger arenaNyeDagpenger, StringBuilder status) {

        try {
            log.info("Sender dagpenger: \n" + Json.pretty(arenaNyeDagpenger));
            ResponseEntity<ArenaNyeDagpengerResponse> response = arenaForvalterConsumer.postArenaDagpenger(arenaNyeDagpenger);
            log.info("Dagpenger mottatt: \n" + Json.pretty(response));
            if (response.hasBody()) {
                if (nonNull(response.getBody().getNyeDagpFeilList()) && !response.getBody().getNyeDagpFeilList().isEmpty()) {
                    response.getBody().getNyeDagpFeilList().forEach(brukerfeil -> {
                        log.info("Brukerfeil inneholder: " + Json.pretty(brukerfeil));
                        status.append(',')
                                .append(brukerfeil.getMiljoe())
                                .append("$Feilstatus: \"")
                                .append(brukerfeil.getNyDagpFeilstatus())
                                .append("\". Se detaljer i logg.");
                        log.error("Feilet å opprette dagpenger for testperson {} i ArenaForvalter på miljø: {}, feilstatus: {}, melding: \"{}\"",
                                brukerfeil.getPersonident(), brukerfeil.getMiljoe(), brukerfeil.getNyDagpFeilstatus(), brukerfeil.getMelding());
                    });
                } else {
                    status.append(',')
                            .append(arenaNyeDagpenger.getMiljoe())
                            .append("$OK");
                }
            } else {
                status.append(',')
                        .append(arenaNyeDagpenger.getMiljoe())
                        .append("Feilstatus: Mottok ugyldig svar fra Arena");
            }
        } catch (RuntimeException e) {

            status.append(',')
                    .append(arenaNyeDagpenger.getMiljoe())
                    .append('$');
            appendErrorText(status, e);
            log.error("Feilet å legge til dagpenger i ArenaForvalter: ", e);
        }
        log.info("status for dagpenger er: " + Json.pretty(status));
    }

    private void sendArenadata(ArenaNyeBrukere arenaNyeBrukere, StringBuilder status) {

        try {
            log.info("Sender Arenadata: \n" + Json.pretty(arenaNyeBrukere));
            ResponseEntity<ArenaNyeBrukereResponse> response = arenaForvalterConsumer.postArenadata(arenaNyeBrukere);
            if (response.hasBody()) {
                if (nonNull((response.getBody().getArbeidsokerList()))) {
                    response.getBody().getArbeidsokerList().forEach(arbeidsoker -> {
                        if ("OK".equals(arbeidsoker.getStatus())) {
                            status.append(',')
                                    .append(arbeidsoker.getMiljoe())
                                    .append('$')
                                    .append(arbeidsoker.getStatus());
                        }
                    });
                }
                if (nonNull(response.getBody().getNyBrukerFeilList())) {
                    response.getBody().getNyBrukerFeilList().forEach(brukerfeil -> {
                        status.append(',')
                                .append(brukerfeil.getMiljoe())
                                .append("$Feilstatus: \"")
                                .append(brukerfeil.getNyBrukerFeilstatus())
                                .append("\". Se detaljer i logg.");
                        log.error("Feilet å opprette testperson {} i ArenaForvalter på miljø: {}, feilstatus: {}, melding: \"{}\"",
                                brukerfeil.getPersonident(), brukerfeil.getMiljoe(), brukerfeil.getNyBrukerFeilstatus(), brukerfeil.getMelding());
                    });
                }
            }

        } catch (RuntimeException e) {

            arenaNyeBrukere.getNyeBrukere().forEach(bruker -> {
                status.append(',')
                        .append(bruker.getMiljoe())
                        .append('$');
                appendErrorText(status, e);
            });
            log.error("Feilet å legge inn ny testperson i ArenaForvalter: ", e);
        }
    }

    private static void appendErrorText(StringBuilder status, RuntimeException e) {
        status.append("Feil: ")
                .append(e.getMessage());

        if (e instanceof HttpClientErrorException) {
            status.append(" (")
                    .append(((HttpClientErrorException) e).getResponseBodyAsString().replace(',', '='))
                    .append(')');
        }
    }
}
