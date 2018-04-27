
## 参数解析：

按照Spring RequestMapping的规范，Param有多种可选，框架仅支持以下几种输入参数：

- @RequestParam

   作用： Servlet request parameters (i.e. query parameters or form data)
   
   > 当类型为Map<String, String> 、 MultiValueMap<String, String> 时表示所有参数的Map，此类参数在生成的client代码中被忽略。

- @RequestHeader

    作用：bind a request header to a method argument in a controller

- @PathVariable

    作用：access to URI template variables
    
    PathVariable支持以`/{name:[a-z-]+}-{version:\\d\\.\\d\\.\\d}{ext:\\.[a-z]+}`的方式指定正则匹配，client端需要做正则校验

- @RequestBody

    作用: For access to the HTTP request body. Body content is converted to the declared method argument type using HttpMessageConverters
    
    > 不可以和@RequestParam同时出现
    
- 无注解的simple type：

    基于[BeanUtils#isSimpleProperty](https://docs.spring.io/spring-framework/docs/5.0.5.RELEASE/javadoc-api/org/springframework/beans/BeanUtils.html#isSimpleProperty-java.lang.Class-)
    判定为simple type的param等同于@RequestParam & optional