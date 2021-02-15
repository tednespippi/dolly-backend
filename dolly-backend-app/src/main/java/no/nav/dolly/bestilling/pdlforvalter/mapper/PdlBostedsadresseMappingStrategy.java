package no.nav.dolly.bestilling.pdlforvalter.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlBostedadresse;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlBostedsadresseHistorikk;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlMatrikkeladresse;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlVegadresse;
import no.nav.dolly.domain.resultset.tpsf.Person;
import no.nav.dolly.domain.resultset.tpsf.adresse.BoAdresse;
import no.nav.dolly.mapper.MappingStrategy;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.dolly.bestilling.pdlforvalter.mapper.PdlAdresseMappingStrategy.getCoadresse;
import static no.nav.dolly.bestilling.pdlforvalter.mapper.PdlAdresseMappingStrategy.getDato;
import static no.nav.dolly.domain.CommonKeysAndUtils.CONSUMER;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;

@Component
public class PdlBostedsadresseMappingStrategy implements MappingStrategy {

    private static PdlBostedadresse prepBoadresse(BoAdresse adresse) {

        PdlBostedadresse bostedadresse = new PdlBostedadresse();
        bostedadresse.setKilde(CONSUMER);
        bostedadresse.setGyldigFraOgMed(getDato(adresse.getFlyttedato()));
        bostedadresse.setCoAdressenavn(getCoadresse(adresse));
        return bostedadresse;
    }

    @Override
    public void register(MapperFactory factory) {

        factory.classMap(Person.class, PdlBostedsadresseHistorikk.class)
                .customize(new CustomMapper<>() {
                    @Override
                    public void mapAtoB(Person person, PdlBostedsadresseHistorikk historikk, MappingContext context) {

                        historikk.getPdlAdresser().addAll(
                                Stream.of(
                                        person.getBoadresse().stream()
                                                .filter(boAdresse -> isNotTrue(boAdresse.getDeltAdresse()) &&
                                                        person.isUtenFastBopel())
                                                .map(boAdresse -> {
                                                    PdlBostedadresse bostedadresse = prepBoadresse(boAdresse);
                                                    bostedadresse.setUkjentBosted(PdlBostedadresse.UkjentBosted.builder()
                                                            .bostedskommune(boAdresse.getKommunenr())
                                                            .build());
                                                    return bostedadresse;
                                                })
                                                .collect(Collectors.toList()),
                                        person.getBoadresse().stream()
                                                .filter(boAdresse -> isNotTrue(boAdresse.getDeltAdresse()) &&
                                                        boAdresse.isGateadresse())
                                                .map(boAdresse -> {
                                                    PdlBostedadresse bostedadresse = prepBoadresse(boAdresse);
                                                    bostedadresse.setVegadresse(mapperFacade.map(
                                                            boAdresse, PdlVegadresse.class));
                                                    return bostedadresse;
                                                })
                                                .collect(Collectors.toList()),
                                        person.getBoadresse().stream()
                                                .filter(boAdresse -> isNotTrue(boAdresse.getDeltAdresse()) &&
                                                        boAdresse.isMatrikkeladresse())
                                                .map(boAdresse -> {
                                                    PdlBostedadresse bostedadresse = prepBoadresse(boAdresse);
                                                    bostedadresse.setMatrikkeladresse(mapperFacade.map(
                                                            boAdresse, PdlMatrikkeladresse.class));
                                                    return bostedadresse;
                                                })
                                                .collect(Collectors.toList())
                                )
                                        .flatMap(Collection::stream)
                                        .collect(Collectors.toList())
                        );
                    }
                })
                .register();
    }
}
