package combit.hu.porphyr.config;

import combit.hu.porphyr.config.domain.PermitEntity;
import combit.hu.porphyr.config.service.PermitService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.Constants.log;

/**
 * A PERMITS táblát aktualizálja a RequestConstants.PERMITS alapján
 */
@Component
public class LoadDataAtStartUp {
    private final @NonNull PermitService permitService;

    @Autowired
    public LoadDataAtStartUp(
        final @NonNull PermitService permitService
    ) {
        this.permitService = permitService;
    }

    //Method 'loadData()' is never used - Hibás riasztás!
    @EventListener(ApplicationReadyEvent.class)
    public void loadData() throws ExecutionException, InterruptedException {
        final @NonNull List<PermitEntity> permitsFromDB = permitService.getPermits();
        final @NonNull Set<String> permitKeys = RequestsConstants.PERMITS.keySet();
        String logInfo;
        for (PermitEntity permitEntity : permitsFromDB) {
            if (!permitKeys.contains(permitEntity.getName()) && permitEntity.getUsable()) {
                permitEntity.setUsable(false);
                permitService.modifyPermit(permitEntity);
                logInfo = "Unusable permit: " + permitEntity.getName();
                log.info(logInfo);
            }
        }
        for (String permitKey : permitKeys) {
            if (permitsFromDB.stream().noneMatch(p -> (p.getName().equals(permitKey)))) {
                @NonNull
                PermitEntity permitEntity = new PermitEntity();
                permitEntity.setName(permitKey);
                permitEntity.setDescription(RequestsConstants.PERMITS.get(permitKey));
                permitEntity.setUsable(true);
                permitService.insertNewPermit(permitEntity);
                logInfo = "New permit: " + permitKey;
                log.info(logInfo);
            }
        }
        for (PermitEntity permitEntity : permitsFromDB) {
            String validDescription = RequestsConstants.PERMITS.get(permitEntity.getName());
            if (validDescription != null && !permitEntity.getDescription().equals(validDescription)) {
                logInfo = "Modified permit description: " + permitEntity.getName() + " : "
                    + permitEntity.getDescription() + " => " + validDescription
                ;
                permitEntity.setDescription(validDescription);
                permitService.modifyPermit(permitEntity);
                log.info(logInfo);
            }
        }
    }
}