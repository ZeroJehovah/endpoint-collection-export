package com.personal.brunohelper.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BrunoHelperBundleTest {

    @Test
    void shouldProvideEnglishBrunoExportTitles() {
        ResourceBundle bundle = ResourceBundle.getBundle("messages.BrunoHelperBundle", Locale.ENGLISH);

        assertEquals("Bruno Export Result", bundle.getString("export.report.title"));
        assertEquals("Bruno Export Result - {0}", bundle.getString("export.console.title"));
    }
}
