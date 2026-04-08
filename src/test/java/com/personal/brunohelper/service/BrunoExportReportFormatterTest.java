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
                "sch-order-service",
                "OrderFileController",
                Path.of("D:\\Documents\\collections\\sch-order-service"),
                Path.of("D:\\Documents\\collections\\sch-order-service\\OrderFileController"),
                5,
                2,
                1,
                1,
                0,
                List.of(
                        new ExportEndpointResult("/order-files/:id", "getById", "查询订单文件", ExportEndpointStatus.SUCCESS, "GET-order-files-id.yml", null),
                        new ExportEndpointResult("/order-files/export", "export", "导出订单文件", ExportEndpointStatus.SKIPPED, "POST-order-files-export.yml", "exists")
                )
        );

        String content = new BrunoExportReportFormatter().format(report);

        assertTrue(content.contains(BrunoHelperBundle.message("export.report.summary.service") + " sch-order-service"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.summary.class") + " OrderFileController"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.summary.total") + " 5"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.summary.selected") + " 2"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.summary.skipped") + " 1"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.summary.success") + " 1"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.summary.failed") + " 0"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.directory.project") + ": D:\\Documents\\collections\\sch-order-service"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.directory.controller") + ": D:\\Documents\\collections\\sch-order-service\\OrderFileController"));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.title")));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.table.relativeUrl")));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.table.methodName")));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.table.exportResult")));
        assertTrue(content.contains(BrunoHelperBundle.message("export.report.table.endpointName")));
        assertTrue(content.contains("/order-files/:id"));
        assertTrue(content.contains("查询订单文件"));
        assertTrue(content.contains("SUCCESS"));
        assertTrue(content.contains("SKIPPED"));
        assertTrue(!content.contains("YAML File"));
        assertTrue(!content.contains("GET-order-files-id.yml"));
        assertTrue(!content.contains("+--"));
    }
}
