package combit.hu.porphyr.config.controller;

import combit.hu.porphyr.config.domain.PermitEntity;
import combit.hu.porphyr.config.domain.RoleEntity;
import combit.hu.porphyr.config.service.PermitService;
import combit.hu.porphyr.config.service.RoleService;
import combit.hu.porphyr.controller.helpers.TemplateData;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.service.PorphyrServiceException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static combit.hu.porphyr.Constants.ON;
import static combit.hu.porphyr.controller.helpers.ControllerConstants.ERROR_TITLE;

@Controller
public class RolePermitsController {

    private static final @NonNull String SUB_DIR = "rights/";
    private static final String ROLES = SUB_DIR + "roles";
    private static final String ROLE_PERMITS = SUB_DIR + "role_permits";
    private static final String ROLE_PERMITS_NEW = SUB_DIR + "role_permit_new";
    private static final String REDIRECT_TO = "redirect:/";

    private final @NonNull RoleService roleService;
    private final @NonNull PermitService permitService;

    @Autowired
    public RolePermitsController(
        final @NonNull RoleService roleService,
        final @NonNull PermitService permitService
    ) {
        this.roleService = roleService;
        this.permitService = permitService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;

    @RequestMapping("/startRolePermits/{roleId}")
    public String startRolePermits(
        @PathVariable(value = "roleId")
        Long roleId,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        RoleEntity role = roleService.getRoleById(roleId);
        if (role != null) {
            setRolePermitsModel(role, model);
        } else {
            webErrorBean.setError(ON, ERROR_TITLE, PorphyrServiceException.Exceptions.NULL_VALUE.getDescription());
            return REDIRECT_TO + ROLES;
        }
        return ROLE_PERMITS;
    }

    @RequestMapping("/startNewRolePermit/{roleId}")
    public String startNewRolePermit(
        @PathVariable(value = "roleId")
        Long roleId,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        RoleEntity role = roleService.getRoleById(roleId);
        if (role != null) {
            List<PermitEntity> availAblePermits = permitService.getValidPermits();
            availAblePermits.removeAll(role.getPermits());
            TemplateData dataToTemplate = new TemplateData();
            dataToTemplate.setId(roleId);
            model.addAttribute("role", role);
            model.addAttribute("permits", availAblePermits);
            model.addAttribute("dataFromTemplate", dataToTemplate);
        } else {
            webErrorBean.setError(ON, ERROR_TITLE, PorphyrServiceException.Exceptions.NULL_VALUE.getDescription());
            return REDIRECT_TO + ROLES;
        }
        return ROLE_PERMITS_NEW;
    }

    @RequestMapping("/insertNewRolePermit")
    public String insertNewRolePermit(
        final @NonNull TemplateData dataFromTemplate,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        @NonNull
        Long roleId = Objects.requireNonNull(dataFromTemplate.getId());
        @NonNull
        Long permitId = Objects.requireNonNull(dataFromTemplate.getLongData());
        RoleEntity role = roleService.getRoleById(roleId);
        if (role != null) {
            role.getPermits().add(permitService.getPermitById(permitId));
            try {
                roleService.modifyRole(role);
            } catch (PorphyrServiceException porphyrServiceException) {
                webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
                setRolePermitsModel(role, model);
                return ROLE_PERMITS;
            }
        } else {
            webErrorBean.setError(ON, ERROR_TITLE, PorphyrServiceException.Exceptions.NULL_VALUE.getDescription());
            return REDIRECT_TO + ROLES;
        }
        setRolePermitsModel(role, model);
        return ROLE_PERMITS;
    }

    @RequestMapping("/startDeleteRolePermit/{roleId}/{permitId}")
    public String startNewRolePermit(
        @PathVariable(value = "roleId")
        Long roleId,
        @PathVariable(value = "permitId")
        Long permitId,
        final @NonNull Model model
    ) throws ExecutionException, InterruptedException {
        RoleEntity role = roleService.getRoleById(roleId);
        if (role != null) {
            role.getPermits().removeIf((PermitEntity p) -> (p.getId().equals(permitId)));
            try {
                roleService.modifyRole(role);
            } catch (PorphyrServiceException porphyrServiceException) {
                webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
                setRolePermitsModel(role, model);
                return ROLE_PERMITS;
            }
        } else {
            webErrorBean.setError(ON, ERROR_TITLE, PorphyrServiceException.Exceptions.NULL_VALUE.getDescription());
            return REDIRECT_TO + ROLES;
        }
        setRolePermitsModel(role, model);
        return ROLE_PERMITS;
    }

    private void setRolePermitsModel(final @NonNull RoleEntity role, final @NonNull Model model) {
        ArrayList<PermitEntity> permitsList =
            role.getPermits()
                .stream().filter(PermitEntity::getUsable)
                .sorted(Comparator.comparing(PermitEntity::getName))
                .collect(Collectors.toCollection(ArrayList::new));
        model.addAttribute("roleId", role.getId());
        model.addAttribute("roleName", role.getRole());
        model.addAttribute("permits", permitsList);
    }
}
