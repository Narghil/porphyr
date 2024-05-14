package combit.hu.porphyr.controller;

import combit.hu.porphyr.service.PorphyrServiceException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@ControllerAdvice
public class ExceptionGeneral {

    private final @NonNull ErrorAttributes errorAttributes;

    @Autowired
    public ExceptionGeneral(final @NonNull ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @ExceptionHandler(Exception.class)
    public @NonNull String exception(Exception ex, Model model, HttpServletRequest rA) {
        String errorPage = "detailedError";

        if (ex instanceof PorphyrServiceException) {
            model.addAttribute("requestURI", rA.getRequestURI());
            model.addAttribute("errMessage", "EXCEPTION:" + ex.getMessage());
            return "exceptionHandler";
        } else {
            WebRequest webRequest = new ServletWebRequest(rA);
            ErrorAttributeOptions errorAttributeOptions = ErrorAttributeOptions.defaults()
                .including(ErrorAttributeOptions.Include.MESSAGE);
            Map<String, Object> error = errorAttributes.getErrorAttributes(webRequest, errorAttributeOptions);
            model.addAttribute("timestamp", error.get("timestamp"));
            model.addAttribute("errorDescription", "EXCEPTION: " + error.get("error"));
            model.addAttribute("message", error.get("message"));
            model.addAttribute("path", error.get("path"));
            model.addAttribute("status", error.get("status"));

            return errorPage;
        }
    }
}
