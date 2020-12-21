package io.github.newhoo.invoker;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import io.github.newhoo.invoker.i18n.InvokerBundle;
import io.github.newhoo.invoker.setting.PluginProjectSetting;
import io.github.newhoo.invoker.util.AppUtils;
import io.github.newhoo.invoker.util.NotificationUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static io.github.newhoo.invoker.common.Constant.SERVICE_METHOD_SPLIT;

/**
 * InvokeBeanAction
 *
 * @author huzunrong
 * @since 1.0.1
 */
public class InvokeBeanAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (project == null || project.isDefault() || psiFile == null || !psiFile.isWritable()) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        PsiMethod positionMethod = AppUtils.getPositionMethod(e);
        e.getPresentation().setEnabledAndVisible(positionMethod != null && positionMethod.getReturnType() != null && AppUtils.isSpringApp(project));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || project.isDefault() || editor == null || !AppUtils.checkConfig(project)) {
            return;
        }
        PsiMethod positionMethod = AppUtils.getPositionMethod(e);
        if (positionMethod == null) {
            NotificationUtils.errorBalloon(InvokerBundle.getMessage("positionMethod.call.error.title"), InvokerBundle.getMessage("positionMethod.null.message"), project);
            return;
        }
        if (!positionMethod.hasModifierProperty(PsiModifier.PUBLIC)
                || positionMethod.getParameterList().getParametersCount() > 0) {
            NotificationUtils.errorBalloon(InvokerBundle.getMessage("positionMethod.call.error.title"), InvokerBundle.message("positionMethod.signature.error.message", positionMethod.getName()),
                    new NotificationListener.Adapter() {
                        @Override
                        protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                            AppUtils.generateTest(project, positionMethod, editor);
                            notification.expire();
                        }
                    }, project);
            return;
        }

        String qualifiedName = positionMethod.getContainingClass().getQualifiedName();
        String methodName = positionMethod.getName();
        int invokePort = new PluginProjectSetting(project).getSpringInvokePort();

        // 发送请求
        sendRequest(qualifiedName, methodName, invokePort, project);
    }

    private void sendRequest(String className, String methodName, int port, Project project) {
        String content = className + SERVICE_METHOD_SPLIT + methodName;
        new Thread(() -> {
            try (
                    Socket s = new Socket("127.0.0.1", port);
                    // 向服务器端发送消息
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            ) {
                bw.write(content + "\r\n");
                bw.flush();
            } catch (IOException e) {
                NotificationUtils.errorBalloon(InvokerBundle.getMessage("positionMethod.call.error.title"), InvokerBundle.message("positionMethod.call.error.message", String.valueOf(port)), project);
            }
        }).start();
    }
}