package com.sevendark.sql.lib;

import org.apache.commons.lang.StringUtils;

public class SQLMapperBean {
    public boolean needParen = false;
    public boolean needQualifier = false;
    public String beStart = "";
    public boolean start = false;
    public String placeholder = "";
    public String sqlName = null;


    SQLMapperBean needParen(boolean needParen){
        this.needParen = needParen;
        return this;
    }

    SQLMapperBean needQualifier(boolean needQualifier){
        this.needQualifier = needQualifier;
        return this;
    }

    SQLMapperBean start(boolean start){
        this.start = start;
        return this;
    }

    SQLMapperBean beStart(String beStart){
        this.beStart = beStart;
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
