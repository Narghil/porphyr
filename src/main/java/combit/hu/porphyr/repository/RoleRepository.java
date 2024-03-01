package combit.hu.porphyr.repository;

import combit.hu.porphyr.domain.RoleEntity;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {
    @Nullable RoleEntity findByRole( final @NonNull String roleName );
    @Nullable RoleEntity findAllById( final @NonNull Long roleId );
    @NonNull List<RoleEntity> findAll();
    void saveAndFlush( final @NonNull RoleEntity roleEntity);
    @NonNull List<RoleEntity> findAllByRoleAndIdNot(final @NonNull String roleName, final @NonNull Long id);
}
