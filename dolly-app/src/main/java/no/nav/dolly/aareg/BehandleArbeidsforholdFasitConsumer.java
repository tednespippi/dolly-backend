package no.nav.dolly.aareg;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import no.nav.dolly.fasit.FasitApiConsumer;
import no.nav.dolly.fasit.FasitResourceWithUnmappedProperties;

@Service
public class BehandleArbeidsforholdFasitConsumer {

    private static final String BEHANDLE_ARBEIDFORHOLD_ALIAS = "virksomhet:BehandleArbeidsforhold_v1";
    private static final String BASE_URL = "BaseUrl";

    private static final String BEHANDLE_ARBEIDSFORHOLD_SERVICE_URL = "/aareg-core/BehandleArbeidsforholdService/v1";

    @Autowired
    private FasitApiConsumer fasitApiConsumer;


    public Map fetchUrlsByEnvironment() {

        Map<String, String> fasitUrlByEnviromenent = new HashMap<>();

        FasitResourceWithUnmappedProperties[] fasitResources = fasitApiConsumer.fetchResources(BEHANDLE_ARBEIDFORHOLD_ALIAS, BASE_URL);

        for (FasitResourceWithUnmappedProperties fasitResource : fasitResources) {
            String url = ((String) ((Map) fasitResource.getProperties()).get("url"))
                    .replaceAll("/aareg-services/BehandleArbeidsforholdService/v1", "") + BEHANDLE_ARBEIDSFORHOLD_SERVICE_URL;
            fasitUrlByEnviromenent.put(fasitResource.getScope().getEnvironment(), url);
        }
        return fasitUrlByEnviromenent;
    }
}
