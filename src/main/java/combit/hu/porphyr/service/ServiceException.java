package combit.hu.porphyr.service;

import lombok.Getter;
import lombok.NonNull;
import lombok.Synchronized;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ExecutionException;

import static combit.hu.porphyr.Constants.*;

@ThreadSafe
public class ServiceException extends IllegalArgumentException {
    public enum ExceptionGroups{   UNDEFINED,
                                    PROJECT_INSERT, PROJECT_MODIFY, PROJECT_DELETE,
                                    DEVELOPER_INSERT, DEVELOPER_MODIFY, DEVELOPER_DELETE,
                                    PROJECTTASKS_INSERT, PROJECTTASKS_MODIFY, PROJECTTASKS_DELETE,
                                    PROJECTDEVELOPERS_INSERT, PROJECTDEVELOPERS_DELETE,
                                    PROJECTTASKDEVELOPERS_INSERT, PROJECTTASKDEVELOPERS_MODIFY, PROJECTTASKDEVELOPERS_DELETE }
    public enum Exceptions{
        PROJECT_INSERT_EMPTY_NAME( ExceptionGroups.PROJECT_INSERT, "A projekt neve nincs kitöltve, nem vihető fel!", 0),
        PROJECT_INSERT_SAME_NAME( ExceptionGroups.PROJECT_INSERT, "Van már ilyen nevű projekt, nem vihető fel még egyszer!", 0),
        PROJECT_MODIFY_NOT_SAVED( ExceptionGroups.PROJECT_MODIFY, "A projekt még nincs elmentve, nem módosítható!", 0 ),
        PROJECT_MODIFY_EMPTY_NAME( ExceptionGroups.PROJECT_MODIFY, "A projekt neve nincs kitöltve, nem módosítható!", 0),
        PROJECT_MODIFY_SAME_NAME( ExceptionGroups.PROJECT_MODIFY, "Van már ilyen nevű projekt, a név nem módosítható erre!", 0),
        PROJECT_DELETE_NOT_SAVED( ExceptionGroups.PROJECT_DELETE, "A projekt még nincs elmentve, nem törölhető!", 0 ),
        PROJECT_DELETE_TASKS_ASSIGNED( ExceptionGroups.PROJECT_DELETE, "A projekthez még tartoznak feladatok, nem törölhető!", 0 ),
        PROJECT_DELETE_DEVELOPERS_ASSIGNED( ExceptionGroups.PROJECT_DELETE, "A projekthez még tartoznak fejlesztők, nem törölhető!", 0 ),

        DEVELOPER_INSERT_EMPTY_NAME( ExceptionGroups.DEVELOPER_INSERT, "A fejlesztő neve nincs kitöltve, nem vihető fel!", 0 ),
        DEVELOPER_INSERT_SAME_NAME( ExceptionGroups.DEVELOPER_INSERT, "Van már ilyen nevű fejlesztő, nem vihető fel még egyszer!", 0 ),
        DEVELOPER_MODIFY_NOT_SAVED( ExceptionGroups.DEVELOPER_MODIFY, "A fejlesztő még nincs elmentve, nem módosítható!", 0 ),
        DEVELOPER_MODIFY_EMPTY_NAME( ExceptionGroups.DEVELOPER_MODIFY, "A fejlesztő neve nincs kitöltve, nem módosítható!", 0 ),
        DEVELOPER_MODIFY_SAME_NAME( ExceptionGroups.DEVELOPER_MODIFY, "Van már ilyen nevű fejlesztő, a név nem módosítható erre!", 0 ),
        DEVELOPER_DELETE_NOT_SAVED( ExceptionGroups.DEVELOPER_DELETE, "A fejlesztő még nincs elmentve, nem törölhető!", 0 ),
        DEVELOPER_DELETE_ASSIGNED_TO_PROJECTS( ExceptionGroups.DEVELOPER_DELETE, "A fejlesztő még dolgozik legalább egy projektben, nem törölhető!", 0 ),

        PROJECTTASK_INSERT_PROJECT_NOT_SAVED( ExceptionGroups.PROJECTTASKS_INSERT, "A kiválasztott projekt még nincs elmentve, a feladat nem vihető fel!",0),
        PROJECTTASK_INSERT_PROJECT_NOT_EXISTS( ExceptionGroups.PROJECTTASKS_INSERT, "A kiválasztott projektet törölték, a feladat nem vihető fel!",0),
        PROJECTTASK_INSERT_EMPTY_NAME( ExceptionGroups.PROJECTTASKS_INSERT, "A feladat neve nincs kitöltve, nem vihető fel!",0),
        PROJECTTASK_INSERT_SAME_PROJECT_AND_NAME( ExceptionGroups.PROJECTTASKS_INSERT, "Már van ilyen nevű feladat a projektben, nem vihető fel!",0),

        PROJECTTASK_MODIFY_PROJECT_NOT_SAVED( ExceptionGroups.PROJECTTASKS_MODIFY, "A kiválasztott projektet még nincs elmentve, a feladat nem módosítható!",0),
        PROJECTTASK_MODIFY_PROJECT_NOT_EXISTS( ExceptionGroups.PROJECTTASKS_MODIFY, "A kiválasztott projektet törölték, a feladat nem módosítható!",0),
        PROJECTTASK_MODIFY_NOT_SAVED( ExceptionGroups.PROJECTTASKS_MODIFY, "A feladat még nincs elmentve, nem módosítható!",0),
        PROJECTTASK_MODIFY_EMPTY_NAME( ExceptionGroups.PROJECTTASKS_MODIFY, "A feladat neve nincs kitöltve, nem módosítható!",0),
        PROJECTTASK_MODIFY_SAME_PROJECT_AND_NAME( ExceptionGroups.PROJECTTASKS_MODIFY, "Már van ilyen nevű feladat, a név nem módosítható erre!",0),

        PROJECTTASK_DELETE_NOT_SAVED( ExceptionGroups.PROJECTTASKS_DELETE, "A feladat még nincs elmentve, nem törölhető!",0),
        PROJECTTASK_DELETE_DEVELOPERS_ASSIGNED( ExceptionGroups.PROJECTTASKS_DELETE, "A feladathoz még tartoznak fejlesztők, nem törölhető!",0),

        PROJECTDEVELOPER_INSERT_PROJECT_NOT_SAVED( ExceptionGroups.PROJECTDEVELOPERS_INSERT, "A kiválasztott projekt nincs elmentve, az összerendelés nem végezhető el!", 0),
        PROJECTDEVELOPER_INSERT_PROJECT_NOT_EXISTS( ExceptionGroups.PROJECTDEVELOPERS_INSERT, "A kiválasztott projektet törölték, az összerendelés nem végezhető el!" , 0),
        PROJECTDEVELOPER_INSERT_DEVELOPER_NOT_SAVED( ExceptionGroups.PROJECTDEVELOPERS_INSERT, "A kiválasztott fejlesztő nincs elmentve, az összerendelés nem végezhető el!", 0),
        PROJECTDEVELOPER_INSERT_DEVELOPER_NOT_EXISTS( ExceptionGroups.PROJECTDEVELOPERS_INSERT, "A kiválasztott fejlesztőt törölték, az összerendelés nem végezhető el!" , 0),
        PROJECTDEVELOPER_INSERT_EXISTING_DATA( ExceptionGroups.PROJECTDEVELOPERS_INSERT, "A kiválasztott projektben már dolgozik a kiválasztott fejlesztő!" , 0),

        PROJECTDEVELOPER_DELETE_NOT_SAVED( ExceptionGroups.PROJECTDEVELOPERS_DELETE, "Az összerendelés még nincs elmentve, nem törölhető!",0),
        PROJECTDEVELOPER_DELETE_ASSIGNED_TO_TASK( ExceptionGroups.PROJECTDEVELOPERS_DELETE, "A fejlesztő nem törölhető a projektből, még dolgozik a projekt feladatain!",0),

        PROJECTTASKDEVELOPER_INSERT_PROJECTTASK_NOT_SAVED( ExceptionGroups.PROJECTTASKDEVELOPERS_INSERT, "A kiválasztott feladat nincs elmentve, az összerendelés nem végezhető el!", 0),
        PROJECTTASKDEVELOPER_INSERT_PROJECTTASK_NOT_EXISTS( ExceptionGroups.PROJECTTASKDEVELOPERS_INSERT, "A kiválasztott feladatot törölték, az összerendelés nem végezhető el!", 0),
        PROJECTTASKDEVELOPER_INSERT_PROJECTDEVELOPER_NOT_SAVED( ExceptionGroups.PROJECTTASKDEVELOPERS_INSERT, "A kiválasztott fejlesztő nincs elmentve, az összerendelés nem végezhető el!", 0),
        PROJECTTASKDEVELOPER_INSERT_PROJECTDEVELOPER_NOT_EXISTS( ExceptionGroups.PROJECTTASKDEVELOPERS_INSERT, "A kiválasztott fejlesztőt törölték, az összerendelés nem végezhető el!", 0),
        PROJECTTASKDEVELOPER_INSERT_TASK_OR_DEVELOPER_NOT_IN_PROJECT( ExceptionGroups.PROJECTTASKDEVELOPERS_INSERT, "A feladat és a fejlesztő más-más projekthez tartozik!", 0),
        PROJECTTASKDEVELOPER_INSERT_EXISTING_DATA( ExceptionGroups.PROJECTTASKDEVELOPERS_INSERT, "A kiválasztott feladaton már dolgozik a kiválasztott fejlesztő!", 0),

        PROJECTTASKDEVELOPER_MODIFY_NOT_SAVED( ExceptionGroups.PROJECTTASKDEVELOPERS_MODIFY, "Az összerendelés nincs elmentve, a módosítás nem végezhető el!", 0),
        PROJECTTASKDEVELOPER_MODIFY_TIME_IS_NEGATIVE( ExceptionGroups.PROJECTTASKDEVELOPERS_MODIFY, "Az eltöltött idő nem lehet nullánál kisebb!", 0),

        PROJECTTASKDEVELOPER_DELETE_NOT_SAVED( ExceptionGroups.PROJECTTASKDEVELOPERS_DELETE, "Az összerendelés nincs elmentve, nem törölhető!", 0),
        PROJECTTASKDEVELOPER_DELETE_TIME_NOT_ZERO( ExceptionGroups.PROJECTTASKDEVELOPERS_DELETE, "Az eltöltött idő nem nulla, az összerendelés nem törölhető!", 0),

        UNDEFINED( ExceptionGroups.UNDEFINED, "Undefined", 0),
        NULL_VALUE( ExceptionGroups.UNDEFINED, "Adathiba: Üres érték, nem várt helyen.", 0)
        ;

        @Getter
        private final ExceptionGroups exceptionGroup ;
        @Getter
        private final String description;
        private Integer counter;

        Exceptions( ExceptionGroups exceptionGroup, String description, Integer counter ){
            this.exceptionGroup = exceptionGroup;
            this.description = description;
            this.counter = counter;
        }
        // public String getDescription() return description
        // public ExceptionGroups getExceptionGroup() return exceptionGroup
        @Synchronized
        public void incrementCounter(){ counter++ ;}
        @Synchronized
        public Integer getCounter(){ return counter; }
    }

    public ServiceException(@NonNull Exceptions exception) {
        super( exception.getDescription() );
        exception.incrementCounter();
    }

    public ServiceException(@NonNull String exceptionMessage) {
        super( exceptionMessage );
    }

    public static void handleExecutionException( ExecutionException exception) throws ExecutionException{
        if( exception.getCause() instanceof ServiceException){
            throw (ServiceException) exception.getCause();
        } else {
            throw exception;
        }
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for( Exceptions exception : Exceptions.values() ){
            if( exception.getCounter() > 0) {
                stringBuilder.append(exception.name());
                stringBuilder.append(" = ");
                stringBuilder.append(exception.getCounter());
                stringBuilder.append(System.lineSeparator());
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Vizsgálat: A megadott csoport valamennyi hibalehetősége ellenőrzésre került-e?
     */
    @TestOnly
    public static void isAllExceptionsThrown( final @NonNull ExceptionGroups exceptionGroup ) {
        boolean isAllThrown = true;
        StringBuilder notThrownExceptions = new StringBuilder(NEWLINE);

        notThrownExceptions.append("Exceptions not thrown:");
        notThrownExceptions.append(NEWLINE);
        for( Exceptions exception : Exceptions.values() ){
            if( ( exception.getExceptionGroup().equals( exceptionGroup )) && (exception.getCounter() == 0) ){
                isAllThrown = false;
                notThrownExceptions.append(exception.name());
                notThrownExceptions.append(NEWLINE);
            }
        }
        if (!isAllThrown) {
            notThrownExceptions.append(NEWLINE);
            throw new ServiceException("Exceptions not thrown:" + notThrownExceptions);
        }
    }

    @TestOnly
    public static void initExceptionsCounter() {
        for (Exceptions exception : Exceptions.values()) {
            exception.counter = 0;
        }
    }

}
