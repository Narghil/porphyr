package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.UserEntity;
import combit.hu.porphyr.repository.UserRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserServiceIF, UserDetailsService {

    private final @NonNull UserRepository userRepository;

    @Autowired
    public UserService(final @NonNull UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = findByLoginName(username);
        if( user == null ){
            throw new UsernameNotFoundException(username);
        }
        return new UserDetailsImp(user);
    }

    @Override
    public UserEntity findByLoginName(final String loginName) {
        return userRepository.findByLoginName(loginName);
    }
}
