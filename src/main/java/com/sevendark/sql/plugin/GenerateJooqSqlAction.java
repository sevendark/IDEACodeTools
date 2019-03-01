package com.sevendark.sql.plugin;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.sevendark.sql.lib.*;
import org.apache.commons.lang.StringUtils;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sevendark.sql.lib.Constant.STR;
import static com.sevendark.sql.lib.Constant.VAR;

public class GenerateJooqSqlAction extends AnAction {
    private static final String Name = "Generate Jooq Sql";

    public GenerateJooqSqlAction() {
        super(Name);
    }

    private Caret caret;
    private SQLMapper dialect = SQLMapper.MYSQL;
    private StringBuilder sqlResult;
    private SQLMapperBean last;
    private List<SQLStatement> root;
    private final CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        if(!validAndSet(event)) return;

        final StringBuilder selectedText = new StringBuilder(Objects.requireNonNull(caret.getSelectedText())
                .chars()
                .boxed().map(e -> Character.toString((char)e.intValue()))
                .filter(e ->  e.matches("[^\\s]+"))
                .collect(Collectors.joining())
        );

        try{
            ini(selectedText);
            root.forEach(this::appendSQL);
            if(sqlResult.length() != 0){
                copyPasteManager.setContents(new StringSelection(new BasicFormatterImpl().format(sqlResult.toString())));
            }
        }catch (Exception e){
            e.printStackTrace();
            Messages.showErrorDialog(e.getLocalizedMessage(), Name);
        }
    }

    private void ini(StringBuilder selectedText){
        sqlResult = new StringBuilder();
        last = null;
        root = SQLReader.readSQL(selectedText);
    }

    private boolean appendSQL(SQLStatement sqlStatementBean) {

        if(Objects.isNull(sqlStatementBean) || sqlStatementBean.refName.length() == 0){
            return false;
        }

        if(!dialect.replaceMap.containsKey(sqlStatementBean.refName.toString())) {
            return false;
        }

        SQLMapperBean rule = dialect.replaceMap.get(sqlStatementBean.refName.toString());

        if(Objects.nonNull(last) && last.start){
            sqlResult.append(rule.beStart);
        }

        if(rule.needQualifier){
            sqlResult.append(replaceVar(sqlStatementBean.qualifierSir));
        }
        sqlResult.append(" ");
        sqlResult.append(rule.getFinalSQLName(sqlStatementBean.refName));
        sqlResult.append(" ");

        if(sqlStatementBean.bodyStr.length() != 0 || rule.placeholder.length() != 0){
            if(rule.needParen) {
                sqlResult.append("(");
            }
            if(sqlStatementBean.bodyStr.length() == 0){
                sqlResult.append(rule.placeholder);
            }else if (!appendSQL(sqlStatementBean)){
                sqlResult.append(replaceVar(sqlStatementBean.bodyStr));
            }
            if(rule.needParen) {
                sqlResult.append(")");
            }
            sqlResult.append(" ");
        }

        last = rule;
        return true;
    }

    private String replaceVar(StringBuilder str){
        if(str.length() == 0){
            return "";
        }
        if(str.toString().matches(STR)){
            return str.toString();
        }
        return str.toString().replaceAll(VAR, "---");
    }

    private boolean validAndSet(AnActionEvent event){
        Project project = event.getProject();
        if (Objects.isNull(project)) {
            Messages.showInfoMessage("Please Select a piece of JOOQ code", Name);
            return false;
        }
        Language language = event.getData(DataKeys.LANGUAGE);
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
        sqlResult = new StringBuilder();
        return true;
    }
}
