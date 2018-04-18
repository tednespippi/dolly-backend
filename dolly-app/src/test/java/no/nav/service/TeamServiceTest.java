package no.nav.service;


import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import no.nav.api.request.CreateTeamRequest;
import no.nav.exceptions.DollyFunctionalException;
import no.nav.jpa.Bruker;
import no.nav.jpa.Team;
import no.nav.repository.BrukerRepository;
import no.nav.repository.TeamRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;

@RunWith(MockitoJUnitRunner.class)
public class TeamServiceTest {
	@Mock
	TeamRepository teamRepository;
	
	@Mock
	BrukerRepository brukerRepository;
	@InjectMocks
	TeamService teamService;
	
	Bruker eier = new Bruker("eierId");
	Team team = Team.builder().eier(eier).navn("teamnavn").datoOpprettet(LocalDateTime.now()).build();
	CreateTeamRequest createTeamRequest = new CreateTeamRequest(team.getNavn(), team.getBeskrivelse(), team.getEier()
			.getNavIdent());
	
	@Before
	public void setupMocks() {
		when(brukerRepository.findBrukerByNavIdent(anyString())).thenReturn(eier);
	}
	
	@Test
	public void shouldMapOgOppretteTeam() {
		teamService.opprettTeam(createTeamRequest);
		
		ArgumentCaptor<Team> argument = ArgumentCaptor.forClass(Team.class);
		Mockito.verify(teamRepository).save(argument.capture());
		assertEquals(team.getNavn(), argument.getValue().getNavn());
		assertEquals(team.getBeskrivelse(), argument.getValue().getBeskrivelse());
		assertEquals(team.getEier(), argument.getValue().getEier());
//		assertThat(argument.getValue().getDatoOpprettet(),is(leq(LocalDateTime.now()))); //FIXME
	}
	
	/**
	 * HVIS team eksisterer fra før SÅ skal funksjonell feilmelding kastes med beskrivende melding.
	 * HVIS NonTransientDataAccessException kastes fra teamRepository.save, SÅ skal funksjonell feilmelding kastes med en beskrivende melding.
	 */
	@Test(expected = DollyFunctionalException.class)
	public void shouldThrowFunctionalExceptionWhenDBConstraintIsViolated() {
		Exception e = new Exception("root");
		when(teamRepository.save(any())).thenThrow(new DuplicateKeyException("feilmelding",e));
		teamService.opprettTeam(createTeamRequest);
	}
}