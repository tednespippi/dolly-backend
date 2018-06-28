package no.nav.service;

import ma.glasnost.orika.MapperFacade;
import no.nav.dolly.repository.BrukerRepository;
import no.nav.dolly.repository.TeamRepository;
import no.nav.dolly.repository.TestGruppeRepository;
import no.nav.exceptions.ConstraintViolationException;
import no.nav.exceptions.DollyFunctionalException;
import no.nav.exceptions.NotFoundException;
import no.nav.freg.security.oidc.common.OidcTokenAuthentication;
import no.nav.jpa.Bruker;
import no.nav.jpa.Team;
import no.nav.jpa.Testgruppe;
import no.nav.jpa.Testident;
import no.nav.resultSet.RsBrukerMedTeamsOgFavoritter;
import no.nav.resultSet.RsOpprettTestgruppe;
import no.nav.resultSet.RsTestgruppe;
import no.nav.resultSet.RsTestident;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestgruppeService {

    @Autowired
    private TestGruppeRepository testGruppeRepository;

    @Autowired
    private BrukerService brukerService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private MapperFacade mapperFacade;

    public RsTestgruppe opprettTestgruppe(RsOpprettTestgruppe rsTestgruppe) {
        Team team = teamService.fetchTeamById(rsTestgruppe.getTeamId());

        OidcTokenAuthentication auth = (OidcTokenAuthentication) SecurityContextHolder.getContext().getAuthentication();
        Bruker bruker = brukerService.fetchBruker(auth.getPrincipal());

        Testgruppe gruppe = mapperFacade.map(rsTestgruppe, Testgruppe.class);
        gruppe.setTeamtilhoerighet(team);
        gruppe.setDatoEndret(LocalDate.now());
        gruppe.setSistEndretAv(bruker);
        gruppe.setOpprettetAv(bruker);

        Testgruppe savedGruppe = saveGruppeTilDB(gruppe);
        return mapperFacade.map(savedGruppe, RsTestgruppe.class);
    }

    public Testgruppe fetchTestgruppeById(Long gruppeId) {
        Optional<Testgruppe> testgruppe = testGruppeRepository.findById(gruppeId);

        if (testgruppe.isPresent()) {
            return testgruppe.get();
        }

        throw new NotFoundException("Finner ikke gruppe basert på gruppeID: " + gruppeId);
    }

    public Set<RsTestgruppe> fetchTestgrupperByTeammedlemskapAndFavoritterOfBruker(String navIdent) {
        RsBrukerMedTeamsOgFavoritter brukerinfo = brukerService.getBrukerMedTeamsOgFavoritter(navIdent);
        Set<RsTestgruppe> testgrupper = brukerinfo.getBruker().getFavoritter();
        brukerinfo.getTeams().forEach(team -> testgrupper.addAll(team.getGrupper()));

        return testgrupper;
    }

    public void saveAllIdenterToTestgruppe(Long gruppeId, Collection<RsTestident> testidenter) {
        Testgruppe testgruppe = fetchTestgruppeById(gruppeId);

        List<Testident> identer = mapperFacade.mapAsList(testidenter, Testident.class);
        testgruppe.getTestidenter().addAll(identer);

        saveGruppeTilDB(testgruppe);
    }

    public Testgruppe saveGruppeTilDB(Testgruppe testgruppe) {
        try {
            return testGruppeRepository.save(testgruppe);
        } catch (DataIntegrityViolationException e) {
            throw new ConstraintViolationException("En Testgruppe DB constraint er brutt! Kan ikke lagre testgruppe. Error: " + e.getMessage());
        } catch (NonTransientDataAccessException e) {
            throw new DollyFunctionalException(e.getRootCause().getMessage(), e);
        }
    }

    @Transactional
    public RsTestgruppe oppdaterTestgruppe(Long gruppeId, RsTestgruppe testgruppe) {
        Testgruppe savedGruppe = fetchTestgruppeById(gruppeId);
        Testgruppe requestGruppe = mapperFacade.map(testgruppe, Testgruppe.class);

        OidcTokenAuthentication auth = (OidcTokenAuthentication) SecurityContextHolder.getContext().getAuthentication();
        Bruker bruker = brukerService.fetchBruker(auth.getPrincipal());

        savedGruppe.setOpprettetAv(requestGruppe.getOpprettetAv());
        savedGruppe.setHensikt(requestGruppe.getHensikt());
        savedGruppe.setNavn(requestGruppe.getNavn());
        savedGruppe.setTestidenter(requestGruppe.getTestidenter());
        savedGruppe.setTeamtilhoerighet(requestGruppe.getTeamtilhoerighet());
        savedGruppe.setFavorisertAv(requestGruppe.getFavorisertAv());
        savedGruppe.setSistEndretAv(bruker);
        savedGruppe.setDatoEndret(LocalDate.now());

        Testgruppe endretGruppe = saveGruppeTilDB(savedGruppe);

        return mapperFacade.map(endretGruppe, RsTestgruppe.class);
    }

    public List<Testgruppe> fetchAlleTestgrupper() {
        return testGruppeRepository.findAll();
    }

}
