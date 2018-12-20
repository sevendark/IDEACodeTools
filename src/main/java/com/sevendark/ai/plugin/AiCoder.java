package com.sevendark.ai.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiManager;
import scala.Option;

import java.util.Objects;

public class AiCoder extends AnAction {

    private static final String EB_REST_NAME = "untitled";

    public AiCoder() {
        super("Eventbank");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        ModuleManager moduleManager = ModuleManager.getInstance(Objects.requireNonNull(project));
        Module eb_rest = moduleManager.findModuleByName(EB_REST_NAME);
        if(Objects.isNull(eb_rest)){
            Messages.showMessageDialog(project, "Can not find eb-rest module", "Eventbank", Messages.getInformationIcon());
            return;
        }

        ScalaPsiManager scalaPsiManager = ScalaPsiManager.instance(project);
        Option<PsiClass> psiClass = scalaPsiManager.getCachedClass(eb_rest.getModuleScope(), "controllers.HomeController");
        if(psiClass.isDefined()){
            Query<PsiReference> a = ReferencesSearch.search(psiClass.get());
            Messages.showMessageDialog(project, Objects.toString(a.findFirst()), "Eventbank", Messages.getInformationIcon());
        }else{
            Messages.showMessageDialog(project, "Can not find play.libs.F.Option", "Eventbank", Messages.getInformationIcon());
        }

    }
}
