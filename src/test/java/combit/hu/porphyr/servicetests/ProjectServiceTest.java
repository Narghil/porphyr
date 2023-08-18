package combit.hu.porphyr.servicetests;

import combit.hu.porphyr.domain.ProjectDevelopersEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTasksEntity;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.service.ProjectService;
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
class ProjectServiceTest {

    @Mock
    private ProjectRepository mockedProjectRepository;
    @InjectMocks
    private ProjectService projectService;

    @Test
    void doTests() {
        final List<ProjectEntity> mockedListFindAll = new ArrayList<>();
        final List<ProjectEntity> mockedListFindAllByName = new ArrayList<>();
        final List<ProjectEntity> mockedListFindAllByNameAndIdNot = new ArrayList<>();
        final List<ProjectDevelopersEntity> mockedListProjectDevelopers = new ArrayList<>();
        final List<ProjectTasksEntity> mockedListProjectTasks = new ArrayList<>();
        final ProjectEntity singleProjectEntity = new ProjectEntity("Project","Description");
        singleProjectEntity.setProjectDevelopers(mockedListProjectDevelopers);
        singleProjectEntity.setProjectTasks(mockedListProjectTasks);

        when(mockedProjectRepository.findAll()).thenReturn(mockedListFindAll);
        when(mockedProjectRepository.findAllByName(anyString())).thenReturn(mockedListFindAllByName);
        when(mockedProjectRepository.findAllByNameAndIdNot(anyString(), anyLong())).thenReturn(mockedListFindAllByNameAndIdNot);
        when(mockedProjectRepository.findAllById(anyLong())).thenReturn(singleProjectEntity);
        when(mockedProjectRepository.save(any(ProjectEntity.class))).thenReturn(null);
        doNothing().when(mockedProjectRepository).deleteById(anyLong());

        //Teljes lekérdezés
        projectService.getProjects();
        verify(mockedProjectRepository, times(1)).findAll();
        //Felvétel
        //singleProjectEntity.setId(null);
        //- Nincs kitöltve a név
        singleProjectEntity.setName("");
        assertThrows(
            ServiceException.class,
            () -> projectService.insertNewProject(singleProjectEntity),
            ServiceException.Exceptions.PROJECT_WITH_EMPTY_NAME_CANT_INSERT.getDescription()
        );
        // - Már van ilyen név
        singleProjectEntity.setName("New Project");
        mockedListFindAllByName.add(singleProjectEntity);
        assertThrows(
            ServiceException.class,
            () -> projectService.insertNewProject(singleProjectEntity),
            ServiceException.Exceptions.PROJECT_WITH_SAME_NAME_CANT_INSERT.getDescription()
        );
        // - Helyes adatokkal
        singleProjectEntity.setName("New Project");
        mockedListFindAllByName.clear();
        assertDoesNotThrow( () -> mockedProjectRepository.save( singleProjectEntity ) );
        verify(mockedProjectRepository, times(1)).save(singleProjectEntity);
        //Módosítás
        // - Nincs kitöltve az ID
        //singleProjectEntity.setId(null);
        assertThrows(
            ServiceException.class,
            () -> projectService.modifyProject(singleProjectEntity),
            ServiceException.Exceptions.PROJECT_NOT_SAVED_CANT_MODIFY.getDescription()
        );
        //- Nincs kitöltve a név
        //singleProjectEntity.setId(0L);
        singleProjectEntity.setName("");
        assertThrows(
            ServiceException.class,
            () -> projectService.modifyProject(singleProjectEntity),
            ServiceException.Exceptions.PROJECT_WITH_EMPTY_NAME_CANT_MODIFY.getDescription()
        );
        // - Van már ilyen név, más ID-n.
        singleProjectEntity.setName("Modified Project");
        mockedListFindAllByNameAndIdNot.add(singleProjectEntity);
        assertThrows(
            ServiceException.class,
            () -> projectService.modifyProject(singleProjectEntity),
            ServiceException.Exceptions.PROJECT_WITH_SAME_NAME_CANT_MODIFY.getDescription()
        );
        verify(mockedProjectRepository, times(1)).findAllByNameAndIdNot(anyString(),anyLong());
        // - Helyes adatokkal
        mockedListFindAllByNameAndIdNot.clear();
        assertDoesNotThrow(() -> mockedProjectRepository.save(singleProjectEntity));
        verify(mockedProjectRepository, times(2)).save(singleProjectEntity);
        // Törlés
        // - Nincs kitöltve az ID;
        //singleProjectEntity.setId(null);
        assertThrows(
            ServiceException.class,
            () -> projectService.deleteProject(singleProjectEntity),
            ServiceException.Exceptions.PROJECT_NOT_SAVED_CANT_DELETE.getDescription()
        );
        //singleProjectEntity.setId(0L);
        // - Hozzá van rendelve egy projekthez
        mockedListProjectDevelopers.add( new ProjectDevelopersEntity() );
        assertThrows(
            ServiceException.class,
            () -> projectService.deleteProject(singleProjectEntity),
            ServiceException.Exceptions.PROJECT_WITH_DEVELOPERS_CANT_DELETE.getDescription()
        );
        verify(mockedProjectRepository, times(1)).findAllById(0L);
        mockedListProjectDevelopers.clear();
        // - Tartozik hozzá task
        mockedListProjectTasks.add( new ProjectTasksEntity());
        assertThrows(
            ServiceException.class,
            () -> projectService.deleteProject(singleProjectEntity),
            ServiceException.Exceptions.PROJECT_WITH_TASKS_CANT_DELETE.getDescription()
        );
        verify(mockedProjectRepository, times(2)).findAllById(0L);
        mockedListProjectTasks.clear();
        // - Jó adatokkal
        assertDoesNotThrow( ()-> projectService.deleteProject(singleProjectEntity));
        verify(mockedProjectRepository, times(3)).findAllById(0L);
        verify(mockedProjectRepository, times(1)).deleteById(0L);
        //Mindent ellenőriztünk?
        assertDoesNotThrow(ServiceException::isAllProjectExceptionsThrown);
    }
}
