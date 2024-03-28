package combit.hu.porphyr.controller;

public class PorphyrNotPermittedException extends RuntimeException{
    public PorphyrNotPermittedException(String errorMessage, Throwable originalError ) {
        super(errorMessage, originalError );
    }
}
