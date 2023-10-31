package combit.hu.porphyr.controller;

import combit.hu.porphyr.service.ServiceException;
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
	private ErrorAttributes errorAttributes;

	@Autowired
	public void setErrorAttributes(ErrorAttributes errorAttributes) {
		this.errorAttributes = errorAttributes;
	}

	@ExceptionHandler(Exception.class)
	public String exception(Exception ex, Model model, HttpServletRequest rA) {
		if (ex instanceof ServiceException) {
			model.addAttribute("requestURI", rA.getRequestURI());
			model.addAttribute("errMessage", ex.getMessage());
			return "exceptionHandler";
		} else {
			WebRequest webRequest = new ServletWebRequest(rA);
			ErrorAttributeOptions errorAttributeOptions = ErrorAttributeOptions.defaults()
				.including(ErrorAttributeOptions.Include.MESSAGE);
			Map<String, Object> error = errorAttributes.getErrorAttributes(webRequest, errorAttributeOptions);

			model.addAttribute("timestamp", error.get("timestamp"));
			model.addAttribute("error", error.get("error"));
			model.addAttribute("message", error.get("message"));
			model.addAttribute("path", error.get("path"));
			model.addAttribute("status", error.get("status"));

			return "detailedError";
		}
	}
}
