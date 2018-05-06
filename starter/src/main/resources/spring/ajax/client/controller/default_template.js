var routes = (function (csrf_token) {
    if (typeof jQuery == 'undefined') {
        console.error("Jquery Required");
        return null;
    }

    var base_path = "";
    /*[+
                methods = [(${methods_json})];
                [#th:block th:if="${context_path !=null && !context_path.isEmpty()}"]
                base_path = "[(${context_path})]";
                [/th:block]
    +]*/

    const routes = {};
    const doAjax = function(path,arg,conf){

        // 1. 校验参数,约束
        //      a) 不止RequestBody + RequestParam 同时存在
        //      b) 不支持多个RequestBody 参数
        if(arg["RequestBody"].length > 0 && arg["RequestParam"].length > 0){
            console.error("@RequestBody and @RequestParam should not exist at the same time.")
            return null;
        }

        if(arg["RequestBody"].length > 1){
            console.error("More than one @RequestBody param");
        }
        // 2. replace path variables
        // 循环替换path 中形如 {abc} 或 {abc:[a-z]+} 的变量。
        var pathVariableParams = arg["PathVariable"];
        while(true){
            var another = path.replace(/\{[^\}]*\}/,function(variable){
                var nameAndRegex = variable.substr(1,variable.length-2).split(":");
                var grep = $.grep(pathVariableParams,function(o){
                    return o.name == nameAndRegex[0];
                });
                var value = grep.shift().value;
                if(!value){
                    console.warn("no value exist for path variable param:"+nameAndRegex[0]);
                }
                if(grep.length){
                    console.warn( "more than one path variable exist with name:"+nameAndRegex[0]);
                }
                return value || "";
            });
            if(another == path){
                break;
            }else{
                path = another;
            }
        }
        // 2. deal header
        var headers = {};
        $.each(arg["RequestHeader"],function(i,h){
            headers[h.name] = h.value;
        });
        headers["csrf_token"] = csrf_token;

        // 3. deal Params
        var reqParam = {};
        $.each(arg["RequestParam"],function(i,p){
            reqParam[p.name] = p.value;
        });

        path = base_path + path;

        if(arg["RequestBody"].length > 0 ){
            return $.ajax($.extend({},{
                url:path,
                headers:headers,
                method:"POST",
                data:arg["RequestBody"][0].value,
                traditional:true,
                contentType:"application/json"

            },conf));
        }else{
            // No Request Body

            return $.ajax($.extend({},{
                url:path,
                headers:headers,
                method:"POST",
                data:reqParam,
                traditional:true
            },conf));
        }
    }


    const newFunction = function (path, params) {
        var call = function () {
            // check arguments
            if (arguments.length != params.length) {
                //error
                return {
                    ajax: function () {
                        console.error("Controller expect "+params.length + " params,but found "+arguments.length,params,arguments);
                    }
                }
            } else {


                for (var i in arguments) {
                    if(typeof arguments[i] == "object" && !Array.isArray(arguments[i])){
                        params[i].value = JSON.stringify(arguments[i]);
                    }else{
                        params[i].value = arguments[i];
                    }
                }
                var groupedParams = {
                    "RequestParam":[],
                    "RequestBody":[],
                    "RequestHeader":[],
                    "PathVariable":[]
                }
                $.each(params,function(i,p){
                    groupedParams[p.paramAnno].push(p);
                });
                return {
                    ajax: function (conf) {
                        return doAjax(path,groupedParams,conf);
                    }
                }
            }

        }
        return call;
    }

    var setByPath = function(target,path,valueToSet){
        var current = target;
        path.split(".").forEach(function (section, index, array) {
            if (index != array.length - 1) {
                current[section] = current[section] || {};
                current = current[section];
            } else {
                current[section] = valueToSet;
            }
        })
    };

    methods.forEach(function (method) {
        var theFunc = newFunction(method.path, method.paramList);
        setByPath(routes,method.name,theFunc);
        $.each(method.alias,function(i,p){
           setByPath(routes,p,theFunc);
        });
    });


    return routes;
})
/*(csrf_token)  csrf_token param will be added by render controller*/

