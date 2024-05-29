package combit.hu.porphyr.controller;

import combit.hu.porphyr.controller.helpers.SessionData;
import combit.hu.porphyr.controller.helpers.TemplateData;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.domain.PostEntity;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.PorphyrServiceException;
import combit.hu.porphyr.service.PostService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.controller.helpers.ControllerConstants.*;

@Controller
public class ProjectsTasksPostsController {

    private final @NonNull PostService postService;
    private final @NonNull DeveloperService developerService;
    public static final @NonNull String PROJECT_NAME = "projectName";

    @Autowired
    public ProjectsTasksPostsController(
        final @NonNull PostService postService,
        final @NonNull DeveloperService developerService
    ) {
        this.postService = postService;
        this.developerService = developerService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;
    @Resource(name = "getSessionData")
    SessionData sessionData;

    static final @NonNull String REDIRECT_TO_PROJECTTASKS_POSTS = "redirect:/project_tasks_posts";
    static final @NonNull String ERROR = "error";

    //------------------ Projektfeladat post-jai ---------------------------------
    @RequestMapping("/project_tasks_posts")
    public @NonNull String projectTaskPosts(final @NonNull Model model)
        throws ExecutionException, InterruptedException {
        // -- Post felvételekor szerzőnek választhatók a feladathoz tartozó developer-ek közül azok,
        //    akiknek a nevében a bejelentkező user eljárhat.
        List<DeveloperEntity> developers = developerService.getDevelopersByProjectTask(sessionData.getSelectedProjectTask());
        developers.retainAll(sessionData.getUserDevelopers());
        TemplateData templateData = sessionData.getDataFromTemplate();
        templateData.setOperation(0);

        model.addAttribute(PROJECT_NAME, sessionData.getSelectedProject().getName());
        model.addAttribute("projectTaskName", sessionData.getSelectedProjectTask().getName());
        model.addAttribute("posts", sessionData.getSelectedProjectTask().getProjectTaskPosts());
        model.addAttribute("developers", developers);
        model.addAttribute("dataFromTemplate", templateData);
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());

        return "project_tasks_posts";
    }

    //------------------------ Műveletválasztó -------------------------------------------
    @PostMapping("/selectProjectTaskPostOperation")
    public @NonNull String selectProjectTaskPostOperation(
        @ModelAttribute
        @NonNull
        final TemplateData dataFromTemplate
    ) throws ExecutionException, InterruptedException {
        @NonNull
        final String result = REDIRECT_TO_PROJECTTASKS_POSTS;
        switch (dataFromTemplate.getOperation()) {
            case MENU_ITEM_INSERT: {
                @NonNull
                final PostEntity postEntity = new PostEntity();
                @NonNull
                final Timestamp created = Timestamp.from(Instant.now());
                postEntity.setCreated(created);
                postEntity.setDeveloperEntity(
                    Objects.requireNonNull(developerService.getDeveloperById(
                        Objects.requireNonNull(dataFromTemplate.getLongData()))
                    )
                );
                postEntity.setProjectTaskEntity(sessionData.getSelectedProjectTask());
                postEntity.setDescription(dataFromTemplate.getDescription());
                postService.insertNewPost(postEntity);
                break;
            }
            case MENU_ITEM_MODIFY: {
                @NonNull
                final PostEntity postEntity =
                    Objects.requireNonNull(postService.getPostById(Objects.requireNonNull(dataFromTemplate.getId())));
                postEntity.setDescription(dataFromTemplate.getDescription());
                postService.modifyPost(postEntity);
                break;
            }
            case MENU_ITEM_DELETE: {
                @NonNull
                final PostEntity postEntity =
                    Objects.requireNonNull(postService.getPostById(Objects.requireNonNull(dataFromTemplate.getId())));
                postService.deletePost(postEntity);
                break;
            }
            default:
                throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        return result;
    }
}
