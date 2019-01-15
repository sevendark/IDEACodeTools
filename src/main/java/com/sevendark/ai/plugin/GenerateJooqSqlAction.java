package com.sevendark.ai.plugin;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.sevendark.ai.lib.Constant;
import com.sevendark.ai.lib.SQL;
import com.sevendark.ai.lib.SQLRule;
import com.sevendark.ai.lib.SQLUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScMethodCall;
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory;

import java.awt.datatransfer.StringSelection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        final CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();

        if(!validAndSet(event)) return;

        final StringBuilder selectedText = new StringBuilder(caret.getSelectedText().chars()
                .boxed().map(e -> Character.toString((char)e.intValue()))
                .filter(e ->  e.matches("[^\\s]+"))
                .collect(Collectors.joining())
        );

        PsiElementFactory elementFactory = null;
        if (Objects.equals("JAVA", language.getID())) {
            final JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            elementFactory = javaPsiFacade.getElementFactory();
        }
        try{
            appendSQL(selectedText, elementFactory);
            if(sql.length() != 0){
                copyPasteManager.setContents(new StringSelection(sql.toString()));
            }
        }catch (Exception e){
            e.printStackTrace();
            Messages.showErrorDialog("Some error of generate SQL, \n" +
                    e.getLocalizedMessage() + "\n" +
                    " Please help to fix it https://github.com/sevendark/IDEACodeTools/issues", Name);
        }
    }

    private boolean appendSQL(final StringBuilder selectedText, final PsiElementFactory elementFactory) {
        String code = getNext(selectedText);
        if(StringUtils.isBlank(code)) return false;

        SQL sqlBean = getSQL(elementFactory, code);

        if(Objects.isNull(sqlBean) || StringUtils.isBlank(sqlBean.refName)){
            return false;
        }

        if(!dialect.replaceMap.containsKey(sqlBean.refName)) {
            return false;
        }

        SQLRule rule = dialect.replaceMap.get(sqlBean.refName);
        if(rule.needQualifier){
            if(StringUtils.isNotBlank(sqlBean.qualifierSir)){
                if(!appendSQL(new StringBuilder(sqlBean.qualifierSir), elementFactory)){
                    sql.append(replaceVar(sqlBean.qualifierSir));
                }
            }
        }
        if(rule.needNewLine){
            sql.append("\n");
        }
        sql.append(" ");
        sql.append(rule.getFinalSQLName(sqlBean.refName));
        sql.append(" ");
        if(rule.needNewLine){
            sql.append("\n");
        }
        if(rule.needParen) {
            sql.append("(");
        }
        if(StringUtils.isBlank(sqlBean.bodyStr)){
            sql.append(rule.placeholder);
        }else if (!appendSQL(new StringBuilder(sqlBean.bodyStr), elementFactory)){
            sql.append(replaceVar(sqlBean.bodyStr));
        }
        if(rule.needParen) {
            sql.append(")");
        }
        if(rule.needNewLine){
            sql.append("\n");
        }
        sql.append(" ");
        appendSQL(selectedText, elementFactory);
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

    private SQL getSQL(final PsiElementFactory elementFactory, final String code){
        SQL sql = new SQL();
        String refName = null;
        String bodyStr = null;
        String qualifierSir = null;
        if(Objects.isNull(elementFactory) && Objects.equals("Scala", language.getID())){
            final PsiElement elementFromText = ScalaPsiElementFactory.createElementFromText(code, project);
            if(!(elementFromText instanceof ScMethodCall)) {
                if(dialect.replaceMap.containsKey(elementFromText.getLastChild().getText())){
                    sql.refName = elementFromText.getLastChild().getText();
                    sql.qualifierSir = elementFromText.getFirstChild().getText();
                    sql.bodyStr = "";
                    return sql;
                }
                return null;
            }
            ScMethodCall methodCall = (ScMethodCall)elementFromText;
            refName = methodCall.getInvokedExpr().getLastChild().getText();
            bodyStr = Stream.of(methodCall.args().exprsArray()).map(PsiElement::getText).collect(Collectors.joining(", "));
            final PsiElement firstChild = methodCall.getInvokedExpr().getFirstChild();
            if(Objects.nonNull(firstChild) &&
                    StringUtils.isNotBlank(firstChild.getText()) &&
                    !Objects.equals(refName, firstChild.getText())){
                qualifierSir = firstChild.getText();
            }
            System.out.println(elementFromText);
        }else if (Objects.nonNull(elementFactory)){
            final PsiStatement statementFromText = elementFactory.createStatementFromText(code, null);
            PsiElement element = statementFromText.getFirstChild();
            if (!(element instanceof PsiMethodCallExpression)) {
                return null;
            }
            refName = ((PsiMethodCallExpression) element).getMethodExpression().getReferenceName();
            Stream<PsiExpression> body = Stream.of(((PsiMethodCallExpression) element).getArgumentList().getExpressions());
            bodyStr = body.map(PsiElement::getText).collect(Collectors.joining(", "));
            PsiElement qualifier = ((PsiMethodCallExpression) element).getMethodExpression().getQualifier();
            if(Objects.nonNull(qualifier)) {
                qualifierSir = qualifier.getText();
            }
        }
        sql.refName = refName;
        sql.bodyStr = bodyStr;
        sql.qualifierSir = qualifierSir;
        return sql;
    }

    private String getNext(final StringBuilder code) {
        Pattern pattern = Pattern.compile(METHOD);
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            int end = matcher.end();
            if(code.charAt(end - 1) == '('){
                end = depFind(code, end);
            }
            String group = code.substring(matcher.start(), end);
            code.delete(0, end);
            return group;
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
