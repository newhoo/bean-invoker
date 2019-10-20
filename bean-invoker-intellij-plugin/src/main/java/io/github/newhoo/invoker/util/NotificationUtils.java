package io.github.newhoo.invoker.util;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import io.github.newhoo.invoker.AppConstant;

/**
 * NotificationUtils
 *
 * @author huzunrong
 * @since 1.0
 */
public class NotificationUtils {

    private static final NotificationGroup logOnlyGroup = NotificationGroup.logOnlyGroup(AppConstant.APP_ID + "_LOG");
    private static final NotificationGroup balloonGroup = NotificationGroup.balloonGroup(AppConstant.APP_ID + "_BALLOON");
//    private static final NotificationGroup toolWindowGroup = NotificationGroup.toolWindowGroup(APP_ID);

    public static void infoEcho(Project project, String title, String msg) {
        logOnlyGroup.createNotification(title, "", msg, NotificationType.INFORMATION)
                    .notify(project);
    }

    public static void infoBalloon(Project project, String title, String msg) {
        balloonGroup.createNotification(title, "", msg, NotificationType.INFORMATION)
                    .notify(project);
    }

    public static void warnEcho(Project project, String title, String msg) {
        logOnlyGroup.createNotification(title, "", msg, NotificationType.WARNING)
                    .notify(project);
    }

    public static void warnBalloon(Project project, String title, String msg) {
        balloonGroup.createNotification(title, "", msg, NotificationType.WARNING)
                    .notify(project);
    }

    public static void errorEcho(Project project, String title, Throwable t) {
        logOnlyGroup.createNotification(title, "", t.toString(), NotificationType.ERROR)
                    .notify(project);
    }

    public static void errorBalloon(Project project, String title, Throwable t) {
        balloonGroup.createNotification(title, "", t.toString(), NotificationType.ERROR)
                    .notify(project);
    }

    public static void errorBalloon(Project project, String title, String msg) {
        balloonGroup.createNotification(title, "", msg, NotificationType.ERROR)
                    .notify(project);
    }
}