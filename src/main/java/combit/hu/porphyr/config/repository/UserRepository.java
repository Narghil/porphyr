package combit.hu.porphyr.config.repository;

import combit.hu.porphyr.config.domain.UserEntity;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    @Nullable UserEntity findByLoginName( final @NonNull String loginName );
    @Nullable UserEntity findAllById( final @NonNull Long userId );
    @NonNull List<UserEntity> findAll();
    void saveAndFlush( final @NonNull UserEntity userEntity);
    @NonNull List<UserEntity> findAllByLoginNameAndIdNot(final @NonNull String loginName, final @NonNull Long id);

    /* Példa saját törlő eljárásra. Az SQL csak natív lehet, mert a tábla itt can't be mapped.
    @Modifying
    @Query( value = "DELETE FROM Users WHERE id = :id", nativeQuery = true)
    void deleteById(@Param("id") @NonNull Long id)
     */

}
