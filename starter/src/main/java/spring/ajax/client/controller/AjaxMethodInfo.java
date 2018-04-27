package spring.ajax.client.controller;

import java.lang.reflect.Type;
import java.util.List;

public class AjaxMethodInfo {
    private String name;
    private String[] alias;
    private String path;

    private List<Param> paramList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Param> getParamList() {
        return paramList;
    }

    public void setParamList(List<Param> paramList) {
        this.paramList = paramList;
    }

    public String[] getAlias() {
        return alias;
    }

    public void setAlias(String[] alias) {
        this.alias = alias;
    }

    public static class Param{
        private String name;
        private Type type;
        private boolean optional;
        private String typeName;
        private String fullTypeName;
        private String paramAnno;
        private String defaultValue;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public boolean isOptional() {
            return optional;
        }

        public void setOptional(boolean optional) {
            this.optional = optional;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public String getFullTypeName() {
            return fullTypeName;
        }

        public void setFullTypeName(String fullTypeName) {
            this.fullTypeName = fullTypeName;
        }

        public String getParamAnno() {
            return paramAnno;
        }

        public void setParamAnno(String paramAnno) {
            this.paramAnno = paramAnno;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }
}
