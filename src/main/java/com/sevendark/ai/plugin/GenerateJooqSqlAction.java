package com.sevendark.ai.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class GenerateJooqSqlAction extends AnAction {
    private static final String Name = "Generate Jooq Sql";

    public GenerateJooqSqlAction() {
        super(Name);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        System.out.println(e.getProject());
    }
}
