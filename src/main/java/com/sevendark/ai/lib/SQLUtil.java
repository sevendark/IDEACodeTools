package com.sevendark.ai.lib;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum SQLUtil {
    MYSQL(new HashMap<String, SQLRule>(){
        {
            put("select", SQLRule.build()
                    .needParen(false)
                    .placehoder("*"));

            put("from", SQLRule.build().needParen(false));
            put("where", SQLRule.build().needParen(false));

            put("and", SQLRule.build().needParen(true));
            put("or", SQLRule.build().needParen(true));

            put("leftJoin", SQLRule.build().needParen(false));
            put("leftOuterJoin", SQLRule.build().needParen(false));
            put("leftInnerJoin", SQLRule.build().needParen(false));
            put("rightJoin", SQLRule.build().needParen(false));
            put("rightOuterJoin", SQLRule.build().needParen(false));
            put("rightInnerJoin", SQLRule.build().needParen(false));

            put("on", SQLRule.build().needParen(false));
            put("orderBy", SQLRule.build().needParen(false));

        }
    }),
    ;
    public Map<String, SQLRule> replaceMap;
    public String pattern;

    SQLUtil(Map<String, SQLRule> replaceMap){
        this.replaceMap = replaceMap;
        pattern = String.join("|", replaceMap.keySet().stream().map(e -> e + "(.*)").collect(Collectors.toSet()));
    }

}
