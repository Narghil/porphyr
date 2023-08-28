package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Service
@Transactional
@Scope("prototype")
public class DeveloperService {

    @Autowired
    @Setter
    private EntityManager entityManager;

    @Autowired
    @Setter
    private DeveloperRepository developerRepository;

    /**
     * Hibalehetőségek: <br/>
     * - Nincs kitöltve a név <br/>
     * - Már van ilyen nevű fejlesztő <br/>
     */
    public void insertNewDeveloper(final @NonNull DeveloperEntity newDeveloperEntity) {
        if (newDeveloperEntity.getName() == null || newDeveloperEntity.getName().isEmpty()) {
            throw (new ServiceException(ServiceException.Exceptions.DEVELOPER_INSERT_EMPTY_NAME));
        } else if (!developerRepository.findAllByName(newDeveloperEntity.getName()).isEmpty()) {
            throw (new ServiceException(ServiceException.Exceptions.DEVELOPER_INSERT_SAME_NAME));
        } else {
            developerRepository.saveAndFlush(newDeveloperEntity);
        }
    }

    /**
     * Hibalehetőségek: <br/>
     * - Nincs kitöltve az ID <br/>
     * - Nincs kitöltve a név <br/>
     * - Van másik ilyen nevű fejlesztő <br/>
     */
    public void modifyDeveloper(final @NonNull DeveloperEntity modifiedDeveloper) {
        entityManager.detach(modifiedDeveloper);
        if (modifiedDeveloper.getId() == null) {
            throw (new ServiceException(ServiceException.Exceptions.DEVELOPER_MODIFY_NOT_SAVED));
        } else if (modifiedDeveloper.getName().isEmpty()) {
            throw (new ServiceException(ServiceException.Exceptions.DEVELOPER_MODIFY_EMPTY_NAME));
        } else if (!developerRepository.findAllByNameAndIdNot(
            modifiedDeveloper.getName(), modifiedDeveloper.getId()).isEmpty()) {
            throw (new ServiceException(ServiceException.Exceptions.DEVELOPER_MODIFY_SAME_NAME));
        } else {
            developerRepository.saveAndFlush(modifiedDeveloper);
        }
    }

    /**
     * Hibalehetőségek: <br/>
     * - Nincs kitöltve az ID <br/>
     * - A fejlesztő hozzá van rendelve egy vagy több projekthez
     */
    public void deleteDeveloper(final @NonNull DeveloperEntity developerEntity) {
        Long developerID = developerEntity.getId();
        if (developerID == null) {
            throw (new ServiceException(ServiceException.Exceptions.DEVELOPER_DELETE_NOT_SAVED));
        } else {
            DeveloperEntity actualDeveloperData = developerRepository.findAllById(developerID);
            if (actualDeveloperData == null) {
                throw (new ServiceException(ServiceException.Exceptions.UNDEFINED));
            } else {
                if (!actualDeveloperData.getDeveloperProjects().isEmpty()) {
                    throw (new ServiceException(ServiceException.Exceptions.DEVELOPER_DELETE_ASSIGNED_TO_PROJECTS));
                } else {
                    developerRepository.deleteById(developerID);
                    entityManager.flush();
                }
            }
        }
    }

    //------------- Lekérdezések -----------------

    public @NonNull List<DeveloperEntity> getDevelopers() {
        return developerRepository.findAll();
    }

    public @Nullable DeveloperEntity getDeveloperById(final @NonNull Long id) {
        return developerRepository.findAllById(id);
    }

    public @Nullable DeveloperEntity getDeveloperByName(final @NonNull String name) {
        List<DeveloperEntity> namedDevelopers = developerRepository.findAllByName(name);
        if (namedDevelopers.isEmpty()) {
            return null;
        } else {
            return namedDevelopers.get(0);
        }
    }
}
