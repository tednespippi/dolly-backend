package no.nav.dolly.mapper.strategy;

import static no.nav.dolly.mapper.BestillingAaregStatusMapper.buildAaregStatusMap;
import static no.nav.dolly.mapper.BestillingArenaforvalterStatusMapper.buildArenaStatusMap;
import static no.nav.dolly.mapper.BestillingBregStubStatusMapper.buildBregStubStatusMap;
import static no.nav.dolly.mapper.BestillingInntektsmeldingStatusMapper.buildInntektsmeldingStatusMap;
import static no.nav.dolly.mapper.BestillingInntektstubStatusMapper.buildInntektstubStatusMap;
import static no.nav.dolly.mapper.BestillingInstdataStatusMapper.buildInstdataStatusMap;
import static no.nav.dolly.mapper.BestillingKrrStubStatusMapper.buildKrrStubStatusMap;
import static no.nav.dolly.mapper.BestillingPdlForvalterStatusMapper.buildPdldataStatusMap;
import static no.nav.dolly.mapper.BestillingPensjonforvalterStatusMapper.buildPensjonforvalterStatusMap;
import static no.nav.dolly.mapper.BestillingSigrunStubStatusMapper.buildSigrunStubStatusMap;
import static no.nav.dolly.mapper.BestillingTpsfStatusMapper.buildTpsfStatusMap;
import static no.nav.dolly.mapper.BestillingUdiStubStatusMapper.buildUdiStubStatusMap;

import java.util.Arrays;
import java.util.Iterator;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import no.nav.dolly.domain.jpa.Bestilling;
import no.nav.dolly.domain.jpa.BestillingProgress;
import no.nav.dolly.domain.resultset.RsDollyBestillingRequest;
import no.nav.dolly.domain.resultset.entity.bestilling.RsBestillingStatus;
import no.nav.dolly.mapper.MappingStrategy;

@Slf4j
@Component
@RequiredArgsConstructor
public class BestillingStatusMappingStrategy implements MappingStrategy {

    private final JsonBestillingMapper jsonBestillingMapper;

    @Override public void register(MapperFactory factory) {
        factory.classMap(Bestilling.class, RsBestillingStatus.class)
                .customize(new CustomMapper<Bestilling, RsBestillingStatus>() {
                    @Override public void mapAtoB(Bestilling bestilling, RsBestillingStatus bestillingStatus, MappingContext context) {

                        Iterator<BestillingProgress> it = bestilling.getProgresser().iterator();
                        while (it.hasNext()) {
                            String ident = it.next().getIdent();
                            if (!bestilling.getGruppe().getTestidenter().stream()
                                    .anyMatch(testident -> testident.getIdent().equals(ident))) {
                                it.remove();
                            }
                        }

                        RsDollyBestillingRequest bestillingRequest = jsonBestillingMapper.mapBestillingRequest(bestilling.getBestKriterier());
                        bestillingStatus.setAntallLevert(bestilling.getProgresser().size());
                        bestillingStatus.setEnvironments(Arrays.asList(bestilling.getMiljoer().split(",")));
                        bestillingStatus.setGruppeId(bestilling.getGruppe().getId());
                        bestillingStatus.getStatus().addAll(buildTpsfStatusMap(bestilling.getProgresser()));
                        bestillingStatus.getStatus().addAll(buildKrrStubStatusMap(bestilling.getProgresser()));
                        bestillingStatus.getStatus().addAll(buildSigrunStubStatusMap(bestilling.getProgresser()));
                        bestillingStatus.getStatus().addAll(buildAaregStatusMap(bestilling.getProgresser()));
                        bestillingStatus.getStatus().addAll(buildArenaStatusMap(bestilling.getProgresser()));
                        bestillingStatus.getStatus().addAll(buildPdldataStatusMap(bestilling.getProgresser()));
                        bestillingStatus.getStatus().addAll(buildInstdataStatusMap(bestilling.getProgresser()));
                        bestillingStatus.getStatus().addAll(buildUdiStubStatusMap(bestilling.getProgresser()));
                        bestillingStatus.getStatus().addAll(buildInntektstubStatusMap(bestilling.getProgresser()));
                        bestillingStatus.getStatus().addAll(buildPensjonforvalterStatusMap(bestilling.getProgresser()));
                        bestillingStatus.getStatus().addAll(buildInntektsmeldingStatusMap(bestilling.getProgresser()));
                        bestillingStatus.getStatus().addAll(buildBregStubStatusMap(bestilling.getProgresser()));
                        bestillingStatus.setBestilling(RsBestillingStatus.RsBestilling.builder()
                                .pdlforvalter(bestillingRequest.getPdlforvalter())
                                .aareg(bestillingRequest.getAareg())
                                .krrstub(bestillingRequest.getKrrstub())
                                .arenaforvalter(bestillingRequest.getArenaforvalter())
                                .instdata(bestillingRequest.getInstdata())
                                .inntektstub(bestillingRequest.getInntektstub())
                                .sigrunstub(bestillingRequest.getSigrunstub())
                                .udistub(bestillingRequest.getUdistub())
                                .pensjonforvalter(bestillingRequest.getPensjonforvalter())
                                .inntektsmelding(bestillingRequest.getInntektsmelding())
                                .bregstub(bestillingRequest.getBregstub())
                                .tpsf(jsonBestillingMapper.mapTpsfRequest(bestilling.getTpsfKriterier()))
                                .build());
                    }
                })
                .byDefault()
                .register();
    }
}
