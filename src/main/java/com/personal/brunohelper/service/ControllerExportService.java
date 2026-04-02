package com.personal.brunohelper.service;

import com.intellij.psi.PsiClass;
import com.personal.brunohelper.model.ExportOutcome;

public interface ControllerExportService {

    ExportOutcome export(PsiClass controllerClass);
}
