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

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.util.Optional.*;

public class AiCoder extends AnAction {

    private static final String EB = "Eventbank";

    public AiCoder() {
        super(EB);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        final Project project = actionEvent.getProject();
        if(Objects.isNull(project)) return;
        final ModuleManager moduleManager = ModuleManager.getInstance(project);
        final JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        final PsiElementFactory javaFactory = javaPsiFacade.getElementFactory();
        final JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(project);

        Stream.of(moduleManager.getModules()).forEach(module -> {
            PsiClass f = getF(module).orElse(null);
            if(Objects.isNull(f)) return;

            PsiClass option = getPlayOption(module).orElse(null);
            if (Objects.isNull(option)) return;

            PsiClass optional = javaPsiFacade.findClass("java.util.Optional", module.getModuleWithLibrariesScope());
            if (Objects.isNull(optional)) return;

            final PsiMethod optionSome = getOptionSome(option);
            final PsiMethod optionNone = getOptionNone(option);
            final PsiMethod optionGetOrElse = getOptionGetOrElse(option);
            final PsiMethod fSome = getFSome(f);
            final PsiMethod fNone = getFNone(f);

            CommandProcessor.getInstance().executeCommand(project, () -> ApplicationManager.getApplication()
                    .runWriteAction(() -> {
                        Set<PsiFile> changedFile = new HashSet<>();
                        Query<PsiReference> search;

                        search = ReferencesSearch.search(optionGetOrElse, module.getModuleScope());
                        replaceGetOrElse2OrElse(search, changedFile, javaFactory, codeStyleManager);

                        search = ReferencesSearch.search(option, module.getModuleScope());
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
                                if(replaced.get().getParent().getParent() instanceof PsiLocalVariable ||
                                        replaced.get().getParent().getParent() instanceof PsiParameter){

                                    PsiVariable variable = (PsiVariable) replaced.get().getParent().getParent();
                                    Query<PsiReference> variableSearch = ReferencesSearch.search(variable, variable.getResolveScope());
                                    variableSearch.forEach(m ->{
                                        if(!(m instanceof PsiReferenceExpression)) return;
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
                                            } else if(variableCall.getMethodExpression().getLastChild().textMatches("getOrElse")){
                                                final PsiMethodCallExpression orElseCall =
                                                        (PsiMethodCallExpression) javaFactory.createExpressionFromText(
                                                                "arg.orElse(arg)",
                                                                null);
                                                orElseCall.getMethodExpression().getQualifierExpression()
                                                        .replace(variableCall.getMethodExpression().getQualifierExpression());
                                                orElseCall.getArgumentList().replace(variableCall.getArgumentList());
                                                replaced.set(variableCall.replace(orElseCall));
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



                        search = ReferencesSearch.search(optionNone, module.getModuleScope());
                        replaceNone2Empty(search, changedFile, javaFactory, codeStyleManager);

                        search = ReferencesSearch.search(optionSome, module.getModuleScope());
                        replaceSome2ofnullable(search, changedFile, javaFactory, codeStyleManager);

                        search = ReferencesSearch.search(fSome, module.getModuleScope());
                        replaceSome2ofnullable(search, changedFile, javaFactory, codeStyleManager);

                        search = ReferencesSearch.search(fNone, module.getModuleScope());
                        replaceNone2Empty(search, changedFile, javaFactory, codeStyleManager);

                        changedFile.forEach(codeStyleManager::optimizeImports);
                    }), "Option2Optional", EB);

        });

    }

    private void replaceSome2ofnullable(Query<PsiReference> search, Set<PsiFile> changedFile, PsiElementFactory javaFactory
            , JavaCodeStyleManager codeStyleManager) {
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
    }

    private void replaceGetOrElse2OrElse(Query<PsiReference> search, Set<PsiFile> changedFile, PsiElementFactory javaFactory
            , JavaCodeStyleManager codeStyleManager){

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
                if (originCall.getMethodExpression().getLastChild().textMatches("getOrElse")) {

                    final PsiMethodCallExpression orElseCall =
                            (PsiMethodCallExpression) javaFactory.createExpressionFromText(
                                    "arg.orElse(arg)",
                                    null);
                    orElseCall.getMethodExpression().getQualifierExpression()
                            .replace(originCall.getMethodExpression().getQualifierExpression());
                    orElseCall.getArgumentList().replace(originCall.getArgumentList());
                    replaced.set(originCall.replace(orElseCall));

                }
                if(Objects.nonNull(replaced.get())){
                    codeStyleManager.shortenClassReferences(replaced.get());
                }
            }
        });
    }

    private void replaceNone2Empty(Query<PsiReference> search, Set<PsiFile> changedFile, PsiElementFactory javaFactory
            , JavaCodeStyleManager codeStyleManager){

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
    }

    private Optional<PsiClass> getPlayOption(Module module) {
        Optional<PsiClass> f = getF(module);
        if (f.isPresent()) {
            PsiClass[] innerClazz = f.get().getInnerClasses();
            for (PsiClass inner : innerClazz) {
                if (Objects.equals(inner.getQualifiedName(), "play.libs.F.Option")) {
                    return of(inner);
                }
            }
        }
        return empty();
    }

    private PsiMethod getOptionNone(PsiClass option){
        return option.findMethodsByName("None", false)[0];
    }

    private PsiMethod getOptionSome(PsiClass option){
        return option.findMethodsByName("Some", false)[0];
    }

    private PsiMethod getOptionGetOrElse(PsiClass option){
        return option.findMethodsByName("getOrElse", false)[0];
    }

    private PsiMethod getFSome(PsiClass f) {
        return f.findMethodsByName("Some", false)[0];
    }

    private PsiMethod getFNone(PsiClass f) {
        return f.findMethodsByName("None", false)[0];
    }

    private Optional<PsiClass> getF(Module module) {
        GlobalSearchScope searchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
        Option<PsiClass> f = ScalaPsiManager.instance(module.getProject()).getCachedClass(searchScope, "play.libs.F");
        if (f.isEmpty()) {
            return empty();
        }else{
            return ofNullable(f.get());
        }
    }

    private void showErrorMsg(Object msg) {
        Messages.showErrorDialog(Objects.toString(msg), EB);
    }
}
