package com.personal.brunohelper.service;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class BrunoExportOptions {

    private static final List<String> BRU_EXECUTABLE_NAMES = List.of("bru.cmd", "bru.exe", "bru.bat", "bru");

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

    public static @Nullable Path resolveBruCliPath(@Nullable String configuredPath) {
        String normalized = normalizeBruCliPath(configuredPath);
        if (normalized == null) {
            return null;
        }
        Path cliPath;
        try {
            cliPath = Paths.get(normalized).normalize();
        } catch (InvalidPathException exception) {
            return null;
        }
        if (Files.isDirectory(cliPath)) {
            return findExecutableInDirectory(cliPath);
        }
        return Files.isRegularFile(cliPath) ? cliPath : null;
    }

    public static boolean hasConfiguredBruCliPath(@Nullable String configuredPath) {
        return normalizeBruCliPath(configuredPath) != null;
    }

    public static @Nullable String validateBruCliPath(@Nullable String configuredPath, boolean allowBlank) {
        String normalized = normalizeBruCliPath(configuredPath);
        if (normalized == null) {
            return allowBlank ? null : "请输入 Bruno 安装路径。";
        }

        Path configured;
        try {
            configured = Paths.get(normalized).normalize();
        } catch (InvalidPathException exception) {
            return "Bruno 安装路径格式无效: " + exception.getMessage();
        }

        if (!configured.isAbsolute()) {
            return "Bruno 安装路径必须是本机绝对路径。";
        }
        if (!Files.exists(configured)) {
            return "Bruno 安装路径不存在: " + configured;
        }
        if (Files.isRegularFile(configured)) {
            return null;
        }
        if (!Files.isDirectory(configured)) {
            return "Bruno 安装路径必须是目录或可执行文件。";
        }
        return findExecutableInDirectory(configured) == null
                ? "所选目录中未找到 Bruno CLI，可执行文件名需为 bru、bru.cmd、bru.exe 或 bru.bat。"
                : null;
    }

    public static String deriveCollectionName(String controllerName) {
        String normalized = controllerName.endsWith("Controller")
                ? controllerName.substring(0, controllerName.length() - "Controller".length())
                : controllerName;
        return normalized.isBlank() ? "BrunoExport" : normalized;
    }

    private static @Nullable Path findExecutableInDirectory(Path directory) {
        for (String executableName : BRU_EXECUTABLE_NAMES) {
            Path candidate = directory.resolve(executableName);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static @Nullable String normalizeBruCliPath(@Nullable String configuredPath) {
        if (configuredPath == null) {
            return null;
        }
        String normalized = configuredPath.trim();
        if (normalized.isEmpty() || "bru".equalsIgnoreCase(normalized)) {
            return null;
        }
        return normalized;
    }
}
