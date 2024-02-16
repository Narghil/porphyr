package combit.hu.porphyr.repository;

import combit.hu.porphyr.domain.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    UserEntity findByLoginName( String loginName );
}
