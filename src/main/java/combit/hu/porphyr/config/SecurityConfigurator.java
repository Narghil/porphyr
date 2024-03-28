package combit.hu.porphyr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

// - import static combit.hu.porphyr.Constants.ROLE_ADMIN
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
            //Ugyanezt más változtatások is képesek előidézni. MINDEN módosítás előtt menteni kell az előző állapotot!
            auth.antMatchers("/css/**").permitAll();
            auth.antMatchers("/icons/**").permitAll();
            auth.antMatchers("/error").permitAll();
            auth.antMatchers("/detailedError").permitAll();
            auth.antMatchers("/authError").permitAll();
            auth.antMatchers("/exceptionHandler").permitAll();
            auth.antMatchers("/layouts/**").permitAll();
            // -- auth.antMatchers("/confirm_logout").permitAll() -- nem működik.
            auth.requestMatchers(toH2Console()).hasAuthority(ROLE_ADMIN);
            //Minden más:
            auth.anyRequest().authenticated();
        })
        ;

        httpSecurity.formLogin(withDefaults());
        httpSecurity.formLogin().loginPage("/login").permitAll();
        // -- httpSecurity.logout().logoutUrl("/confirm_logout").permitAll() -- nem működik.
        httpSecurity.logout().logoutSuccessUrl("/logged_out")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .permitAll();

        httpSecurity.addFilterAfter(new AuthLogAfterFilter(httpSecurity), BasicAuthenticationFilter.class) ;

        return httpSecurity.build();
    }
}
