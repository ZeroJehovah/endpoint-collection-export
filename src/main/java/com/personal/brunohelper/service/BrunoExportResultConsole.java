package com.personal.brunohelper.service;

import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.ide.actions.RevealFileAction;
import com.intellij.openapi.project.Project;
import com.personal.brunohelper.i18n.BrunoHelperBundle;
import com.personal.brunohelper.model.ExportOutcome;
import com.personal.brunohelper.model.ExportReport;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public final class BrunoExportResultConsole {

    private final BrunoExportReportFormatter formatter = new BrunoExportReportFormatter();

    public void show(Project project, ExportOutcome outcome) {
        ExportReport report = outcome.getReport();
        if (report == null) {
            return;
        }

        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        consoleView.print(formatter.formatSummary(report), ConsoleViewContentType.NORMAL_OUTPUT);
        printDirectoryLine(consoleView, BrunoHelperBundle.message("export.report.directory.project"), report.projectDirectory());
        printDirectoryLine(consoleView, BrunoHelperBundle.message("export.report.directory.controller"), report.controllerDirectory());
        consoleView.print("\n", ConsoleViewContentType.NORMAL_OUTPUT);
        consoleView.print(formatter.formatTable(report), ConsoleViewContentType.NORMAL_OUTPUT);
        consoleView.requestScrollingToEnd();

        RunContentDescriptor descriptor = new RunContentDescriptor(
                consoleView,
                null,
                consoleView.getComponent(),
                BrunoHelperBundle.message("export.console.title", report.className()),
                null,
                null,
                consoleView.createConsoleActions()
        );
        descriptor.setActivateToolWindowWhenAdded(true);
        descriptor.setSelectContentWhenAdded(true);
        RunContentManager.getInstance(project)
                .showRunContent(DefaultRunExecutor.getRunExecutorInstance(), descriptor);
    }

    private void printDirectoryLine(ConsoleView consoleView, String label, @Nullable Path directory) {
        if (directory == null) {
            return;
        }
        consoleView.print(label + ": ", ConsoleViewContentType.NORMAL_OUTPUT);
        HyperlinkInfo hyperlinkInfo = ignored -> RevealFileAction.openDirectory(directory);
        consoleView.printHyperlink(directory.toString(), hyperlinkInfo);
        consoleView.print("\n", ConsoleViewContentType.NORMAL_OUTPUT);
    }
}
