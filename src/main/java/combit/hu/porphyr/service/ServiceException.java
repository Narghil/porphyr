package combit.hu.porphyr.service;

import lombok.NonNull;
import lombok.Synchronized;
import org.jetbrains.annotations.TestOnly;

public class ServiceException extends IllegalArgumentException {
    private enum ExceptionGroups{ UNDEFINED, PROJECT, DEVELOPER, PROJECTTASKS, PROJECTDEVELOPERS, PROJECTTASKDEVELOPERS }
    public enum Exceptions{
        PROJECT_WITH_EMPTY_NAME_CANT_INSERT( ExceptionGroups.PROJECT, "A projekt neve nincs kitöltve, nem vihető fel!", 0),
        PROJECT_WITH_SAME_NAME_CANT_INSERT( ExceptionGroups.PROJECT, "Van már ilyen nevű projekt, nem vihető fel még egyszer!", 0),
        PROJECT_NOT_SAVED_CANT_MODIFY( ExceptionGroups.PROJECT, "A projekt még nincs elmentve, nem módosítható!", 0 ),
        PROJECT_WITH_EMPTY_NAME_CANT_MODIFY( ExceptionGroups.PROJECT, "A projekt neve nincs kitöltve, nem módosítható!", 0),
        PROJECT_WITH_SAME_NAME_CANT_MODIFY( ExceptionGroups.PROJECT, "Van már ilyen nevű projekt, a név nem módosítható erre!", 0),
        PROJECT_NOT_SAVED_CANT_DELETE( ExceptionGroups.PROJECT, "A projekt még nincs elmentve, nem törölhető!", 0 ),
        PROJECT_WITH_TASKS_CANT_DELETE( ExceptionGroups.PROJECT, "A projekthez még tartoznak feladatok, nem törölhető!", 0 ),
        PROJECT_WITH_DEVELOPERS_CANT_DELETE( ExceptionGroups.PROJECT, "A projekthez még tartoznak fejlesztők, nem törölhető!", 0 ),

        DEVELOPER_WITH_EMPTY_NAME_CANT_INSERT( ExceptionGroups.DEVELOPER, "A fejlesztő neve nincs kitöltve, nem vihető fel!", 0 ),
        DEVELOPER_WITH_SAME_NAME_CANT_INSERT( ExceptionGroups.DEVELOPER, "Van már ilyen nevű fejlesztő, nem vihető fel még egyszer!", 0 ),
        DEVELOPER_NOT_SAVED_CANT_MODIFY( ExceptionGroups.DEVELOPER, "A fejlesztő még nincs elmentve, nem módosítható!", 0 ),
        DEVELOPER_WITH_EMPTY_NAME_CANT_MODIFY( ExceptionGroups.DEVELOPER, "A fejlesztő neve nincs kitöltve, nem módosítható!", 0 ),
        DEVELOPER_WITH_SAME_NAME_CANT_MODIFY( ExceptionGroups.DEVELOPER, "Van már ilyen nevű fejlesztő, a név nem módosítható erre!", 0 ),
        DEVELOPER_NOT_SAVED_CANT_DELETE( ExceptionGroups.DEVELOPER, "A fejlesztő még nincs elmentve, nem törölhető!", 0 ),
        DEVELOPER_WITH_PROJECTS_CANT_DELETE( ExceptionGroups.DEVELOPER, "A fejlesztő még dolgozik legalább egy projektben, nem törölhető!", 0 ),

        PROJECTTASK_PROJECT_IS_NULL_CANT_INSERT( ExceptionGroups.PROJECTTASKS, "Nincs kiválasztott projekt, a feladat nem vihető fel!",0),
        PROJECTTASK_PROJECT_NOT_EXISTS_CANT_INSERT( ExceptionGroups.PROJECTTASKS, "A kiválasztott projektet törölték, a feladat nem vihető fel!",0),
        PROJECTTASK_WITH_EMPTY_NAME_CANT_INSERT( ExceptionGroups.PROJECTTASKS, "A feladat neve nincs kitöltve, nem vihető fel!",0),
        PROJECTTASK_WITH_SAME_NAME_CANT_INSERT( ExceptionGroups.PROJECTTASKS, "Már van ilyen nevű feladat, nem vihető fel!",0),
        PROJECTTASK_NOT_SAVED_CANT_MODIFY( ExceptionGroups.PROJECTTASKS, "A feladat még nincs elmentve, nem módosítható!",0),
        PROJECTTASK_PROJECT_IS_NULL_CANT_MODIFY( ExceptionGroups.PROJECTTASKS, "Nincs kiválasztott projekt, a feladat nem módosítható!",0),
        PROJECTTASK_PROJECT_NOT_EXISTS_CANT_MODIFY( ExceptionGroups.PROJECTTASKS, "A kiválasztott projektet törölték, a feladat nem módosítható!",0),
        PROJECTTASK_WITH_EMPTY_NAME_CANT_MODIFY( ExceptionGroups.PROJECTTASKS, "A feladat neve nincs kitöltve, nem módosítható!",0),
        PROJECTTASK_WITH_SAME_NAME_CANT_MODIFY( ExceptionGroups.PROJECTTASKS, "Már van ilyen nevű feladat, a név nem módosítható erre!",0),
        PROJECTTASK_NOT_SAVED_CANT_DELETE( ExceptionGroups.PROJECTTASKS, "A feladat még nincs elmentve, nem törölhető!",0),
        PROJECTTASK_WITH_DEVELOPERS_CANT_DELETE( ExceptionGroups.PROJECTTASKS, "A feladathoz még tartoznak fejlesztők, nem törölhető!",0),

        PROJECTDEVELOPER_NOT_SAVED_CANT_DELETE( ExceptionGroups.PROJECTDEVELOPERS, "A projekt-fejlesztő összerendelés még nincs elmentve, nem törölhető!",0),
        PROJECTDEVELOPER_NOT_SAVED_CANT_MODIFY( ExceptionGroups.PROJECTDEVELOPERS, "A projekt-fejlesztő összerendelés még nincs elmentve, nem módosítható!",0),
        PROJECTDEVELOPER_WITH_TASKS_CANT_DELETE( ExceptionGroups.PROJECTDEVELOPERS, "A projekt-fejlesztő összerendeléshez még tartozik feladat, nem törölhető!",0),
        PROJECTDEVELOPERS_WITH_TASKS_CANT_DELETE( ExceptionGroups.PROJECTDEVELOPERS, "Legalább egy projekt-fejlesztő összerendeléshez még tartozik feladat, valamennyi összerendelés nem törölhető egyszerre!",0),

        PROJECTTASKDEVELOPER_NOT_SAVED_CANT_DELETE( ExceptionGroups.PROJECTTASKDEVELOPERS, "A fejlesztő-feladat összerendelés még nincs elmentve, nem törölhető!",0),
        PROJECTTASKDEVELOPER_NOT_SAVED_CANT_MODIFY( ExceptionGroups.PROJECTTASKDEVELOPERS, "A fejlesztő-feladat összerendelés még nincs elmentve, nem módosítható!",0),

        UNDEFINED( ExceptionGroups.UNDEFINED, "Undefined", 0),
        NULLVALUE( ExceptionGroups.UNDEFINED, "Adathiba: Üres érték, nem várt helyen.", 0)
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

    public ServiceException(@NonNull Exceptions exception) {
        super( exception.getDescription() );
        exception.incrementCounter();
    }

    public ServiceException(@NonNull String exceptionMessage) {
        super( exceptionMessage );
    }

    private static void isAllExceptionsThrown( final @NonNull ExceptionGroups exceptionGroup ) {
        boolean isAllThrown = true;
        String separator = System.getProperty("line.separator");
        StringBuilder notThrownExceptions = new StringBuilder(separator);

        notThrownExceptions.append("Exceptions not thrown:");
        notThrownExceptions.append(separator);
        for( Exceptions exception : Exceptions.values() ){
            if( ( exception.getExceptionGroup().equals( exceptionGroup )) && (exception.getCounter() == 0) ){
                isAllThrown = false;
                notThrownExceptions.append(exception.name());
                notThrownExceptions.append(separator);
            }
        }
        if (!isAllThrown) {
            notThrownExceptions.append(separator);
            throw new ServiceException("Exceptions not thrown:" + notThrownExceptions);
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
                stringBuilder.append(System.getProperty("line.separator"));
            }
        }
        return stringBuilder.toString();
    }

    private static String toString( final @NonNull ExceptionGroups exceptionGroup){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(exceptionGroup.name());
        stringBuilder.append( " - number of thrown exeptions:");
        stringBuilder.append(System.getProperty("line.separator"));
        for( Exceptions exception : Exceptions.values() ){
            if( exception.getExceptionGroup() == exceptionGroup && exception.getCounter() > 0) {
                stringBuilder.append(exception.name());
                stringBuilder.append(" = ");
                stringBuilder.append(exception.getCounter());
                stringBuilder.append(System.getProperty("line.separator"));
            }
        }
        stringBuilder.append(System.getProperty("line.separator"));
        return stringBuilder.toString();
    }

    @TestOnly
    public static void toStringProjectTaskExceptionsThrown(){
        System.out.println(toString(ExceptionGroups.PROJECTTASKS)); }

    @TestOnly
    public static void toStringProjectExceptionsThrown(){
        System.out.println(toString(ExceptionGroups.PROJECT)); }

    @TestOnly
    public static void isAllProjectExceptionsThrown() {
        isAllExceptionsThrown( ExceptionGroups.PROJECT );
    }

    @TestOnly
    public static void isAllDeveloperExceptionsThrown() {
        isAllExceptionsThrown( ExceptionGroups.DEVELOPER );
    }

    @TestOnly
    public static void isAllProjectTaskExceptionsThrown() {
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

/*
Megjegyzés: A tesztelési lehetőségekre azért van szükség, mert:
 - Az assertThrows( <class>, <executable>, <message> ), ha a kivétel más üzenetet ad vissza, mint a <message>,
   akkor is tövábbengedi a futást.
   Magyarán ennyiből még nem ellenőrizhető, hogy a kívánt kivételt dobta-e a teszt.
 */