package com.personal.brunohelper.service;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.personal.brunohelper.model.ControllerExportModel;
import com.personal.brunohelper.model.ExportOutcome;
import com.personal.brunohelper.parser.SpringControllerParser;
import com.personal.brunohelper.settings.BrunoHelperSettingsState;
import org.jetbrains.annotations.Nullable;

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
    public ExportOutcome export(
            SmartPsiElementPointer<PsiClass> controllerPointer,
            @Nullable SmartPsiElementPointer<PsiMethod> methodPointer
    ) {
        ControllerExportModel exportModel = ReadAction.compute(() -> buildModel(controllerPointer, methodPointer));
        if (exportModel == null) {
            return ExportOutcome.failure("当前 controller 已失效，无法继续导出。");
        }
        if (exportModel.getEndpoints().isEmpty()) {
            return ExportOutcome.failure("未在当前 controller 中识别到 Spring MVC 接口。");
        }

        BrunoHelperSettingsState settings = BrunoHelperSettingsState.getInstance();
        Path projectDirectory;
        Path controllerDirectory;
        try {
            projectDirectory = resolveProjectDirectory(settings);
            controllerDirectory = BrunoExportOptions.resolveControllerDirectory(projectDirectory, exportModel.getControllerName());
            Files.createDirectories(projectDirectory);
        } catch (IOException exception) {
            return ExportOutcome.failure("创建 Bruno 输出目录失败: " + exception.getMessage());
        }

        BrunoCollectionWriter.PreparedCollection preparedCollection = ReadAction.compute(() ->
                exportModel == null ? null : collectionWriter.prepareCollection(exportModel, project.getName(), projectDirectory, controllerDirectory)
        );
        if (preparedCollection == null) {
            return ExportOutcome.failure("当前 controller 已失效，无法继续导出。");
        }

        try {
            BrunoCollectionWriter.GenerationResult result = collectionWriter.writePreparedCollection(preparedCollection);
            return ExportOutcome.success("已更新 Bruno 项目 `" + result.collectionName()
                    + "`，项目目录: " + result.projectDirectory()
                    + "，controller目录: " + result.controllerDirectory()
                    + "，新增 " + result.createdRequestCount() + " 个接口文件，跳过 " + result.skippedRequestCount() + " 个已存在文件。");
        } catch (IOException exception) {
            return ExportOutcome.failure("生成 Bruno Collection 文件失败: " + exception.getMessage());
        }
    }

    public SmartPsiElementPointer<PsiClass> createPointer(PsiClass controllerClass) {
        return SmartPointerManager.getInstance(project).createSmartPsiElementPointer(controllerClass);
    }

    public @Nullable SmartPsiElementPointer<PsiMethod> createPointer(@Nullable PsiMethod method) {
        if (method == null) {
            return null;
        }
        return SmartPointerManager.getInstance(project).createSmartPsiElementPointer(method);
    }

    private ControllerExportModel buildModel(
            SmartPsiElementPointer<PsiClass> controllerPointer,
            @Nullable SmartPsiElementPointer<PsiMethod> methodPointer
    ) {
        PsiClass controllerClass = controllerPointer.getElement();
        if (controllerClass == null || !controllerClass.isValid()) {
            return null;
        }
        PsiMethod method = methodPointer == null ? null : methodPointer.getElement();
        if (methodPointer != null && (method == null || !method.isValid())) {
            return null;
        }
        return parser.parse(controllerClass, method);
    }

    private Path resolveProjectDirectory(BrunoHelperSettingsState settings) {
        Path baseOutputDirectory = BrunoExportOptions.resolveBaseOutputDirectory(settings.getCollectionOutputDirectory());
        return BrunoExportOptions.resolveProjectDirectory(baseOutputDirectory, project.getName());
    }
}
