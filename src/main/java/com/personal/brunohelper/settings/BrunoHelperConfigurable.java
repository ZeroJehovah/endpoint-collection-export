package com.personal.brunohelper.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.personal.brunohelper.i18n.BrunoHelperBundle;
import com.personal.brunohelper.service.BrunoExportOptions;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public final class BrunoHelperConfigurable implements Configurable {

    private JPanel panel;
    private TextFieldWithBrowseButton outputDirectoryField;

    @Override
    public @Nls String getDisplayName() {
        return BrunoHelperBundle.message("settings.display.name");
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            outputDirectoryField = new TextFieldWithBrowseButton();
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    .withTitle(BrunoHelperBundle.message("settings.output.directory.chooser.title"))
                    .withDescription(BrunoHelperBundle.message("settings.output.directory.chooser.description"));
            outputDirectoryField.addBrowseFolderListener(new TextBrowseFolderListener(descriptor));

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(0, 0, 8, 8);
            constraints.anchor = GridBagConstraints.WEST;
            panel.add(new JLabel(BrunoHelperBundle.message("settings.output.directory.label")), constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            panel.add(outputDirectoryField, constraints);

            constraints.gridx = 1;
            constraints.gridy = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(0, 0, 0, 0);
            panel.add(new JLabel(BrunoHelperBundle.message("settings.output.directory.help")), constraints);
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
    public void apply() throws ConfigurationException {
        String outputDirectory = outputDirectoryField.getText().trim();
        String validationError = BrunoExportOptions.validateBaseOutputDirectory(outputDirectory, false);
        if (validationError != null) {
            throw new ConfigurationException(validationError);
        }
        BrunoHelperSettingsState settings = BrunoHelperSettingsState.getInstance();
        settings.setCollectionOutputDirectory(outputDirectory);
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
