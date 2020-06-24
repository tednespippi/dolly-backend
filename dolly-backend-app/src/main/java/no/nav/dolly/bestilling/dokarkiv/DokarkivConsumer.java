package no.nav.dolly.bestilling.dokarkiv;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.net.URI;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.dolly.bestilling.dokarkiv.domain.DokarkivRequest;
import no.nav.dolly.bestilling.dokarkiv.domain.DokarkivResponse;
import no.nav.dolly.metrics.Timed;
import no.nav.dolly.properties.ProvidersProps;
import no.nav.dolly.security.sts.StsOidcService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DokarkivConsumer {

    private static final String DOKARKIV_URL = "/rest/journalpostapi/v1/journalpost";
    private static final String FORSOEK_FERDIGSTILL = "?forsoekFerdigstill=true";
    private static final String PREPROD_ENV = "q";
    private static final String TEST_ENV = "t";

    private final RestTemplate restTemplate;
    private final ProvidersProps providersProps;
    private final StsOidcService stsOidcService;

    @Timed(name = "providers", tags = { "operation", "dokarkiv-opprett" })
    public ResponseEntity<DokarkivResponse> postDokarkiv(String environment, DokarkivRequest dokarkivRequest) {
        return restTemplate.exchange(
                RequestEntity.post(URI.create(providersProps.getDokarkiv().getUrl().replace("$", environment) + DOKARKIV_URL + FORSOEK_FERDIGSTILL))
                        .header(AUTHORIZATION, stsOidcService.getIdToken(environment.contains(PREPROD_ENV) ? PREPROD_ENV : TEST_ENV))
                        .body(dokarkivRequest),
                DokarkivResponse.class);
    }
}
