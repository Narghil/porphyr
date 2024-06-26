
package combit.hu.porphyr.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import combit.hu.porphyr.config.NotPermittedException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Controller
public class ErrorPageController implements ErrorController {

    private final @NonNull ErrorAttributes errorAttributes;

    @Autowired
    public ErrorPageController(final @NonNull ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public @NonNull String error(Model model, HttpServletRequest request) {
        String errorPage = "detailedError";

        WebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> error = errorAttributes.getErrorAttributes(
            webRequest,
            ErrorAttributeOptions.defaults().including(ErrorAttributeOptions.Include.MESSAGE)
        );

        model.addAttribute("timestamp", error.get("timestamp"));
        model.addAttribute("errorDescription", "PAGE:" + error.get("error"));
        model.addAttribute("message", error.get("message"));
        model.addAttribute("path", error.get("path"));
        model.addAttribute("status", error.get("status"));

        if (errorAttributes.getError(webRequest) instanceof NotPermittedException) {
            errorPage = "authError";
        }

        return errorPage;
    }
}
