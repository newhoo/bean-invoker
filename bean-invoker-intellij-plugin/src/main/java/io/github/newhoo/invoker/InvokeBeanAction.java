package io.github.newhoo.invoker;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import io.github.newhoo.invoker.setting.PluginProjectSetting;
import io.github.newhoo.invoker.util.AppUtils;
import io.github.newhoo.invoker.util.NotificationUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * InvokeBeanAction
 *
 * @author huzunrong
 * @since 1.0
 */
public class InvokeBeanAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null || project.isDefault()) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        PluginProjectSetting pluginProjectSetting = new PluginProjectSetting(project);
        e.getPresentation().setEnabledAndVisible(
                pluginProjectSetting.getEnableQuickInvoke() && AppUtils.getPositionMethod(e) != null && AppUtils.isSpringApp(project));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiMethod positionMethod = AppUtils.getPositionMethod(e);
        if (positionMethod == null) {
            NotificationUtils.errorBalloon(e.getProject(), "找不到test方法", "当前文件必须是Java文件");
            return;
        }
        if (!positionMethod.hasModifierProperty(PsiModifier.PUBLIC)
                || positionMethod.getParameterList().getParametersCount() > 0) {
            NotificationUtils.errorBalloon(e.getProject(), "调用方法必须为无参public方法", "");
            return;
        }

        String qualifiedName = positionMethod.getContainingClass().getQualifiedName();
        String methodName = positionMethod.getName();
        int invokePort = new PluginProjectSetting(e.getProject()).getSpringInvokePort();

        // 发送请求
        sendRequest(qualifiedName, methodName, invokePort, e.getProject());
    }

    private void sendRequest(String className, String methodName, int port, Project project) {
        String content = className + "::" + methodName;
        new Thread(() -> {
            try {
                Socket s = new Socket("127.0.0.1", port);

                // 向服务器端发送消息
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                bw.write(content + "\r\n");
                bw.flush();

                IOUtils.closeQuietly(bw);
                IOUtils.closeQuietly(s);
            } catch (IOException e) {
                NotificationUtils.errorBalloon(project, "调用方法失败", "请确定BeanInvoker启动正常，当前调用端口为：" + port);
            }
        }).start();
    }
}