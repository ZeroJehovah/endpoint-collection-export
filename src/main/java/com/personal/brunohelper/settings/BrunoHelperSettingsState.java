package com.personal.brunohelper.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "BrunoHelperSettings", storages = @Storage("bruno-helper.xml"))
public final class BrunoHelperSettingsState implements PersistentStateComponent<BrunoHelperSettingsState.State> {

    private State state = new State();

    public static BrunoHelperSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(BrunoHelperSettingsState.class);
    }

    @Override
    public @Nullable State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public String getBruCliPath() {
        return state.bruCliPath;
    }

    public void setBruCliPath(String bruCliPath) {
        state.bruCliPath = bruCliPath == null ? "" : bruCliPath.trim();
    }

    public String getCollectionOutputDirectory() {
        return state.collectionOutputDirectory;
    }

    public void setCollectionOutputDirectory(String collectionOutputDirectory) {
        state.collectionOutputDirectory = collectionOutputDirectory == null ? "" : collectionOutputDirectory.trim();
    }

    public boolean isKeepTemporaryOpenApiFile() {
        return state.keepTemporaryOpenApiFile;
    }

    public void setKeepTemporaryOpenApiFile(boolean keepTemporaryOpenApiFile) {
        state.keepTemporaryOpenApiFile = keepTemporaryOpenApiFile;
    }

    public static final class State {
        public String bruCliPath = "bru";
        public String collectionOutputDirectory = "";
        public boolean keepTemporaryOpenApiFile = false;
    }
}
