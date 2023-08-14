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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class DeveloperServiceTest {

    @Mock
    private DeveloperRepository mockedDeveloperRepository;
    @InjectMocks
    private DeveloperService developerService;

    @Autowired
    public void setDeveloperRepository(DeveloperRepository developerRepository) {
        this.mockedDeveloperRepository = developerRepository;
    }

    @Autowired
    public void setDeveloperService(DeveloperService developerService) {
        this.developerService = developerService;
    }

    @Test
    void doTests() {
        List<DeveloperEntity> mockedListFindAll = new ArrayList<DeveloperEntity>() {
            {
                add(new DeveloperEntity("Developer"));
            }
        };
        List<DeveloperEntity> mockedListFindByName = new ArrayList<DeveloperEntity>() {
            {
                add(new DeveloperEntity("Developer"));
            }
        };
        final DeveloperEntity singleDeveloperEntity = new DeveloperEntity("");
        List<DeveloperEntity> actualDeveloperList;

        when(mockedDeveloperRepository.findAll()).thenReturn(mockedListFindAll);
        when(mockedDeveloperRepository.findAllByName(anyString())).thenReturn(mockedListFindByName);
        when(mockedDeveloperRepository.findAllById(anyLong())).thenReturn(singleDeveloperEntity);
        when(mockedDeveloperRepository.save(any(DeveloperEntity.class))).thenReturn(null);
        doNothing().when(mockedDeveloperRepository).deleteById(anyLong());
        //Teljes lekérdezés
        actualDeveloperList = developerService.getDevelopers();
        assertEquals(actualDeveloperList, mockedListFindAll);
        verify(mockedDeveloperRepository, times(1)).findAll();
        //Felvétel
        singleDeveloperEntity.setId(null);
        //- Nincs kitöltve a név
        singleDeveloperEntity.setName("");
        assertThrows(
            ServiceException.class,
            () -> developerService.insertNewDeveloper(singleDeveloperEntity),
            ServiceException.Exceptions.DEVELOPER_WITH_EMPTY_NAME_CANT_INSERT.getDescription()
        );
        // - Már van ilyen név
        singleDeveloperEntity.setName(mockedListFindAll.get(0).getName());
        mockedListFindByName.add(mockedListFindAll.get(0));
        assertThrows(
            ServiceException.class,
            () -> developerService.insertNewDeveloper(singleDeveloperEntity),
            ServiceException.Exceptions.DEVELOPER_WITH_SAME_NAME_CANT_INSERT.getDescription()
        );
        // - Helyes adatokkal
        singleDeveloperEntity.setName("New Developer");
        assertDoesNotThrow( () -> mockedDeveloperRepository.save( singleDeveloperEntity) );
        //Módosítás
        // - Nincs kitöltve az ID
        singleDeveloperEntity.setId(null);
        assertThrows(
            ServiceException.class,
            () -> developerService.modifyDeveloper(singleDeveloperEntity),
            ServiceException.Exceptions.DEVELOPER_NOT_SAVED_CANT_MODIFY.getDescription()
        );
        //- Nincs kitöltve a név
        singleDeveloperEntity.setId(0L);
        singleDeveloperEntity.setName("");
        assertThrows(
            ServiceException.class,
            () -> developerService.modifyDeveloper(singleDeveloperEntity),
            ServiceException.Exceptions.DEVELOPER_WITH_EMPTY_NAME_CANT_MODIFY.getDescription()
        );
        // - Van már ilyen név
        singleDeveloperEntity.setName(mockedListFindAll.get(0).getName());
        mockedListFindByName.get(0).setName(mockedListFindAll.get(0).getName());
        assertThrows(
            ServiceException.class,
            () -> developerService.modifyDeveloper(singleDeveloperEntity),
            ServiceException.Exceptions.DEVELOPER_WITH_SAME_NAME_CANT_MODIFY.getDescription()
        );
        // - Helyes adatokkal
        singleDeveloperEntity.setName("Modified Developer");
        assertDoesNotThrow( () -> mockedDeveloperRepository.save( singleDeveloperEntity) );
        // Törlés
        // - Nincs kitöltve az ID;
        singleDeveloperEntity.setId(null);
        assertThrows(
            ServiceException.class,
            () -> developerService.deleteDeveloper(singleDeveloperEntity),
            ServiceException.Exceptions.DEVELOPER_NOT_SAVED_CANT_DELETE.getDescription()
        );
        // - Hozzá van rendelve egy projekthez
        singleDeveloperEntity.setId(0L);
        singleDeveloperEntity.setDeveloperProjects( new ArrayList<ProjectDevelopersEntity>() {
            {
                add( new ProjectDevelopersEntity() );
            }
        } );
        assertThrows(
            ServiceException.class,
            () -> developerService.deleteDeveloper(singleDeveloperEntity),
            ServiceException.Exceptions.DEVELOPER_WITH_PROJECTS_CANT_DELETE.getDescription()
        );
        // - Jó adatokkal
        singleDeveloperEntity.setDeveloperProjects( new ArrayList<>());
        assertDoesNotThrow( ()-> developerService.deleteDeveloper(singleDeveloperEntity));
        //Mindent ellenőriztünk?
        assertDoesNotThrow(ServiceException::isAllDeveloperExceptionsThrown);
    }
}
