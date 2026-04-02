package com.personal.brunohelper.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpringRequestPathUtilTest {

    @Test
    void shouldCombineClassAndMethodPaths() {
        List<String> paths = SpringRequestPathUtil.combinePaths(List.of("/saleOrder"), List.of("/list", "detail"));

        assertEquals(List.of("/saleOrder/list", "/saleOrder/detail"), paths);
    }

    @Test
    void shouldNormalizeBlankPathsToRoot() {
        assertEquals("/", SpringRequestPathUtil.normalizePath("", ""));
        assertEquals("/saleOrder", SpringRequestPathUtil.normalizePath("/saleOrder/", ""));
        assertEquals("/list", SpringRequestPathUtil.normalizePath("", "list"));
    }
}
