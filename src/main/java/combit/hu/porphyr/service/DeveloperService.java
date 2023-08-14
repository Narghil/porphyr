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

    /**
    * Hibalehetőségek: <br/>
    * - Nincs kitöltve a név <br/>
    * - Már van ilyen nevű fejlesztő <br/>
    */
    public void insertNewDeveloper(final @NonNull DeveloperEntity newDeveloperEntity) {
        if (newDeveloperEntity.getName().isEmpty()){
            throw( new ServiceException( ServiceException.Exceptions.DEVELOPER_WITH_EMPTY_NAME_CANT_INSERT) );
        } else if ( ! developerRepository.findAllByName(newDeveloperEntity.getName()).isEmpty() ) {
            throw( new ServiceException( ServiceException.Exceptions.DEVELOPER_WITH_SAME_NAME_CANT_INSERT) );
        } else {
            developerRepository.save(newDeveloperEntity);
        }
    }

    /**
     * Hibalehetőségek: <br/>
     * - Nincs kitöltve az ID <br/>
     * - Nincs kitöltve a név <br/>
     * - Már van ilyen nevű fejlesztő <br/>
     */
    public void modifyDeveloper(final @NonNull DeveloperEntity modifiedDeveloper) {
        if( modifiedDeveloper.getId() == null) {
            throw (new ServiceException(ServiceException.Exceptions.DEVELOPER_NOT_SAVED_CANT_MODIFY));
        } else if( modifiedDeveloper.getName().isEmpty()) {
            throw (new ServiceException(ServiceException.Exceptions.DEVELOPER_WITH_EMPTY_NAME_CANT_MODIFY));
        } else if ( ! developerRepository.findAllByName(modifiedDeveloper.getName()).isEmpty() ) {
            throw( new ServiceException( ServiceException.Exceptions.DEVELOPER_WITH_SAME_NAME_CANT_MODIFY) );
        } else {
            developerRepository.save(modifiedDeveloper);
        }
    }

    /**
     * Hibalehetőségek: <br/>
     * - Nincs kitöltve az ID <br/>
     * - A fejlesztő hozzá van rendelve egy vagy több projekthez
     */
    public void deleteDeveloper(final @NonNull DeveloperEntity developerEntity) {
        Long developerID = developerEntity.getId();
        DeveloperEntity actualDeveloperEntity;
        if( developerID != null) {
            actualDeveloperEntity = developerRepository.findAllById( developerEntity.getId() );
            if (actualDeveloperEntity.getDeveloperProjects().isEmpty()) {
                developerRepository.deleteById(developerID);
            } else {
                throw( new ServiceException( ServiceException.Exceptions.DEVELOPER_WITH_PROJECTS_CANT_DELETE) );
            }
        } else {
            throw( new ServiceException( ServiceException.Exceptions.DEVELOPER_NOT_SAVED_CANT_DELETE) );
        }
    }

}
