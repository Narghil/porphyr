package combit.hu.porphyr.config.controller;

import combit.hu.porphyr.config.domain.RoleEntity;
import combit.hu.porphyr.config.domain.UserEntity;
import combit.hu.porphyr.config.service.RoleService;
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
public class UserRolesController {

    private static final @NonNull String SUB_DIR = "rights/";
    private static final String ERROR = "error";
    private static final String USER_ROLES = SUB_DIR + "user_roles";
    private static final String USER_ROLE_NEW = SUB_DIR + "user_role_new";

    private final @NonNull UserService userService;
    private final @NonNull RoleService roleService;

    @Autowired
    public UserRolesController(
        final @NonNull UserService userService,
        final @NonNull RoleService roleService
    ) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;

    @RequestMapping("/startUserRoles/{userId}")
    public String startUserRoles(
        @PathVariable(value = "userId")
        Long userId,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        UserEntity user = userService.getUserById(userId);
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute("user", user);
        return USER_ROLES;
    }

    @RequestMapping("/startDeleteUserRole/{userId}/{roleId}")
    public String starDeleteUserRole(
        @PathVariable(value = "userId")
        Long userId,
        @PathVariable(value = "roleId")
        Long roleId,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        UserEntity user = userService.getUserById(userId);
        if (user != null) {
            user.getRoles().removeIf(role -> (Objects.equals(role.getId(), roleId)));
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

    @RequestMapping("/startNewUserRole/{userId}")
    public String starDeleteUserRole(
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
            List<RoleEntity> roles = roleService.getRoles();
            roles.removeAll( user.getRoles() );

            model.addAttribute(ERROR, webErrorBean.getWebErrorData());
            model.addAttribute("userName", user.getFullName());
            model.addAttribute("dataFromTemplate", dataToTemplate);
            model.addAttribute("roles", roles);
        } else {
            webErrorBean.setError(ON, ERROR_TITLE, PorphyrServiceException.Exceptions.NULL_VALUE.getDescription());
            model.addAttribute("user", user);
            result = USER_ROLES;
        }

        return result;
    }

    @RequestMapping("/newUserRoleDataProcessing")
    public String newUserRoleDataProcessing(
        @ModelAttribute
        @NonNull
        final TemplateData dataFromTemplate,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        final @NonNull Long userId = Objects.requireNonNull( dataFromTemplate.getId() );
        final @NonNull Long roleId = Objects.requireNonNull( dataFromTemplate.getLongData());
        UserEntity user = userService.getUserById(userId);
        RoleEntity role = roleService.getRoleById(roleId);
        if( user != null && role != null ) {
            user.getRoles().add(role);
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
