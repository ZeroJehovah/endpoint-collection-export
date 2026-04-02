package com.personal.brunohelper.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BrunoExportOptionsTest {

    @Test
    void shouldUseProjectDefaultOutputDirectoryWhenBlank() {
        Path outputDirectory = BrunoExportOptions.resolveOutputDirectory("/workspace/demo", "");

        assertEquals(Path.of("/workspace/demo/bruno"), outputDirectory);
    }

    @Test
    void shouldResolveRelativeCliPathAgainstProject() {
        String cliPath = BrunoExportOptions.resolveBruCliPath("/workspace/demo", "tools/bru");

        assertEquals(Path.of("/workspace/demo/tools/bru").toString(), cliPath);
    }

    @Test
    void shouldTrimControllerSuffixForCollectionName() {
        assertEquals("SaleOrder", BrunoExportOptions.deriveCollectionName("SaleOrderController"));
        assertEquals("HealthCheck", BrunoExportOptions.deriveCollectionName("HealthCheck"));
    }
}
