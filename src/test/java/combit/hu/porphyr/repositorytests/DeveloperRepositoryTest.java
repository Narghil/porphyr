package combit.hu.porphyr.repositorytests;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("development")
class DeveloperRepositoryTest {

    private DeveloperRepository developerRepository;

    @Autowired
    public void setDeveloperRepository(DeveloperRepository developerRepository) {
        this.developerRepository = developerRepository;
    }

    @Test
    void doTests() {
        DeveloperEntity singleDeveloperEntity;
        List<DeveloperEntity> expectedDeveloperList = developerRepository.findAll();
        int testElementIdx = expectedDeveloperList.size();
        //Felvitel
        expectedDeveloperList.add(new DeveloperEntity("Teszt fejlesztő"));
        developerRepository.save(expectedDeveloperList.get(testElementIdx));
        assertNotNull(expectedDeveloperList.get(testElementIdx).getId());
        //Teljes lekérdezés
        List<DeveloperEntity> actualDeveloperList = developerRepository.findAll();
        assertEquals(expectedDeveloperList, actualDeveloperList);
        //Név szerinti lekérdezés
        singleDeveloperEntity = expectedDeveloperList.get(expectedDeveloperList.size() - 1);
        assertThat(developerRepository.findAllByName(singleDeveloperEntity.getName()))
            .isEqualTo(Collections.singletonList(singleDeveloperEntity));
        //Módosítás
        assertNotNull(expectedDeveloperList.get(testElementIdx).getId());
        expectedDeveloperList.get(testElementIdx).setName("Teszt fejlesztő módosított neve");
        developerRepository.save(expectedDeveloperList.get(testElementIdx));
        actualDeveloperList = developerRepository.findAll();
        assertEquals(expectedDeveloperList, actualDeveloperList);
        //Törlés
        assertNotNull(expectedDeveloperList.get(testElementIdx).getId());
        developerRepository.delete(expectedDeveloperList.get(testElementIdx));
        expectedDeveloperList.remove(testElementIdx);
        actualDeveloperList = developerRepository.findAll();
        assertEquals(expectedDeveloperList, actualDeveloperList);
    }
}
