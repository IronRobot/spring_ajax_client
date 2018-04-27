package spring.ajax.client.controller;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import spring.ajax.client.controller.exception.ErrorE;

public interface MappingValidator {
    void validate(RequestMappingInfo mappingInfo, HandlerMethod handlerMethod) throws ErrorE;
}
