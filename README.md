
## Why：

spring-boot-ajax-starter 主要是为了简化基于spring-boot开发的web项目中前端JS调用后端Controller的代码，
提供与后端Controller的方法签名基本一致的方法调用来发起Ajax请求。


## How：

- Dependency:在maven或gradle中增加以下依赖:

        compile "com.github.ironrobot:spring-boot-starter-ajaxclient:0.0.1"
- Server Side 声明供ajax访问的方法,如:

        @AjaxMapping(path = "/a_{id}")
        public String simpleRequestBodyController(@RequestBody Map<String, String> map, @PathVariable int id)

        //AjaxMapping 是继承RequestMapping、ResponseBody的注解，用来明确的声明那些需要暴露给ajax的方法。
        // 目前支持的参数类型包括：@Pathvariable @RequestParam @RequestBody @Header
- Client Side:

        <script type="text/javascript" src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
        <script type="text/javascript" src="/hello_js"></script>
        // 其中hello_js是可配置的路径，参考配置项。
        <script type="text/javascript">
            routes // 
            .spring.ajax.demo.controller.Controller // fullpath classname
            .simpleRequestBodyController(           // method name
                {                                   // 1st param @RequestBody Map<String, String> map
                    "a":123,
                    "b":"b1"
                },
                0700                                // 2nd param @PathVariable int id
            ).ajax({
                "beforeSend":function(){            // extra ajax config,like timeout ...
                    console.log("add more ajax setting")
                }
            }).then(function(rst){console.log(rst)}); // ajax response handler
        </script>

### 配置项

在application.properties中支持以下配置项:
1. `spring.ajax.client.js_path=/hello_js` : 定义生成js的访问路径
2. `spring.ajax.client.code_template=classpath:/spring/ajax/client/controller/default_template.js` : 自定义生成JS的模板，关于模板参考扩展一节的介绍

## 扩展:

项目提供了自定义 js template 的能力来优化生成的js代码（比如不依赖jquery ？)，个人JS 小白，欢迎贡献更好的实现

js template 应该是合法的thymeleaf 模板， 框架在渲染模板时提供以下context variable:

1. methods : 所有ajax 方法的数组，每个method 的属性字段参考:`spring.ajax.client.controller.AjaxMethodInfo`
2. methods_json : methods json序列化后的字符串
3. context_path : servlet context path,如/ , /abc/
