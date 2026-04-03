package com.personal.brunohelper.service;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.SmartPsiElementPointer;
import com.personal.brunohelper.model.ExportOutcome;
import org.jetbrains.annotations.Nullable;

public interface ControllerExportService {

    ExportOutcome export(
            SmartPsiElementPointer<PsiClass> controllerPointer,
            @Nullable SmartPsiElementPointer<PsiMethod> methodPointer
    );
}
