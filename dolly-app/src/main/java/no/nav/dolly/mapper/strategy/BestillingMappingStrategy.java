package no.nav.dolly.mapper.strategy;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.nonNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import no.nav.dolly.domain.jpa.Bestilling;
import no.nav.dolly.domain.jpa.BestillingProgress;
import no.nav.dolly.domain.resultset.RsBestilling;
import no.nav.dolly.domain.resultset.RsIdentStatus;
import no.nav.dolly.mapper.MappingStrategy;

@Component
public class BestillingMappingStrategy implements MappingStrategy {

    private static final String SUCCESS = "OK";

    @Override public void register(MapperFactory factory) {
        factory.classMap(Bestilling.class, RsBestilling.class)
                .customize(new CustomMapper<Bestilling, RsBestilling>() {
                    @Override public void mapAtoB(Bestilling bestilling, RsBestilling rsBestilling, MappingContext context) {
                        rsBestilling.setEnvironments(Arrays.asList(bestilling.getMiljoer().split(",")));
                        rsBestilling.setGruppeId(bestilling.getGruppe().getId());
                        Map<String, Map<String, Set<String>>> errorEnvIdents = new HashMap<>();
                        bestilling.getProgresser().forEach(progress ->
                                prepStatus(errorEnvIdents, progress)
                            );
                        errorEnvIdents.keySet().forEach(env ->
                                rsBestilling.getStatus().add(RsIdentStatus.builder()
                                        .statusMelding(env)
                                        .environmentIdents(errorEnvIdents.get(env))
                                        .build())
                        );
                    }
                })
                .byDefault()
                .register();
    }

    private void prepStatus(Map<String, Map<String, Set<String>>> errorEnvIdents, BestillingProgress progress) {
        if (nonNull(progress.getFeil())) {
            newArrayList(progress.getFeil().split(",")).forEach(error -> {
                String environ = error.split(":", 2)[0];
                String errMsg = error.split(":", 2)[1].trim();
                checkNUpdateStatus(errorEnvIdents, progress.getIdent(), environ, errMsg);
            });
        }
        if (nonNull(progress.getTpsfSuccessEnv())) {
            newArrayList(progress.getTpsfSuccessEnv().split(",")).forEach(environ ->
                checkNUpdateStatus(errorEnvIdents, progress.getIdent(), environ, SUCCESS)
            );
        }

    }

    private void checkNUpdateStatus(Map<String, Map<String, Set<String>>> errorEnvIdents, String ident, String environ, String status) {
        if (errorEnvIdents.containsKey(status)) {
            if (errorEnvIdents.get(status).containsKey(environ)) {
                errorEnvIdents.get(status).get(environ).add(ident);
            } else {
                errorEnvIdents.get(status).put(environ, newHashSet(ident));
            }
        } else {
            Map<String, Set<String>> entry = new HashMap();
            entry.put(environ, newHashSet(ident));
            errorEnvIdents.put(status, entry);
        }
    }
}
