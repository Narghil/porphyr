package combit.hu.porphyr.controller;

import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;

public class HomeControllerExceptionHandler {
    public String exceptionHandler(HttpServletRequest rA, Exception ex, Model model) {
        model.addAttribute("requestURI", rA.getRequestURI());
        model.addAttribute("errMessage", ex.getMessage());
        return "exceptionHandler";
    }
}
