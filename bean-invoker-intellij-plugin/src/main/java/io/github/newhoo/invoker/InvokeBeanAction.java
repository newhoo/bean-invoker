package io.github.newhoo.invoker;

import com.intellij.debugger.engine.JVMNameUtil;
import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import io.github.newhoo.invoker.i18n.InvokerBundle;
import io.github.newhoo.invoker.setting.PluginProjectSetting;
import io.github.newhoo.invoker.util.AppUtils;
import io.github.newhoo.invoker.util.NotificationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.asJava.LightClassUtilsKt;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

import static io.github.newhoo.invoker.common.Constant.SERVICE_METHOD_SPLIT;

/**
 * InvokeBeanAction
 *
 * @author huzunrong
 * @since 1.0.1
 */
public class InvokeBeanAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (psiElement == null || project == null || project.isDefault()
                || !showAction(psiElement)
                || !new PluginProjectSetting(project).showContextInvokeButton()) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        e.getPresentation().setText(InvokerBundle.getMessage("positionMethod.call.name"));
        e.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (project == null || project.isDefault() || editor == null || psiElement == null || !AppUtils.checkConfig(project)) {
            return;
        }
        PsiMethod positionMethod = getPsiMethod(psiElement);
        if (positionMethod == null) {
            return;
        }
        if (!positionMethod.hasModifierProperty(PsiModifier.PUBLIC) || positionMethod.getParameterList().getParametersCount() > 0) {
            NotificationUtils.errorBalloon(InvokerBundle.getMessage("positionMethod.call.error.title"), InvokerBundle.message("positionMethod.signature.error.message", positionMethod.getName()),
                    new NotificationAction(InvokerBundle.message("positionMethod.signature.error.messageBtn")) {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                            notification.expire();
                            AppUtils.generateTest(project, positionMethod, editor);
                        }
                    }, project);
            return;
        }

        String className = JVMNameUtil.getClassVMName(positionMethod.getContainingClass());
        String methodName = positionMethod.getName();
        int invokePort = new PluginProjectSetting(project).getSpringInvokePort();

        // 发送请求
        sendRequest(className, methodName, invokePort, project);
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
                NotificationUtils.errorBalloon(InvokerBundle.getMessage("positionMethod.call.error.title"), InvokerBundle.message("positionMethod.call.error.message", String.valueOf(port)), null, project);
            }
        }).start();
    }

    private boolean showAction(PsiElement psiElement) {
        Language language = psiElement.getLanguage();
        if (language instanceof JavaLanguage) {
            return psiElement instanceof PsiMethod && !((PsiMethod) psiElement).isConstructor();
        } else if (language instanceof KotlinLanguage) {
            return psiElement instanceof KtNamedFunction;
        }
        return false;
    }

    private PsiMethod getPsiMethod(PsiElement psiElement) {
        Language language = psiElement.getLanguage();
        if (language instanceof JavaLanguage) {
            return (PsiMethod) psiElement;
        } else if (language instanceof KotlinLanguage) {
            if (psiElement instanceof KtNamedFunction) {
                List<PsiMethod> lightMethods = LightClassUtilsKt.toLightMethods(psiElement);
                if (!lightMethods.isEmpty()) {
                    return lightMethods.get(0);
                }
            }
        }
        return null;
    }
}