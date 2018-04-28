package spring.ajax.demo.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import spring.ajax.client.anno.AjaxMapping;

import java.util.Map;

@org.springframework.stereotype.Controller
public class Controller {

    @AjaxMapping(path = "/a_{id}")
    public String simpleRequestBodyController(@RequestBody Map<String, String> map, @PathVariable int id) {
        return String.format("req params is \n map:%s,\n id:%d", map.toString(), id);
    }
}
