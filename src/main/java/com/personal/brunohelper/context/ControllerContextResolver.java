package com.personal.brunohelper.context;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.personal.brunohelper.parser.AnnotationUtils;
import com.personal.brunohelper.util.SpringControllerUtil;
import org.jetbrains.annotations.Nullable;

public final class ControllerContextResolver {

    private static final String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";
    private static final String GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping";
    private static final String POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping";
    private static final String PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping";
    private static final String DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping";
    private static final String PATCH_MAPPING = "org.springframework.web.bind.annotation.PatchMapping";

    private ControllerContextResolver() {
    }

    public static @Nullable ExportTarget resolveTarget(AnActionEvent event) {
        PsiElement element = event.getData(CommonDataKeys.PSI_ELEMENT);
        if (element == null && event.getData(CommonDataKeys.EDITOR) != null) {
            PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
            if (psiFile != null) {
                element = PsiUtilBase.getElementAtCaret(event.getData(CommonDataKeys.EDITOR));
            }
        }
        if (element == null) {
            return null;
        }

        PsiClass psiClass = element instanceof PsiClass ? (PsiClass) element : PsiTreeUtil.getParentOfType(element, PsiClass.class);
        if (!SpringControllerUtil.isSpringController(psiClass)) {
            return null;
        }

        PsiMethod psiMethod = resolveMethodTarget(element);
        if (psiMethod != null && !isRequestMappingMethod(psiMethod)) {
            return null;
        }
        return new ExportTarget(psiClass, psiMethod);
    }

    public static @Nullable PsiClass resolveController(AnActionEvent event) {
        ExportTarget target = resolveTarget(event);
        return target == null ? null : target.controllerClass();
    }

    private static @Nullable PsiMethod resolveMethodTarget(PsiElement element) {
        PsiMethod method = element instanceof PsiMethod ? (PsiMethod) element : PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (method == null) {
            return null;
        }
        PsiElement nameIdentifier = method.getNameIdentifier();
        if (nameIdentifier != null && PsiTreeUtil.isAncestor(nameIdentifier, element, false)) {
            return method;
        }
        return element == method ? method : null;
    }

    private static boolean isRequestMappingMethod(PsiMethod method) {
        return AnnotationUtils.findMethodAnnotationIncludingSupers(
                method,
                GET_MAPPING,
                POST_MAPPING,
                PUT_MAPPING,
                DELETE_MAPPING,
                PATCH_MAPPING,
                REQUEST_MAPPING
        ) != null;
    }

    public record ExportTarget(PsiClass controllerClass, @Nullable PsiMethod targetMethod) {
    }
}
