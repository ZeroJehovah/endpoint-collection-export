package com.personal.brunohelper.service;

import com.personal.brunohelper.i18n.BrunoHelperBundle;
import com.personal.brunohelper.model.ExportEndpointResult;
import com.personal.brunohelper.model.ExportEndpointStatus;
import com.personal.brunohelper.model.ExportReport;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BrunoExportReportFormatterTest {

    @Test
    void shouldRenderSummaryAndTable() {
        ExportReport report = new ExportReport(
                "demo-service",
                "SampleController",
                Path.of("C:\\api-collections\\demo-service"),
                Path.of("C:\\api-collections\\demo-service\\SampleController"),
                5,
                2,
                1,
                1,
                0,
                List.of(
                        new ExportEndpointResult("/samples/:id", "getById", "查询示例接口", ExportEndpointStatus.SUCCESS, "GET-samples-id.yml", null),
                        new ExportEndpointResult("/samples/export", "export", "导出示例接口", ExportEndpointStatus.SKIPPED, "POST-samples-export.yml", "exists")
                )
        );

        String content = new BrunoExportReportFormatter().format(report);

        assertTrue(content.contains(BrunoHelperBundle.message("export.report.summary.service") + " demo-service"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.summary.class") + " SampleController"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.summary.total") + " 5"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.summary.selected") + " 2"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.summary.skipped") + " 1"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.summary.success") + " 1"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.summary.failed") + " 0"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.directory.project") + ": C:\\api-collections\\demo-service"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.directory.controller") + ": C:\\api-collections\\demo-service\\SampleController"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.title")));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.table.relativeUrl")));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.table.methodName")));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.table.exportResult")));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.table.endpointName")));
        assertTrue(content.contains("/samples/:id"));
        assertTrue(content.contains("查询示例接口"));
        assertTrue(content.contains("SUCCESS"));
        assertTrue(content.contains("SKIPPED"));
        assertTrue(!content.contains("YAML File"));
        assertTrue(!content.contains("GET-samples-id.yml"));
        assertTrue(!content.contains("+--"));
    }
}
