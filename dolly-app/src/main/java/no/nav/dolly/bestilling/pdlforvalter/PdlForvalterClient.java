package no.nav.dolly.bestilling.pdlforvalter;

import static java.time.LocalDate.now;
import static java.util.Objects.nonNull;
import static no.nav.dolly.util.NullcheckUtil.blankcheckSetDefaultValue;
import static no.nav.dolly.util.NullcheckUtil.nullcheckSetDefaultValue;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import no.nav.dolly.bestilling.ClientRegister;
import no.nav.dolly.domain.jpa.BestillingProgress;
import no.nav.dolly.domain.resultset.RsDollyBestillingRequest;
import no.nav.dolly.domain.resultset.pdlforvalter.Pdldata;
import no.nav.dolly.domain.resultset.pdlforvalter.doedsbo.PdlKontaktinformasjonForDoedsbo;
import no.nav.dolly.domain.resultset.pdlforvalter.falskidentitet.PdlFalskIdentitet;
import no.nav.dolly.domain.resultset.pdlforvalter.utenlandsid.PdlUtenlandskIdentifikasjonsnummer;
import no.nav.dolly.domain.resultset.tpsf.RsTpsfUtvidetBestilling;
import no.nav.dolly.domain.resultset.tpsf.TpsPerson;
import no.nav.dolly.errorhandling.ErrorStatusDecoder;
import no.nav.dolly.util.DatoFraIdentService;

@Slf4j
@Service
public class PdlForvalterClient implements ClientRegister {

    public static final String KONTAKTINFORMASJON_DOEDSBO = "KontaktinformasjonForDoedsbo";
    public static final String UTENLANDS_IDENTIFIKASJONSNUMMER = "UtenlandskIdentifikasjonsnummer";
    public static final String FALSK_IDENTITET = "FalskIdentitet";
    public static final String PDL_FORVALTER = "PdlForvalter";
    private static final String KILDE = "Dolly";
    private static final String SYNTH_ENV = "q2";
    private static final String HENDELSE_ID = "hendelseId";

    @Autowired
    private PdlForvalterConsumer pdlForvalterConsumer;

    @Autowired
    private DatoFraIdentService datoFraIdentService;

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private ErrorStatusDecoder errorStatusDecoder;

    @Override public void gjenopprett(RsDollyBestillingRequest bestilling, TpsPerson tpsPerson, BestillingProgress progress) {

        if (bestilling.getEnvironments().contains(SYNTH_ENV) || nonNull(bestilling.getPdlforvalter())) {

            StringBuilder status = new StringBuilder();

            if (bestilling.getEnvironments().contains(SYNTH_ENV)) {

                sendDeleteIdent(tpsPerson);
                sendFoedselsmelding(tpsPerson);
                sendDoedsfall(bestilling.getTpsf(), tpsPerson);

                if (nonNull(bestilling.getPdlforvalter())) {
                    Pdldata pdldata = mapperFacade.map(bestilling.getPdlforvalter(), Pdldata.class);
                    sendUtenlandsid(pdldata, tpsPerson.getHovedperson(), status);
                    sendDoedsbo(pdldata, tpsPerson.getHovedperson(), status);
                    sendFalskIdentitet(pdldata, tpsPerson.getHovedperson(), status);
                }

            } else {

                status.append('$')
                        .append(PDL_FORVALTER)
                        .append("&Feil: Bestilling ble ikke sendt til Persondataløsningen (PDL) da miljø '")
                        .append(SYNTH_ENV)
                        .append("' ikke er valgt");
            }

            if (status.length() > 1) {
                progress.setPdlforvalterStatus(status.substring(1));
            }
        }
    }

    @Override
    public void release(List<String> identer) {

        identer.forEach(ident -> pdlForvalterConsumer.deleteIdent(ident));
    }

    private void sendDoedsfall(RsTpsfUtvidetBestilling bestilling, TpsPerson tpsPerson) {

        sendDoedsfall(tpsPerson.getHovedperson(), bestilling.getDoedsdato());
        if (nonNull(bestilling.getRelasjoner())) {
            sendDoedsfall(tpsPerson.getPartner(), bestilling.getRelasjoner().getPartner().getDoedsdato());
            for (int i = 0; i < tpsPerson.getBarn().size(); i++) {
                sendDoedsfall(tpsPerson.getBarn().get(i), bestilling.getRelasjoner().getBarn().get(i).getDoedsdato());
            }
        }
    }

    private void sendDoedsfall(String ident, LocalDateTime doedsdato) {

        if (nonNull(doedsdato)) {
            try {
                pdlForvalterConsumer.postDoedsfall(PdlDoedsfall.builder()
                        .doedsdato(doedsdato.toLocalDate())
                        .kilde(KILDE)
                        .build(), ident);

            } catch (HttpClientErrorException e) {
                log.error("Feilet å sende dødsmelding for ident {} til PDL-forvalter: {}", ident, e.getResponseBodyAsString());

            } catch (RuntimeException e) {
                log.error("Feilet å sende dødsmelding for ident {} til PDL-forvalter.", ident, e);
            }
        }
    }

    private void sendFoedselsmelding(TpsPerson tpsPerson) {

        if (nonNull(tpsPerson.getBarn())) {
            tpsPerson.getBarn().forEach(barn -> {
                try {
                    pdlForvalterConsumer.postFoedsel(PdlFoedsel.builder()
                            .foedselsdato(datoFraIdentService.extract(barn).toLocalDate())
                            .kilde(KILDE)
                            .build(), barn);

                } catch (HttpClientErrorException e) {
                    log.error("Feilet å sende fødselsmelding for ident {} til PDL-forvalter: {}", barn, e.getResponseBodyAsString());

                } catch (RuntimeException e) {
                    log.error("Feilet å sende fødselsmelding for ident {} til PDL-forvalter.", barn, e);
                }
            });
        }
    }

    private void sendUtenlandsid(Pdldata pdldata, String ident, StringBuilder status) {

        if (nonNull(pdldata) && nonNull(pdldata.getUtenlandskIdentifikasjonsnummer())) {
            try {
                appendName(UTENLANDS_IDENTIFIKASJONSNUMMER, status);

                PdlUtenlandskIdentifikasjonsnummer utenlandskId = pdldata.getUtenlandskIdentifikasjonsnummer();
                utenlandskId.setKilde(nullcheckSetDefaultValue(utenlandskId.getKilde(), KILDE));

                ResponseEntity<JsonNode> response =
                        pdlForvalterConsumer.postUtenlandskIdentifikasjonsnummer(utenlandskId, ident);

                appendOkStatus(response.getBody(), status);

            } catch (RuntimeException exception) {

                appendErrorStatus(exception, status);
                log.error(exception.getMessage(), exception);
            }
        }
    }

    private void sendDoedsbo(Pdldata pdldata, String ident, StringBuilder status) {

        if (nonNull(pdldata) && nonNull(pdldata.getKontaktinformasjonForDoedsbo())) {
            try {
                appendName(KONTAKTINFORMASJON_DOEDSBO, status);

                PdlKontaktinformasjonForDoedsbo kontaktinformasjon = pdldata.getKontaktinformasjonForDoedsbo();
                kontaktinformasjon.setKilde(KILDE);
                kontaktinformasjon.setUtstedtDato(nullcheckSetDefaultValue(kontaktinformasjon.getUtstedtDato(), now()));
                kontaktinformasjon.setLandkode(blankcheckSetDefaultValue(kontaktinformasjon.getLandkode(), "NOR"));

                ResponseEntity<JsonNode> response =
                        pdlForvalterConsumer.postKontaktinformasjonForDoedsbo(kontaktinformasjon, ident);

                appendOkStatus(response.getBody(), status);

            } catch (RuntimeException exception) {

                appendErrorStatus(exception, status);
                log.error(exception.getMessage(), exception);
            }
        }
    }

    private void sendFalskIdentitet(Pdldata pdldata, String ident, StringBuilder status) {

        if (nonNull(pdldata) && nonNull(pdldata.getFalskIdentitet())) {
            try {
                appendName(FALSK_IDENTITET, status);

                PdlFalskIdentitet falskIdentitet = pdldata.getFalskIdentitet();
                falskIdentitet.setErFalsk(nullcheckSetDefaultValue(falskIdentitet.getErFalsk(), true));
                falskIdentitet.setKilde(nullcheckSetDefaultValue(falskIdentitet.getKilde(), KILDE));

                ResponseEntity<JsonNode> response =
                        pdlForvalterConsumer.postFalskIdentitet(falskIdentitet, ident);

                appendOkStatus(response.getBody(), status);

            } catch (RuntimeException exception) {

                appendErrorStatus(exception, status);
                log.error(exception.getMessage(), exception);
            }
        }
    }

    private void sendDeleteIdent(TpsPerson tpsPerson) {//NOSONAR

        try {
            pdlForvalterConsumer.deleteIdent(tpsPerson.getHovedperson());

            if (nonNull(tpsPerson.getPartner())) {
                pdlForvalterConsumer.deleteIdent(tpsPerson.getPartner());
            }

            if (nonNull(tpsPerson.getBarn())) {
                tpsPerson.getBarn().forEach(barn -> pdlForvalterConsumer.deleteIdent(barn));
            }

        } catch (RuntimeException e) {

            log.error(e.getMessage(), e);
        }
    }

    private static void appendName(String utenlandsIdentifikasjonsnummer, StringBuilder builder) {
        builder.append('$')
                .append(utenlandsIdentifikasjonsnummer);
    }

    private static void appendOkStatus(JsonNode jsonNode, StringBuilder builder) {
        builder.append("&OK");
        if (nonNull(jsonNode) && nonNull(jsonNode.get(HENDELSE_ID))) {
            builder.append(", ")
                    .append(HENDELSE_ID)
                    .append(": ")
                    .append(jsonNode.get(HENDELSE_ID));
        }
    }

    private void appendErrorStatus(RuntimeException exception, StringBuilder builder) {

        builder.append("&")
                .append(errorStatusDecoder.decodeRuntimeException(exception));
    }
}