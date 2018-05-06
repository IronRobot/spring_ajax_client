package spring.ajax.client.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringValueResolver;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.dialect.SpringStandardDialect;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import spring.ajax.client.anno.AjaxMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static spring.ajax.client.interceptor.CSRFProtectInterceptor.CSRF_TOKEN;

@Controller
public class JSClientCodeGeneratorController implements EmbeddedValueResolverAware, ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private List<MappingValidator> validatorList;

    @Value("#{servletContext.contextPath}")
    private String servletContextPath;


    private final ParameterNameDiscoverer discover = new DefaultParameterNameDiscoverer();

    private ApplicationContext applicationContext;
    private StringValueResolver stringValueResolver;

    private volatile String js;


    @Value("${spring.ajax.client.code_template:classpath:/spring/ajax/client/controller/default_template.js}")
    private String template;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        // on refresh
        this.js = rebuild(event.getApplicationContext());

    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        //
        this.stringValueResolver = resolver;
    }


    @RequestMapping("/${spring.ajax.client.js_path:/a_client}")
    @ResponseBody
    public String doIt(HttpServletRequest request) {

        String tokenInSession = (String) request.getSession().getAttribute(CSRF_TOKEN);

        return String.format("%s('%s')",js,tokenInSession);
    }


    private String rebuild(ApplicationContext appContext) {
        Map<RequestMappingInfo, HandlerMethod> allMapping = this.requestMappingHandlerMapping.getHandlerMethods();

        // do filter all ajax mapping

        Collection<Map.Entry<RequestMappingInfo, HandlerMethod>> ajaxMappings = getAllAjaxMappings(allMapping.entrySet());
        // do validation

        doValidation(ajaxMappings);
        // do build methodInfo
        List<AjaxMethodInfo> methods = mapToMethodInfo(ajaxMappings);

        return doRender(methods, appContext);
    }

    private String doRender(List<AjaxMethodInfo> methods, ApplicationContext appContext) {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(appContext);
        resolver.setTemplateMode("JAVASCRIPT");
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        Context context = new Context();
        context.setVariable("methods", methods);
        context.setVariable("context_path",servletContextPath);
        try {
            ObjectMapper mapper = new ObjectMapper();

            context.setVariable("methods_json", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(methods));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        engine.setDialect(new SpringStandardDialect());
        return engine.process(template, context);
    }

    private Collection<Map.Entry<RequestMappingInfo, HandlerMethod>> getAllAjaxMappings(Collection<Map.Entry<RequestMappingInfo, HandlerMethod>> collection) {
        return collection.stream().filter(e -> e.getValue().hasMethodAnnotation(AjaxMapping.class)).collect(Collectors.toList());
    }

    private void doValidation(Collection<Map.Entry<RequestMappingInfo, HandlerMethod>> collection) {
        collection.stream().forEach(
                e -> validatorList.stream().forEach(v -> v.validate(e.getKey(), e.getValue()))
        );
    }

    private List<AjaxMethodInfo> mapToMethodInfo(Collection<Map.Entry<RequestMappingInfo, HandlerMethod>> collection) {
        return collection.stream().map(e ->
                {
                    final Method METHOD = e.getValue().getMethod();
                    final AjaxMethodInfo rt = new AjaxMethodInfo();
                    rt.setName(METHOD.getDeclaringClass().getName() + "." + METHOD.getName());
                    rt.setAlias(new String[]{METHOD.getDeclaringClass().getSimpleName()+"."+METHOD.getName()});
                    rt.setPath(getOnePath(METHOD));
                    final String[] names = discover.getParameterNames(METHOD);
                    rt.setParamList(Arrays.stream(e.getValue().getMethodParameters())
                            .filter(this::isAjaxParam)
                            .map(p -> {
                                AjaxMethodInfo.Param param = new AjaxMethodInfo.Param();
                                param.setName(names[p.getParameterIndex()]);
                                adjustByParamAnnotations(param, p);
                                Type type = p.getParameterType();
                                param.setOptional(param.isOptional() || type == Optional.class);
                                if (type == Optional.class) {
                                    type = ((ParameterizedType) p.getGenericParameterType()).getActualTypeArguments()[0];
                                }
                                param.setType(type);
                                param.setFullTypeName(type.getTypeName());
                                return param;
                            }).collect(Collectors.toList())
                    );
                    return rt;
                }
        ).collect(Collectors.toList());
    }

    private void adjustByParamAnnotations(AjaxMethodInfo.Param param, MethodParameter mp) {
        if (mp.hasParameterAnnotation(RequestParam.class)) {
            RequestParam rp = mp.getParameterAnnotation(RequestParam.class);
            String name = rp.name();
            if (!isEmpty(name)) {
                param.setName(name);
            }
            param.setOptional(!(rp.required() && rp.defaultValue() == null));
            param.setDefaultValue(rp.defaultValue());
            param.setParamAnno(RequestParam.class.getSimpleName());
        } else if (mp.hasParameterAnnotation(RequestHeader.class)) {
            RequestHeader rh = mp.getParameterAnnotation(RequestHeader.class);
            String name = rh.name();
            if (!isEmpty(name)) {
                param.setName(name);
            }
            param.setDefaultValue(rh.defaultValue());
            param.setOptional(!(rh.required() && rh.defaultValue() == null));
            param.setParamAnno(RequestHeader.class.getSimpleName());
        } else if (mp.hasParameterAnnotation(PathVariable.class)) {
            PathVariable pv = mp.getParameterAnnotation(PathVariable.class);
            String name = pv.name();
            if (!isEmpty(name)) {
                param.setName(name);
            }
            param.setOptional(!pv.required());
            param.setParamAnno(PathVariable.class.getSimpleName());
        } else if(mp.hasParameterAnnotation(RequestBody.class)){
            RequestBody requestBody = mp.getParameterAnnotation(RequestBody.class);
            param.setOptional(!requestBody.required());
            param.setParamAnno(RequestBody.class.getSimpleName());
        }else {
            // simple type
            param.setOptional(true);
            param.setParamAnno(RequestParam.class.getSimpleName());
        }
    }

    private String getOnePath(Method method) {
        StringBuilder sb = new StringBuilder();
        RequestMapping controllerMapping = AnnotatedElementUtils.getMergedAnnotation(method.getDeclaringClass(), RequestMapping.class);
        if (controllerMapping != null) {
            sb.append(controllerMapping.path()[0]);
        }

        RequestMapping methodMapping = AnnotatedElementUtils.getMergedAnnotation(method, RequestMapping.class);
        if (methodMapping != null) {
            sb.append(methodMapping.path()[0]);
        }
        return this.stringValueResolver.resolveStringValue(sb.toString());
    }


    private boolean isAjaxParam(MethodParameter parameter) {

        return
                (parameter.hasParameterAnnotation(RequestParam.class) && (parameter.getParameterType() != Map.class && parameter.getParameterType() != MultiValueMap.class))
                        || parameter.hasParameterAnnotation(RequestHeader.class)
                        || parameter.hasParameterAnnotation(RequestBody.class)
                        || parameter.hasParameterAnnotation(PathVariable.class)
                        || BeanUtils.isSimpleProperty(parameter.getParameterType())
                        || (
                                Optional.class == parameter.getParameterType() //
                                && BeanUtils.isSimpleProperty((Class<?>)((ParameterizedType) parameter.getGenericParameterType()).getActualTypeArguments()[0])
                        )
                ;
    }

    private boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
