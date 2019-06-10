package com.sevendark.ai.plugin.lib.sql;

import com.sevendark.ai.plugin.lib.Constant;
import com.sevendark.ai.plugin.lib.sql.formatter.HibernateSqlFormatter;
import com.sevendark.ai.plugin.lib.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.sevendark.ai.plugin.lib.Constant.JAVA_COMMENT;

public class JooqToSqlConverter {

    private JooqToSqlConverter() {
    }

    private SQLMapper dialect = SQLMapper.MYSQL;
    private StringBuilder sqlResult;
    private List<SQLStatement> root;

    public static String convert(String text) {
        if(StringUtils.isBlank(text)){
            return "";
        }
        text = text.replaceAll(JAVA_COMMENT, "");
        final StringBuilder selectedText = new StringBuilder(text
                .chars()
                .boxed().map(e -> Character.toString((char)e.intValue()))
                .filter(e ->  e.matches("[^\\s]+"))
                .collect(Collectors.joining())
        );
        if(StringUtils.isBlank(text)){
            return "";
        }
        return new JooqToSqlConverter().doConvert(new StringBuilder(selectedText));
    }

    public String doConvert(StringBuilder text) {
        try{
            ini(text);
            SQLStatement parent = new SQLStatement();
            parent.body = root;
            root.forEach(e -> appendSQL(e, parent));
            if(sqlResult.length() != 0){
                return new HibernateSqlFormatter().format(sqlResult.toString());
            }
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return null;
    }

    private void ini(StringBuilder selectedText){
        sqlResult = new StringBuilder();
        root = SQLReader.readSQL(selectedText);
    }

    private void appendSQL(SQLStatement sqlStatementBean, SQLStatement parent) {
        List<SQLStatement> myList = parent.body;

        if(!dialect.replaceMap.containsKey(sqlStatementBean.refName.toString())) {
            sqlResult.append(replaceStatement(sqlStatementBean));
            SQLMapperBean parentRule;
            if(StringUtils.isNotBlank(parent.refName.toString())) {
                parentRule = getRule(parent);
            }else{
                parentRule = new SQLMapperBean();
                parentRule.replaceSplit = "";
            }

            if(sqlStatementBean.haveDott && StringUtils.isBlank(parentRule.replaceSplit)){
                sqlResult.append(",");
            }
            if(sqlStatementBean.haveDott && StringUtils.isNotBlank(parentRule.replaceSplit)){
                sqlResult.append(parentRule.replaceSplit);
            }
            return;
        }

        SQLMapperBean rule = getRule(sqlStatementBean);

        if(getPreMapper(sqlStatementBean, myList).isStart && getPreStatement(sqlStatementBean, myList).body.size() == 0){
            sqlResult.append(rule.beStart);
        }

        if(rule.needQualifier){
            sqlResult.append(replaceStr(sqlStatementBean.qualifierSir));
        }
        sqlResult.append(" ");
        if(StringUtils.isBlank(rule.onlyNeedFirst)){
            sqlResult.append(rule.getFinalSQLName(sqlStatementBean.refName));
        } else if(StringUtils.isNotBlank(rule.onlyNeedFirst) && isFirstMe(myList, sqlStatementBean)){
            sqlResult.append(rule.getFinalSQLName(sqlStatementBean.refName));
        } else if(StringUtils.isNotBlank(rule.onlyNeedFirst) && !isFirstMe(myList, sqlStatementBean)) {
            sqlResult.append(rule.onlyNeedFirst);
        }
        sqlResult.append(" ");

        if(sqlStatementBean.body.size() != 0){
            if(rule.needParen && sqlStatementBean.body.size() > 1) {
                sqlResult.append("(");
            }
            if(sqlStatementBean.body.size() == 0){
                sqlResult.append(rule.placeholder);
            }else{
                sqlStatementBean.body.forEach(e -> appendSQL(e, sqlStatementBean));
            }
            if(rule.needParen && sqlStatementBean.body.size() > 1) {
                sqlResult.append(")");
            }
            sqlResult.append(" ");
        }else {
            sqlResult.append(rule.placeholder);
        }

    }

    private boolean isFirstMe(List<SQLStatement> myList, SQLStatement sqlStatementBean){
        final Optional<SQLStatement> first = myList.stream()
                .filter(s -> s.refName.toString().equals(sqlStatementBean.refName.toString()))
                .findFirst();
        return first.map(s -> s.i == sqlStatementBean.i).orElse(true);
    }

    private SQLStatement getPreStatement(SQLStatement sqlStatementBean, List<SQLStatement> myList){
        int i = sqlStatementBean.i;
        if(i > 0){
            i--;
        }
        return myList.get(i);
    }

    private SQLMapperBean getPreMapper(SQLStatement sqlStatementBean, List<SQLStatement> myList){
        return getRule(getPreStatement(sqlStatementBean, myList));
    }

    private SQLMapperBean getRule(SQLStatement sqlStatementBean){
        final SQLMapperBean sqlMapperBean = dialect.replaceMap.get(sqlStatementBean.refName.toString());
        if(sqlMapperBean == null) throw new IllegalArgumentException("not support " +
                sqlStatementBean.refName +
                "now , but you can only transform condition of it.");
        return sqlMapperBean;
    }

    private String replaceStatement(SQLStatement sqlStatementBean){
        if(sqlStatementBean.refName.toString().equals("name")){
            final List<SQLStatement> enumQu = SQLReader.readSQL(sqlStatementBean.qualifierSir);
            if(enumQu.size() > 0){
                return enumQu.get(0).refName.insert(0, "'").append("'").toString();
            }
        }
        StringBuilder all;
        if(sqlStatementBean.qualifierSir.length() == 0){
            all = sqlStatementBean.refName;
        }else{
            all = sqlStatementBean.qualifierSir.append(".").append(sqlStatementBean.refName);
        }
        return replaceStr(all);
    }

    private String replaceStr(StringBuilder str){
        if(str.length() == 0){
            return "";
        }
        if(str.toString().matches(Constant.STR)){
            return str.toString();
        }
        if(str.toString().matches(Constant.VAR)){
            str.insert(0, "'");
            str.append("'");
            return str.toString();
        }
        return str.toString();
    }
}
