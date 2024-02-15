package combit.hu.porphyr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;
import static org.springframework.security.config.Customizer.withDefaults;


@EnableGlobalMethodSecurity(securedEnabled = true)
@Configuration
public class SecurityConfigurator {

    private static final String ROLE_ADMIN = "ADMIN";

    @Autowired
    public void configureAuth(AuthenticationManagerBuilder authManBuilder) throws Exception {
        authManBuilder.inMemoryAuthentication()
            .withUser("admin")
            .password("{bcrypt}$2a$10$xU/NYqThNXLNFhBMyORlfupt5NTIE93fiwabOmTB1/fRs03YoqodC")
            .roles(ROLE_ADMIN);
        authManBuilder.inMemoryAuthentication()
            .withUser("user")
            .password("{bcrypt}$2a$10$F9t0jXQI4o95P0mhzFkTzuuXUpm8aCadvTpyk4B3jaPU6uKb00APa")
            .roles("USER");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().ignoringRequestMatchers(toH2Console());
        httpSecurity.headers().frameOptions().sameOrigin();
        httpSecurity.authorizeHttpRequests(auth ->
        {
            auth.antMatchers("/").authenticated();
            auth.antMatchers("/css/**").permitAll();
            auth.antMatchers("/icons/**").permitAll();
            auth.antMatchers("/").authenticated();
            auth.requestMatchers(toH2Console()).hasRole(ROLE_ADMIN);
            auth.antMatchers("/confirm_logout").permitAll();
            auth.antMatchers("/logged_out").permitAll();
            auth.anyRequest().authenticated();
        })
        ;
        httpSecurity.formLogin(withDefaults());
        httpSecurity.formLogin().loginPage("/login").permitAll();
        httpSecurity.logout().logoutSuccessUrl("/logged_out")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .permitAll();

        return httpSecurity.build();
    }
}
