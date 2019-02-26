package com.sevendark.ai.lib;

import org.apache.commons.lang.StringUtils;

public class SQLRule {
    public boolean needParen = false;
    public boolean needQualifier = false;
    public String beStart = "";
    public boolean start = false;
    public String placeholder = "";
    public String sqlName = null;


    SQLRule needParen(boolean needParen){
        this.needParen = needParen;
        return this;
    }

    SQLRule needQualifier(boolean needQualifier){
        this.needQualifier = needQualifier;
        return this;
    }

    SQLRule start(boolean start){
        this.start = start;
        return this;
    }

    SQLRule beStart(String beStart){
        this.beStart = beStart;
        return this;
    }

    SQLRule placehoder(String placeholder){
        this.placeholder = placeholder;
        return this;
    }

    SQLRule sqlName(String sqlName){
        this.sqlName = sqlName;
        return this;
    }

    static SQLRule build(){
        return new SQLRule();
    }

    public String getFinalSQLName(String sqlName){
        return StringUtils.isBlank(this.sqlName) ? sqlName : this.sqlName;
    }

}
