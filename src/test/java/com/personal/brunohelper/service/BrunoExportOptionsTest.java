package com.personal.brunohelper.service;

import com.personal.brunohelper.i18n.BrunoHelperBundle;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BrunoExportOptionsTest {

    @Test
    void shouldValidateGlobalBaseOutputDirectory() {
        assertEquals(
                BrunoHelperBundle.message("export.validation.baseOutput.blank"),
                BrunoExportOptions.validateBaseOutputDirectory("", false)
        );
        assertEquals(
                BrunoHelperBundle.message("export.validation.baseOutput.absolute"),
                BrunoExportOptions.validateBaseOutputDirectory("collection/output", false)
        );
        assertNull(BrunoExportOptions.validateBaseOutputDirectory("/workspace/collection-output", false));
    }

    @Test
    void shouldResolveProjectAndControllerDirectories() {
        Path projectDirectory = BrunoExportOptions.resolveProjectDirectory(
                Path.of("/workspace/collection-output"),
                "demo project"
        );
        assertEquals(
                Path.of("/workspace/collection-output/demo-project"),
                projectDirectory
        );
        assertEquals(
                Path.of("/workspace/collection-output/demo-project/SampleController"),
                BrunoExportOptions.resolveControllerDirectory(projectDirectory, "SampleController")
        );
        assertEquals(
                Path.of("/workspace/workspace.yml"),
                BrunoExportOptions.resolveWorkspaceFile(Path.of("/workspace/collection-output"))
        );
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
        assertEquals("collection-export", BrunoExportOptions.sanitizeFileSystemName("   "));
        assertEquals("user-list", BrunoExportOptions.sanitizeFileSystemName("user   list"));
    }
}
