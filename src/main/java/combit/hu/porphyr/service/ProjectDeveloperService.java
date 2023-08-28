package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import combit.hu.porphyr.repository.ProjectDeveloperRepository;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.repository.ProjectTaskDeveloperRepository;
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
public class ProjectDeveloperService {

    @Autowired
    @Setter
    private EntityManager entityManager;

    @Autowired
    @Setter
    private ProjectDeveloperRepository projectDeveloperRepository;

    @Autowired
    @Setter
    private ProjectRepository projectRepository;

    @Autowired
    @Setter
    private ProjectTaskDeveloperRepository projectTaskDeveloperRepository;

    @Autowired
    @Setter
    private DeveloperRepository developerRepository;

    /**
     * Hibalehetőségek <br />
     * - projectEntity = null
     * - projectEntity.id = null
     * - projectEntity nincs az adatbázisban
     * - developerEntity = null
     * - developerEntity.id = nul
     * - developerEntity nincs az adatbázisban
     * - már van ilyen tétel
     */
    public void insertNewProjectDeveloper(final @NonNull ProjectDeveloperEntity newProjectDeveloperEntity) {
        @Nullable ServiceException serviceException = null;
        entityManager.detach(newProjectDeveloperEntity);

        if (newProjectDeveloperEntity.getProjectEntity().getId() == null) {
            serviceException = new ServiceException(ServiceException.Exceptions.PROJECTDEVELOPER_INSERT_PROJECT_NOT_SAVED);
        } else if (projectRepository.findAllById(newProjectDeveloperEntity.getProjectEntity().getId()) == null) {
            serviceException = new ServiceException(ServiceException.Exceptions.PROJECTDEVELOPER_INSERT_PROJECT_NOT_EXISTS);
        } else if (newProjectDeveloperEntity.getDeveloperEntity().getId() == null) {
            serviceException = new ServiceException(ServiceException.Exceptions.PROJECTDEVELOPER_INSERT_DEVELOPER_NOT_SAVED);
        } else if (developerRepository.findAllById(newProjectDeveloperEntity.getDeveloperEntity().getId()) == null) {
            serviceException = new ServiceException(ServiceException.Exceptions.PROJECTDEVELOPER_INSERT_DEVELOPER_NOT_EXISTS);
        } else if (projectDeveloperRepository.findAllByProjectEntityAndDeveloperEntity(
            newProjectDeveloperEntity.getProjectEntity(), newProjectDeveloperEntity.getDeveloperEntity()) != null) {
            serviceException = new ServiceException(ServiceException.Exceptions.PROJECTDEVELOPER_INSERT_EXISTING_DATA);
        }
        if (serviceException != null) {
            throw serviceException;
        } else {
            projectDeveloperRepository.saveAndFlush(newProjectDeveloperEntity);
        }
    }

    /**
     * - Az entity még nincs elmentve
     * - nem törölhető az az összerendelés, amihez tartozik ProjectTaskDeveloper bejegyzés
     */
    public void deleteProjectDeveloper(final @NonNull ProjectDeveloperEntity projectDeveloperEntity) {
        if (projectDeveloperEntity.getId() == null) {
            throw new ServiceException(ServiceException.Exceptions.PROJECTDEVELOPER_DELETE_NOT_SAVED);
        } else if (!projectTaskDeveloperRepository.findAllByProjectDeveloperEntity(projectDeveloperEntity).isEmpty()) {
            throw new ServiceException(ServiceException.Exceptions.PROJECTDEVELOPER_DELETE_ASSIGNED_TO_TASK);
        } else {
            projectDeveloperRepository.deleteById(projectDeveloperEntity.getId());
            entityManager.flush();
        }
    }

    public @Nullable ProjectDeveloperEntity getProjectDeveloperById(final @NonNull Long id) {
        return projectDeveloperRepository.findAllById(id);
    }

    public @Nullable ProjectDeveloperEntity getProjectDeveloperByProjectAndDeveloper(
        final @NonNull ProjectEntity project,
        final @NonNull DeveloperEntity developer
    ) {
        return projectDeveloperRepository.findAllByProjectEntityAndDeveloperEntity(project, developer);
    }

    public @NonNull List<ProjectDeveloperEntity> getProjectDevelopers() {
        return projectDeveloperRepository.findAll();
    }

    public @NonNull List<ProjectDeveloperEntity> getProjectDevelopersByDeveloper(final @NonNull DeveloperEntity developer) {
        return projectDeveloperRepository.findAllByDeveloperEntity(developer);
    }

    public @NonNull List<ProjectDeveloperEntity> getProjectDevelopersByProject(final @NonNull ProjectEntity project) {
        return projectDeveloperRepository.findAllByProjectEntity(project);
    }
}
