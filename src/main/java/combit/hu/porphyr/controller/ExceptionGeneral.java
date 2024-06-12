package combit.hu.porphyr.controller;

import combit.hu.porphyr.service.PorphyrServiceException;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@ControllerAdvice
public class ExceptionGeneral {

    @Autowired
    public ExceptionGeneral(final @NonNull ErrorAttributes errorAttributes) {
    }

    @ExceptionHandler(Exception.class)
    public @NonNull String exception(Exception ex, Model model, HttpServletRequest rA) {

        if (ex instanceof PorphyrServiceException) {
            model.addAttribute("requestURI", rA.getRequestURI());
            model.addAttribute("errMessage", "EXCEPTION:" + ex.getMessage());
            return "exceptionHandler";
        } else {
            final ArrayList<String> errorMessages = getErrorMessages(ex);
            model.addAttribute("requestURI", rA.getRequestURI());
            model.addAttribute("errorMessages", errorMessages);
            return "commonExceptionHandler";
        }
    }

    private static @NotNull ArrayList<String> getErrorMessages(final @NonNull Exception unEx) {
        @NonNull
        ArrayList<String> errorMessages = new ArrayList<>();
        errorMessages.add("Error type: " + unEx.getClass().getName());
        errorMessages.add("Message: " + unEx.getMessage());
        errorMessages.add("Cause  : " + unEx.getCause());
        errorMessages.add("Stack trace:");
        if (unEx.getStackTrace().length == 0) {
            errorMessages.add("N/A");
        } else {
            for (StackTraceElement element : unEx.getStackTrace()) {
                if (element.getClassName().contains("porphyr")) {
                    errorMessages.add("   Method: " + element.getMethodName() + " in file: " + element.getFileName() + " at line: " + element.getLineNumber());
                }
            }
        }
        return errorMessages;
    }
}
