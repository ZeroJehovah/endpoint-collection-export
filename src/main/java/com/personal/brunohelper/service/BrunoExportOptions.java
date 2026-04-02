package com.personal.brunohelper.service;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class BrunoExportOptions {

    private BrunoExportOptions() {
    }

    public static Path resolveOutputDirectory(@Nullable String projectBasePath, @Nullable String configuredDirectory) {
        if (configuredDirectory == null || configuredDirectory.isBlank()) {
            return projectBasePath == null || projectBasePath.isBlank()
                    ? Paths.get("bruno")
                    : Paths.get(projectBasePath, "bruno");
        }
        Path outputDirectory = Paths.get(configuredDirectory);
        if (outputDirectory.isAbsolute()) {
            return outputDirectory;
        }
        return projectBasePath == null || projectBasePath.isBlank()
                ? outputDirectory
                : Paths.get(projectBasePath).resolve(outputDirectory).normalize();
    }

    public static String resolveBruCliPath(@Nullable String projectBasePath, @Nullable String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank() || "bru".equals(configuredPath)) {
            return "bru";
        }
        Path cliPath = Paths.get(configuredPath);
        if (cliPath.isAbsolute()) {
            return cliPath.toString();
        }
        return projectBasePath == null || projectBasePath.isBlank()
                ? cliPath.toString()
                : Paths.get(projectBasePath).resolve(cliPath).normalize().toString();
    }

    public static String deriveCollectionName(String controllerName) {
        String normalized = controllerName.endsWith("Controller")
                ? controllerName.substring(0, controllerName.length() - "Controller".length())
                : controllerName;
        return normalized.isBlank() ? "BrunoExport" : normalized;
    }
}
