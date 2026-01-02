package io.github.newhoo.invoker;

import com.intellij.debugger.engine.JVMNameUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import io.github.newhoo.invoker.i18n.InvokerBundle;
import io.github.newhoo.invoker.setting.PluginProjectSetting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

/**
 * 指向正在运行的非第一个应用，默认按名称排序
 */
public class InvokeBeanDynamicActionGroup extends ActionGroup {
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        if (e == null) {
            return new AnAction[0];
        }
        Project project = e.getData(PlatformDataKeys.PROJECT);
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || project.isDefault() || editor == null || psiElement == null
                || !new PluginProjectSetting(project).getEnableQuickInvoke()
                || !ActionHelper.showAction(psiElement)) {
            e.getPresentation().setEnabledAndVisible(false);
            return new AnAction[0];
        }

        // 获取菜单
        return ActionHelper.getBeanExposedPorts(project).stream()
                .skip(1)
                .map(namePort -> {
                    return new AnAction(() -> String.format("%s [%s]", InvokerBundle.message("positionMethod.call.name"), namePort), AllIcons.Actions.Lightning) {

                        @Override
                        public @NotNull ActionUpdateThread getActionUpdateThread() {
                            return ActionUpdateThread.BGT;
                        }

                        @Override
                        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                            PsiMethod positionMethod = ActionHelper.getPositionMethod(psiElement, editor, project);
                            if (positionMethod == null) {
                                return;
                            }
                            String className = JVMNameUtil.getClassVMName(positionMethod.getContainingClass());
                            String methodName = positionMethod.getName();

                            // 发送请求
                            ActionHelper.sendRequest(className, methodName, Integer.parseInt(namePort.substring(namePort.lastIndexOf(":") + 1)), project);
                        }
                    };
                })
                .collect(Collectors.toList())
                .toArray(new AnAction[0]);
    }
}
