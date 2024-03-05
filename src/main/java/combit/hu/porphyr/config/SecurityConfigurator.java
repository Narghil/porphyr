package combit.hu.porphyr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static combit.hu.porphyr.Constants.ROLE_ADMIN;
import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;
import static org.springframework.security.config.Customizer.withDefaults;

@EnableGlobalMethodSecurity(securedEnabled = true)
@Configuration
public class SecurityConfigurator {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.csrf().ignoringRequestMatchers(toH2Console());
        httpSecurity.headers().frameOptions().sameOrigin();
        httpSecurity.authorizeHttpRequests(auth ->
        {
            auth.antMatchers("/").authenticated();
            //A login_decorator használ CSS-t, de ha nincs megadva rá az engedély, csak a belépés után éri el
            //Eredmény: A belépés UTÁN tölti be a css-t, és text-ként megjeleníti, a fő oldal HELYETT.
            auth.antMatchers("/css/**").permitAll();
            auth.requestMatchers(toH2Console()).hasAuthority(ROLE_ADMIN);
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
