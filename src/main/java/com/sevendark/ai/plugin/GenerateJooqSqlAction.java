package com.sevendark.ai.plugin;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.sevendark.ai.plugin.lib.Constant;
import com.sevendark.ai.plugin.lib.sql.SQLMapper;
import com.sevendark.ai.plugin.lib.sql.SQLMapperBean;
import com.sevendark.ai.plugin.lib.sql.SQLReader;
import com.sevendark.ai.plugin.lib.sql.SQLStatement;
import org.apache.commons.lang.StringUtils;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GenerateJooqSqlAction extends AnAction {
    private static final String Name = "Generate Jooq Sql";

    public GenerateJooqSqlAction() {
        super(Name);
    }

    private Caret caret;
    private SQLMapper dialect = SQLMapper.MYSQL;
    private StringBuilder sqlResult;
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
            root.forEach(e -> appendSQL(e, root));
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
        root = SQLReader.readSQL(selectedText);
    }

    private boolean appendSQL(SQLStatement sqlStatementBean, List<SQLStatement> myList) {

        if(!dialect.replaceMap.containsKey(sqlStatementBean.refName.toString())) {
            sqlResult.append(replaceStr(sqlStatementBean.refName));
            return false;
        }

        SQLMapperBean rule = getRule(sqlStatementBean);

        if(getPreMapper(sqlStatementBean, myList).isStart && getPreStatement(sqlStatementBean, myList).body.size() == 0){
            sqlResult.append(rule.beStart);
        }

        if(rule.needQualifier){
            sqlResult.append(replaceStr(sqlStatementBean.qualifierSir));
        }
        sqlResult.append(" ");
        sqlResult.append(rule.getFinalSQLName(sqlStatementBean.refName));
        sqlResult.append(" ");

        if(sqlStatementBean.body.size() != 0){
            if(rule.needParen && sqlStatementBean.body.size() > 1) {
                sqlResult.append("(");
            }
            if(sqlStatementBean.body.size() == 0){
                sqlResult.append(rule.placeholder);
            }else{
                sqlStatementBean.body.forEach(e -> appendSQL(e, sqlStatementBean.body));
            }
            if(rule.needParen && sqlStatementBean.body.size() > 1) {
                sqlResult.append(")");
            }
            sqlResult.append(" ");
        }

        return true;
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
        return dialect.replaceMap.get(sqlStatementBean.refName.toString());
    }

    private String replaceStr(StringBuilder str){
        if(str.length() == 0){
            return "";
        }
        if(str.toString().matches(Constant.STR)){
            return str.toString();
        }
        return str.toString().replaceAll(Constant.VAR, "---");
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
