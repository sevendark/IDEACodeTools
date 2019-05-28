package com.sevendark.ai.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.Messages;
import com.sevendark.ai.plugin.lib.sql.parser.SqlParserVisitor;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

public class SqlToJooqAction extends AnAction {

    private static final String Name = "SQL to Jooq";

    public SqlToJooqAction() {
        super(Name);
    }

    private final CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        String src = copyPasteManager.getContents(DataFlavor.stringFlavor);
        if (src == null) return;
        String res = SqlParserVisitor.of().parse(src);
        if (res == null) {
            Messages.showErrorDialog("Invalid SQL", Name);
            return;
        }
        copyPasteManager.setContents(new StringSelection(res));
        Messages.showInfoMessage("Success!", Name);
    }
}
