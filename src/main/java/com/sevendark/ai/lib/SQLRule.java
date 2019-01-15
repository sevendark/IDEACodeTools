package com.sevendark.ai.lib;

import org.apache.commons.lang.StringUtils;

public class SQLRule {
    public boolean needParen = false;
    public boolean needQualifier = false;
    public boolean needNewLine = false;
    public String placeholder = "";
    public String sqlName = null;


    public SQLRule needParen(boolean needParen){
        this.needParen = needParen;
        return this;
    }

    public SQLRule needQualifier(boolean needQualifier){
        this.needQualifier = needQualifier;
        return this;
    }

    public SQLRule needNewLine(boolean needNewLine){
        this.needNewLine = needNewLine;
        return this;
    }

    public SQLRule placehoder(String placeholder){
        this.placeholder = placeholder;
        return this;
    }

    public SQLRule sqlName(String sqlName){
        this.sqlName = sqlName;
        return this;
    }

    public static SQLRule build(){
        return new SQLRule();
    }

    public String getFinalSQLName(String sqlName){
        return StringUtils.isBlank(this.sqlName) ? sqlName : this.sqlName;
    }

}
