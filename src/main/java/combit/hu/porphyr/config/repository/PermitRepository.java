package combit.hu.porphyr.config.repository;

import combit.hu.porphyr.config.domain.PermitEntity;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermitRepository extends CrudRepository<PermitEntity, Long> {
    @Nullable
    PermitEntity findByName(final @NonNull String permitName);

    @Nullable
    PermitEntity findAllById(final @NonNull Long permitId);

    @NonNull
    List<PermitEntity> findAll();

    void saveAndFlush(final @NonNull PermitEntity permitEntity);
}
