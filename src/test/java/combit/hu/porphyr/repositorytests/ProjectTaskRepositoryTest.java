package combit.hu.porphyr.repositorytests;

import combit.hu.porphyr.domain.ProjectEntity;
import combit.hu.porphyr.domain.ProjectTasksEntity;
import combit.hu.porphyr.repository.ProjectRepository;
import combit.hu.porphyr.repository.ProjectTaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@SpringBootTest
@ActiveProfiles("development")
class ProjectTaskRepositoryTest {

    private ProjectTaskRepository projectTaskRepository;
    private ProjectRepository projectRepository;

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository) {
        this.projectTaskRepository = projectTaskRepository;
    }

    @Autowired
    public void setProjectRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Test
    void doTests() {
        //Project biztosítása a task-ok felvételéhez.
        List<ProjectEntity> projectList = projectRepository.findAll();
        if (projectList.size() == 0) {
            projectRepository.save(new ProjectEntity("Test project", "For project task repository test"));
        }
        projectList = projectRepository.findAll();
        assertThat(projectList).isNotEmpty();
        ProjectEntity projectEntity = projectList.get( projectList.size() -1);

        List<ProjectTasksEntity> expectedProjectTaskList = projectTaskRepository.findAll();
        int firstTestElementIdx = expectedProjectTaskList.size();
        int secondTestElementIdx = firstTestElementIdx + 1;
        //Felvitel
        expectedProjectTaskList.add(new ProjectTasksEntity(projectEntity, "Első teszt projekt", "Ez az első teszt projekt leírása."));
        expectedProjectTaskList.add(new ProjectTasksEntity(projectEntity, "Második teszt projekt", "Ez a második teszt projekt leírása."));
        projectTaskRepository.save(expectedProjectTaskList.get(firstTestElementIdx));
        assertNotNull(expectedProjectTaskList.get(firstTestElementIdx).getId());
        projectTaskRepository.save(expectedProjectTaskList.get(secondTestElementIdx));
        assertNotNull(expectedProjectTaskList.get(secondTestElementIdx).getId());
        List<ProjectTasksEntity> actualProjectTaskList = projectTaskRepository.findAll();
        assertEquals(expectedProjectTaskList, actualProjectTaskList);
        //Módosítás
        expectedProjectTaskList.get(secondTestElementIdx).setName("Második teszt projekt módosított neve");
        expectedProjectTaskList.get(secondTestElementIdx).setDescription("Ez a második teszt projekt módosított leírása.");
        projectTaskRepository.save(expectedProjectTaskList.get(secondTestElementIdx));
        actualProjectTaskList = projectTaskRepository.findAll();
        assertEquals(expectedProjectTaskList, actualProjectTaskList);
        //Törlés
        //Egy task
        assertNotNull(expectedProjectTaskList.get(secondTestElementIdx).getId());
        projectTaskRepository.delete(expectedProjectTaskList.get(secondTestElementIdx));
        expectedProjectTaskList.remove(secondTestElementIdx);
        actualProjectTaskList = projectTaskRepository.findAll();
        assertEquals(expectedProjectTaskList, actualProjectTaskList);
        //Minden task
        expectedProjectTaskList.clear();
        projectTaskRepository.deleteAll();
        actualProjectTaskList = projectTaskRepository.findAll();
        assertEquals(expectedProjectTaskList, actualProjectTaskList);
        //Projekt törlése
        projectRepository.delete( projectEntity );
    }
}
