package combit.hu.porphyr.repositorytests;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectDevelopersEntity;
import combit.hu.porphyr.repository.DeveloperRepository;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.repository.ProjectDeveloperRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@SpringBootTest
@ActiveProfiles("development")
class ProjectDeveloperRepositoryTest {

    private ProjectDeveloperRepository projectDeveloperRepository;
    private ProjectRepository projectRepository;
    private DeveloperRepository developerRepository;

    @Autowired
    public void setProjectDeveloperRepository(ProjectDeveloperRepository projectDeveloperRepository) {
        this.projectDeveloperRepository = projectDeveloperRepository;
    }

    @Autowired
    public void setProjectRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Autowired
    public void setDeveloperRepository(DeveloperRepository developerRepository) {
        this.developerRepository = developerRepository;
    }

    @Test
    void doTests() {
        //Project biztosítása a felvételhez.
        List<ProjectEntity> projectList = projectRepository.findAll();
        if (projectList.size() == 0) {
            projectRepository.save(new ProjectEntity("Test project", "For project developer repository test"));
        }
        projectList = projectRepository.findAll();
        assertThat(projectList).isNotEmpty();
        ProjectEntity projectEntity = projectList.get(projectList.size() -1);

        //Developer biztosítása a felvételhez.
        List<DeveloperEntity> developerList = developerRepository.findAll();
        if (developerList.size() == 0) {
            developerRepository.save(new DeveloperEntity("Test developer" ));
        }
        developerList = developerRepository.findAll();
        assertThat(developerList).isNotEmpty();
        DeveloperEntity developerEntity = developerList.get(developerList.size() -1);

        List<ProjectDevelopersEntity> expectedProjectDeveloperList = projectDeveloperRepository.findAll();
        int projectDeveloperTestElementIdx = expectedProjectDeveloperList.size();

        //Felvitel
        expectedProjectDeveloperList.add(new ProjectDevelopersEntity(projectEntity, developerEntity));
        projectDeveloperRepository.save(expectedProjectDeveloperList.get(projectDeveloperTestElementIdx));
        assertNotNull(expectedProjectDeveloperList.get(projectDeveloperTestElementIdx).getId());
        List<ProjectDevelopersEntity> actualProjectDeveloperList = projectDeveloperRepository.findAll();
        assertEquals(expectedProjectDeveloperList, actualProjectDeveloperList);
        //Módosítás - nincs.
        //Egy összerendelés
        assertNotNull(expectedProjectDeveloperList.get(projectDeveloperTestElementIdx).getId());
        projectDeveloperRepository.delete(expectedProjectDeveloperList.get(projectDeveloperTestElementIdx));
        expectedProjectDeveloperList.remove(projectDeveloperTestElementIdx);
        actualProjectDeveloperList = projectDeveloperRepository.findAll();
        assertEquals(expectedProjectDeveloperList, actualProjectDeveloperList);
        //Minden developer
        expectedProjectDeveloperList.clear();
        projectDeveloperRepository.deleteAll();
        actualProjectDeveloperList = projectDeveloperRepository.findAll();
        assertEquals(expectedProjectDeveloperList, actualProjectDeveloperList);
        //Felvitt developer törlése
        developerRepository.delete(developerEntity);
        //Felvitt projekt törlése
        projectRepository.delete(projectEntity);
    }
}
