package com.sevendark.ai.plugin.lib.sql;

import com.sevendark.ai.plugin.lib.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLReader {

    public static List<SQLStatement> readSQL(StringBuilder selectedText){
        List<SQLStatement> root = new ArrayList<>();

        int i = 0;
        for(SQLStatement statement = getNext(selectedText);
            Objects.nonNull(statement);
            statement = getNext(selectedText)){
            statement.i = i++;
            if(statement.bodyStr.toString().matches(Constant.STR)){
                List<SQLStatement> temp = new ArrayList<>();
                SQLStatement s = new SQLStatement();
                s.refName = new StringBuilder(statement.bodyStr);
                temp.add(s);
                statement.body = temp;
            }else{
                statement.body = readSQL(statement.bodyStr);
            }

            root.add(statement);
        }

        return root;
    }

    private static SQLStatement getSQL(StringBuilder fullMethod, boolean haveDott){
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
        sqlStatement.haveDott = haveDott;
        return sqlStatement;
    }

    private static SQLStatement getNext(final StringBuilder code) {
        Pattern pattern = Pattern.compile(Constant.METHOD);
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            int end = matcher.end();
            if(code.charAt(end - 1) == '('){
                end = depFind(code, end);
            }
            String group = code.substring(matcher.start(), end);
            code.delete(0, end);
            boolean haveDott = false;
            if(code.length() > 0 && code.charAt(0) == ','){
                haveDott = true;
                code.deleteCharAt(0);
            }
            return getSQL(new StringBuilder(group), haveDott);
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
