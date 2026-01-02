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
        return "Spring Bean Invoker";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return settingForm.mainPanel;
    }

    @Override
    public boolean isModified() {
        return !String.valueOf(projectSetting.getEnableQuickInvoke()).equals(String.valueOf(settingForm.invokeEnableCheckBox.isSelected()));
    }

    @Override
    public void apply() throws ConfigurationException {
        projectSetting.setEnableQuickInvoke(settingForm.invokeEnableCheckBox.isSelected());
    }

    @Override
    public void reset() {
        settingForm.reset(project);
    }
}