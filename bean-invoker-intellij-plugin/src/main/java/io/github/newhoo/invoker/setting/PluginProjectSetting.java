package io.github.newhoo.invoker.setting;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;

import static io.github.newhoo.invoker.common.Constant.DEFAULT_INVOKE_PORT;

/**
 * PluginProjectSetting
 *
 * @author huzunrong
 * @since 1.0.1
 */
public class PluginProjectSetting {

    private static final String KEY_IS_SPRING_APP = "bean-invoker.isSpringApp";
    private static final String KEY_AGENT_PATH = "bean-invoker.agentPath";
    private static final String KEY_SPRING_INVOKE_ENABLE = "bean-invoker.enableQuickInvoke";
    private static final String KEY_INVOKE_PORT = "bean-invoker.invokePort";
    private static final String KEY_SETTING_INVOKE_PORT = "bean-invoker.settingInvokePort";

    private final PropertiesComponent propertiesComponent;

    public PluginProjectSetting(Project project) {
        this.propertiesComponent = PropertiesComponent.getInstance(project);
    }

    public boolean isSpringApp() {
        return propertiesComponent.getBoolean(KEY_IS_SPRING_APP, Boolean.FALSE);
    }

    public void setIsSpringApp(boolean isSpringApp) {
        propertiesComponent.setValue(KEY_IS_SPRING_APP, isSpringApp);
    }

    public String getAgentPath() {
        return propertiesComponent.getValue(KEY_AGENT_PATH);
    }

    public void setAgentPath(String agentPath) {
        propertiesComponent.setValue(KEY_AGENT_PATH, agentPath);
    }

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

    public int getSettingInvokePort() {
        return propertiesComponent.getInt(KEY_SETTING_INVOKE_PORT, 0);
    }

    public void setSettingInvokePort(int port) {
        propertiesComponent.setValue(KEY_SETTING_INVOKE_PORT, port, 0);
    }

}