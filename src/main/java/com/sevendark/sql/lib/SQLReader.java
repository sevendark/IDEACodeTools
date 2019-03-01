package com.sevendark.sql.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sevendark.sql.lib.Constant.METHOD;

public class SQLReader {

    public static List<SQLStatement> readSQL(StringBuilder selectedText){
        List<SQLStatement> root = new ArrayList<>();

        for(SQLStatement statement = getNext(selectedText);
            Objects.nonNull(statement);
            statement = getNext(selectedText)){
            statement.body = readSQL(statement.bodyStr);
            root.add(statement);
        }

        return root;
    }

    private static SQLStatement getSQL(StringBuilder fullMethod){
        SQLStatement sqlStatement = new SQLStatement();

        if(Objects.equals(fullMethod.charAt(0), '.')){
            fullMethod.deleteCharAt(0);
        }

        final int lastIndexOfLP = fullMethod.indexOf("(");
        if(lastIndexOfLP != -1){
            sqlStatement.bodyStr = new StringBuilder(fullMethod.substring(fullMethod.indexOf("(") + 1, fullMethod.length() - 1));
            fullMethod.delete(fullMethod.indexOf("("), fullMethod.length());
        }

        sqlStatement.refName = new StringBuilder(fullMethod.substring(fullMethod.lastIndexOf(".") +  1, fullMethod.length()));
        final int lastIndexOfPoint = fullMethod.lastIndexOf(".");
        if(lastIndexOfPoint == -1){
            fullMethod.delete(0, fullMethod.length());
        }else{
            fullMethod.delete(fullMethod.lastIndexOf("."), fullMethod.length());
        }

        sqlStatement.qualifierSir = new StringBuilder(fullMethod);
        return sqlStatement;
    }

    private static SQLStatement getNext(final StringBuilder code) {
        Pattern pattern = Pattern.compile(METHOD);
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            int end = matcher.end();
            if(code.charAt(end - 1) == '('){
                end = depFind(code, end);
            }
            String group = code.substring(matcher.start(), end);
            code.delete(0, end);
            return getSQL(new StringBuilder(group));
        }
        return null;
    }

    private static int depFind(final StringBuilder code, int start){
        char[] chars = code.toString().toCharArray();
        int stack = 1;
        int end = start;
        for (int i = start; i < chars.length; i++) {
            end++;
            if (chars[i] == '(') {
                stack++;
            } else if (chars[i] == ')') {
                stack--;
                if (stack == 0) {
                    return end;
                }
            }
        }
        return start;
    }
}
