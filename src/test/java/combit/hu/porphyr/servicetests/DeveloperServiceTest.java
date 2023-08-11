package combit.hu.porphyr.servicetests;

import combit.hu.porphyr.domain.DeveloperEntity;
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
        List<DeveloperEntity> mockedDeveloperList = new ArrayList<DeveloperEntity>() {
            {
                add(new DeveloperEntity("Developer"));
            }
        };
        final DeveloperEntity singleDeveloperEntity = new DeveloperEntity("New Developer");
        List<DeveloperEntity> actualDeveloperList;

        when(mockedDeveloperRepository.findAll()).thenReturn(mockedDeveloperList);
        when(mockedDeveloperRepository.save(any(DeveloperEntity.class))).thenReturn(null);
        doNothing().when(mockedDeveloperRepository).deleteById(anyLong());
        doNothing().when(mockedDeveloperRepository).deleteAll();
        //Teljes lekérdezés
        actualDeveloperList = developerService.getDevelopers();
        assertEquals(actualDeveloperList, mockedDeveloperList);
        verify(mockedDeveloperRepository, times(1)).findAll();
        //Felvétel
        mockedDeveloperList.add(singleDeveloperEntity);
        developerService.insertNewDeveloper(singleDeveloperEntity) ;
        verify(mockedDeveloperRepository, times(1)).save(singleDeveloperEntity);
        //Hibás ID beállítása
        singleDeveloperEntity.setId(null);
        mockedDeveloperList.get(0).setId(null);
        //Módosítás
        assertThrows(ServiceException.class, () -> developerService.modifyDeveloper(singleDeveloperEntity), ServiceException.Exceptions.DEVELOPER_NOT_SAVED_CANT_MODIFY.getDescription());
        verify(mockedDeveloperRepository, times(1)).save(singleDeveloperEntity);
        //Törlés
        // - Egy
        assertThrows(ServiceException.class, () -> developerService.deleteDeveloper(singleDeveloperEntity), ServiceException.Exceptions.DEVELOPER_NOT_SAVED_CANT_DELETE.getDescription());
        verify(mockedDeveloperRepository, times(0)).deleteById(anyLong());
        // - Mind
        //Jó ID beállítása
        singleDeveloperEntity.setId(0L);
        mockedDeveloperList.get(0).setId(0L);
        //Módosítás
        assertDoesNotThrow(() -> developerService.modifyDeveloper(singleDeveloperEntity), ServiceException.Exceptions.DEVELOPER_NOT_SAVED_CANT_MODIFY.getDescription());
        verify(mockedDeveloperRepository, times(2)).save(singleDeveloperEntity);
        //Törlés
        // - Egy
        assertDoesNotThrow(() -> developerService.deleteDeveloper(singleDeveloperEntity), ServiceException.Exceptions.DEVELOPER_NOT_SAVED_CANT_DELETE.getDescription());
        verify(mockedDeveloperRepository, times(1)).deleteById(anyLong());
        // - Mind
        developerService.deleteAllDevelopers() ;
        verify(mockedDeveloperRepository, times(1)).deleteAll();
        //Mindent ellenőriztünk?
        assertDoesNotThrow( ServiceException::isAllDeveloperExceptionsThrown );
    }
}
