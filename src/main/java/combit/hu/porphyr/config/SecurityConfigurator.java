package combit.hu.porphyr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableGlobalMethodSecurity(securedEnabled = true)
@Configuration
public class SecurityConfigurator {

    @Autowired
    public void configureAuth(AuthenticationManagerBuilder authManBuilder) throws Exception{
        authManBuilder.inMemoryAuthentication().withUser("pg").password("{noop}pg").roles("USER");
        authManBuilder.inMemoryAuthentication().withUser("admin").password("{noop}admin").roles("ADMIN");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.formLogin(withDefaults());
        httpSecurity.authorizeHttpRequests(auth ->
            {
                auth.antMatchers("/").authenticated();
                auth.antMatchers("/devs").hasRole("ADMIN");
                auth.anyRequest().authenticated();
            })
        ;
        return httpSecurity.build();
    }

}
