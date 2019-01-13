package com.sevendark.ai.lib;

public class SQLRule {
    public boolean needParen;
    public String placeholder;

    public SQLRule needParen(boolean needParen){
        this.needParen = needParen;
        return this;
    }

    public SQLRule placehoder(String placeholder){
        this.placeholder = placeholder;
        return this;
    }

    public static SQLRule build(){
        return new SQLRule();
    }
}
