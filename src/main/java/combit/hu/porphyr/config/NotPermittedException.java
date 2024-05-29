package combit.hu.porphyr.config;

public class NotPermittedException extends RuntimeException {
    public NotPermittedException(String errorMessage, Throwable originalError) {
        super(errorMessage, originalError);
    }
}
