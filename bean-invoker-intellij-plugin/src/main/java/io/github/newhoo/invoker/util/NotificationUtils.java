package io.github.newhoo.invoker.util;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * NotificationUtils
 *
 * @author huzunrong
 * @since 1.0
 */
public class NotificationUtils {

    private static final NotificationGroup balloonGroup = NotificationGroupManager.getInstance().getNotificationGroup("bean-invoker-notification");

    public static void infoBalloon(@NotNull String title, @NotNull String message, @Nullable NotificationAction action, @NotNull Project project) {
        notify(title, message, NotificationType.INFORMATION, action, project);
    }

    public static void errorBalloon(@NotNull String title, @NotNull String msg, @Nullable NotificationAction action, @NotNull Project project) {
        notify(title, msg, NotificationType.ERROR, action, project);
    }

    public static void warnBalloon(@NotNull String title, @NotNull String message, @NotNull Project project) {
        notify(title, message, NotificationType.WARNING, null, project);
    }

    private static void notify(@NotNull String title,
                               @NotNull String message,
                               @NotNull NotificationType type,
                               @Nullable NotificationAction action,
                               @NotNull Project project) {
        if (title.isEmpty()) {
            title = "Bean Invoker Tips";
        }
        if (message.isEmpty()) {
            message = title;
            title = "";
        }
        Notification notification = balloonGroup.createNotification(message, type);
        notification.setTitle(title);
        if (action != null) {
            notification.addAction(action);
        }
        notification.notify(project);
    }
}