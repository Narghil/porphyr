package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Scope("prototype")
public class DeveloperService {

    private DeveloperRepository developerRepository;

    @Autowired
    public void setDeveloperRepository(DeveloperRepository developerRepository) {
        this.developerRepository = developerRepository;
    }

    public @NonNull List<DeveloperEntity> getDevelopers() {
        return developerRepository.findAll();
    }

    public void insertNewDeveloper(final @NonNull DeveloperEntity newDeveloperEntity) {
        developerRepository.save(newDeveloperEntity);
    }

    public void modifyDeveloper(final @NonNull DeveloperEntity modifiedDeveloper) {
        Long developerID = modifiedDeveloper.getId();
        if( developerID != null) {
            developerRepository.save( modifiedDeveloper );
        } else {
            throw( new ServiceException( ServiceException.Exceptions.DEVELOPER_NOT_SAVED_CANT_MODIFY) );
        }
    }

    public void deleteDeveloper(final @NonNull DeveloperEntity developerEntity) {
        Long developerID = developerEntity.getId();
        if( developerID != null) {
            developerRepository.deleteById( developerID );
        } else {
            throw( new ServiceException( ServiceException.Exceptions.DEVELOPER_NOT_SAVED_CANT_DELETE) );
        }
    }

    public void deleteAllDevelopers() {
        developerRepository.deleteAll();
    }

}
