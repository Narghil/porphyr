package combit.hu.porphyr.repositorytests;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("development")
class ProjectRepositoryTest {
    private ProjectRepository projectRepository;

    @Autowired
    public void setProjectRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Test
    void doTests() {
        List<ProjectEntity> expectedProjectList = projectRepository.findAll();
        int firstTestElementIdx = expectedProjectList.size();
        int secondTestElementIdx = firstTestElementIdx + 1;

        //Felvitel
        expectedProjectList.add(new ProjectEntity("Első teszt projekt", "Ez az első teszt projekt leírása."));
        expectedProjectList.add(new ProjectEntity("Második teszt projekt", "Ez a második teszt projekt leírása."));
        projectRepository.save(expectedProjectList.get(firstTestElementIdx));
        assertNotNull(expectedProjectList.get(firstTestElementIdx).getId());
        projectRepository.save(expectedProjectList.get(secondTestElementIdx));
        assertNotNull(expectedProjectList.get(secondTestElementIdx).getId());
        List<ProjectEntity> actualProjectList = projectRepository.findAll();
        assertEquals(expectedProjectList, actualProjectList);
        //Módosítás
        expectedProjectList.get(secondTestElementIdx).setName("Második teszt projekt módosított neve");
        expectedProjectList.get(secondTestElementIdx).setDescription("Ez a második teszt projekt módosított leírása.");
        projectRepository.save(expectedProjectList.get(secondTestElementIdx));
        actualProjectList = projectRepository.findAll();
        assertEquals(expectedProjectList, actualProjectList);
        //Törlés
        //Egy projekt
        assertNotNull(expectedProjectList.get(secondTestElementIdx).getId());
        projectRepository.delete(expectedProjectList.get(secondTestElementIdx));
        expectedProjectList.remove(secondTestElementIdx);
        actualProjectList = projectRepository.findAll();
        assertEquals(expectedProjectList, actualProjectList);
        //Minden projekt
        expectedProjectList.clear();
        projectRepository.deleteAll();
        actualProjectList = projectRepository.findAll();
        assertEquals(expectedProjectList, actualProjectList);
    }
}