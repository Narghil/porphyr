package combit.hu.porphyr.service;

import lombok.NonNull;
import lombok.Synchronized;
import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.List;

public class ServiceException extends IllegalArgumentException {
    private enum ExceptionGroups{ UNDEFINED, PROJECT, DEVELOPER, PROJECTTASKS, PROJECTDEVELOPERS, PROJECTTASKDEVELOPERS }
    public enum Exceptions{
        PROJECT_NOT_SAVED_CANT_DELETE( ExceptionGroups.PROJECT, "A projekt még nincs elmentve, nem törölhető!", 0 ),
        PROJECT_NOT_SAVED_CANT_MODIFY( ExceptionGroups.PROJECT, "A projekt még nincs elmentve, nem módosítható!", 0 ),
        PROJECT_WITH_TASKS_CANT_DELETE( ExceptionGroups.PROJECT, "A projekthez még tartoznak feladatok, nem törölhető!", 0 ),
        PROJECTS_WITH_TASKS_CANT_DELETE( ExceptionGroups.PROJECT, "Legalább egy projekthez még tartoznak feladatok, valamennyi projekt nem törölhető egyszerre!", 0 ),
        PROJECT_WITH_DEVELOPER_CANT_DELETE( ExceptionGroups.PROJECT, "A projekthez még tartoznak fejlesztők, nem törölhető!", 0 ),
        PROJECTS_WITH_DEVELOPERS_CANT_DELETE( ExceptionGroups.PROJECT, "Legalább egy projekthez még tartoznak fejlesztők, valamennyi projekt nem törölhető egyszerre!", 0 ),

        DEVELOPER_NOT_SAVED_CANT_DELETE( ExceptionGroups.DEVELOPER, "A fejlesztő még nincs elmentve, nem törölhető!", 0 ),
        DEVELOPER_NOT_SAVED_CANT_MODIFY( ExceptionGroups.DEVELOPER, "A fejlesztő még nincs elmentve, nem módosítható!", 0 ),
        DEVELOPER_WITH_TASKS_CANT_DELETE( ExceptionGroups.DEVELOPER, "A fejlesztőhöz még tartoznak feladatok, nem törölhető!", 0 ),
        DEVELOPERS_WITH_TASKS_CANT_DELETE( ExceptionGroups.DEVELOPER, "Legalább egy fejlesztőhöz még tartoznak feladatok, valamennyi fejlesztő nem törölhető egyszerre!", 0 ) ,
        DEVELOPER_WITH_PROJECTS_CANT_DELETE( ExceptionGroups.DEVELOPER, "A fejlesztő még dolgozik legalább egy projekten, nem törölhető!", 0 ),
        DEVELOPERS_WITH_PROJECTS_CANT_DELETE( ExceptionGroups.DEVELOPER, "Legalább egy fejlesztő még dolgozik legalább egy projekten, valamennyi fejlesztő nem törölhető egyszerre!", 0 ),

        PROJECTTASK_NOT_SAVED_CANT_DELETE( ExceptionGroups.PROJECTTASKS, "A feladat még nincs elmentve, nem törölhető!",0),
        PROJECTTASK_NOT_SAVED_CANT_MODIFY( ExceptionGroups.PROJECTTASKS, "A feladat még nincs elmentve, nem módosítható!",0),
        PROJECTTASK_WITH_DEVELOPERS_CANT_DELETE( ExceptionGroups.PROJECTTASKS, "A feladathoz még tartoznak fejlesztők, nem törölhető!",0),

        PROJECTDEVELOPER_NOT_SAVED_CANT_DELETE( ExceptionGroups.PROJECTDEVELOPERS, "A projekt-fejlesztő összerendelés még nincs elmentve, nem törölhető!",0),
        PROJECTDEVELOPER_NOT_SAVED_CANT_MODIFY( ExceptionGroups.PROJECTDEVELOPERS, "A projekt-fejlesztő összerendelés még nincs elmentve, nem módosítható!",0),
        PROJECTDEVELOPER_WITH_TASKS_CANT_DELETE( ExceptionGroups.PROJECTDEVELOPERS, "A projekt-fejlesztő összerendeléshez még tartozik feladat, nem törölhető!",0),
        PROJECTDEVELOPERS_WITH_TASKS_CANT_DELETE( ExceptionGroups.PROJECTDEVELOPERS, "Legalább egy projekt-fejlesztő összerendeléshez még tartozik feladat, valamennyi összerendelés nem törölhető egyszerre!",0),

        PROJECTTASKDEVELOPER_NOT_SAVED_CANT_DELETE( ExceptionGroups.PROJECTTASKDEVELOPERS, "A fejlesztő-feladat összerendelés még nincs elmentve, nem törölhető!",0),
        PROJECTTASKDEVELOPER_NOT_SAVED_CANT_MODIFY( ExceptionGroups.PROJECTTASKDEVELOPERS, "A fejlesztő-feladat összerendelés még nincs elmentve, nem módosítható!",0),

        UNDEFINED( ExceptionGroups.UNDEFINED, "Undefined", 0)
        ;

        private final ExceptionGroups exceptionGroup ;
        private final String description;
        private Integer counter;

        Exceptions( ExceptionGroups exceptionGroup, String description, Integer counter ){
            this.exceptionGroup = exceptionGroup;
            this.description = description;
            this.counter = counter;
        }
        public String getDescription(){ return description; }
        public ExceptionGroups getExceptionGroup(){ return exceptionGroup; }
        @Synchronized
        public void incrementCounter(){ counter++ ;}
        @Synchronized
        public Integer getCounter(){ return counter; }
    }

    private final String exceptionMessage;

    ServiceException(@NonNull Exceptions exception) {
        super( exception.getDescription() );
        this.exceptionMessage = exception.getDescription() ;
        exception.incrementCounter();
    }

    ServiceException(@NonNull String exceptionMessage) {
        super( exceptionMessage );
        this.exceptionMessage = exceptionMessage ;
    }

    @Override
    public String toString() {
        return exceptionMessage;
    }

    private static void isAllExceptionsThrown( final @NonNull ExceptionGroups exceptionGroup ) {
        boolean isAllThrown = true;
        List<String> notThrownExceptions = new ArrayList<>();

        for( Exceptions exception : Exceptions.values() ){
            if( ( exception.getExceptionGroup().equals( exceptionGroup )) && (exception.getCounter() == 0) ){
                isAllThrown = false;
                notThrownExceptions.add( exception.name() );
            }
        }
        if (!isAllThrown) {
            throw new ServiceException("Exceptions not thrown:" + notThrownExceptions);
        }
    }

    @TestOnly
    public static void isAllProjectExceptionsThrown() {
        isAllExceptionsThrown( ExceptionGroups.PROJECT );
    }

    @TestOnly
    public static void isAllDeveloperExceptionsThrown() {
        isAllExceptionsThrown( ExceptionGroups.DEVELOPER );
    }

    @TestOnly
    public static void isAllProjectTasksExceptionsThrown() {
        isAllExceptionsThrown( ExceptionGroups.PROJECTTASKS );
    }

    @TestOnly
    public static void isAllProjectDevelopersExceptionsThrown() {
        isAllExceptionsThrown( ExceptionGroups.PROJECTDEVELOPERS );
    }

    @TestOnly
    public static void isAllProjectTaskDevelopersExceptionsThrown() {
        isAllExceptionsThrown( ExceptionGroups.PROJECTTASKDEVELOPERS );
    }

}
