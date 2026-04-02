package com.personal.brunohelper.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class SpringControllerUtil {

    private static final Set<String> CONTROLLER_ANNOTATIONS = Set.of(
            "org.springframework.web.bind.annotation.RestController",
            "org.springframework.stereotype.Controller"
    );

    private SpringControllerUtil() {
    }

    public static boolean isSpringController(@Nullable PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        for (PsiAnnotation annotation : psiClass.getModifierList() == null
                ? PsiAnnotation.EMPTY_ARRAY
                : psiClass.getModifierList().getAnnotations()) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName != null && CONTROLLER_ANNOTATIONS.contains(qualifiedName)) {
                return true;
            }
        }
        return false;
    }
}
