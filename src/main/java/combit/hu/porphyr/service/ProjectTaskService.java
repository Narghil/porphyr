package combit.hu.porphyr.service;

import combit.hu.porphyr.domain.ProjectTasksEntity;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.repository.ProjectTaskRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@Scope("prototype")
public class ProjectTaskService {

    private ProjectTaskRepository projectTaskRepository;
    private ProjectRepository projectRepository;

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository) {
        this.projectTaskRepository = projectTaskRepository;
    }

    @Autowired
    public void setProjectRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Hibalehetőségek: <br/>
     * - A project entity üres vagy nem létező project -re mutat
     * - A task neve üres
     * - Ilyen nevű task már van
     */
    public void insertNewProjectTask(final @NonNull ProjectTasksEntity newProjectTasksEntity) {
        if( newProjectTasksEntity.getProjectEntity() == null) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_PROJECT_IS_NULL_CANT_INSERT));
        } else if(newProjectTasksEntity.getProjectEntity().getId() == null) {
            throw( new ServiceException(ServiceException.Exceptions.NULLVALUE ) );
        } else if( projectRepository.findAllById( newProjectTasksEntity.getProjectEntity().getId()) == null ){
            throw( new ServiceException( ServiceException.Exceptions.PROJECTTASK_PROJECT_NOT_EXISTS_CANT_INSERT) );
        } else if( newProjectTasksEntity.getName().isEmpty() ) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_WITH_EMPTY_NAME_CANT_INSERT));
        } else if( ! projectTaskRepository.findAllByName( newProjectTasksEntity.getName()).isEmpty() ){
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_WITH_SAME_NAME_CANT_INSERT));
        } else {
            projectTaskRepository.save(newProjectTasksEntity);
        }
    }

    /**
     * Hibalehetőségek: <br/>
     * - A task ID-je üres
     * - A project entity üres vagy nem létező project-re mutat
     * - A task neve üres
     * - Ilyen nevű task már van
     */
    public void modifyProjectTask(final @NonNull ProjectTasksEntity modifiedTask) {
        if (modifiedTask.getId() == null) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_NOT_SAVED_CANT_MODIFY));
        } else if( modifiedTask.getProjectEntity() == null) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_PROJECT_IS_NULL_CANT_MODIFY));
        } else if(modifiedTask.getProjectEntity().getId() == null){
            throw( new ServiceException(ServiceException.Exceptions.NULLVALUE ) );
        } else if( projectRepository.findAllById(modifiedTask.getProjectEntity().getId()) == null  ){
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_PROJECT_NOT_EXISTS_CANT_MODIFY));
        } else if( modifiedTask.getName().isEmpty()){
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_WITH_EMPTY_NAME_CANT_MODIFY));
        } else if( ! projectTaskRepository.findAllByNameAndIdNot(modifiedTask.getName(), modifiedTask.getId()).isEmpty()){
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_WITH_SAME_NAME_CANT_MODIFY));
        } else {
            projectTaskRepository.save(modifiedTask);
        }
    }

    /**
     * Hibalehetőségek: <br/>
     * - A task ID-je üres
     * - A task-hoz még van hozzárendelve fejlesztő
     */
    public void deleteProjectTask(final @NonNull ProjectTasksEntity task) {
        if (task.getId() == null) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_NOT_SAVED_CANT_DELETE));
        } else if( ! Objects.requireNonNull(projectTaskRepository.findAllById(task.getId())).getProjectTaskDevelopers().isEmpty() ) {
            throw (new ServiceException(ServiceException.Exceptions.PROJECTTASK_WITH_DEVELOPERS_CANT_DELETE));
        } else {
            projectTaskRepository.deleteById(task.getId());
        }
    }

    public List<ProjectTasksEntity> getProjectTasks(){
        return projectTaskRepository.findAll();
    }

}
