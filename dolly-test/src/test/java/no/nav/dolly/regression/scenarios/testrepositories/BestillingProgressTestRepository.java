package no.nav.dolly.regression.scenarios.testrepositories;

import no.nav.dolly.domain.jpa.BestillingProgress;
import org.springframework.data.repository.CrudRepository;

public interface BestillingProgressTestRepository extends CrudRepository<BestillingProgress, Long> {

    void deleteAll();

}