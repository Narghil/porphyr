package combit.hu.porphyr.servicetests;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTasksEntity;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.service.ProjectService;
import combit.hu.porphyr.service.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository mockedProjectRepository;
    @InjectMocks
    private ProjectService projectService;

    //Kell ez?
    @Autowired
    public void setProjectRepository(ProjectRepository projectRepository) {
        this.mockedProjectRepository = projectRepository;
    }

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Test
    void doTests() {
        List<ProjectEntity> mockedProjectList = new ArrayList<ProjectEntity>() {
            {
                ProjectEntity projectEntity = new ProjectEntity("Project","Project Description");
                projectEntity.setTasks( new ArrayList<ProjectTasksEntity>() {
                    { add( new ProjectTasksEntity( projectEntity, "Task for project"," Description for task"));
                    }
                });
                add( projectEntity );
            }
        };
        final ProjectEntity singleProjectEntity = new ProjectEntity("Single Project","Single Project Description");
        singleProjectEntity.setTasks( new ArrayList<ProjectTasksEntity>() {
            { add( new ProjectTasksEntity( singleProjectEntity, "Task for single project"," Description for task"));
            }
        });
        List<ProjectEntity> actualProjectList;

        when(mockedProjectRepository.findAll()).thenReturn(mockedProjectList);
        when(mockedProjectRepository.save(any(ProjectEntity.class))).thenReturn(null);
        doNothing().when(mockedProjectRepository).deleteById(anyLong());
        doNothing().when(mockedProjectRepository).deleteAll();
        //Teljes lekérdezés
        actualProjectList = projectService.getProjects();
        assertEquals(actualProjectList, mockedProjectList);
        verify(mockedProjectRepository, times(1)).findAll();
        //Felvétel
        mockedProjectList.add(singleProjectEntity);
        projectService.insertNewProject(singleProjectEntity) ;
        verify(mockedProjectRepository, times(1)).save(singleProjectEntity);
        //Hibás ID beállítása:
        singleProjectEntity.setId(null);
        mockedProjectList.get(0).setId(null);
        //Módosítás, hibás ID-vel
        assertThrows( ServiceException.class, () -> projectService.modifyProject(singleProjectEntity), ServiceException.Exceptions.PROJECT_NOT_SAVED_CANT_MODIFY.getDescription());
        verify(mockedProjectRepository, times(1)).save(singleProjectEntity);
        //Törlés, hibás ID-vel
        // - egy
        assertThrows(ServiceException.class, () -> projectService.deleteProject(singleProjectEntity), ServiceException.Exceptions.PROJECT_NOT_SAVED_CANT_DELETE.getDescription());
        verify(mockedProjectRepository, times(0)).deleteById(anyLong());
        //Jó ID beállítása
        singleProjectEntity.setId(0L);
        mockedProjectList.get(0).setId(0L);
        //Módosítás
        assertDoesNotThrow(() -> projectService.modifyProject(singleProjectEntity),ServiceException.Exceptions.PROJECT_NOT_SAVED_CANT_MODIFY.getDescription());
        verify(mockedProjectRepository, times(2)).save(singleProjectEntity);
        //Törlés
        // - egy
        assertThrows( ServiceException.class, () -> projectService.deleteProject(singleProjectEntity), ServiceException.Exceptions.PROJECT_WITH_TASKS_CANT_DELETE.getDescription());
        verify(mockedProjectRepository, times(0)).deleteById(anyLong());
        // - mind
        assertThrows( ServiceException.class, () -> projectService.deleteAllProjects(), ServiceException.Exceptions.PROJECTS_WITH_TASKS_CANT_DELETE.getDescription() );
        verify(mockedProjectRepository, times(0)).deleteAll();
        //Törlés, jó ID-vel, task-ok nélkül
        singleProjectEntity.setTasks( new ArrayList<>());
        mockedProjectList.get(0).setTasks( new ArrayList<>());
        // - egy
        assertDoesNotThrow( () -> projectService.deleteProject(singleProjectEntity), ServiceException.Exceptions.PROJECT_WITH_TASKS_CANT_DELETE.getDescription());
        verify(mockedProjectRepository, times(1)).deleteById(anyLong());
        // - mind
        assertDoesNotThrow(() -> projectService.deleteAllProjects(), ServiceException.Exceptions.PROJECTS_WITH_TASKS_CANT_DELETE.getDescription() );
        verify(mockedProjectRepository, times(1)).deleteAll();
        //Minden lehetséges hibát ellenőriztünk?
        assertDoesNotThrow(ServiceException::isAllProjectExceptionsThrown);
    }
}
