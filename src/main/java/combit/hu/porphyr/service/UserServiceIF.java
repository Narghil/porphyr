package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.UserEntity;

public interface UserServiceIF {
    UserEntity findByLoginName( String loginName );
}
