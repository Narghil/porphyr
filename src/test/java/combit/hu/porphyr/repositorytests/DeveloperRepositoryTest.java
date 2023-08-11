package combit.hu.porphyr.repositorytests;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DeveloperRepositoryTest {

    private DeveloperRepository developerRepository;

    @Autowired
    public void setDeveloperRepository(DeveloperRepository developerRepository) {
        this.developerRepository = developerRepository;
    }

    @Test
    void doTests() {
        DeveloperEntity emptyDeveloperEntity = new DeveloperEntity();
        List<DeveloperEntity> expectedDeveloperList = developerRepository.findAll();
        int firstTestElementIdx = expectedDeveloperList.size();
        int secondTestElementIdx = firstTestElementIdx + 1;

        //Felvitel
        expectedDeveloperList.add(new DeveloperEntity("Első teszt fejlesztő"));
        expectedDeveloperList.add(new DeveloperEntity("Második teszt fejlesztő"));
        developerRepository.save(expectedDeveloperList.get(firstTestElementIdx));
        assertNotNull(expectedDeveloperList.get(firstTestElementIdx).getId());
        developerRepository.save(expectedDeveloperList.get(secondTestElementIdx));
        assertNotNull(expectedDeveloperList.get(secondTestElementIdx).getId());
        List<DeveloperEntity> actualDeveloperList = developerRepository.findAll();
        assertEquals(expectedDeveloperList, actualDeveloperList);
        // - hibás felvitel: Nincs kitöltve a név
        assertThrows(Exception.class, () -> developerRepository.save(emptyDeveloperEntity));
        //Módosítás
        expectedDeveloperList.get(secondTestElementIdx).setName("Második teszt fejlesztő módosított neve");
        developerRepository.save(expectedDeveloperList.get(secondTestElementIdx));
        actualDeveloperList = developerRepository.findAll();
        assertEquals(expectedDeveloperList, actualDeveloperList);
        //Törlés
        // - Nem létező fejlesztő törlése
        developerRepository.delete(emptyDeveloperEntity);
        // - Egy fejlesztő törlése
        assertNotNull(expectedDeveloperList.get(secondTestElementIdx).getId());
        developerRepository.delete(expectedDeveloperList.get(secondTestElementIdx));
        expectedDeveloperList.remove(secondTestElementIdx);
        actualDeveloperList = developerRepository.findAll();
        assertEquals(expectedDeveloperList, actualDeveloperList);
        // - Minden fejlesztő törlése
        expectedDeveloperList.clear();
        developerRepository.deleteAll();
        actualDeveloperList = developerRepository.findAll();
        assertEquals(expectedDeveloperList, actualDeveloperList);
    }
}
