package com.sevendark.ai.plugin;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.sevendark.ai.lib.Constant;
import com.sevendark.ai.lib.SQL;
import com.sevendark.ai.lib.SQLRule;
import com.sevendark.ai.lib.SQLUtil;
import org.apache.commons.lang.StringUtils;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.sevendark.ai.lib.Constant.*;

public class GenerateJooqSqlAction extends AnAction {
    private static final String Name = "Generate Jooq Sql";

    public GenerateJooqSqlAction() {
        super(Name);
    }

    private Project project;
    private Language language;
    private Caret caret;
    private SQLUtil dialect = SQLUtil.MYSQL;
    private StringBuilder sql;
    private SQLRule last = null;

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        final CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();

        if(!validAndSet(event)) return;

        final StringBuilder selectedText = new StringBuilder(Objects.requireNonNull(caret.getSelectedText())
                .chars()
                .boxed().map(e -> Character.toString((char)e.intValue()))
                .filter(e ->  e.matches("[^\\s]+"))
                .collect(Collectors.joining())
        );

        try{
            appendSQL(selectedText);
            if(sql.length() != 0){
                copyPasteManager.setContents(new StringSelection(new BasicFormatterImpl().format(sql.toString())));
            }
        }catch (Exception e){
            e.printStackTrace();
            Messages.showErrorDialog(e.getLocalizedMessage(), Name);
        }
    }

    private boolean appendSQL(final StringBuilder selectedText) {
        SQL sqlBean = getNext(selectedText);

        if(Objects.isNull(sqlBean) || StringUtils.isBlank(sqlBean.refName)){
            return false;
        }

        if(!dialect.replaceMap.containsKey(sqlBean.refName)) {
            return false;
        }

        SQLRule rule = dialect.replaceMap.get(sqlBean.refName);

        if(Objects.nonNull(last) && last.start){
            sql.append(rule.beStart);
        }

        if(rule.needQualifier){
            if(StringUtils.isNotBlank(sqlBean.qualifierSir)){
                if(!appendSQL(new StringBuilder(sqlBean.qualifierSir))){
                    sql.append(replaceVar(sqlBean.qualifierSir));
                }
            }
        }
        sql.append(" ");
        sql.append(rule.getFinalSQLName(sqlBean.refName));
        sql.append(" ");

        if(StringUtils.isNotBlank(sqlBean.bodyStr) || StringUtils.isNotBlank(rule.placeholder)){
            if(rule.needParen) {
                sql.append("(");
            }
            if(StringUtils.isBlank(sqlBean.bodyStr)){
                sql.append(rule.placeholder);
            }else if (!appendSQL(new StringBuilder(sqlBean.bodyStr))){
                sql.append(replaceVar(sqlBean.bodyStr));
            }
            if(rule.needParen) {
                sql.append(")");
            }
            sql.append(" ");
        }

        last = rule;
        appendSQL(selectedText);
        return true;
    }

    private String replaceVar(String str){
        if(StringUtils.isBlank(str)){
            return "";
        }
        if(str.matches(STR)){
            return str;
        }
        return str.replaceAll(VAR, "---");
    }

    private SQL getSQL(StringBuilder fullMethod){
        SQL sql = new SQL();

        if(Objects.equals(fullMethod.charAt(0), '.')){
            fullMethod.deleteCharAt(0);
        }

        final int lastIndexOfLP = fullMethod.indexOf("(");
        if(lastIndexOfLP != -1){
            sql.bodyStr = fullMethod.substring(fullMethod.indexOf("(") + 1, fullMethod.length() - 1);
            fullMethod.delete(fullMethod.indexOf("("), fullMethod.length());
        }

        sql.refName = fullMethod.substring(fullMethod.lastIndexOf(".") +  1, fullMethod.length());
        final int lastIndexOfPoint = fullMethod.lastIndexOf(".");
        if(lastIndexOfPoint == -1){
            fullMethod.delete(0, fullMethod.length());
        }else{
            fullMethod.delete(fullMethod.lastIndexOf("."), fullMethod.length());
        }

        sql.qualifierSir = fullMethod.toString();
        return sql;
    }

    private SQL getNext(final StringBuilder code) {
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

    private int depFind(final StringBuilder code, int start){
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

    private boolean validAndSet(AnActionEvent event){
        project = event.getProject();
        if (Objects.isNull(project)) {
            Messages.showInfoMessage("Please Select a piece of JOOQ code", Name);
            return false;
        }
        language = event.getData(DataKeys.LANGUAGE);
        if (Objects.isNull(language)) {
            Messages.showInfoMessage("Please Select a piece of JOOQ code", Name);
            return false;
        }
        if(!Constant.supportLanuage.contains(language.getID())){
            Messages.showInfoMessage("Only Support : " + Constant.supportLanuage.toString(), Name);
        }
        caret = event.getData(DataKeys.CARET);
        if (Objects.isNull(caret)) {
            Messages.showInfoMessage("Please Select a piece of JOOQ code", Name);
            return false;
        }
        if (!caret.hasSelection()) {
            Messages.showInfoMessage("Please Select a piece of JOOQ code", Name);
            return false;
        }
        if (StringUtils.isBlank((caret.getSelectedText()))) {
            Messages.showInfoMessage("Please Select a piece of JOOQ code", Name);
            return false;
        }
        sql = new StringBuilder();
        return true;
    }
}
