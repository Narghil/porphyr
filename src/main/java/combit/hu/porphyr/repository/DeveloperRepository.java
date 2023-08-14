package combit.hu.porphyr.repository;

import java.util.List;

import combit.hu.porphyr.domain.DeveloperEntity;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DeveloperRepository extends CrudRepository<DeveloperEntity, Long> {

    @NonNull List<DeveloperEntity> findAll();
    @NonNull DeveloperEntity findAllById(Long id);
    @NonNull List<DeveloperEntity> findAllByName(String name);
}
