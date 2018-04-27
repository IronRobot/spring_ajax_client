package spring.ajax.client.controller.validators;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import spring.ajax.client.controller.MappingValidator;
import spring.ajax.client.controller.exception.ErrorE;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NoStarInPathValidator implements MappingValidator, EmbeddedValueResolverAware {
    private StringValueResolver stringValueResolver;

    @Override
    public void validate(RequestMappingInfo mappingInfo, HandlerMethod handlerMethod) throws ErrorE {
        RequestMapping controllerMapping = AnnotatedElementUtils.getMergedAnnotation(handlerMethod.getMethod().getDeclaringClass(), RequestMapping.class);
        if (controllerMapping != null) {
            List<String> anyWithStar = collectStarPaths(controllerMapping);

            if(!anyWithStar.isEmpty()){
                throw new ErrorE();
            }
        }

        RequestMapping methodMapping = AnnotatedElementUtils.getMergedAnnotation(handlerMethod.getMethod(), RequestMapping.class);
        if (methodMapping != null) {
            List<String> anyWithStar = collectStarPaths(methodMapping);

            if(!anyWithStar.isEmpty()){
                throw new ErrorE();
            }
        }

    }

    private List<String> collectStarPaths(RequestMapping controllerMapping) {
        return Arrays.stream(controllerMapping.value()).map(str->stringValueResolver.resolveStringValue(str)).filter(str->str.contains("*")||str.contains("?")).collect(Collectors.toList());
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.stringValueResolver = resolver;
    }
}
