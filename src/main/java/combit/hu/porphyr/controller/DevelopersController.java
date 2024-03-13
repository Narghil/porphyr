package combit.hu.porphyr.controller;

import combit.hu.porphyr.controller.helpers.SessionData;
import combit.hu.porphyr.controller.helpers.TemplateData;
import combit.hu.porphyr.controller.helpers.WebErrorBean;
import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.service.DeveloperService;
import combit.hu.porphyr.service.PorphyrServiceException;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.Constants.ON;
import static combit.hu.porphyr.controller.helpers.HomeControllerConstants.*;

@Controller
public class DevelopersController {

    private final @NonNull DeveloperService developerService;

    @Autowired
    public DevelopersController(final @NonNull DeveloperService developerService) {
        this.developerService = developerService;
    }

    @Resource(name = "getWebErrorBean")
    WebErrorBean webErrorBean;
    @Resource(name = "getSessionData")
    SessionData sessionData;

    static final @NonNull String REDIRECT_TO_DEVELOPERS = "redirect:/developers";
    static final @NonNull String REDIRECT_TO_DEVELOPER_MODIFY = "redirect:/developer_modify";
    static final @NonNull String REDIRECT_TO_DEVELOPER_TASKS = "redirect:/developer_tasks";
    static final @NonNull String REDIRECT_TO_DEVELOPER_NEW = "redirect:/developer_new";
    static final @NonNull String ERROR = "error";
    static final @NonNull String DATA_FROM_TEMPLATE = "dataFromTemplate";

    //------------------ Műveletválasztó ----------------------------------------
    @RequestMapping("/selectDeveloperOperation")
    public @NonNull String selectOperation(
        @ModelAttribute
        @NonNull
        final TemplateData dataFromTemplate
    ) throws ExecutionException, InterruptedException {
        @NonNull String result;
        @Nullable
        final Long developerId = dataFromTemplate.getId();
        if (developerId == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        @Nullable DeveloperEntity developer = developerService.getDeveloperById(dataFromTemplate.getId());
        if (developer == null) {
            throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        sessionData.setSelectedDeveloperId(developerId);
        switch (dataFromTemplate.getOperation()) {
            case MENU_ITEM_TASKS: {
                result = REDIRECT_TO_DEVELOPER_TASKS;
                break;
            }
            case MENU_ITEM_MODIFY: {
                result = startModifyDeveloper();
                break;
            }
            case MENU_ITEM_DELETE: {
                deleteDeveloper();
                result = REDIRECT_TO_DEVELOPERS;
                break;
            }
            default:
                throw new PorphyrServiceException(PorphyrServiceException.Exceptions.NULL_VALUE);
        }
        return result;
    }

    //------------------ Új fejlesztő felvétele ---------------------------------
    @RequestMapping("/developers_new_start")
    public @NonNull String startNewDeveloper(final @NonNull Model model) {
        sessionData.getDataToTemplate().setName(null);
        return newDeveloper(model);
    }

    @RequestMapping("/developer_new")
    public @NonNull String newDeveloper(
        final @NonNull Model model
    ) {
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute(DATA_FROM_TEMPLATE, sessionData.getDataToTemplate());
        return "developer_new";
    }

    @RequestMapping("/insertNewDeveloper")
    public @NonNull String insertNewDeveloper(
        @ModelAttribute
        @NonNull
        final TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        @NonNull String result = REDIRECT_TO_DEVELOPERS;
        @Nullable String developerName = dataFromTemplate.getName();
        sessionData.setDataFromTemplate(dataFromTemplate);
        if (developerName == null) {
            webErrorBean.setError(
                ON,
                ERROR_TITLE,
                PorphyrServiceException.Exceptions.DEVELOPER_INSERT_EMPTY_NAME.getDescription()
            );
        } else {
            try {
                developerService.insertNewDeveloper(
                    new DeveloperEntity(developerName)
                );
            } catch (PorphyrServiceException porphyrServiceException) {
                webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
                result = REDIRECT_TO_DEVELOPER_NEW;
            }
        }
        return result;
    }

    //------------------------ Fejlesztő módosítása
    public @NonNull String startModifyDeveloper()
        throws InterruptedException, ExecutionException
    {
        final @NonNull DeveloperEntity developer = sessionData.getSelectedDeveloper();
        sessionData.getDataFromTemplate().setId(developer.getId());
        sessionData.getDataFromTemplate().setName(developer.getName());
        return REDIRECT_TO_DEVELOPER_MODIFY;
    }

    @RequestMapping("/developer_modify")
    public @NotNull String loadDataBeforeModifyDeveloper(
        final @NonNull Model model
    ) {
        sessionData.moveDataFromToTemplate();
        final @NotNull TemplateData dataToTemplate = sessionData.getDataToTemplate();
        model.addAttribute(ERROR, webErrorBean.getWebErrorData());
        model.addAttribute(DATA_FROM_TEMPLATE, dataToTemplate);

        return "developer_modify";
    }

    @PostMapping("/modifyDeveloper")
    public @NonNull String modifyDeveloper(
        @ModelAttribute
        @NonNull TemplateData dataFromTemplate
    ) throws InterruptedException, ExecutionException {
        @NonNull String result = REDIRECT_TO_DEVELOPERS;
        sessionData.setDataFromTemplate(dataFromTemplate);

        final @Nullable DeveloperEntity modifiedDeveloper = sessionData.getSelectedDeveloper();
        modifiedDeveloper.setName(Objects.requireNonNull(dataFromTemplate.getName()));

        try {
            developerService.modifyDeveloper(modifiedDeveloper);
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
            result = REDIRECT_TO_DEVELOPER_MODIFY;
        }

        return result;
    }

    //------------------------ Fejlesztő törlése
    public void deleteDeveloper(
    ) throws InterruptedException, ExecutionException {
        final @Nullable DeveloperEntity developer = sessionData.getSelectedDeveloper();
        try {
            developerService.deleteDeveloper(developer);
        } catch (PorphyrServiceException porphyrServiceException) {
            webErrorBean.setError(ON, ERROR_TITLE, porphyrServiceException.getMessage());
        }
    }
}
