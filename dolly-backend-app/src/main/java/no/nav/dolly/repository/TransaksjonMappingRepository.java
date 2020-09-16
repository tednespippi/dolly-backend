package no.nav.dolly.repository;

import no.nav.dolly.domain.jpa.TransaksjonMapping;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface TransaksjonMappingRepository extends Repository<TransaksjonMapping, Long> {

    TransaksjonMapping save(TransaksjonMapping transaksjonMapping);

    Optional<List<TransaksjonMapping>> findAllByIdent(String ident);

    Optional<List<TransaksjonMapping>> findAllBySystemAndIdent(String system, String ident);

    @Query(value = "from TransaksjonMapping t where (t.bestillingId is null or " +
            "(t.bestillingId is not null and t.bestillingId=:bestillingId)) and " +
            "(t.ident is null or (t.ident is not null and t.ident=:ident))")
    Optional<List<TransaksjonMapping>> findAllByBestillingIdAndIdent(Long bestillingId, String ident);
}