package io.github.newhoo.invoker.setting;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;

/**
 * @author huzunrong
 * @date 2021/11/28 7:26 下午
 * @since 1.0.0
 */
public class SettingAction extends ToggleAction {

    @Override
    public boolean isSelected(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return false;
        }
        return new PluginProjectSetting(project).getEnableQuickInvoke();
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        new PluginProjectSetting(e.getProject()).setEnableQuickInvoke(state);
    }
}
