package no.nav.repository;

import no.nav.jpa.Bruker;
import org.springframework.data.repository.Repository;

public interface BrukerRepository extends Repository<Bruker, Long>  {
	void save(Bruker bruker);
	
	Bruker findBrukerByNavIdent(String navIdent);
}