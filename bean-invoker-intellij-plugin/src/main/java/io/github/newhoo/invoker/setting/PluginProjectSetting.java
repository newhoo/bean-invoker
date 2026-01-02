package io.github.newhoo.invoker.setting;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;

/**
 * PluginProjectSetting
 *
 * @author huzunrong
 * @since 1.0.1
 */
public class PluginProjectSetting {

    private static final String KEY_SPRING_INVOKE_ENABLE = "bean-invoker.enableQuickInvoke";

    private final PropertiesComponent projectSetting;

    public PluginProjectSetting(Project project) {
        this.projectSetting = PropertiesComponent.getInstance(project);
    }

    public boolean getEnableQuickInvoke() {
        return projectSetting.getBoolean(KEY_SPRING_INVOKE_ENABLE, Boolean.TRUE);
    }

    public void setEnableQuickInvoke(boolean enableQuickInvoke) {
        projectSetting.setValue(KEY_SPRING_INVOKE_ENABLE, enableQuickInvoke, Boolean.TRUE);
    }
}