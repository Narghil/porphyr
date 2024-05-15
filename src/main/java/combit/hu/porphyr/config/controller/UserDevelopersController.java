package combit.hu.porphyr.config.controller;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.config.domain.UserEntity;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.config.service.UserService;
import combit.hu.porphyr.controller.helpers.TemplateData;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.service.PorphyrServiceException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.Constants.ON;
import static combit.hu.porphyr.controller.helpers.ControllerConstants.ERROR_TITLE;

@Controller
public class UserDevelopersController {

    private static final @NonNull String SUB_DIR = "rights/";
    private static final String ERROR = "error";
    private static final String USER_ROLES = SUB_DIR + "user_developers";
    private static final String USER_ROLE_NEW = SUB_DIR + "user_developer_new";

    private final @NonNull UserService userService;
    private final @NonNull DeveloperService developerService;

    @Autowired
    public UserDevelopersController(
        final @NonNull UserService userService,
        final @NonNull DeveloperService developerService
    ) {
        this.userService = userService;
        this.developerService = developerService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;

    @RequestMapping("/startUserDevelopers/{userId}")
    public String startUserDevelopers(
        @PathVariable(value = "userId")
        Long userId,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        UserEntity user = userService.getUserById(userId);
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute("user", user);
        return USER_ROLES;
    }

    @RequestMapping("/startDeleteUserDeveloper/{userId}/{developerId}")
    public String starDeleteUserDeveloper(
        @PathVariable(value = "userId")
        Long userId,
        @PathVariable(value = "developerId")
        Long developerId,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        UserEntity user = userService.getUserById(userId);
        if (user != null) {
            user.getDevelopers().removeIf(developer -> (Objects.equals(developer.getId(), developerId)));
            try {
                userService.modifyUser(user);
            } catch (PorphyrServiceException porphyrServiceException) {
                webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
            }
        } else {
            webErrorBean.setError(ON, ERROR_TITLE, PorphyrServiceException.Exceptions.NULL_VALUE.getDescription());
        }
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute("user", user);
        return USER_ROLES;
    }

    @RequestMapping("/startNewUserDeveloper/{userId}")
    public String starDeleteUserDeveloper(
        @PathVariable(value = "userId")
        Long userId,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        String result = USER_ROLE_NEW;
        UserEntity user = userService.getUserById(userId);
        if (user != null) {
            model.addAttribute("userName", user.getFullName());
            TemplateData dataToTemplate = new TemplateData();
            dataToTemplate.setId(userId);
            List<DeveloperEntity> developers = developerService.getDevelopers();
            developers.removeAll( user.getDevelopers() );

            model.addAttribute(ERROR, webErrorBean.getWebErrorData());
            model.addAttribute("userName", user.getFullName());
            model.addAttribute("dataFromTemplate", dataToTemplate);
            model.addAttribute("developers", developers);
        } else {
            webErrorBean.setError(ON, ERROR_TITLE, PorphyrServiceException.Exceptions.NULL_VALUE.getDescription());
            model.addAttribute("user", user);
            result = USER_ROLES;
        }

        return result;
    }

    @RequestMapping("/newUserDeveloperDataProcessing")
    public String newUserDeveloperDataProcessing(
        @ModelAttribute
        @NonNull
        final TemplateData dataFromTemplate,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        final @NonNull Long userId = Objects.requireNonNull( dataFromTemplate.getId() );
        final @NonNull Long developerId = Objects.requireNonNull( dataFromTemplate.getLongData());
        UserEntity user = userService.getUserById(userId);
        DeveloperEntity developer = developerService.getDeveloperById(developerId);
        if( user != null && developer != null ) {
            user.getDevelopers().add(developer);
            try {
                userService.modifyUser(user);
            } catch (PorphyrServiceException porphyrServiceException) {
                webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
            }
        } else {
            webErrorBean.setError(ON, ERROR_TITLE, PorphyrServiceException.Exceptions.NULL_VALUE.getDescription());
        }
        model.addAttribute("user", user);

        return USER_ROLES;
    }
}
