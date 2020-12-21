package io.github.newhoo.invoker.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.github.newhoo.invoker.common.Constant.APP_ID;

/**
 * NotificationUtils
 *
 * @author huzunrong
 * @since 1.0
 */
public class NotificationUtils {

    private static final NotificationGroup logOnlyGroup = NotificationGroup.logOnlyGroup(APP_ID + "_LOG");
    private static final NotificationGroup balloonGroup = NotificationGroup.balloonGroup(APP_ID + "_BALLOON");
//    private static final NotificationGroup toolWindowGroup = NotificationGroup.toolWindowGroup(APP_ID);

    public static Notification infoBalloon(@NotNull String title, @NotNull String message,
                                           @Nullable NotificationListener listener,
                                           @NotNull Project project) {
        return notify(balloonGroup, title, message, NotificationType.INFORMATION, listener, project);
    }

    public static Notification errorBalloon(@NotNull String title, @NotNull String msg, @Nullable NotificationListener listener, @NotNull Project project) {
        return notify(balloonGroup, title, msg, NotificationType.ERROR, listener, project);
    }

    public static Notification errorBalloon(@NotNull String title, @NotNull String msg, @NotNull Project project) {
        return notify(balloonGroup, title, msg, NotificationType.ERROR, null, project);
    }

    public static Notification warnBalloon(@NotNull String title, @NotNull String message, @NotNull Project project) {
        return notify(balloonGroup, title, message, NotificationType.WARNING, null, project);
    }

    private static Notification notify(@NotNull NotificationGroup notificationGroup,
                                       String title,
                                       @NotNull String message,
                                       @NotNull NotificationType type,
                                       @Nullable NotificationListener listener,
                                       @NotNull Project project) {
        if (title == null) {
            title = "Bean Invoker Tips";
        }
        Notification notification = createNotification(notificationGroup, title, message, type, listener);
        return notify(notification, project);
    }

    private static Notification notify(@NotNull Notification notification, @NotNull Project project) {
        notification.notify(project);
        return notification;
    }

    private static Notification createNotification(@NotNull NotificationGroup notificationGroup,
                                                   @NotNull String title,
                                                   @NotNull String message,
                                                   @NotNull NotificationType type,
                                                   @Nullable NotificationListener listener) {
        // title can be empty; message can't be neither null, nor empty
        if (StringUtil.isEmptyOrSpaces(message)) {
            message = title;
            title = "";
        }
        // if both title and message were empty, then it is a problem in the calling code =>
        // Notifications engine assertion will notify.
        return notificationGroup.createNotification(title, message, type, listener);
    }
}