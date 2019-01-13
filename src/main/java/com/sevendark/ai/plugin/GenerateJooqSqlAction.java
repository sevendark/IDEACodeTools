package com.sevendark.ai.plugin;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.sevendark.ai.lib.SQLRule;
import com.sevendark.ai.lib.SQLUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sevendark.ai.lib.Constant.METHOD;

public class GenerateJooqSqlAction extends AnAction {
    private static final String Name = "Generate Jooq Sql";

    public GenerateJooqSqlAction() {
        super(Name);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        final SQLUtil dialect = SQLUtil.MYSQL;
        final CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();
        final Project project = event.getProject();
        if (Objects.isNull(project)) return;
        final Language language = event.getData(DataKeys.LANGUAGE);
        if (Objects.isNull(language)) return;
        final Caret caret = event.getData(DataKeys.CARET);
        if (Objects.isNull(caret)) return;
        if (!caret.hasSelection()) return;
        if (StringUtils.isBlank((caret.getSelectedText()))) return;

        final StringBuilder selectedText = new StringBuilder(caret.getSelectedText().chars()
                .boxed().map(e -> Character.toString((char)e.intValue()))
                .filter(e ->  e.matches("[^\\s]+"))
                .collect(Collectors.joining())
        );

        StringBuilder sql = new StringBuilder();
        if (Objects.equals("JAVA", language.getID())) {
            final JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            final PsiElementFactory elementFactory = javaPsiFacade.getElementFactory();
            appendSQL(selectedText, sql, dialect, elementFactory);
            copyPasteManager.setContents(new StringSelection(sql.toString()));
            System.out.println(sql.toString());
        }
    }

    private boolean appendSQL(final StringBuilder selectedText, final StringBuilder sql, final SQLUtil dialect, final PsiElementFactory elementFactory) {
        String code = getNext(selectedText, dialect);
        if(StringUtils.isBlank(code)) return false;

        final PsiStatement statementFromText = elementFactory.createStatementFromText(code, null);
        PsiElement element = statementFromText.getFirstChild();
        if (!(element instanceof PsiMethodCallExpression)) return false;

        String refName = ((PsiMethodCallExpression) element).getMethodExpression().getReferenceName();
        if(!dialect.replaceMap.containsKey(refName)) return false;
        Stream<PsiExpression> body = Stream.of(((PsiMethodCallExpression) element).getArgumentList().getExpressions());
        String bodyStr = body.map(PsiElement::getText).collect(Collectors.joining(", "));
        SQLRule rule = dialect.replaceMap.get(refName);
        if(rule.needQualifier){
            PsiElement qualifier = ((PsiMethodCallExpression) element).getMethodExpression().getQualifier();
            if(Objects.nonNull(qualifier)){
                if(!appendSQL(new StringBuilder(qualifier.getText()), sql, dialect, elementFactory)){
                    sql.append(qualifier.getText());
                }
            }
        }
        sql.append(" ");
        sql.append(rule.getFinalSQLName(refName));
        sql.append(" ");
        if(rule.needParen) sql.append("(");
        if(StringUtils.isBlank(bodyStr)){
            sql.append(rule.placeholder);
        }else{
            if(!appendSQL(new StringBuilder(bodyStr), sql, dialect, elementFactory)){
                sql.append(bodyStr);
            }
        }
        if(rule.needParen) sql.append(")");
        sql.append(" ");
        appendSQL(selectedText, sql, dialect, elementFactory);
        return true;
    }

    private String getNext(final StringBuilder code, final SQLUtil dialect) {
        Pattern pattern = Pattern.compile(METHOD);
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            int end = depFind(code, matcher.end());
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
}
