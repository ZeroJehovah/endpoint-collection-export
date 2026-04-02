package com.personal.brunohelper.service;

import com.intellij.psi.PsiClass;
import com.personal.brunohelper.model.ExportOutcome;

public final class StubControllerExportService implements ControllerExportService {

    @Override
    public ExportOutcome export(PsiClass controllerClass) {
        return ExportOutcome.failure("导出链路尚未完成，下一阶段会接入 Spring 解析和 Bruno CLI。");
    }
}
