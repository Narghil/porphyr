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

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.persistence.EntityManager;
import java.util.List;

@Service
@Transactional
@ThreadSafe
public class ProjectDeveloperService {

    @Autowired
    @Setter
    @GuardedBy("this")
    private EntityManager entityManager;

    @Autowired
    @Setter
    @GuardedBy("this")
    private ProjectDeveloperRepository projectDeveloperRepository;

    @Autowired
    @Setter
    @GuardedBy("this")
    private ProjectRepository projectRepository;

    @Autowired
    @Setter
    @GuardedBy("this")
    private ProjectTaskDeveloperRepository projectTaskDeveloperRepository;

    @Autowired
    @Setter
    @GuardedBy("this")
    private DeveloperRepository developerRepository;

    /**
     * Hibalehetőségek <br />
     * - projectEntity = null <br />
     * - projectEntity.id = null <br />
     * - projectEntity nincs az adatbázisban <br />
     * - developerEntity = null <br />
     * - developerEntity.id = null <br />
     * - developerEntity nincs az adatbázisban <br />
     * - már van ilyen tétel <br />
     */
    public synchronized void insertNewProjectDeveloper(final @NonNull ProjectDeveloperEntity newProjectDeveloperEntity) {
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
     * - Az entity még nincs elmentve<br />
     * - nem törölhető az az összerendelés, amihez tartozik ProjectTaskDeveloper bejegyzés<br />
     */
    public synchronized void deleteProjectDeveloper(final @NonNull ProjectDeveloperEntity projectDeveloperEntity) {
        if (projectDeveloperEntity.getId() == null) {
            throw new ServiceException(ServiceException.Exceptions.PROJECTDEVELOPER_DELETE_NOT_SAVED);
        } else if (!projectTaskDeveloperRepository.findAllByProjectDeveloperEntity(projectDeveloperEntity).isEmpty()) {
            throw new ServiceException(ServiceException.Exceptions.PROJECTDEVELOPER_DELETE_ASSIGNED_TO_TASK);
        } else {
            projectDeveloperRepository.deleteById(projectDeveloperEntity.getId());
            entityManager.flush();
        }
    }

    public synchronized @Nullable ProjectDeveloperEntity getProjectDeveloperById(final @NonNull Long id) {
        return projectDeveloperRepository.findAllById(id);
    }

    public synchronized @Nullable ProjectDeveloperEntity getProjectDeveloperByProjectAndDeveloper(
        final @NonNull ProjectEntity project,
        final @NonNull DeveloperEntity developer
    ) {
        return projectDeveloperRepository.findAllByProjectEntityAndDeveloperEntity(project, developer);
    }

    public synchronized @NonNull List<ProjectDeveloperEntity> getProjectDevelopers() {
        return projectDeveloperRepository.findAll();
    }

    public synchronized @NonNull List<ProjectDeveloperEntity> getProjectDevelopersByDeveloper(final @NonNull DeveloperEntity developer) {
        return projectDeveloperRepository.findAllByDeveloperEntity(developer);
    }

    public synchronized @NonNull List<ProjectDeveloperEntity> getProjectDevelopersByProject(final @NonNull ProjectEntity project) {
        return projectDeveloperRepository.findAllByProjectEntity(project);
    }
}
