package com.personal.brunohelper.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public final class BrunoHelperConfigurable implements Configurable {

    private JPanel panel;
    private JTextField outputDirectoryField;

    @Override
    public @Nls String getDisplayName() {
        return "Bruno Helper";
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            outputDirectoryField = new JTextField();

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(0, 0, 8, 8);
            constraints.anchor = GridBagConstraints.WEST;
            panel.add(new JLabel("Bruno Collection 输出目录"), constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            panel.add(outputDirectoryField, constraints);

            constraints.gridx = 1;
            constraints.gridy = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(0, 0, 0, 0);
            panel.add(new JLabel("留空时默认使用当前项目根目录下的 bruno/，导出时会在该目录下生成 Collection 文件夹。"), constraints);
        }

        reset();
        return panel;
    }

    @Override
    public boolean isModified() {
        BrunoHelperSettingsState settings = BrunoHelperSettingsState.getInstance();
        return !outputDirectoryField.getText().trim().equals(settings.getCollectionOutputDirectory());
    }

    @Override
    public void apply() {
        BrunoHelperSettingsState settings = BrunoHelperSettingsState.getInstance();
        settings.setCollectionOutputDirectory(outputDirectoryField.getText());
    }

    @Override
    public void reset() {
        BrunoHelperSettingsState settings = BrunoHelperSettingsState.getInstance();
        if (outputDirectoryField != null) {
            outputDirectoryField.setText(settings.getCollectionOutputDirectory());
        }
    }

    @Override
    public void disposeUIResources() {
        panel = null;
        outputDirectoryField = null;
    }
}
