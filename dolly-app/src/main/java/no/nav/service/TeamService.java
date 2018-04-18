package no.nav.service;

import no.nav.api.request.CreateTeamRequest;
import no.nav.exceptions.DollyFunctionalException;
import no.nav.jpa.Bruker;
import no.nav.jpa.Team;
import no.nav.repository.BrukerRepository;
import no.nav.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TeamService {
	
	@Autowired
	TeamRepository teamRepository;
	@Autowired
	BrukerRepository brukerRepository;
	
	public Team opprettTeam(CreateTeamRequest createTeamRequest) {
		Bruker eier = findBrukerByNavIdent(createTeamRequest.getEierensNavIdent());
		Team team = Team.builder()
				.navn(createTeamRequest.getNavn())
				.beskrivelse(createTeamRequest.getBeskrivelse())
				.eier(eier)
				.datoOpprettet(LocalDateTime.now()).build();
		
		return saveToTeamRepository(team);
	}
	
	private Bruker findBrukerByNavIdent(String eierensNavIdent) {
		Bruker eier = brukerRepository.findBrukerByNavIdent(eierensNavIdent);
		if (eier == null) {
			throw new DollyFunctionalException("Eierens NAV-Ident eksisterer ikke i Dolly databasen.");
		}
		return eier;
	}
	
	private Team saveToTeamRepository(Team team) {
		try {
			return teamRepository.save(team);
		} catch (NonTransientDataAccessException e) {
			throw new DollyFunctionalException(e.getRootCause().getMessage(), e);
		}
	}
}