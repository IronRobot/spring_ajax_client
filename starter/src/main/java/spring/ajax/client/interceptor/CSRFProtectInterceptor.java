package spring.ajax.client.interceptor;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import spring.ajax.client.anno.CSRF;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 保护所有使用了@CSRF注解的Controller 方法调用，
 * 要求请求时header中必须有与session中相同的token，header的name可以配置
 */
public class CSRFProtectInterceptor implements HandlerInterceptor {
    public static final String CSRF_TOKEN = "csrf_token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        if (session.getAttribute(CSRF_TOKEN) == null) {
            synchronized (session) {
                if (session.getAttribute("csrf_token") == null) {
                    session.setAttribute("csrf_token", System.nanoTime() + "");
                }
            }
        } else {
            if (handler instanceof HandlerMethod) {
                HandlerMethod method = (HandlerMethod) handler;
                if (method.hasMethodAnnotation(CSRF.class)) {
                    return session.getAttribute("csrf_token").equals(
                            request.getHeader(method.getMethodAnnotation(CSRF.class).tokenHeader()));
                }
            }
        }
        return true;
    }
}
