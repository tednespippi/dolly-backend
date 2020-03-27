package no.nav.dolly.bestilling.pdlforvalter.mapper;

import static java.util.Objects.isNull;
import static no.nav.dolly.bestilling.pdlforvalter.PdlForvalterClient.KILDE;
import static no.nav.dolly.bestilling.pdlforvalter.domain.PdlFamilierelasjon.*;
import static no.nav.dolly.bestilling.pdlforvalter.domain.PdlSivilstand.Sivilstand.ENKE_ELLER_ENKEMANN;
import static no.nav.dolly.bestilling.pdlforvalter.domain.PdlSivilstand.Sivilstand.GIFT;
import static no.nav.dolly.bestilling.pdlforvalter.domain.PdlSivilstand.Sivilstand.GJENLEVENDE_PARTNER;
import static no.nav.dolly.bestilling.pdlforvalter.domain.PdlSivilstand.Sivilstand.REGISTRERT_PARTNER;
import static no.nav.dolly.bestilling.pdlforvalter.domain.PdlSivilstand.Sivilstand.SEPARERT;
import static no.nav.dolly.bestilling.pdlforvalter.domain.PdlSivilstand.Sivilstand.SEPARERT_PARTNER;
import static no.nav.dolly.bestilling.pdlforvalter.domain.PdlSivilstand.Sivilstand.SKILT;
import static no.nav.dolly.bestilling.pdlforvalter.domain.PdlSivilstand.Sivilstand.SKILT_PARTNER;
import static no.nav.dolly.bestilling.pdlforvalter.domain.PdlSivilstand.Sivilstand.UGIFT;
import static no.nav.dolly.bestilling.pdlforvalter.domain.PdlSivilstand.Sivilstand.UOPPGITT;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlFamilierelasjon;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlSivilstand;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlSivilstand.Sivilstand;
import no.nav.dolly.domain.resultset.tpsf.Person;
import no.nav.dolly.domain.resultset.tpsf.Relasjon;
import no.nav.dolly.domain.resultset.tpsf.Sivilstand.Sivilstatus;
import no.nav.dolly.mapper.MappingStrategy;

@Component
public class PdlRelasjonerMappingStrategy implements MappingStrategy {

    @Override
    public void register(MapperFactory factory) {

        factory.classMap(Relasjon.class, PdlFamilierelasjon.class)
                .customize(new CustomMapper<Relasjon, PdlFamilierelasjon>() {
                    @Override
                    public void mapAtoB(Relasjon relasjon, PdlFamilierelasjon familierelasjon, MappingContext context) {

                        familierelasjon.setRelatertPerson(relasjon.getPersonRelasjonMed().getIdent());
                        familierelasjon.setRelatertPersonsRolle(decode(relasjon.getRelasjonTypeNavn()));
                        relasjon.getPersonRelasjonTil().getRelasjoner().forEach(relasjon1 -> {
                            if (relasjon1.getPersonRelasjonMed().getIdent().equals(relasjon.getPerson().getIdent())) {
                                familierelasjon.setMinRolleForPerson(decode(relasjon1.getRelasjonTypeNavn()));
                            }
                        });
                        familierelasjon.setKilde(KILDE);
                    }
                })
                .register();

        factory.classMap(Person.class, PdlSivilstand.class)
                .customize(new CustomMapper<Person, PdlSivilstand>() {
                    @Override
                    public void mapAtoB(Person person, PdlSivilstand sivilstand, MappingContext context) {

                        sivilstand.setType(mapSivilstand(person.getSivilstand()));
                        sivilstand.setSivilstandsdato(mapperFacade.map(person.getSivilstandRegdato(), LocalDate.class));
                        sivilstand.setRelatertVedSivilstand(person.getRelasjoner().stream()
                                .filter(Relasjon::isPartner)
                                .map(Relasjon::getPersonRelasjonMed)
                                .filter(Person::isSivilstandGift)
                                .map(Person::getIdent)
                                .findFirst().orElse(null));
                        sivilstand.setKilde(KILDE);
                    }

                    private Sivilstand mapSivilstand(Sivilstatus sivilstatus) {

                        if (isNull(sivilstatus)) {
                            return UOPPGITT;
                        } else {
                            switch (sivilstatus) {
                            case UGIF:
                                return UGIFT;
                            case GIFT:
                                return GIFT;
                            case ENKE:
                                return ENKE_ELLER_ENKEMANN;
                            case SKIL:
                                return SKILT;
                            case SEPR:
                                return SEPARERT_PARTNER;
                            case REPA:
                                return REGISTRERT_PARTNER;
                            case SEPA:
                                return SEPARERT;
                            case SKPA:
                                return SKILT_PARTNER;
                            case GJPA:
                                return GJENLEVENDE_PARTNER;
                            case SAMB:
                            default:
                                return UOPPGITT;
                            }
                        }
                    }
                })
                .register();
    }
}