package com.sevendark.ai.plugin.lib.sql;

import java.util.LinkedHashMap;
import java.util.Map;

public enum SQLMapper {
    /**
     * mysql
     */
    MYSQL(new LinkedHashMap<String, SQLMapperBean>(){
        {
            put("select", SQLMapperBean.build().placehoder("*"));
            put("selectOne", SQLMapperBean.build().sqlName("select 1"));
            put("selectDistinct", SQLMapperBean.build().sqlName("select distinct"));
            put("selectFrom", SQLMapperBean.build().sqlName("select * from"));
            put("selectCount", SQLMapperBean.build().sqlName("select \n\tcount(1)"));
            put("as", SQLMapperBean.build().needQualifier(true));

            put("from", SQLMapperBean.build());

            put("join", SQLMapperBean.build());
            put("leftJoin", SQLMapperBean.build().sqlName("left join"));
            put("leftOuterJoin", SQLMapperBean.build().sqlName("left outer join"));
            put("rightJoin", SQLMapperBean.build().sqlName("right join"));
            put("rightOuterJoin", SQLMapperBean.build().sqlName("right outer join"));
            put("on", SQLMapperBean.build().needParen(true));

            put("where", SQLMapperBean.build().needParen(true).isStart(true));
            put("orderBy", SQLMapperBean.build().sqlName("order by"));
            put("groupBy", SQLMapperBean.build().sqlName("group by"));

            put("eq", SQLMapperBean.build().needParen(true).needQualifier(true).sqlName("="));
            put("equal", SQLMapperBean.build().needParen(true).needQualifier(true).sqlName("="));
            put("ne", SQLMapperBean.build().needParen(true).needQualifier(true).sqlName("<>"));
            put("in", SQLMapperBean.build().needParen(true).needQualifier(true).sqlName("in"));
            put("notIn", SQLMapperBean.build().needParen(true).needQualifier(true).sqlName("not in"));
            put("andExists", SQLMapperBean.build().needParen(true).needQualifier(true).sqlName("and exists"));
            put("ge", SQLMapperBean.build().needParen(true).needQualifier(true).sqlName(">"));
            put("gt", SQLMapperBean.build().needParen(true).needQualifier(true).sqlName(">="));
            put("le", SQLMapperBean.build().needParen(true).needQualifier(true).sqlName("<"));
            put("lt", SQLMapperBean.build().needParen(true).needQualifier(true).sqlName("<="));
            put("isTrue", SQLMapperBean.build().needQualifier(true).sqlName("= True"));
            put("falseCondition", SQLMapperBean.build().sqlName("0=1"));
            put("trueCondition", SQLMapperBean.build().sqlName("1=1"));
            put("isFalse", SQLMapperBean.build().needQualifier(true).sqlName("= False"));
            put("isNull", SQLMapperBean.build().needQualifier(true).sqlName("is null"));
            put("isNotNull", SQLMapperBean.build().needQualifier(true).sqlName("is not null"));
            put("like", SQLMapperBean.build().needQualifier(true));

            put("and", SQLMapperBean.build().needParen(true).beStart("1=1"));
            put("or", SQLMapperBean.build().needParen(true).beStart("0=1"));

            put("set", SQLMapperBean.build().onlyNeedFirst(",").replaceSplit("="));
            put("update", SQLMapperBean.build());
            put("deleteFrom", SQLMapperBean.build());

            put("desc", SQLMapperBean.build().needQualifier(true));
            put("asc", SQLMapperBean.build().needQualifier(true));
        }
    }),
    ;
    public Map<String, SQLMapperBean> replaceMap;

    SQLMapper(Map<String, SQLMapperBean> replaceMap){
        this.replaceMap = replaceMap;
    }

}
