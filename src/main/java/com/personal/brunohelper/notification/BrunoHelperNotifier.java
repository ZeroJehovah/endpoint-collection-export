package com.personal.brunohelper.notification;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public final class BrunoHelperNotifier {

    public static final String GROUP_ID = "Bruno Helper";

    private BrunoHelperNotifier() {
    }

    public static void info(Project project, String content) {
        notify(project, content, NotificationType.INFORMATION);
    }

    public static void warn(Project project, String content) {
        notify(project, content, NotificationType.WARNING);
    }

    public static void error(Project project, String content) {
        notify(project, content, NotificationType.ERROR);
    }

    private static void notify(Project project, String content, NotificationType type) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup(GROUP_ID)
                .createNotification(content, type)
                .notify(project);
    }
}
