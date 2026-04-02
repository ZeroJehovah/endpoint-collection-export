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
}
