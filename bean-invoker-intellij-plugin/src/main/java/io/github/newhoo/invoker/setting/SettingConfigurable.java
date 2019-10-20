package io.github.newhoo.invoker.setting;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * SettingConfigurable
 *
 * @author huzunrong
 * @since 1.0
 */
public class SettingConfigurable implements Configurable {

    private final PluginProjectSetting projectSetting;
    private final SettingForm settingForm;

    public SettingConfigurable(Project project) {
        this.projectSetting = new PluginProjectSetting(project);
        this.settingForm = new SettingForm();
    }

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Bean Invoker";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        reset();

        return settingForm.mainPanel;
    }

    @Override
    public boolean isModified() {
        return projectSetting.getEnableQuickInvoke() != settingForm.invokeEnableCheckBox.isSelected();
    }

    @Override
    public void apply() {
        projectSetting.setEnableQuickInvoke(settingForm.invokeEnableCheckBox.isSelected());
    }

    @Override
    public void reset() {
        settingForm.invokeEnableCheckBox.setSelected(projectSetting.getEnableQuickInvoke());
    }
}