package com.personal.brunohelper.parser;

import java.util.ArrayList;
import java.util.List;

public final class SpringRequestPathUtil {

    private SpringRequestPathUtil() {
    }

    public static List<String> combinePaths(List<String> classPaths, List<String> methodPaths) {
        List<String> paths = new ArrayList<>();
        for (String classPath : classPaths) {
            for (String methodPath : methodPaths) {
                String combined = normalizePath(classPath, methodPath);
                if (!paths.contains(combined)) {
                    paths.add(combined);
                }
            }
        }
        return paths.isEmpty() ? List.of("/") : paths;
    }

    public static String normalizePath(String classPath, String methodPath) {
        String left = normalizePathSegment(classPath);
        String right = normalizePathSegment(methodPath);
        if (left.isEmpty() && right.isEmpty()) {
            return "/";
        }
        if (left.isEmpty()) {
            return right;
        }
        if (right.isEmpty() || "/".equals(right)) {
            return left;
        }
        return (left + "/" + right.substring(1)).replace("//", "/");
    }

    private static String normalizePathSegment(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.startsWith("/") ? value : "/" + value;
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
