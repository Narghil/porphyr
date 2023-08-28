package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.repository.ProjectRepository;
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
public class ProjectService {

    @Autowired
    @Setter
    private EntityManager entityManager;

    @Autowired
    @Setter
    private ProjectRepository projectRepository;

    public @NonNull List<ProjectEntity> getProjects() {
        return projectRepository.findAll();
    }

    /**
     * Hibalehetőségek: <br/>
     * - Nincs kitöltve a név <br/>
     * - Van másik ilyen nevű projekt <br/>
     */
    public void insertNewProject(final @NonNull ProjectEntity newProjectEntity) {
        if (newProjectEntity.getName().isEmpty()) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECT_INSERT_EMPTY_NAME));
        } else if (!projectRepository.findAllByName(newProjectEntity.getName()).isEmpty()) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECT_INSERT_SAME_NAME));
        } else {
            projectRepository.saveAndFlush(newProjectEntity);
        }
    }

    /**
     * Hibalehetőségek: <br/>
     * - Nincs kitöltve az ID <br/>
     * - Nincs kitöltve a név <br/>
     * - Van másik ilyen nevű projekt <br/>
     */
    public void modifyProject(final @NonNull ProjectEntity modifiedProject) {
        entityManager.detach(modifiedProject);
        if (modifiedProject.getId() == null) {
            throw new ServiceException(ServiceException.Exceptions.PROJECT_MODIFY_NOT_SAVED);
        } else if (modifiedProject.getName().isEmpty()) {
            throw new ServiceException(ServiceException.Exceptions.PROJECT_MODIFY_EMPTY_NAME);
        } else if ( ! projectRepository.findAllByNameAndIdNot(
            modifiedProject.getName(), modifiedProject.getId()).isEmpty()) {
            throw new ServiceException(ServiceException.Exceptions.PROJECT_MODIFY_SAME_NAME);
        } else {
            projectRepository.saveAndFlush(modifiedProject);
        }
    }

    /**
     * Hibalehetőségek. <br/>
     * - Az ID nincs kitöltve
     * - A projekthez még van hozzárendelt fejlesztő
     * - A projekthez még tartozik feladat
     */
    public void deleteProject(final @NonNull ProjectEntity projectEntity) {
        Long projectId = projectEntity.getId();
        if (projectId == null) {
            throw new ServiceException(ServiceException.Exceptions.PROJECT_DELETE_NOT_SAVED);
        } else {
            ProjectEntity actualProjectData = projectRepository.findAllById(projectId);
            if( actualProjectData == null ){
                throw (new ServiceException(ServiceException.Exceptions.UNDEFINED));
            } else {
                if (!actualProjectData.getProjectTasks().isEmpty()) {
                    throw new ServiceException(ServiceException.Exceptions.PROJECT_DELETE_TASKS_ASSIGNED);
                } else if (!actualProjectData.getProjectDevelopers().isEmpty()) {
                    throw new ServiceException(ServiceException.Exceptions.PROJECT_DELETE_DEVELOPERS_ASSIGNED);
                } else {
                    projectRepository.deleteById(projectEntity.getId());
                    entityManager.flush();
                }
            }
        }
    }

    public @Nullable ProjectEntity getProjectById( final @NonNull Long id ){return projectRepository.findAllById(id);}
    public @Nullable ProjectEntity getProjectByName( final @NonNull String name ){
        List<ProjectEntity> namedProjects = projectRepository.findAllByName( name );
        if( namedProjects.isEmpty()){
            return null;
        } else {
            return namedProjects.get(0);
        }
    }

}
