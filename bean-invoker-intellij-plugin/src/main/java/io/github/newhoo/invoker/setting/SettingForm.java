package io.github.newhoo.invoker.setting;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import io.github.newhoo.invoker.i18n.InvokerBundle;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static io.github.newhoo.invoker.common.Constant.PROPERTIES_KEY_INVOKE_PORT;

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

    private JPanel previewPanel;
    private JLabel previewLabel;
    private JTextArea previewTextArea;

    private final PluginProjectSetting projectSetting;

    public SettingForm(Project project, PluginProjectSetting projectSetting) {
        this.projectSetting = projectSetting;

        invokeEnableCheckBox.setText(InvokerBundle.getMessage("plugin.enable"));
        portLabel.setText(InvokerBundle.getMessage("plugin.setting.listenPortLabel"));
        portTextField.setToolTipText(InvokerBundle.getMessage("plugin.setting.listenPortToolTip"));

        previewLabel.setText(InvokerBundle.getMessage("plugin.setting.previewLabel"));
        addFocusListener(portTextField);
        invokeEnableCheckBox.addItemListener(e -> setPreview());
        if (StringUtils.isEmpty(projectSetting.getAgentPath())) {
            return;
        }
        previewLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 生成快捷按钮
                DefaultActionGroup generateActionGroup = new DefaultActionGroup(
                        new AnAction("Copy as Line String") {
                            @Override
                            public @NotNull ActionUpdateThread getActionUpdateThread() {
                                return ActionUpdateThread.BGT;
                            }

                            @Override
                            public void actionPerformed(@NotNull AnActionEvent e) {
                                String s = previewTextArea.getText().replace("\\", "\\\\").replace("\n", " ");
                                CopyPasteManager.getInstance().setContents(new StringSelection(s));
                            }
                        },
                        new AnAction("Copy as String Array") {
                            @Override
                            public @NotNull ActionUpdateThread getActionUpdateThread() {
                                return ActionUpdateThread.BGT;
                            }

                            @Override
                            public void actionPerformed(@NotNull AnActionEvent e) {
                                String s = previewTextArea.getText().replace("\\", "\\\\").replace("\n", ",\n    ");
                                CopyPasteManager.getInstance().setContents(new StringSelection("[\n    " + s + "\n]"));
                            }
                        }
                );

                DataContext dataContext = DataManager.getInstance().getDataContext(previewLabel);
                final ListPopup popup = JBPopupFactory.getInstance()
                                                      .createActionGroupPopup(
                                                              null,
                                                              generateActionGroup,
                                                              dataContext,
                                                              JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                                                              true);
                popup.showInBestPositionFor(dataContext);
            }
        });
    }

    private void addFocusListener(JTextField jTextField) {
        jTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                setPreview();
            }

            @Override
            public void focusGained(FocusEvent e) {
            }
        });
    }

    public void reset(Project project) {
        invokeEnableCheckBox.setSelected(projectSetting.getEnableQuickInvoke());
        portTextField.setText(String.valueOf(projectSetting.getSettingInvokePort()));
        
        setPreview();
    }

    private void setPreview() {
        boolean selected = invokeEnableCheckBox.isSelected();
        previewPanel.setVisible(selected);

        if (!selected) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        String agentPath = projectSetting.getAgentPath();
        if (StringUtils.isEmpty(agentPath)) {
            sb.append("Agent not found! Try check your spring project and reopen it.");
        } else {
            sb.append("\"").append("-javaagent:").append(agentPath).append("\"\n");

            sb.append("\"").append("-D").append(PROPERTIES_KEY_INVOKE_PORT).append("=").append(StringUtils.defaultIfEmpty(portTextField.getText(), "0")).append("\"");
        }
        previewTextArea.setText(sb.toString());
    }
}