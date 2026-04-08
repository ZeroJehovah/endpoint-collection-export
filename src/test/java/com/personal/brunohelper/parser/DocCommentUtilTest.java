package com.personal.brunohelper.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocCommentUtilTest {

    @Test
    void shouldKeepOnlyFirstLineAndRemoveHtmlTags() {
        String summary = DocCommentUtil.sanitizeSummaryText("<p>导出示例接口</p>\n<p>第二行说明</p>");
        assertEquals("导出示例接口", summary);
    }

    @Test
    void shouldTreatBrAsLineBreakForSummary() {
        String summary = DocCommentUtil.sanitizeSummaryText("导出示例接口<br/>第二行说明");
        assertEquals("导出示例接口", summary);
    }
}
