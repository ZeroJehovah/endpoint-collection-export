package com.personal.brunohelper.model;

public enum ExportEndpointStatus {
    SUCCESS("SUCCESS"),
    FAILED("FAILED"),
    SKIPPED("SKIPPED");

    private final String displayName;

    ExportEndpointStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
