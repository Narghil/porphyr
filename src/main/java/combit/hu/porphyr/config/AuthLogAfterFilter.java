package combit.hu.porphyr.config;

import combit.hu.porphyr.controller.PorphyrNotPermittedException;
import combit.hu.porphyr.controller.helpers.SessionData;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

import static combit.hu.porphyr.RequestsConstants.PROTECTED_REQUEST_CALLS;

@Component //Ellenkező esetben a sessionData mindig null lesz.
public class AuthLogAfterFilter implements Filter {

    @Resource(name = "getSessionData")
    SessionData sessionData;

    final @NonNull HttpSecurity httpSecurity;

    @Autowired
    public AuthLogAfterFilter( @NonNull HttpSecurity httpSecurity){
        this.httpSecurity = httpSecurity;
    }

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        final @Nullable Authentication contextAuth = SecurityContextHolder.getContext().getAuthentication();

        String requestURI = ((HttpServletRequest) request).getRequestURI();
        if (sessionData != null && contextAuth != null && !requestURI.contains(".")
            && !sessionData.getUserLoginName().equals("anonymus")
            && !sessionData.getUserPermittedRequestCalls().contains(requestURI)
        ) {
            for (Map.Entry<String, List<String>> entry : PROTECTED_REQUEST_CALLS.entrySet()) {
                if (entry.getValue().contains(requestURI)) {
                    requestURI = entry.getKey();
                }
            }
            String qm = "\"";
            String reason = "User " + qm + sessionData.getUserLoginName() + qm + " is not authorized to access the " + qm + requestURI + qm + " function!";
            throw new PorphyrNotPermittedException(reason, new AccessDeniedException(requestURI, "", reason));
        }
        chain.doFilter(request, response);
    }
}
