package com.personal.brunohelper.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public final class BrunoHelperConfigurable implements Configurable {

    private JPanel panel;
    private JTextField bruCliPathField;
    private JTextField outputDirectoryField;
    private JCheckBox keepTemporaryFileCheckBox;

    @Override
    public @Nls String getDisplayName() {
        return "Bruno Helper";
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            bruCliPathField = new JTextField();
            outputDirectoryField = new JTextField();
            keepTemporaryFileCheckBox = new JCheckBox("失败时保留临时 OpenAPI 文件");

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(0, 0, 8, 8);
            constraints.anchor = GridBagConstraints.WEST;
            panel.add(new JLabel("Bru CLI 路径"), constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            panel.add(bruCliPathField, constraints);

            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            panel.add(new JLabel("Bruno Collection 输出目录"), constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            panel.add(outputDirectoryField, constraints);

            constraints.gridx = 1;
            constraints.gridy = 2;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(4, 0, 0, 0);
            panel.add(keepTemporaryFileCheckBox, constraints);
        }

        reset();
        return panel;
    }

    @Override
    public boolean isModified() {
        BrunoHelperSettingsState settings = BrunoHelperSettingsState.getInstance();
        return !bruCliPathField.getText().trim().equals(settings.getBruCliPath())
                || !outputDirectoryField.getText().trim().equals(settings.getCollectionOutputDirectory())
                || keepTemporaryFileCheckBox.isSelected() != settings.isKeepTemporaryOpenApiFile();
    }

    @Override
    public void apply() {
        BrunoHelperSettingsState settings = BrunoHelperSettingsState.getInstance();
        settings.setBruCliPath(bruCliPathField.getText());
        settings.setCollectionOutputDirectory(outputDirectoryField.getText());
        settings.setKeepTemporaryOpenApiFile(keepTemporaryFileCheckBox.isSelected());
    }

    @Override
    public void reset() {
        BrunoHelperSettingsState settings = BrunoHelperSettingsState.getInstance();
        if (bruCliPathField != null) {
            bruCliPathField.setText(settings.getBruCliPath());
        }
        if (outputDirectoryField != null) {
            outputDirectoryField.setText(settings.getCollectionOutputDirectory());
        }
        if (keepTemporaryFileCheckBox != null) {
            keepTemporaryFileCheckBox.setSelected(settings.isKeepTemporaryOpenApiFile());
        }
    }

    @Override
    public void disposeUIResources() {
        panel = null;
        bruCliPathField = null;
        outputDirectoryField = null;
        keepTemporaryFileCheckBox = null;
    }
}
