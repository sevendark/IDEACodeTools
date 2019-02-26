package com.sevendark.ai.lib;

import java.util.LinkedHashMap;
import java.util.Map;

public enum SQLUtil {
    /**
     * mysql
     */
    MYSQL(new LinkedHashMap<String, SQLRule>(){
        {
            put("select", SQLRule.build().placehoder("*"));
            put("selectOne", SQLRule.build().sqlName("select 1"));
            put("selectDistinct", SQLRule.build().sqlName("select distinct"));
            put("selectCount", SQLRule.build().sqlName("select \n\tcount(1)"));
            put("as", SQLRule.build().needQualifier(true));

            put("from", SQLRule.build());

            put("join", SQLRule.build());
            put("leftJoin", SQLRule.build().sqlName("left join"));
            put("leftOuterJoin", SQLRule.build().sqlName("left outer join"));
            put("rightJoin", SQLRule.build().sqlName("right join"));
            put("rightOuterJoin", SQLRule.build().sqlName("right outer join"));
            put("on", SQLRule.build().needParen(true));

            put("where", SQLRule.build().needParen(true).start(true));
            put("orderBy", SQLRule.build().sqlName("order by"));
            put("groupBy", SQLRule.build().sqlName("group by"));

            put("eq", SQLRule.build().needParen(true).needQualifier(true).sqlName("="));
            put("equal", SQLRule.build().needParen(true).needQualifier(true).sqlName("="));
            put("ne", SQLRule.build().needParen(true).needQualifier(true).sqlName("<>"));
            put("in", SQLRule.build().needParen(true).needQualifier(true).sqlName("in"));
            put("notIn", SQLRule.build().needParen(true).needQualifier(true).sqlName("not in"));
            put("ge", SQLRule.build().needParen(true).needQualifier(true).sqlName(">"));
            put("gt", SQLRule.build().needParen(true).needQualifier(true).sqlName(">="));
            put("le", SQLRule.build().needParen(true).needQualifier(true).sqlName("<"));
            put("lt", SQLRule.build().needParen(true).needQualifier(true).sqlName("<="));
            put("isTrue", SQLRule.build().needQualifier(true).sqlName("= True"));
            put("isFalse", SQLRule.build().needQualifier(true).sqlName("= False"));
            put("isNull", SQLRule.build().needQualifier(true).sqlName("is null"));
            put("isNotNull", SQLRule.build().needQualifier(true).sqlName("is not null"));
            put("like", SQLRule.build().needQualifier(true));

            put("and", SQLRule.build().needParen(true).beStart("1=1"));
            put("or", SQLRule.build().needParen(true).beStart("0=1"));

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
