package combit.hu.porphyr.config.controller;

import combit.hu.porphyr.config.domain.RoleEntity;
import combit.hu.porphyr.config.domain.UserEntity;
import combit.hu.porphyr.config.service.RoleService;
import combit.hu.porphyr.config.service.UserService;
import combit.hu.porphyr.controller.helpers.SessionData;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class RightsController {

    private static final @NonNull String SUB_DIR = "rights/";
    private final @NonNull UserService userService;
    private final @NonNull RoleService roleService;

    @Autowired
    public RightsController(
        final @NonNull UserService userService,
        final @NonNull RoleService roleService
    ){
        this.userService = userService;
        this.roleService = roleService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;

    @Resource(name = "getSessionData")
    SessionData sessionData;

    private static final String ERROR = "error";

    @RequestMapping( "/rights" )
    public String rights(final @NonNull Model model) {
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute("userPermitNames", sessionData.getUserPermitNames() );
        return SUB_DIR + "rights";
    }

    @RequestMapping( "/rights/users" )
    public String users(final @NonNull Model model) throws ExecutionException, InterruptedException {
        final List<UserEntity> users = userService.getUsers();
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute( "users", users );
        return SUB_DIR + "users";
    }

    @RequestMapping( "/rights/roles" )
    public String roles(final @NonNull Model model) throws ExecutionException, InterruptedException {
        final List<RoleEntity> roles = roleService.getRoles();
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute( "roles", roles );
        return SUB_DIR + "roles";
    }

}
