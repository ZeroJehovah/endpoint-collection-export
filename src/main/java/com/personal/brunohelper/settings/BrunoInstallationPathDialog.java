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

public final class BrunoInstallationPathDialog extends DialogWrapper {

    private final TextFieldWithBrowseButton installationPathField = new TextFieldWithBrowseButton();

    public BrunoInstallationPathDialog(@Nullable Project project, @Nullable String initialPath) {
        super(project);
        setTitle("配置 Bruno 安装路径");
        installationPathField.setText(initialPath == null ? "" : initialPath.trim());
        installationPathField.addBrowseFolderListener(
                "选择 Bruno 安装目录",
                "请选择包含 bru 可执行文件的目录，也可以手动输入 Bruno 可执行文件的绝对路径。",
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
        panel.add(new JLabel("Bruno 安装路径"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 8, 0);
        panel.add(installationPathField, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        panel.add(new JLabel("请选择包含 bru、bru.cmd、bru.exe 或 bru.bat 的目录。该配置为全局配置，对所有项目生效。"), constraints);
        return panel;
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        String error = BrunoExportOptions.validateBruCliPath(installationPathField.getText(), false);
        return error == null ? null : new ValidationInfo(error, installationPathField.getTextField());
    }

    public String getInstallationPath() {
        return installationPathField.getText().trim();
    }
}
