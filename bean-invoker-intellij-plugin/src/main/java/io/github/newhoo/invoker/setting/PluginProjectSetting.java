package io.github.newhoo.invoker.setting;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;

import static io.github.newhoo.invoker.common.Constant.DEFAULT_INVOKE_PORT;

/**
 * PluginProjectSetting
 *
 * @author huzunrong
 * @since 1.0
 */
public class PluginProjectSetting {

    private static final String KEY_SPRING_INVOKE_ENABLE = "bean-invoker.enableQuickInvoke";
    private static final String KEY_INVOKE_PORT = "bean-invoker.invokePort";

    private final PropertiesComponent propertiesComponent;

    public PluginProjectSetting(Project project) {
        this.propertiesComponent = PropertiesComponent.getInstance(project);
    }

    // spring

    public boolean getEnableQuickInvoke() {
        return propertiesComponent.getBoolean(KEY_SPRING_INVOKE_ENABLE, Boolean.FALSE);
    }

    public void setEnableQuickInvoke(boolean enableQuickInvoke) {
        propertiesComponent.setValue(KEY_SPRING_INVOKE_ENABLE, enableQuickInvoke);
    }

    public int getSpringInvokePort() {
        return propertiesComponent.getInt(KEY_INVOKE_PORT, DEFAULT_INVOKE_PORT);
    }

    public void setSpringInvokePort(int port) {
        propertiesComponent.setValue(KEY_INVOKE_PORT, port, DEFAULT_INVOKE_PORT);
    }

}