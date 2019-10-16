package no.nav.dolly.provider.api;

import static java.lang.String.format;
import static no.nav.dolly.config.CachingConfig.CACHE_TEAM;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import no.nav.dolly.domain.resultset.entity.team.RsOpprettTeam;
import no.nav.dolly.domain.resultset.entity.team.RsTeam;
import no.nav.dolly.domain.resultset.entity.team.RsTeamUtvidet;
import no.nav.dolly.exceptions.NotFoundException;
import no.nav.dolly.service.TeamService;
import no.nav.dolly.service.TestgruppeService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Transactional
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/team")
public class TeamController {

    private final TeamService teamService;
    private final TestgruppeService testgruppeService;

    @Cacheable(CACHE_TEAM)
    @GetMapping
    @ApiOperation("Hent alle Team med tilknyttet medlem med navIdent")
    public List<RsTeam> getTeams(@RequestParam(value = "navIdent", required = false) String navIdent) {
        return Optional.ofNullable(navIdent)
                .map(teamService::fetchTeamsByMedlemskapInTeamsMapped)
                .orElse(teamService.findAllOrderByNavn());
    }

    @Cacheable(CACHE_TEAM)
    @GetMapping("/{teamId}")
    @ApiOperation("Hent Team med teamId")
    public RsTeamUtvidet fetchTeamById(@PathVariable("teamId") Long teamid) {
        return teamService.getTeamById(teamid);
    }

    @CacheEvict(value = CACHE_TEAM, allEntries = true)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Opprett Team")
    public RsTeamUtvidet opprettTeam(@RequestBody RsOpprettTeam createTeamRequest) {
        return teamService.opprettTeam(createTeamRequest);
    }

    @CacheEvict(value = CACHE_TEAM, allEntries = true)
    @PutMapping("/{teamId}/leggTilMedlemmer")
    @ApiOperation("Legg til Brukere som medlemmer i Team")
    public RsTeamUtvidet addBrukereSomTeamMedlemmerByNavidenter(@PathVariable("teamId") Long teamId, @RequestBody List<String> navIdenter) {
        return teamService.addMedlemmerByNavidenter(teamId, navIdenter);
    }

    @CacheEvict(value = CACHE_TEAM, allEntries = true)
    @PutMapping("/{teamId}/fjernMedlemmer")
    @ApiOperation("Fjern Brukere som medlemmer i Team")
    public RsTeamUtvidet fjernBrukerefraTeam(@PathVariable("teamId") Long teamId, @RequestBody List<String> navIdenter) {
        return teamService.fjernMedlemmer(teamId, navIdenter);
    }

    @CacheEvict(value = CACHE_TEAM, allEntries = true)
    @PutMapping("/{teamId}")
    @ApiOperation("Oppdater informasjon om Team")
    public RsTeamUtvidet endreTeaminfo(@PathVariable("teamId") Long teamId, @RequestBody RsTeamUtvidet createTeamRequest) {
        return teamService.updateTeamInfo(teamId, createTeamRequest);
    }

    @CacheEvict(value = CACHE_TEAM, allEntries = true)
    @DeleteMapping("/{teamId}/deleteMedlem")
    @ApiOperation("Fjern Bruker som medlem i Team")
    public RsTeamUtvidet deleteMedlemfraTeam(@PathVariable("teamId") Long teamId, @RequestParam String navIdent) {
        return teamService.slettMedlem(teamId, navIdent);
    }

    @CacheEvict(value = CACHE_TEAM, allEntries = true)
    @DeleteMapping("/{teamId}")
    @ApiOperation("Slett Team")
    public void deleteTeam(@PathVariable("teamId") Long teamId) {
        if (teamService.deleteTeam(teamId) == 0) {
            throw new NotFoundException(format("Team med id %d ble ikke funnet.", teamId));
        }
    }
}