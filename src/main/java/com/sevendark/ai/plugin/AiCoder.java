package com.sevendark.ai.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiManager;
import scala.Option;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.*;

public class AiCoder extends AnAction {

    private static final String EB_REST_NAME = "untitled";
    private static final String EB = "Eventbank";

    public AiCoder() {
        super("Eventbank");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Project project = actionEvent.getProject();
        Module module = getEbRest(project).orElse(null);
        PsiClass option = getPlayOption(project).orElse(null);
        if(Objects.isNull(option) || Objects.isNull(module)){
            return;
        }
        Query<PsiReference> search = ReferencesSearch.search(option, module.getModuleScope());
        search.forEach(e -> {
            System.out.println(e.getCanonicalText()); //play.libs.F
            System.out.println(e.getElement()); // Code Reference Element : play.libs.F
            System.out.println(e.getRangeInElement()); // (10, 11)
            System.out.println(Arrays.toString(e.getVariants())); // [Scala, F, Akka, Json, XML, HttpExecution]
        });
    }

    private Optional<Module> getEbRest(Project project){
        if(Objects.isNull(project)){
            showErrorMsg("Can't find project");
            return empty();
        }
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module eb_rest = moduleManager.findModuleByName(EB_REST_NAME);
        if(Objects.isNull(eb_rest)){
            showErrorMsg("Can't find " + EB_REST_NAME);
        }
        return ofNullable(eb_rest);
    }

    private Optional<PsiClass> getPlayOption(Project project){
        Module module = getEbRest(project).orElse(null);
        if(Objects.isNull(module)){
            return empty();
        }
        GlobalSearchScope searchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
        Option<PsiClass> f = ScalaPsiManager.instance(project).getCachedClass(searchScope, "play.libs.F");
        if(f.isEmpty()){
            showErrorMsg("Can't find play.libs.F");
        }
        PsiClass[] innerClazz = f.get().getInnerClasses();
        for (PsiClass inner : innerClazz){
            if(Objects.equals(inner.getQualifiedName(), "play.libs.F.Option")){
                return of(inner);
            }
        }
        showErrorMsg("Can't find play.libs.F$Option");
        return empty();
    }

    private void showMsg(Object msg){
        Messages.showInfoMessage(Objects.toString(msg), EB);
    }

    private void showErrorMsg(Object msg){
        Messages.showErrorDialog(Objects.toString(msg), EB);
    }
}
