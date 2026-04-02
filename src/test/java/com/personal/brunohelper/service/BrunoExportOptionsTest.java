package com.personal.brunohelper.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class BrunoExportOptionsTest {

    @Test
    void shouldUseProjectDefaultOutputDirectoryWhenBlank() {
        Path outputDirectory = BrunoExportOptions.resolveOutputDirectory("/workspace/demo", "");

        assertEquals(Path.of("/workspace/demo/bruno"), outputDirectory);
    }

    @Test
    void shouldTreatLegacyBruCommandAsMissingConfiguration() {
        assertFalse(BrunoExportOptions.hasConfiguredBruCliPath("bru"));
        assertNull(BrunoExportOptions.resolveBruCliPath("bru"));
    }

    @Test
    void shouldRejectRelativeBruInstallationPath() {
        assertEquals("Bruno 安装路径必须是本机绝对路径。", BrunoExportOptions.validateBruCliPath("tools/bru", false));
    }

    @Test
    void shouldResolveExecutableInsideConfiguredDirectory(@TempDir Path tempDir) throws IOException {
        Path bruExecutable = Files.createFile(tempDir.resolve("bru.cmd"));

        assertEquals(bruExecutable, BrunoExportOptions.resolveBruCliPath(tempDir.toString()));
        assertNull(BrunoExportOptions.validateBruCliPath(tempDir.toString(), false));
    }

    @Test
    void shouldTrimControllerSuffixForCollectionName() {
        assertEquals("SaleOrder", BrunoExportOptions.deriveCollectionName("SaleOrderController"));
        assertEquals("HealthCheck", BrunoExportOptions.deriveCollectionName("HealthCheck"));
    }
}
