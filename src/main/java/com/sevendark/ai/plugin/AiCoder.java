package com.sevendark.ai.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.module.Module;
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

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class AiCoder extends AnAction {

    private static final String EB = "Eventbank";

    public AiCoder() {
        super(EB);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Module eb_rest = actionEvent.getRequiredData(LangDataKeys.MODULE);
        Project project = actionEvent.getProject();
        //Module eb_rest = getEbRest(project).orElse(null);
        PsiClass option = getPlayOption(eb_rest).orElse(null);
        if (Objects.isNull(project) || Objects.isNull(option)) {
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
                    Set<PsiFile> changedFile = new HashSet<>();
                    Query<PsiReference> search;
                    search = ReferencesSearch.search(option, eb_rest.getModuleScope());
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

                            if (originCall.getMethodExpression().getLastChild().textMatches("Some")) {

                                final PsiMethodCallExpression ofNullableCall =
                                        (PsiMethodCallExpression) javaFactory.createExpressionFromText(
                                                "java.util.Optional.ofNullable(arg)",
                                                null);
                                ofNullableCall.getArgumentList().replace(originCall.getArgumentList());
                                replaced.set(originCall.replace(ofNullableCall));

                            } else if (originCall.getMethodExpression().getLastChild().textMatches("None")) {

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
                                    }else if (variableRef.getParent() instanceof PsiForeachStatement){
                                        PsiForeachStatement foreachStatement = (PsiForeachStatement) variableRef.getParent();
                                        PsiBlockStatement foreachBlock = (PsiBlockStatement) foreachStatement.getLastChild();
                                        Query<PsiReference> iteraVarSearch = ReferencesSearch.search(foreachStatement.getIterationParameter(), foreachBlock.getResolveScope());
                                        iteraVarSearch.forEach(iv ->{
                                            PsiReferenceExpression iteraVarInBlock = (PsiReferenceExpression) iv;
                                            final PsiMethodCallExpression getCall =
                                                    (PsiMethodCallExpression) javaFactory.createExpressionFromText(
                                                            "arg.get()",
                                                            null);
                                            getCall.getMethodExpression().getQualifierExpression().replace(variableRef);

                                            iteraVarInBlock.replace(getCall);
                                        });
                                        final PsiMethodCallExpression isPresentCall =
                                                (PsiMethodCallExpression) javaFactory.createExpressionFromText(
                                                        "arg.isPresent()",
                                                        null);
                                        isPresentCall.getMethodExpression().getQualifierExpression().replace(variableRef);
                                        PsiIfStatement ifStatement = (PsiIfStatement)
                                                javaFactory.createStatementFromText(
                                                        "if ( arg ) {}", null);
                                        ifStatement.getCondition().replace(isPresentCall);
                                        ifStatement.getThenBranch().replace(foreachBlock);
                                        variableRef.getParent().replace(ifStatement);
                                    }
                                });
                            }
                        }
                        if(Objects.nonNull(replaced.get())){
                            codeStyleManager.shortenClassReferences(replaced.get());
                        }
                    });
                    search = ReferencesSearch.search(option.findMethodsByName("None", false)[0], eb_rest.getModuleScope());
                    search.forEach(e -> {
                        PsiJavaCodeReferenceElement javaCode;
                        if (e instanceof PsiJavaCodeReferenceElement) {
                            javaCode = (PsiJavaCodeReferenceElement) e;
                        } else {
                            return;
                        }
                        changedFile.add(javaCode.getContainingFile());
                        if (javaCode.getParent() instanceof PsiMethodCallExpression) {

                            PsiMethodCallExpression originCall = (PsiMethodCallExpression) javaCode.getParent();
                            AtomicReference<PsiElement> replaced = new AtomicReference<>();
                            if (originCall.getMethodExpression().getLastChild().textMatches("None")) {

                                final PsiMethodCallExpression emptyCall =
                                        (PsiMethodCallExpression) javaFactory.createExpressionFromText(
                                                "java.util.Optional.empty()",
                                                null);
                                replaced.set(originCall.replace(emptyCall));

                            }
                            if(Objects.nonNull(replaced.get())){
                                codeStyleManager.shortenClassReferences(replaced.get());
                            }
                        }
                    });
                    search = ReferencesSearch.search(option.findMethodsByName("Some", false)[0], eb_rest.getModuleScope());
                    search.forEach(e -> {
                        PsiJavaCodeReferenceElement javaCode;
                        if (e instanceof PsiJavaCodeReferenceElement) {
                            javaCode = (PsiJavaCodeReferenceElement) e;
                        } else {
                            return;
                        }
                        changedFile.add(javaCode.getContainingFile());
                        if (javaCode.getParent() instanceof PsiMethodCallExpression) {

                            PsiMethodCallExpression originCall = (PsiMethodCallExpression) javaCode.getParent();
                            AtomicReference<PsiElement> replaced = new AtomicReference<>();
                            if (originCall.getMethodExpression().getLastChild().textMatches("Some")) {

                                final PsiMethodCallExpression ofNullableCall =
                                        (PsiMethodCallExpression) javaFactory.createExpressionFromText(
                                                "java.util.Optional.ofNullable(arg)",
                                                null);
                                ofNullableCall.getArgumentList().replace(originCall.getArgumentList());
                                replaced.set(originCall.replace(ofNullableCall));

                            }
                            if(Objects.nonNull(replaced.get())){
                                codeStyleManager.shortenClassReferences(replaced.get());
                            }
                        }
                    });
                    changedFile.forEach(codeStyleManager::optimizeImports);
                }), "Option2Optional", EB);
    }

    private Optional<PsiClass> getPlayOption(Module module) {
        GlobalSearchScope searchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
        Option<PsiClass> f = ScalaPsiManager.instance(module.getProject()).getCachedClass(searchScope, "play.libs.F");
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

    private void showErrorMsg(Object msg) {
        Messages.showErrorDialog(Objects.toString(msg), EB);
    }
}
