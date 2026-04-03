package com.personal.brunohelper.service;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.personal.brunohelper.model.ControllerExportModel;
import com.personal.brunohelper.model.ExportOutcome;
import com.personal.brunohelper.parser.SpringControllerParser;
import com.personal.brunohelper.settings.BrunoHelperSettingsState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BrunoControllerExportService implements ControllerExportService {

    private final Project project;
    private final SpringControllerParser parser = new SpringControllerParser();
    private final BrunoCollectionWriter collectionWriter = new BrunoCollectionWriter();

    public BrunoControllerExportService(Project project) {
        this.project = project;
    }

    @Override
    public ExportOutcome export(SmartPsiElementPointer<PsiClass> controllerPointer) {
        ControllerExportModel exportModel = ReadAction.compute(() -> buildModel(controllerPointer));
        if (exportModel == null) {
            return ExportOutcome.failure("当前 controller 已失效，无法继续导出。");
        }
        if (exportModel.getEndpoints().isEmpty()) {
            return ExportOutcome.failure("未在当前 controller 中识别到 Spring MVC 接口。");
        }

        BrunoHelperSettingsState settings = BrunoHelperSettingsState.getInstance();
        Path collectionDirectory;
        try {
            collectionDirectory = resolveCollectionDirectory(settings, exportModel.getControllerName());
            Files.createDirectories(collectionDirectory.getParent());
        } catch (IOException exception) {
            return ExportOutcome.failure("创建 Bruno 输出目录失败: " + exception.getMessage());
        }

        BrunoCollectionWriter.PreparedCollection preparedCollection = ReadAction.compute(() ->
                exportModel == null ? null : collectionWriter.prepareCollection(exportModel, collectionDirectory)
        );
        if (preparedCollection == null) {
            return ExportOutcome.failure("当前 controller 已失效，无法继续导出。");
        }

        try {
            BrunoCollectionWriter.GenerationResult result = collectionWriter.writePreparedCollection(preparedCollection);
            return ExportOutcome.success("已生成 Bruno Collection `" + result.collectionName()
                    + "`，目录: " + result.collectionDirectory());
        } catch (IOException exception) {
            return ExportOutcome.failure("生成 Bruno Collection 文件失败: " + exception.getMessage());
        }
    }

    public SmartPsiElementPointer<PsiClass> createPointer(PsiClass controllerClass) {
        return SmartPointerManager.getInstance(project).createSmartPsiElementPointer(controllerClass);
    }

    private ControllerExportModel buildModel(SmartPsiElementPointer<PsiClass> controllerPointer) {
        PsiClass controllerClass = controllerPointer.getElement();
        if (controllerClass == null || !controllerClass.isValid()) {
            return null;
        }
        return parser.parse(controllerClass);
    }

    private Path resolveCollectionDirectory(BrunoHelperSettingsState settings, String controllerName) {
        Path baseOutputDirectory = BrunoExportOptions.resolveBaseOutputDirectory(settings.getCollectionOutputDirectory());
        return BrunoExportOptions.resolveCollectionDirectory(baseOutputDirectory, project.getName(), controllerName);
    }
}
