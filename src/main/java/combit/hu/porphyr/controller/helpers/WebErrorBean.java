package combit.hu.porphyr.controller.helpers;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import static combit.hu.porphyr.Constants.OFF;

@Component
@Setter
@Getter
@ToString
public class WebErrorBean {

    private @NonNull String onOff;
    private @NonNull String title;
    private @NonNull String message;

    public void setError(final @NonNull String onOff, final @NonNull String title, final @NonNull String message) {
        this.onOff = onOff;
        this.title = title;
        this.message = message;
    }

    @Autowired
    public WebErrorBean() {
        this.onOff = OFF;
        this.title = "";
        this.message = "";
    }

    public WebErrorBean(WebErrorBean oldWebError) {
        this.onOff = oldWebError.onOff;
        this.title = oldWebError.title;
        this.message = oldWebError.message;
    }

    public @NonNull WebErrorBean getWebErrorData() {
        WebErrorBean result = new WebErrorBean( this );
        this.setError(OFF,"","");
        return result;
    }

    @Bean
    @SessionScope
    public WebErrorBean getWebErrorBean() {
        return new WebErrorBean();
    }
}


