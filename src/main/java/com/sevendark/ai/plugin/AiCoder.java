package com.sevendark.ai.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.PsiSearchHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AiCoder extends AnAction {

    public AiCoder() {
        super("Eventbank");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if(Objects.isNull(project)){
            Messages.showMessageDialog(project, "Please select project", "Eventbank", Messages.getInformationIcon());
            return;
        }
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module eb_rest = moduleManager.findModuleByName("eb-rest");
        if(Objects.isNull(eb_rest)){
            Messages.showMessageDialog(project, "Can not find eb-rest module", "Eventbank", Messages.getInformationIcon());
            return;
        }
        PsiSearchHelper searchHelper = PsiSearchHelper.getInstance(project);
        PsiFile[] a = searchHelper.findFilesWithPlainTextWords("import play.libs.F.Option;");

        Messages.showMessageDialog(project, Objects.toString(a.length), "Eventbank", Messages.getInformationIcon());
        /*Query<PsiReference> a = ReferencesSearch.search();*/
    }
}
