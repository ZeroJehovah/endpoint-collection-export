package com.personal.brunohelper.service;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public static @Nullable String resolveBruCliPath(@Nullable String configuredPath) {
        String normalized = normalizeBruCliPath(configuredPath);
        if (normalized == null) {
            return null;
        }
        Path cliPath = parsePath(normalized);
        if (cliPath == null) {
            return resolveCommandName(normalized);
        }
        if (Files.isDirectory(cliPath)) {
            Path executable = findExecutableInDirectory(cliPath);
            return executable == null ? null : executable.toString();
        }
        return Files.isRegularFile(cliPath) ? cliPath.toString() : resolveCommandName(normalized);
    }

    public static boolean hasConfiguredBruCliPath(@Nullable String configuredPath) {
        return normalizeBruCliPath(configuredPath) != null;
    }

    public static @Nullable String validateBruCliPath(@Nullable String configuredPath, boolean allowBlank) {
        String normalized = normalizeBruCliPath(configuredPath);
        if (normalized == null) {
            return allowBlank ? null : "请输入 Bruno CLI 命令或可执行文件路径。";
        }

        Path configured = parsePath(normalized);
        if (configured == null) {
            return isCommandName(normalized)
                    ? null
                    : "请输入 Bruno CLI 命令（如 bru）或 Bruno CLI 可执行文件绝对路径。";
        }
        if (!configured.isAbsolute()) {
            return isCommandName(normalized)
                    ? null
                    : "Bruno CLI 可执行文件路径必须使用绝对路径；如已在 PATH 中，可直接填写 bru。";
        }
        if (!Files.exists(configured)) {
            return "Bruno CLI 路径不存在: " + configured;
        }
        if (Files.isRegularFile(configured)) {
            return null;
        }
        if (!Files.isDirectory(configured)) {
            return "Bruno CLI 配置必须是命令名、可执行文件或包含 CLI 的目录。";
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
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized;
    }

    private static @Nullable Path parsePath(String configuredPath) {
        try {
            return Paths.get(configuredPath).normalize();
        } catch (InvalidPathException exception) {
            return null;
        }
    }

    private static boolean isCommandName(String configuredPath) {
        return !configuredPath.contains("/")
                && !configuredPath.contains("\\")
                && !configuredPath.contains(" ");
    }

    private static @Nullable String resolveCommandName(String configuredPath) {
        if (!isCommandName(configuredPath)) {
            return null;
        }
        if (!isWindows()) {
            return configuredPath;
        }

        String resolved = resolveCommandOnWindows(configuredPath, System.getenv("PATH"), System.getenv("PATHEXT"));
        return resolved == null ? configuredPath : resolved;
    }

    static @Nullable String resolveCommandOnWindows(String commandName, @Nullable String pathEnv, @Nullable String pathExtEnv) {
        if (!isCommandName(commandName)) {
            return null;
        }

        List<String> executableNames = new ArrayList<>();
        String lowerCaseCommandName = commandName.toLowerCase(Locale.ROOT);
        for (String extension : splitPathExtensions(pathExtEnv)) {
            if (!lowerCaseCommandName.endsWith(extension.toLowerCase(Locale.ROOT))) {
                executableNames.add(commandName + extension);
            }
        }
        executableNames.add(commandName);

        for (String pathEntry : splitPathEntries(pathEnv)) {
            Path directory = parsePath(pathEntry);
            if (directory == null) {
                continue;
            }
            for (String executableName : executableNames) {
                String resolved = resolveRegularFileIgnoringCase(directory, executableName);
                if (resolved != null) {
                    return resolved;
                }
            }
        }

        return null;
    }

    private static List<String> splitPathEntries(@Nullable String pathEnv) {
        if (pathEnv == null || pathEnv.isBlank()) {
            return List.of();
        }
        List<String> entries = new ArrayList<>();
        for (String entry : pathEnv.split(";")) {
            if (!entry.isBlank()) {
                entries.add(entry.trim());
            }
        }
        return entries;
    }

    private static List<String> splitPathExtensions(@Nullable String pathExtEnv) {
        if (pathExtEnv == null || pathExtEnv.isBlank()) {
            return List.of(".exe", ".cmd", ".bat", ".com");
        }
        List<String> extensions = new ArrayList<>();
        for (String extension : pathExtEnv.split(";")) {
            String normalized = extension.trim();
            if (normalized.isEmpty()) {
                continue;
            }
            if (!normalized.startsWith(".")) {
                normalized = "." + normalized;
            }
            extensions.add(normalized.toLowerCase(Locale.ROOT));
        }
        return extensions.isEmpty() ? List.of(".exe", ".cmd", ".bat", ".com") : extensions;
    }

    private static @Nullable String resolveRegularFileIgnoringCase(Path directory, String fileName) {
        Path candidate = directory.resolve(fileName);
        if (Files.isRegularFile(candidate)) {
            return candidate.toString();
        }
        try (var children = Files.list(directory)) {
            String expectedName = fileName.toLowerCase(Locale.ROOT);
            return children
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).equals(expectedName))
                    .map(Path::toString)
                    .findFirst()
                    .orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }
}
