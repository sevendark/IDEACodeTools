package com.sevendark.ai.plugin.lib.sql;

import com.sevendark.ai.plugin.lib.util.StringUtils;

public class SQLMapperBean {
    public boolean needParen = false;
    public boolean needQualifier = false;
    public String beStart = "";
    public boolean isStart = false;
    public String onlyNeedFirst = "";
    public String placeholder = "";
    public String replaceSplit = "";
    public String sqlName = null;


    SQLMapperBean needParen(boolean needParen){
        this.needParen = needParen;
        return this;
    }

    SQLMapperBean needQualifier(boolean needQualifier){
        this.needQualifier = needQualifier;
        return this;
    }

    SQLMapperBean isStart(boolean isStart){
        this.isStart = isStart;
        return this;
    }

    SQLMapperBean onlyNeedFirst(String onlyNeedFirst){
        this.onlyNeedFirst = onlyNeedFirst;
        return this;
    }

    SQLMapperBean beStart(String beStart){
        this.beStart = beStart;
        return this;
    }

    SQLMapperBean replaceSplit(String replaceSplit){
        this.replaceSplit = replaceSplit;
        return this;
    }

    SQLMapperBean placehoder(String placeholder){
        this.placeholder = placeholder;
        return this;
    }

    SQLMapperBean sqlName(String sqlName){
        this.sqlName = sqlName;
        return this;
    }

    static SQLMapperBean build(){
        return new SQLMapperBean();
    }

    public String getFinalSQLName(StringBuilder sqlName){
        return StringUtils.isBlank(this.sqlName) ? sqlName.toString() : this.sqlName;
    }

}
