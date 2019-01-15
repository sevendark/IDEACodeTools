package com.sevendark.ai.lib;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.sevendark.ai.lib.Constant.AFTER_REG;
import static com.sevendark.ai.lib.Constant.PRE_REG;

public enum SQLUtil {
    /**
     * mysql
     */
    MYSQL(new LinkedHashMap<String, SQLRule>(){
        {
            put("select", SQLRule.build().needNewLine(true).placehoder("*").pattern("select" + AFTER_REG));
            put("selectDistinct", SQLRule.build().needNewLine(true).sqlName("select distinct").pattern("selectDistinct" + AFTER_REG));
            put("as", SQLRule.build().needQualifier(true).pattern(PRE_REG + "as" + AFTER_REG));

            put("from", SQLRule.build().needNewLine(true).pattern("from" + AFTER_REG));

            put("join", SQLRule.build().pattern("join" + AFTER_REG));
            put("leftJoin", SQLRule.build().sqlName("left join").pattern("leftJoin" + AFTER_REG));
            put("leftOuterJoin", SQLRule.build().sqlName("left outer join").pattern("leftOuterJoin" + AFTER_REG));
            put("rightJoin", SQLRule.build().sqlName("right join").pattern("rightJoin" + AFTER_REG));
            put("rightOuterJoin", SQLRule.build().sqlName("right outer join").pattern("rightOuterJoin" + AFTER_REG));
            put("on", SQLRule.build().needParen(true).pattern("on" + AFTER_REG));

            put("where", SQLRule.build().needNewLine(true).needParen(true).pattern("where" + AFTER_REG));
            put("orderBy", SQLRule.build().sqlName("order by").pattern("orderBy" + AFTER_REG));
            put("groupBy", SQLRule.build().sqlName("group by").pattern("groupBy" + AFTER_REG));

            put("eq", SQLRule.build().needParen(true).sqlName(" = ").needQualifier(true).pattern(PRE_REG + "eq" + AFTER_REG));
            put("in", SQLRule.build().needParen(true).needQualifier(true).sqlName(" in ").pattern(PRE_REG + "in" + AFTER_REG));
            put("ge", SQLRule.build().needParen(true).sqlName(" > ").needQualifier(true).pattern(PRE_REG + "ge" + AFTER_REG));
            put("le", SQLRule.build().needParen(true).needQualifier(true).sqlName(" < ").pattern(PRE_REG + "lq" + AFTER_REG));
            put("isTrue", SQLRule.build().needQualifier(true).sqlName(" = True").pattern(PRE_REG + "isTrue").placehoder(""));
            put("isFalse", SQLRule.build().needQualifier(true).sqlName(" = False").pattern(PRE_REG + "isFalse").placehoder(""));
            put("isNull", SQLRule.build().needQualifier(true).sqlName("is null").pattern(PRE_REG + "isNull").placehoder(""));
            put("isNotNull", SQLRule.build().needQualifier(true).sqlName("is not Null").pattern(PRE_REG + "isNotNull").placehoder(""));
            put("notIn", SQLRule.build().needQualifier(true).sqlName("not in").pattern(PRE_REG + "notIn" + AFTER_REG));
            put("like", SQLRule.build().needQualifier(true).pattern(PRE_REG + "like" + AFTER_REG));

            put("and", SQLRule.build().needParen(true).pattern("and" + AFTER_REG));
            put("or", SQLRule.build().needParen(true).pattern("or" + AFTER_REG));

            put("desc", SQLRule.build().needQualifier(true).pattern(PRE_REG + "desc").placehoder(""));
            put("asc", SQLRule.build().needQualifier(true).pattern(PRE_REG + "asc").placehoder(""));
        }
    }),
    ;
    public Map<String, SQLRule> replaceMap;

    SQLUtil(Map<String, SQLRule> replaceMap){
        this.replaceMap = replaceMap;
    }

}
