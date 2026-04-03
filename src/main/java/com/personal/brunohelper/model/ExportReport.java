package com.personal.brunohelper.model;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public record ExportReport(
        String serviceName,
        String className,
        @Nullable Path projectDirectory,
        @Nullable Path controllerDirectory,
        int controllerEndpointCount,
        int exportedEndpointCount,
        int skippedEndpointCount,
        int succeededEndpointCount,
        int failedEndpointCount,
        List<ExportEndpointResult> endpointResults
) {
}
