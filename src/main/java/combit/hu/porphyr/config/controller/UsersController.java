package combit.hu.porphyr.config.controller;

import combit.hu.porphyr.config.domain.UserEntity;
import combit.hu.porphyr.config.service.UserService;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.service.PorphyrServiceException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.Constants.ON;
import static combit.hu.porphyr.controller.helpers.ControllerConstants.ERROR_TITLE;

@Controller
public class UsersController {

    private static final @NonNull String SUB_DIR = "rights/";
    private static final String ERROR = "error";
    private static final String USERS = SUB_DIR + "users";
    private static final String USERS_INPUT = SUB_DIR + "users_input_form";
    private static final String REDIRECT_TO = "redirect:/";

    private final @NonNull UserService userService;

    @Autowired
    public UsersController(final @NonNull UserService userService) {
        this.userService = userService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;

    @RequestMapping("/startNewUser")
    public String startNewUser(final @NonNull Model model) {
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute("user", new UserEntity());
        return USERS_INPUT;
    }

    @RequestMapping("/startModifyUser/{id}")
    public String startModifyUser(
        @PathVariable(value = "id")
        Long id,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        model.addAttribute("user", userService.getUserById( id ) );
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        return USERS_INPUT ;
    }

    @RequestMapping("/startDeleteUser/{id}")
    public String startDeleteUser(
        @PathVariable(value = "id")
        Long id,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        UserEntity user = userService.getUserById(id);
        try {
            if( user != null) userService.deleteUser( user );
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
        }
        return REDIRECT_TO + USERS;
    }

    @RequestMapping("/userInputDataProcessing")
    public String userInputDataProcessing(
        @ModelAttribute
        @NonNull
        final UserEntity.Pojo userData,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        @NonNull String result = REDIRECT_TO + USERS;
        try {
            if (userData.getId() == null) {
                userData.setPassword("{bcrypt}" + new BCryptPasswordEncoder().encode(userData.getNewPassword()));
                final @NonNull UserEntity userEntity = new UserEntity(userData);
                userService.insertNewUser(userEntity);
            } else {
                final UserEntity userEntity = userService.getUserById( Objects.requireNonNull( userData.getId() ) );
                if( userEntity != null ) {
                    userData.setPassword( userEntity.getPassword() );
                    userEntity.readPojo( userData );
                    if (userData.getNewPassword() != null && !userData.getNewPassword().isEmpty() ) {
                        userEntity.setPassword("{bcrypt}" + new BCryptPasswordEncoder().encode(userData.getNewPassword()));
                    }
                    userService.modifyUser(userEntity);
                }
            }
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
            model.addAttribute(ERROR, webErrorBean.getWebErrorData());
            model.addAttribute("user", userData);
            result = USERS_INPUT;
        }
        return result;
    }
}
