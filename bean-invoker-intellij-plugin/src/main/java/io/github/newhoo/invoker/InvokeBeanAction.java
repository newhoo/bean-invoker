package io.github.newhoo.invoker;

import com.intellij.debugger.engine.JVMNameUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import io.github.newhoo.invoker.i18n.InvokerBundle;
import io.github.newhoo.invoker.setting.PluginProjectSetting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * InvokeBeanAction
 * 指向正在运行的第一个应用，默认按名称排序
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
                || !ActionHelper.showAction(psiElement)
                || !new PluginProjectSetting(project).getEnableQuickInvoke()) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        List<String> beanExposePort = ActionHelper.getBeanExposedPorts(project);
        if (beanExposePort.isEmpty()) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        e.getPresentation().setEnabledAndVisible(true);
        e.getPresentation().setText(beanExposePort.size() > 1 ? String.format("%s [%s]", InvokerBundle.message("positionMethod.call.name"), beanExposePort.get(0)) : InvokerBundle.message("positionMethod.call.name"));
        e.getPresentation().setDescription(beanExposePort.get(0));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || project.isDefault() || editor == null || psiElement == null) {
            return;
        }
        List<String> beanExposePort = ActionHelper.getBeanExposedPorts(project);
        if (beanExposePort.isEmpty()) {
            return;
        }
        PsiMethod positionMethod = ActionHelper.getPositionMethod(psiElement, editor, project);
        if (positionMethod == null) {
            return;
        }
        String className = JVMNameUtil.getClassVMName(positionMethod.getContainingClass());
        String methodName = positionMethod.getName();

        // port
        String namePort = e.getPresentation().getDescription();
        // 发送请求
        ActionHelper.sendRequest(className, methodName, Integer.parseInt(namePort.substring(namePort.lastIndexOf(":") + 1)), project);
    }
}