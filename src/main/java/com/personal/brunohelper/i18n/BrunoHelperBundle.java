package com.personal.brunohelper.i18n;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

public final class BrunoHelperBundle extends DynamicBundle {

    @NonNls
    private static final String BUNDLE = "messages.BrunoHelperBundle";

    private static final BrunoHelperBundle INSTANCE = new BrunoHelperBundle();

    private BrunoHelperBundle() {
        super(BUNDLE);
    }

    public static @Nls String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }
}
