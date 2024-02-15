package combit.hu.porphyr;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.FormLoginRequestBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SecurityTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginWithValidUserThenAuthenticated() throws Exception {
        FormLoginRequestBuilder login = formLogin()
            .user("user")
            .password("user");
        mockMvc.perform(login)
            .andExpect(authenticated().withUsername("user"));
    }

    @Test
    void loginWithInvalidUserThenUnauthenticated() throws Exception {
        FormLoginRequestBuilder login = formLogin()
            .user("invalid")
            .password("invalid_password");

        mockMvc.perform(login)
            .andExpect(unauthenticated());
    }

    @Test
    void accessUnsecuredResourceThenOk() throws Exception {
        mockMvc.perform(get("/logout"))
            .andExpect( status().isOk() )
        ;
    }

    @Test
    void accessSecuredResourceUnauthenticatedThenRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser
    void accessSecuredResourceAuthenticatedThenOk() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andReturn();
        assertThat(mvcResult.getResponse().getContentAsString()).contains(getMessageProperty("MainPageTitle"));
    }

    @Test
    @WithMockUser
    void performLogout() throws Exception{
        mockMvc
            .perform(post("/logout").secure(true).with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(unauthenticated())
            .andReturn()
        ;
    }

    //------ Helper
    @Autowired
    private MessageSource messageSource;

    private String getMessageProperty( String propertyName){
        Locale localeValue = new Locale("");
        return messageSource.getMessage(propertyName, null, "- none -", localeValue);
    }

}
