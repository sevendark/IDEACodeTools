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
import com.sevendark.ai.plugin.lib.sql.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.Objects;
import java.util.stream.Collectors;

public class GenerateJooqSqlAction extends AnAction {
    private static final String Name = "Transform Jooq to Sql";

    public GenerateJooqSqlAction() {
        super(Name);
    }

    private Caret caret;
    private final CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        if(!validAndSet(event)) return;

        String result = JooqToSqlConverter.convert(caret.getSelectedText());
        if (result != null) {
            copyPasteManager.setContents(new StringSelection(result));
        } else {
            Messages.showInfoMessage("Convert error", Name);
        }
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
        return true;
    }
}
