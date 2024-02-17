package combit.hu.porphyr.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
// import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@EnableWebMvc
@Configuration
public class WebConfigurator implements WebMvcConfigurer{

	@Override
	public void addViewControllers(@NotNull ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("authentication/login");
		registry.addViewController("/confirm_logout").setViewName("authentication/confirm_logout");
		registry.addViewController("/logged_out").setViewName("authentication/logged_out");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
	}

}
