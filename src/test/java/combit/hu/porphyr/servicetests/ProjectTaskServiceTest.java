package combit.hu.porphyr.servicetests;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTaskDevelopersEntity;
import combit.hu.porphyr.domain.ProjectTasksEntity;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.repository.ProjectTaskRepository;
import combit.hu.porphyr.service.ProjectTaskService;
import combit.hu.porphyr.service.ServiceException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("service_test")
class ProjectTaskServiceTest {

    @Mock
    private ProjectTaskRepository mockedProjectTaskRepository;
    @Mock
    private ProjectRepository mockedProjectRepository;
    @InjectMocks
    private ProjectTaskService projectTaskService;

    @Test
    void doTest(){
        final List<ProjectTasksEntity> mockedListFindAll = new ArrayList<>();
        final List<ProjectTasksEntity> mockedListFindAllByName = new ArrayList<>();
        final List<ProjectTasksEntity> mockedListFindAllByNameAndIdNot = new ArrayList<>();
        final List<ProjectTaskDevelopersEntity> mockedListProjectTaskDevelopers = new ArrayList<>();
        final ProjectEntity singleProjectEntity = new ProjectEntity();
        final ProjectTasksEntity singleProjectTasksEntity = new ProjectTasksEntity(singleProjectEntity, "Task", "Description of task" );
        singleProjectTasksEntity.setProjectTaskDevelopers( mockedListProjectTaskDevelopers );
        singleProjectTasksEntity.setProjectEntity(singleProjectEntity);

        when(mockedProjectTaskRepository.findAll()).thenReturn(mockedListFindAll);
        when(mockedProjectTaskRepository.findAllByName(anyString())).thenReturn(mockedListFindAllByName);
        when(mockedProjectTaskRepository.findAllByNameAndIdNot(anyString(), anyLong())).thenReturn(mockedListFindAllByNameAndIdNot);
        when(mockedProjectTaskRepository.findAllById(anyLong())).thenReturn(singleProjectTasksEntity);
        when(mockedProjectTaskRepository.save(any(ProjectTasksEntity.class))).thenReturn(null);
        when(mockedProjectRepository.findAllById( anyLong() ) ).thenReturn( singleProjectEntity );
        doNothing().when(mockedProjectTaskRepository).deleteById(anyLong());

        //Teljes lekérdezés
        projectTaskService.getProjectTasks();
        verify(mockedProjectTaskRepository, times(1)).findAll();
        //Felvétel
        // - Nincs kiválasztott project
        singleProjectTasksEntity.setProjectEntity(null);
        assertThrows(
            ServiceException.class,
            () -> projectTaskService.insertNewProjectTask(singleProjectTasksEntity),
            ServiceException.Exceptions.PROJECTTASK_PROJECT_IS_NULL_CANT_INSERT.getDescription()
        );
        // - A project entity nem létező projektre mutat;
        singleProjectTasksEntity.setProjectEntity(singleProjectEntity);
        //singleProjectTasksEntity.getProjectEntity().setId(0L);
        when(mockedProjectRepository.findAllById( anyLong() ) ).thenReturn(null);
        assertThrows(
            ServiceException.class,
            () -> projectTaskService.insertNewProjectTask(singleProjectTasksEntity),
            ServiceException.Exceptions.PROJECTTASK_PROJECT_NOT_EXISTS_CANT_INSERT.getDescription()
        );
        when(mockedProjectRepository.findAllById( anyLong() ) ).thenReturn( singleProjectEntity );
        // - Nincs kitöltve a név
        singleProjectTasksEntity.setName("");
        assertThrows(
            ServiceException.class,
            () -> projectTaskService.insertNewProjectTask(singleProjectTasksEntity),
            ServiceException.Exceptions.PROJECTTASK_WITH_EMPTY_NAME_CANT_INSERT.getDescription()
        );
        // - Már van ilyen név
        singleProjectTasksEntity.setName("New ProjectTask");
        mockedListFindAllByName.add(singleProjectTasksEntity);
        assertThrows(
            ServiceException.class,
            () -> projectTaskService.insertNewProjectTask(singleProjectTasksEntity),
            ServiceException.Exceptions.PROJECTTASK_WITH_SAME_NAME_CANT_INSERT.getDescription()
        );
        // - Helyes adatokkal
        singleProjectTasksEntity.setName("New ProjectTask");
        mockedListFindAllByName.clear();
        assertDoesNotThrow( () -> mockedProjectTaskRepository.save( singleProjectTasksEntity ) );
        verify(mockedProjectTaskRepository, times(1)).save(singleProjectTasksEntity);
        //Módosítás
        // - Nincs kitöltve az ID
        //singleProjectTasksEntity.setId(null);
        assertThrows(
            ServiceException.class,
            () -> projectTaskService.modifyProjectTask(singleProjectTasksEntity),
            ServiceException.Exceptions.PROJECTTASK_NOT_SAVED_CANT_MODIFY.getDescription()
        );
        // - Nincs kiválasztott project
        //singleProjectTasksEntity.setId(0L);
        singleProjectTasksEntity.setProjectEntity(null);
        assertThrows(
            ServiceException.class,
            () -> projectTaskService.modifyProjectTask(singleProjectTasksEntity),
            ServiceException.Exceptions.PROJECTTASK_PROJECT_IS_NULL_CANT_MODIFY.getDescription()
        );
        // - A project entity nem létező projektre mutat;
        singleProjectTasksEntity.setProjectEntity(singleProjectEntity);
        //singleProjectTasksEntity.getProjectEntity().setId(0L);
        when(mockedProjectRepository.findAllById( anyLong() ) ).thenReturn(null);
        assertThrows(
            ServiceException.class,
            () -> projectTaskService.modifyProjectTask(singleProjectTasksEntity),
            ServiceException.Exceptions.PROJECTTASK_PROJECT_NOT_EXISTS_CANT_MODIFY.getDescription()
        );
        when(mockedProjectRepository.findAllById( anyLong() ) ).thenReturn( singleProjectEntity );
        //- Nincs kitöltve a név
        //singleProjectTasksEntity.setId(0L);
        singleProjectTasksEntity.setName("");
        assertThrows(
            ServiceException.class,
            () -> projectTaskService.modifyProjectTask(singleProjectTasksEntity),
            ServiceException.Exceptions.PROJECTTASK_WITH_EMPTY_NAME_CANT_MODIFY.getDescription()
        );
        // - Van már ilyen név, más ID-n.
        singleProjectTasksEntity.setName("Modified ProjectTask");
        mockedListFindAllByNameAndIdNot.add(singleProjectTasksEntity);
        assertThrows(
            ServiceException.class,
            () -> projectTaskService.modifyProjectTask(singleProjectTasksEntity),
            ServiceException.Exceptions.PROJECTTASK_WITH_SAME_NAME_CANT_MODIFY.getDescription()
        );
        verify(mockedProjectTaskRepository, times(1)).findAllByNameAndIdNot(anyString(),anyLong());
        // - Helyes adatokkal
        mockedListFindAllByNameAndIdNot.clear();
        assertDoesNotThrow(() -> mockedProjectTaskRepository.save(singleProjectTasksEntity));
        verify(mockedProjectTaskRepository, times(2)).save(singleProjectTasksEntity);
        // Törlés
        // - Nincs kitöltve az ID;
        //singleProjectTasksEntity.setId(null);
        assertThrows(
            ServiceException.class,
            () -> projectTaskService.deleteProjectTask(singleProjectTasksEntity),
            ServiceException.Exceptions.PROJECTTASK_NOT_SAVED_CANT_DELETE.getDescription()
        );
        //singleProjectTasksEntity.setId(0L);
        // - Van még hozzárendelt fejlesztő
        mockedListProjectTaskDevelopers.add( new ProjectTaskDevelopersEntity() );
        assertThrows(
            ServiceException.class,
            () -> projectTaskService.deleteProjectTask(singleProjectTasksEntity),
            ServiceException.Exceptions.PROJECTTASK_WITH_DEVELOPERS_CANT_DELETE.getDescription()
        );
        verify(mockedProjectTaskRepository, times(1)).findAllById(0L);
        mockedListProjectTaskDevelopers.clear();
        // - Jó adatokkal
        assertDoesNotThrow( ()-> projectTaskService.deleteProjectTask(singleProjectTasksEntity));
        verify(mockedProjectTaskRepository, times(2)).findAllById(0L);
        verify(mockedProjectTaskRepository, times(1)).deleteById(0L);
        //Mindent ellenőriztünk?
        assertDoesNotThrow(ServiceException::isAllProjectTaskExceptionsThrown);

    }
}
