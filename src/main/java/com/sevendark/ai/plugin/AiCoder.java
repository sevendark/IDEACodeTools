package com.sevendark.ai.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiManager;
import scala.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
        Module eb_rest = getEbRest(project).orElse(null);
        PsiClass option = getPlayOption(project).orElse(null);
        if (Objects.isNull(project) || Objects.isNull(option) || Objects.isNull(eb_rest)) {
            return;
        }
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        PsiElementFactory javaFactory = javaPsiFacade.getElementFactory();
        PsiClass optional = javaPsiFacade.findClass("java.util.Optional", eb_rest.getModuleWithLibrariesScope());
        if (Objects.isNull(optional)) {
            showErrorMsg("Can't find java.util.Optional");
            return;
        }
        final JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(project);

        CommandProcessor.getInstance().executeCommand(project, () -> ApplicationManager.getApplication()
                .runWriteAction(() -> {
                    Query<PsiReference> search = ReferencesSearch.search(option, eb_rest.getModuleScope());
                    List<PsiFile> changedFile = new ArrayList<>();
                    search.forEach(e -> {
                        PsiJavaCodeReferenceElement javaCode;
                        if (e instanceof PsiJavaCodeReferenceElement) {
                            javaCode = (PsiJavaCodeReferenceElement) e;
                        } else {
                            return;
                        }
                        AtomicReference<PsiElement> replaced = new AtomicReference<>();
                        changedFile.add(javaCode.getContainingFile());
                        if (javaCode.getParent() instanceof PsiReferenceExpression) {

                            PsiMethodCallExpression originCall = (PsiMethodCallExpression) javaCode.getParent().getParent();

                            if (Objects.equals(originCall.getMethodExpression().getQualifiedName(),
                                    "Option.Some")) {

                                final PsiMethodCallExpression ofNullableCall =
                                        (PsiMethodCallExpression) javaFactory.createExpressionFromText(
                                                "java.util.Optional.ofNullable(arg)",
                                                null);
                                ofNullableCall.getArgumentList().replace(originCall.getArgumentList());
                                replaced.set(originCall.replace(ofNullableCall));

                            } else if (Objects.equals(originCall.getMethodExpression().getQualifiedName(),
                                    "Option.None")) {

                                final PsiMethodCallExpression emptyCall =
                                        (PsiMethodCallExpression) javaFactory.createExpressionFromText(
                                                "java.util.Optional.empty()",
                                                null);
                                replaced.set(originCall.replace(emptyCall));

                            }

                        } else if (javaCode.getParent() instanceof PsiTypeElement) {
                            PsiJavaCodeReferenceElement optionalRef = javaFactory.createReferenceElementByType(
                                    javaFactory.createType(optional, javaCode.getTypeParameters()));
                            replaced.set(javaCode.replace(optionalRef));
                            if(Objects.nonNull(replaced.get())){
                                codeStyleManager.shortenClassReferences(replaced.get());
                            }
                            if(replaced.get().getParent().getParent() instanceof PsiLocalVariable){
                                PsiLocalVariable variable = (PsiLocalVariable) replaced.get().getParent().getParent();
                                Query<PsiReference> variableSearch = ReferencesSearch.search(variable, variable.getResolveScope());
                                variableSearch.forEach(m ->{
                                    PsiReferenceExpression variableRef = ((PsiReferenceExpression) m);
                                    if(variableRef.getParent().getParent() instanceof PsiMethodCallExpression){
                                        PsiMethodCallExpression variableCall = (PsiMethodCallExpression) variableRef.getParent().getParent();
                                        if(variableCall.getMethodExpression().getLastChild().textMatches("isDefined")){
                                            final PsiMethodCallExpression isPresentCall =
                                                    (PsiMethodCallExpression) javaFactory.createExpressionFromText(
                                                            "arg.isPresent()",
                                                            null);
                                            isPresentCall.getMethodExpression().getQualifierExpression()
                                                    .replace(variableCall.getMethodExpression().getQualifierExpression());
                                            replaced.set(variableCall.replace(isPresentCall));
                                        }else if(variableCall.getMethodExpression().getLastChild().textMatches("isEmpty")){
                                            final PsiPrefixExpression isNotPresent =
                                                    (PsiPrefixExpression) javaFactory.createExpressionFromText(
                                                            "!arg.isPresent()",
                                                            null);
                                            PsiMethodCallExpression isNotPresentCall = (PsiMethodCallExpression) isNotPresent.getLastChild();
                                            isNotPresentCall.getMethodExpression().getQualifierExpression()
                                                    .replace(variableCall.getMethodExpression().getQualifierExpression());
                                            replaced.set(variableCall.replace(isNotPresent));
                                        }
                                    }
                                });
                            }
                        }
                        if(Objects.nonNull(replaced.get())){
                            codeStyleManager.shortenClassReferences(replaced.get());
                        }
                    });
                    changedFile.forEach(codeStyleManager::optimizeImports);
                }), "Option2Optional", EB);
    }

    private Optional<Module> getEbRest(Project project) {
        if (Objects.isNull(project)) {
            showErrorMsg("Can't find project");
            return empty();
        }
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module eb_rest = moduleManager.findModuleByName(EB_REST_NAME);
        if (Objects.isNull(eb_rest)) {
            showErrorMsg("Can't find " + EB_REST_NAME);
        }
        return ofNullable(eb_rest);
    }

    private Optional<PsiClass> getPlayOption(Project project) {
        Module module = getEbRest(project).orElse(null);
        if (Objects.isNull(module)) {
            return empty();
        }
        GlobalSearchScope searchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
        Option<PsiClass> f = ScalaPsiManager.instance(project).getCachedClass(searchScope, "play.libs.F");
        if (f.isEmpty()) {
            showErrorMsg("Can't find play.libs.F");
        }
        PsiClass[] innerClazz = f.get().getInnerClasses();
        for (PsiClass inner : innerClazz) {
            if (Objects.equals(inner.getQualifiedName(), "play.libs.F.Option")) {
                return of(inner);
            }
        }
        showErrorMsg("Can't find play.libs.F$Option");
        return empty();
    }

    private void showMsg(Object msg) {
        Messages.showInfoMessage(Objects.toString(msg), EB);
    }

    private void showErrorMsg(Object msg) {
        Messages.showErrorDialog(Objects.toString(msg), EB);
    }
}
