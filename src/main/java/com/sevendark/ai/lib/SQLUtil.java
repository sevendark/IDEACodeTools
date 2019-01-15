package com.sevendark.ai.lib;

import java.util.LinkedHashMap;
import java.util.Map;

public enum SQLUtil {
    /**
     * mysql
     */
    MYSQL(new LinkedHashMap<String, SQLRule>(){
        {
            put("select", SQLRule.build().needNewLine(true).placehoder("*"));
            put("selectDistinct", SQLRule.build().needNewLine(true).sqlName("select distinct"));
            put("as", SQLRule.build().needQualifier(true));

            put("from", SQLRule.build().needNewLine(true));

            put("join", SQLRule.build().needNewLine(true));
            put("leftJoin", SQLRule.build().sqlName("left join"));
            put("leftOuterJoin", SQLRule.build().sqlName("left outer join"));
            put("rightJoin", SQLRule.build().sqlName("right join"));
            put("rightOuterJoin", SQLRule.build().sqlName("right outer join"));
            put("on", SQLRule.build().needParen(true));

            put("where", SQLRule.build().needNewLine(true).needParen(true));
            put("orderBy", SQLRule.build().needNewLine(true).sqlName("order by"));
            put("groupBy", SQLRule.build().needNewLine(true).sqlName("group by"));

            put("eq", SQLRule.build().needParen(true).needQualifier(true).sqlName("="));
            put("in", SQLRule.build().needParen(true).needQualifier(true).sqlName("in"));
            put("ge", SQLRule.build().needParen(true).needQualifier(true).sqlName(">"));
            put("le", SQLRule.build().needParen(true).needQualifier(true).sqlName("<"));
            put("isTrue", SQLRule.build().needQualifier(true).sqlName("= True"));
            put("isFalse", SQLRule.build().needQualifier(true).sqlName("= False"));
            put("isNull", SQLRule.build().needQualifier(true).sqlName("is null"));
            put("isNotNull", SQLRule.build().needQualifier(true).sqlName("is not null"));
            put("notIn", SQLRule.build().needParen(true).needQualifier(true).sqlName("not in"));
            put("like", SQLRule.build().needQualifier(true));

            put("and", SQLRule.build().needParen(true));
            put("or", SQLRule.build().needParen(true));

            put("desc", SQLRule.build().needQualifier(true));
            put("asc", SQLRule.build().needQualifier(true));
        }
    }),
    ;
    public Map<String, SQLRule> replaceMap;

    SQLUtil(Map<String, SQLRule> replaceMap){
        this.replaceMap = replaceMap;
    }

}
