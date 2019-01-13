package com.sevendark.ai.plugin;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.sevendark.ai.lib.SQLUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        final StringBuilder selectedText = new StringBuilder(caret.getSelectedText());

        StringBuilder sql = new StringBuilder();
        if (Objects.equals("JAVA", language.getID())) {
            final JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            final PsiElementFactory elementFactory = javaPsiFacade.getElementFactory();
            String code = getNext(selectedText, dialect);
            while (StringUtils.isNotBlank(code)) {
                final PsiStatement statementFromText = elementFactory.createStatementFromText(code, null);
                appendSQL(statementFromText.getFirstChild(), sql, dialect);
                code = getNext(selectedText, dialect);
            }

            copyPasteManager.setContents(new StringSelection(sql.toString()));
            System.out.println(sql.toString());
        }
    }

    private void appendSQL(final PsiElement element, final StringBuilder sql, final SQLUtil dialect) {
        if (!(element instanceof PsiMethodCallExpression)) return;

        String refName = ((PsiMethodCallExpression) element).getMethodExpression().getReferenceName();
        Stream<PsiExpression> body = Stream.of(((PsiMethodCallExpression) element).getArgumentList().getExpressions());
        String bodyStr = body.map(PsiElement::getText).collect(Collectors.joining(", "));
        sql.append(refName);
        sql.append("\n");
        if(dialect.replaceMap.get(refName).needParen) sql.append("(");
        sql.append(StringUtils.isBlank(bodyStr) ? dialect.replaceMap.get(refName).placeholder : bodyStr);
        if(dialect.replaceMap.get(refName).needParen) sql.append(")");
        sql.append("\n");
    }

    private String getNext(final StringBuilder code, final SQLUtil dialect) {
        Pattern pattern = Pattern.compile(dialect.pattern);
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            String group = matcher.group();
            code.delete(matcher.start(), matcher.end());
            return group;
        }
        return null;
    }
}
