package spring.ajax.client.anno;

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CSRF {
    String tokenHeader() default "csrf_token";
}
