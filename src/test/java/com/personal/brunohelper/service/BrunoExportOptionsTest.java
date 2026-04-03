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
    void shouldTrimControllerSuffixForCollectionName() {
        assertEquals("SaleOrder", BrunoExportOptions.deriveCollectionName("SaleOrderController"));
        assertEquals("HealthCheck", BrunoExportOptions.deriveCollectionName("HealthCheck"));
    }

    @Test
    void shouldSanitizeCollectionDirectoryName() {
        assertEquals(
                "Sale-Order-Export",
                BrunoExportOptions.sanitizeFileSystemName(" Sale / Order : Export ")
        );
        assertEquals("bruno-export", BrunoExportOptions.sanitizeFileSystemName("   "));
        assertEquals("user-list", BrunoExportOptions.sanitizeFileSystemName("user   list"));
    }
}
