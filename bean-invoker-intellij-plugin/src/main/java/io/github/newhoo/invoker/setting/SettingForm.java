package io.github.newhoo.invoker.setting;

import io.github.newhoo.invoker.i18n.InvokerBundle;

import javax.swing.*;

/**
 * SettingForm
 *
 * @author huzunrong
 * @since 1.0.1
 */
public class SettingForm {
    public JPanel mainPanel;

    public JCheckBox invokeEnableCheckBox;
    private JLabel portLabel;
    public JTextField portTextField;

    public SettingForm() {
        invokeEnableCheckBox.setText(InvokerBundle.getMessage("plugin.enable"));
        portLabel.setText(InvokerBundle.getMessage("plugin.setting.listenPortLabel"));
        portTextField.setToolTipText(InvokerBundle.getMessage("plugin.setting.listenPortToolTip"));
    }
}