package spring.ajax.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import spring.ajax.client.interceptor.CSRFProtectInterceptor;

@Configuration
@ComponentScan
public class Config implements WebMvcConfigurer {

    @Value("${ajax_client.csrf_on:true}")
    private boolean csrfProtectOn;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if(this.csrfProtectOn) {
            registry.addInterceptor(new CSRFProtectInterceptor());
        }
    }
}
