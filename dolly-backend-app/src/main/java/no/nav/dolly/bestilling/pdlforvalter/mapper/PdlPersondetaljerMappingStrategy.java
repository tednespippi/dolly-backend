package no.nav.dolly.bestilling.pdlforvalter.mapper;

import static no.nav.dolly.bestilling.pdlforvalter.PdlForvalterClient.KILDE;
import static no.nav.dolly.bestilling.pdlforvalter.domain.PdlAdressebeskyttelse.AdresseBeskyttelse.FORTROLIG;
import static no.nav.dolly.bestilling.pdlforvalter.domain.PdlAdressebeskyttelse.AdresseBeskyttelse.STRENGT_FORTROLIG;
import static no.nav.dolly.bestilling.pdlforvalter.domain.PdlAdressebeskyttelse.AdresseBeskyttelse.UGRADERT;
import static no.nav.dolly.bestilling.pdlforvalter.domain.PdlFamilierelasjon.decode;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.springframework.stereotype.Component;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import no.nav.dolly.bestilling.pdlforvalter.domain.Kjoenn;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlAdressebeskyttelse;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlDoedsfall;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlFamilierelasjon;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlFoedsel;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlKjoenn;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlNavn;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlOpprettPerson;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlStatsborgerskap;
import no.nav.dolly.bestilling.pdlforvalter.domain.PdlTelefonnummer;
import no.nav.dolly.domain.resultset.tpsf.Person;
import no.nav.dolly.domain.resultset.tpsf.Relasjon;
import no.nav.dolly.domain.resultset.tpsf.Statsborgerskap;
import no.nav.dolly.mapper.MappingStrategy;

@Component
public class PdlPersondetaljerMappingStrategy implements MappingStrategy {

    @Override
    public void register(MapperFactory factory) {

        factory.classMap(Person.class, PdlOpprettPerson.class)
                .customize(new CustomMapper<Person, PdlOpprettPerson>() {
                    @Override
                    public void mapAtoB(Person person, PdlOpprettPerson pdlOpprettPerson, MappingContext context) {

                        pdlOpprettPerson.setOpprettetIdent(person.getIdent());
                    }
                })
                .register();

        factory.classMap(Person.class, PdlFoedsel.class)
                .customize(new CustomMapper<Person, PdlFoedsel>() {
                    @Override
                    public void mapAtoB(Person person, PdlFoedsel pdlFoedsel, MappingContext context) {

                        pdlFoedsel.setKilde(KILDE);
                    }
                })
                .byDefault()
                .register();

        factory.classMap(Person.class, PdlNavn.class)
                .customize(new CustomMapper<Person, PdlNavn>() {
                    @Override
                    public void mapAtoB(Person person, PdlNavn pdlNavn, MappingContext context) {

                        pdlNavn.setKilde(KILDE);
                    }
                })
                .byDefault()
                .register();


        factory.classMap(Person.class, PdlKjoenn.class)
                .customize(new CustomMapper<Person, PdlKjoenn>() {
                    @Override
                    public void mapAtoB(Person person, PdlKjoenn pdlKjoenn, MappingContext context) {

                        pdlKjoenn.setKjoenn(Kjoenn.decode(person.getKjonn()));
                        pdlKjoenn.setKilde(KILDE);
                    }
                })
                .register();

        factory.classMap(Statsborgerskap.class, PdlStatsborgerskap.class)
                .customize(new CustomMapper<Statsborgerskap, PdlStatsborgerskap>() {
                    @Override
                    public void mapAtoB(Statsborgerskap statsborgerskap, PdlStatsborgerskap pdlStatsborgerskap, MappingContext context) {

                        pdlStatsborgerskap.setLandkode(statsborgerskap.getStatsborgerskap());
                        pdlStatsborgerskap.setKilde(KILDE);
                    }
                })
                .register();

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

        factory.classMap(Person.class, PdlTelefonnummer.class)
                .customize(new CustomMapper<Person, PdlTelefonnummer>() {
                    @Override
                    public void mapAtoB(Person person, PdlTelefonnummer telefonnummer, MappingContext context) {

                        if (isNotBlank(person.getTelefonnummer_1())) {
                            telefonnummer.getTelfonnumre().add(PdlTelefonnummer.Entry.builder()
                                    .kilde(KILDE)
                                    .prioritet(1)
                                    .landskode(person.getTelefonLandskode_1())
                                    .nummer(person.getTelefonnummer_1())
                                    .build());
                        }
                        if (isNotBlank(person.getTelefonnummer_2())) {
                            telefonnummer.getTelfonnumre().add(PdlTelefonnummer.Entry.builder()
                                    .kilde(KILDE)
                                    .prioritet(2)
                                    .landskode(person.getTelefonLandskode_2())
                                    .nummer(person.getTelefonnummer_2())
                                    .build());
                        }
                    }
                })
                .register();

        factory.classMap(Person.class, PdlAdressebeskyttelse.class)
                .customize(new CustomMapper<Person, PdlAdressebeskyttelse>() {
                    @Override
                    public void mapAtoB(Person person, PdlAdressebeskyttelse adressebeskyttelse, MappingContext context) {

                        if ("SPSF".equals(person.getSpesreg())) {
                            adressebeskyttelse.setGradering(STRENGT_FORTROLIG);

                        } else if ("SPFO".equals(person.getSpesreg())) {
                            adressebeskyttelse.setGradering(FORTROLIG);

                        } else {
                            adressebeskyttelse.setGradering(UGRADERT);
                        }

                        adressebeskyttelse.setKilde(KILDE);
                    }
                })
                .register();


        factory.classMap(Person.class, PdlDoedsfall.class)
                .customize(new CustomMapper<Person, PdlDoedsfall>() {
                    @Override
                    public void mapAtoB(Person person, PdlDoedsfall pdlDoedsfall, MappingContext context) {

                        pdlDoedsfall.setKilde(KILDE);
                    }
                })
                .byDefault()
                .register();
    }
}