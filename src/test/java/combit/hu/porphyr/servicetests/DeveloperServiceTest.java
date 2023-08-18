package combit.hu.porphyr.servicetests;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectDevelopersEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class DeveloperServiceTest {

    @Mock
    private DeveloperRepository mockedDeveloperRepository;
    @InjectMocks
    private DeveloperService developerService;

    @Test
    void doTests() {
        final List<DeveloperEntity> mockedListFindAll = new ArrayList<>();
        final List<DeveloperEntity> mockedListFindAllByName = new ArrayList<>();
        final List<DeveloperEntity> mockedListFindAllByNameAndIdNot = new ArrayList<>();
        final List<ProjectDevelopersEntity> mockedListDeveloperProjects = new ArrayList<>();
        final DeveloperEntity singleDeveloperEntity = new DeveloperEntity("Developer");
        singleDeveloperEntity.setDeveloperProjects(mockedListDeveloperProjects);

        when(mockedDeveloperRepository.findAll()).thenReturn(mockedListFindAll);
        when(mockedDeveloperRepository.findAllByName(anyString())).thenReturn(mockedListFindAllByName);
        when(mockedDeveloperRepository.findAllByNameAndIdNot(anyString(), anyLong())).thenReturn(mockedListFindAllByNameAndIdNot);
        when(mockedDeveloperRepository.findAllById(anyLong())).thenReturn(singleDeveloperEntity);
        when(mockedDeveloperRepository.save(any(DeveloperEntity.class))).thenReturn(null);
        doNothing().when(mockedDeveloperRepository).deleteById(anyLong());

        //Teljes lekérdezés
        developerService.getDevelopers();
        verify(mockedDeveloperRepository, times(1)).findAll();
        //Felvétel
        //singleDeveloperEntity.setId(null);
        //- Nincs kitöltve a név
        singleDeveloperEntity.setName("");
        assertThrows(
            ServiceException.class,
            () -> developerService.insertNewDeveloper(singleDeveloperEntity),
            ServiceException.Exceptions.DEVELOPER_WITH_EMPTY_NAME_CANT_INSERT.getDescription()
        );
        // - Már van ilyen név
        singleDeveloperEntity.setName("New Developer");
        mockedListFindAllByName.add(singleDeveloperEntity);
        assertThrows(
            ServiceException.class,
            () -> developerService.insertNewDeveloper(singleDeveloperEntity),
            ServiceException.Exceptions.DEVELOPER_WITH_SAME_NAME_CANT_INSERT.getDescription()
        );
        // - Helyes adatokkal
        singleDeveloperEntity.setName("New Developer");
        mockedListFindAllByName.clear();
        assertDoesNotThrow( () -> developerService.insertNewDeveloper( singleDeveloperEntity ) );
        verify(mockedDeveloperRepository, times(1)).save(singleDeveloperEntity);
        //Módosítás
        // - Nincs kitöltve az ID
        //singleDeveloperEntity.setId(null);
        assertThrows(
            ServiceException.class,
            () -> developerService.modifyDeveloper(singleDeveloperEntity),
            ServiceException.Exceptions.DEVELOPER_NOT_SAVED_CANT_MODIFY.getDescription()
        );
        //- Nincs kitöltve a név
        //singleDeveloperEntity.setId(0L);
        singleDeveloperEntity.setName("");
        assertThrows(
            ServiceException.class,
            () -> developerService.modifyDeveloper(singleDeveloperEntity),
            ServiceException.Exceptions.DEVELOPER_WITH_EMPTY_NAME_CANT_MODIFY.getDescription()
        );
        // - Van már ilyen név, más ID-n.
        singleDeveloperEntity.setName("Modified Developer");
        mockedListFindAllByNameAndIdNot.add(singleDeveloperEntity);
        assertThrows(
            ServiceException.class,
            () -> developerService.modifyDeveloper(singleDeveloperEntity),
            ServiceException.Exceptions.DEVELOPER_WITH_SAME_NAME_CANT_MODIFY.getDescription()
        );
        verify(mockedDeveloperRepository, times(1)).findAllByNameAndIdNot(anyString(),anyLong());
        // - Helyes adatokkal
        mockedListFindAllByNameAndIdNot.clear();
        assertDoesNotThrow(() -> mockedDeveloperRepository.save(singleDeveloperEntity));
        verify(mockedDeveloperRepository, times(2)).save(singleDeveloperEntity);
        // Törlés
        // - Nincs kitöltve az ID;
        //singleDeveloperEntity.setId(null);
        assertThrows(
            ServiceException.class,
            () -> developerService.deleteDeveloper(singleDeveloperEntity),
            ServiceException.Exceptions.DEVELOPER_NOT_SAVED_CANT_DELETE.getDescription()
        );
        // - Hozzá van rendelve egy projekthez
        //singleDeveloperEntity.setId(0L);
        mockedListDeveloperProjects.add( new ProjectDevelopersEntity() );
        assertThrows(
            ServiceException.class,
            () -> developerService.deleteDeveloper(singleDeveloperEntity),
            ServiceException.Exceptions.DEVELOPER_WITH_PROJECTS_CANT_DELETE.getDescription()
        );
        verify(mockedDeveloperRepository, times(1)).findAllById(0L);
        // - Jó adatokkal
        mockedListDeveloperProjects.clear();
        assertDoesNotThrow( ()-> developerService.deleteDeveloper(singleDeveloperEntity));
        verify(mockedDeveloperRepository, times(2)).findAllById(0L);
        verify(mockedDeveloperRepository, times(1)).deleteById(0L);
        //Mindent ellenőriztünk?
        assertDoesNotThrow(ServiceException::isAllDeveloperExceptionsThrown);
    }
}
