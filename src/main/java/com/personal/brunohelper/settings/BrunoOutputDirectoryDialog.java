package com.personal.brunohelper.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.personal.brunohelper.service.BrunoExportOptions;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public final class BrunoOutputDirectoryDialog extends DialogWrapper {

    private final TextFieldWithBrowseButton outputDirectoryField = new TextFieldWithBrowseButton();

    public BrunoOutputDirectoryDialog(@Nullable Project project, @Nullable String initialValue) {
        super(project);
        setTitle("配置 Bruno 基础输出目录");
        outputDirectoryField.setText(initialValue == null ? "" : initialValue.trim());
        outputDirectoryField.addBrowseFolderListener(
                "选择 Bruno 基础输出目录",
                "该配置为全局配置，对所有项目生效。",
                project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 8, 8);
        panel.add(new JLabel("Bruno 基础输出目录"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 8, 0);
        panel.add(outputDirectoryField, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        panel.add(new JLabel("该配置为全局配置，对所有项目生效；导出目录结构为：基础目录 / 项目名 / controller名。"), constraints);
        return panel;
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        String error = BrunoExportOptions.validateBaseOutputDirectory(outputDirectoryField.getText(), false);
        return error == null ? null : new ValidationInfo(error, outputDirectoryField.getTextField());
    }

    public String getOutputDirectory() {
        return outputDirectoryField.getText().trim();
    }
}
