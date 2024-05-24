package io.github.newhoo.invoker.setting;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * SettingConfigurable
 *
 * @author huzunrong
 * @since 1.0.1
 */
public class SettingConfigurable implements Configurable {

    private final PluginProjectSetting projectSetting;
    private final SettingForm settingForm;
    private final Project project;

    public SettingConfigurable(Project project) {
        this.project = project;
        this.projectSetting = new PluginProjectSetting(project);
        this.settingForm = new SettingForm(project, projectSetting);
    }

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Bean Invoker";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return settingForm.mainPanel;
    }

    @Override
    public boolean isModified() {
        int port = projectSetting.getSettingInvokePort();
        String value = settingForm.portTextField.getText();
        return projectSetting.getEnableQuickInvoke() != settingForm.invokeEnableCheckBox.isSelected()
                || !String.valueOf(port).equals(value);
    }

    @Override
    public void apply() throws ConfigurationException {
        try {
            int port = Integer.parseInt(settingForm.portTextField.getText());
            if (port < 0 || port > 65535) {
                throw new ConfigurationException("Wrong port " + port);
            }
            projectSetting.setSettingInvokePort(port);
        } catch (Exception e) {
            throw new ConfigurationException(e.toString());
        }
        projectSetting.setEnableQuickInvoke(settingForm.invokeEnableCheckBox.isSelected());
    }

    @Override
    public void reset() {
        settingForm.reset(project);
    }
}