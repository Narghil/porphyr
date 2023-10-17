package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.ProjectDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskDeveloperEntity;
import combit.hu.porphyr.domain.ProjectTaskEntity;
import combit.hu.porphyr.repository.ProjectDeveloperRepository;
import combit.hu.porphyr.repository.ProjectTaskDeveloperRepository;
import combit.hu.porphyr.repository.ProjectTaskRepository;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.persistence.EntityManager;
import java.util.List;

@Service
@Transactional
@ThreadSafe
public class ProjectTaskDeveloperService {

    @Autowired
    @Setter
    @GuardedBy("this")
    EntityManager entityManager;

    @Autowired
    @Setter
    @GuardedBy("this")
    ProjectTaskDeveloperRepository projectTaskDeveloperRepository;

    @Autowired
    @Setter
    @GuardedBy("this")
    ProjectTaskRepository projectTaskRepository;

    @Autowired
    @Setter
    @GuardedBy("this")
    ProjectDeveloperRepository projectDeveloperRepository;

    /**
     * Hibalehetőségek: <br />
     * - projectTaskEntity nincs elmentve  <br />
     * - projectTaskEntity nincs az adatbázisban  <br />
     * - projectDeveloperEntity nincs elmentve  <br />
     * - projectDeveloperEntity nincs az adatbázisban  <br />
     * - A projectTaskEntity és a projectDeveloperEntity más-más projekthez tartozik <br />
     * - Már létezik ilyen összerendelés  <br />
     */
    public synchronized void insertNewProjectTaskDeveloper(
        final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity
    ) {
        @Nullable ServiceException serviceException = null;
        final @NonNull ProjectTaskEntity projectTaskEntity = projectTaskDeveloperEntity.getProjectTaskEntity();
        final @NonNull ProjectDeveloperEntity projectDeveloperEntity = projectTaskDeveloperEntity.getProjectDeveloperEntity();

        entityManager.detach(projectTaskDeveloperEntity);

        if (projectTaskEntity.getId() == null) {
            serviceException = new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTTASK_NOT_SAVED);
        } else if (projectTaskRepository.findAllById(projectTaskEntity.getId()) == null) {
            serviceException = new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTTASK_NOT_EXISTS);
        } else if (projectDeveloperEntity.getId() == null) {
            serviceException = new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTDEVELOPER_NOT_SAVED);
        } else if (projectDeveloperRepository.findAllById(projectDeveloperEntity.getId()) == null) {
            serviceException = new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_PROJECTDEVELOPER_NOT_EXISTS);
        } else {
            Long taskProjectId = projectTaskEntity.getProjectEntity().getId();
            Long developerProjectId = projectDeveloperEntity.getProjectEntity().getId();
            if (taskProjectId == null || developerProjectId == null) {
                serviceException = new ServiceException(ServiceException.Exceptions.NULL_VALUE);
            } else if (!taskProjectId.equals(developerProjectId)) {
                serviceException = new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_TASK_OR_DEVELOPER_NOT_IN_PROJECT);
            } else if (projectTaskDeveloperRepository.findAllByProjectTaskEntityAndProjectDeveloperEntity(
                projectTaskEntity, projectDeveloperEntity
            ) != null) {
                serviceException = new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_INSERT_EXISTING_DATA);
            }
        }

        if (serviceException != null) {
            throw serviceException;
        } else {
            projectTaskDeveloperRepository.saveAndFlush(projectTaskDeveloperEntity);
        }
    }

    /**
     * Hibalehetőségek: <br />
     * - projectTaskDeveloperEntity nincs elmentve  <br />
     * - spendTime < 0<br />
     */
    public synchronized void modifyProjectTaskDeveloper(
        final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity
    ) {
        if( projectTaskDeveloperEntity.getId() == null){
            throw new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_MODIFY_NOT_SAVED );
        } else if( projectTaskDeveloperEntity.getSpendTime() < 0 ){
            throw new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_MODIFY_TIME_IS_NEGATIVE );
        } else {
            projectTaskDeveloperRepository.saveAndFlush(projectTaskDeveloperEntity);
        }
    }

    /**
     * Hibalehetőségek: <br />
     * - projectTaskDeveloperEntity nincs elmentve  <br />
     * - A projektben eltöltött idő nem 0.<br />
     */
    public synchronized void deleteProjectTaskDeveloper(
        final @NonNull ProjectTaskDeveloperEntity projectTaskDeveloperEntity
    ) {
        if( projectTaskDeveloperEntity.getId() == null){
            throw new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_DELETE_NOT_SAVED );
        } else if( projectTaskDeveloperEntity.getSpendTime() > 0 ){
            throw new ServiceException(ServiceException.Exceptions.PROJECTTASKDEVELOPER_DELETE_TIME_NOT_ZERO );
        } else {
            projectTaskDeveloperRepository.deleteById(projectTaskDeveloperEntity.getId());
            entityManager.flush();
        }
    }

    // ---- Lekérdezések ----
    public synchronized @NonNull List<ProjectTaskDeveloperEntity> getProjectTaskDevelopers() {
        return projectTaskDeveloperRepository.findAll();
    }

    public synchronized @Nullable ProjectTaskDeveloperEntity getProjectTaskDeveloperById(final @NonNull Long id) {
        return projectTaskDeveloperRepository.findAllById(id);
    }

    public synchronized @Nullable ProjectTaskDeveloperEntity getProjectTaskDeveloperByProjectTaskAndProjectDeveloper(
        final @NonNull ProjectTaskEntity projectTaskEntity,
        final @NonNull ProjectDeveloperEntity projectDeveloperEntity
    ) {
        return projectTaskDeveloperRepository.findAllByProjectTaskEntityAndProjectDeveloperEntity(
            projectTaskEntity, projectDeveloperEntity
        );
    }

    public synchronized @NonNull List<ProjectTaskDeveloperEntity> getProjectTaskDevelopersByProjectTask(
        final @NonNull ProjectTaskEntity projectTaskEntity
    ) {
        return projectTaskDeveloperRepository.findAllByProjectTaskEntity(projectTaskEntity);
    }

    public synchronized @NonNull List<ProjectTaskDeveloperEntity> getProjectTaskDevelopersByProjectDeveloper(
        final @NonNull ProjectDeveloperEntity projectDeveloperEntity
    ) {
        return projectTaskDeveloperRepository.findAllByProjectDeveloperEntity(projectDeveloperEntity);
    }
}
