package combit.hu.porphyr.controller;

import combit.hu.porphyr.domain.DeveloperEntity;
import combit.hu.porphyr.service.DeveloperService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;
import static combit.hu.porphyr.RequestsConstants.*;

/**
 * Példa osztály RestController-re.
 */
@RestController
public class ApiController {

    private final @NonNull DeveloperService developerService;

    @Autowired
    public ApiController(final @NonNull DeveloperService developerService) {
        this.developerService = developerService;
    }

    @RequestMapping( CALL_RAW_DEVELOPER_LIST )
    public @NonNull String developers() throws ExecutionException, InterruptedException {
        List<DeveloperEntity> developerList;
        developerList = developerService.getDevelopers();
        return developerList.toString();
    }
}
