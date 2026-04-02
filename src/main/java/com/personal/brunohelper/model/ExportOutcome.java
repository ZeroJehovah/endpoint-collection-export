package com.personal.brunohelper.model;

public final class ExportOutcome {

    private final boolean success;
    private final String message;

    private ExportOutcome(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ExportOutcome success(String message) {
        return new ExportOutcome(true, message);
    }

    public static ExportOutcome failure(String message) {
        return new ExportOutcome(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
