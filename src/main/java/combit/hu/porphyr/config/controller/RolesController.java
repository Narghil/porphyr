package combit.hu.porphyr.config.controller;

import combit.hu.porphyr.config.domain.RoleEntity;
import combit.hu.porphyr.config.service.RoleService;
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
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.Constants.ON;
import static combit.hu.porphyr.controller.helpers.ControllerConstants.ERROR_TITLE;

@Controller
public class RolesController {

    private static final @NonNull String SUB_DIR = "rights/";
    private static final String ERROR = "error";
    private static final String ROLES = SUB_DIR + "roles";
    private static final String ROLES_INPUT = SUB_DIR + "roles_input_form";
    private static final String REDIRECT_TO = "redirect:/";

    private final @NonNull RoleService roleService;

    @Autowired
    public RolesController(final @NonNull RoleService roleService) {
        this.roleService = roleService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;

    @RequestMapping("/startNewRole")
    public String startNewRole(final @NonNull Model model) {
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute("role", new RoleEntity());
        return ROLES_INPUT;
    }

    @RequestMapping("/startModifyRole/{id}")
    public String startModifyRole(
        @PathVariable(value = "id")
        Long id,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        model.addAttribute("role", roleService.getRoleById( id ) );
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        return ROLES_INPUT ;
    }

    @RequestMapping("/startDeleteRole/{id}")
    public String startDeleteRole(
        @PathVariable(value = "id")
        Long id,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        RoleEntity role = roleService.getRoleById(id);
        try {
            if( role != null) roleService.deleteRole( role );
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
        }
        return REDIRECT_TO + ROLES;
    }

    @RequestMapping("/roleInputDataProcessing")
    public String roleInputDataProcessing(
        @ModelAttribute
        final @NonNull RoleEntity.Pojo roleData,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        @NonNull String result = REDIRECT_TO + ROLES;
        try {
            if (roleData.getId() == null) {
                roleService.insertNewRole(new RoleEntity(roleData));
            } else {
                final RoleEntity roleEntity = roleService.getRoleById( Objects.requireNonNull( roleData.getId() ) );
                if( roleEntity != null) {
                    roleEntity.readPojo(roleData);
                    roleService.modifyRole(roleEntity);
                }
            }
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
            model.addAttribute(ERROR, webErrorBean.getWebErrorData());
            model.addAttribute("role", roleData);
            result = ROLES_INPUT;
        }
        return result;
    }
}
