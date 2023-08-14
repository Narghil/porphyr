package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTasksEntity;
import combit.hu.porphyr.repository.ProjectRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Scope("prototype")
public class ProjectService {

    private ProjectRepository projectRepository;

    @Autowired
    public void setProjectRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public @NonNull List<ProjectEntity> getProjects() {
        return projectRepository.findAll();
    }

    /**
     * Hibalehetőségek. <br/>
     * - A név üres
     * - A név már létezik
     */
    public void insertNewProject(final @NonNull ProjectEntity newProjectEntity) {
        if( newProjectEntity.getName().isEmpty() ){
            throw(new ServiceException(ServiceException.Exceptions.DEVELOPER_WITH_EMPTY_NAME_CANT_INSERT) );
        } else if( ! projectRepository.findAllByName(newProjectEntity.getName()).isEmpty()){
            throw(new ServiceException(ServiceException.Exceptions.DEVELOPER_WITH_EMPTY_NAME_CANT_INSERT) );
        }
        projectRepository.save(newProjectEntity);
    }

    /**
     * Hibalehetőségek. <br/>
     * - Az ID nincs kitöltve
     * - A név üres
     * - A név már létezik
     */
    public void modifyProject(final @NonNull ProjectEntity modifiedProject) {

        if (modifiedProject.getId() == null) {
            throw new ServiceException(ServiceException.Exceptions.PROJECT_NOT_SAVED_CANT_MODIFY);
        } else {
            projectRepository.save(modifiedProject);
        }
    }

    /**
     * Hibalehetőségek. <br/>
     * - Az ID nincs kitöltve
     * - A projekthez még tartozik fejlesztő
     * - A projekthez még tartozik feladat
     */
    public void deleteProject(final @NonNull ProjectEntity projectEntity) {
        if (projectEntity.getId() != null) {
            if (projectEntity.getTasks().isEmpty()) {
                projectRepository.deleteById(projectEntity.getId());
            } else {
                throw new ServiceException(ServiceException.Exceptions.PROJECT_WITH_TASKS_CANT_DELETE);
            }
        } else {
            throw new ServiceException(ServiceException.Exceptions.PROJECT_NOT_SAVED_CANT_DELETE);
        }
    }

}
