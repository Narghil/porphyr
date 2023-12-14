package combit.hu.porphyr.controller;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@Setter
@Getter
@ToString
public class WebErrorBean {

    private @Nullable String onOff;
    private @Nullable String title;
    private @Nullable String message;

    public void setError(final @NonNull String onOff, final @NonNull String title, final @NonNull String message) {
        this.onOff = onOff;
        this.title = title;
        this.message = message;
    }

    public WebErrorBean() {
        this.setError("OFF", "", "");
    }

    public WebErrorBean(WebErrorBean oldWebError) {
        this.onOff = oldWebError.onOff;
        this.title = oldWebError.title;
        this.message = oldWebError.message;
    }

    public @NonNull WebErrorBean getWebErrorData() {
        WebErrorBean result = new WebErrorBean( this );
        this.setError("OFF","","");
        return result;
    }

    @Bean
    @SessionScope
    public WebErrorBean getWebErrorBean() {
        return new WebErrorBean();
    }
}


