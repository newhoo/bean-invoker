package io.github.newhoo.invoker.setting;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import io.github.newhoo.invoker.JavaToolHelper;
import io.github.newhoo.invoker.i18n.InvokerBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
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

    private JPanel previewPanel;
    private JLabel previewLabel;
    private JTextArea previewTextArea;

    private final PluginProjectSetting projectSetting;

    public SettingForm(Project project, PluginProjectSetting projectSetting) {
        this.projectSetting = projectSetting;

        invokeEnableCheckBox.setText(InvokerBundle.getMessage("plugin.enable"));

        previewLabel.setText(InvokerBundle.getMessage("plugin.setting.previewLabel"));
        invokeEnableCheckBox.addItemListener(e -> setPreview());
        if (JavaToolHelper.getBeanInvokerAgentPath().isEmpty()) {
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

    public void reset(Project project) {
        invokeEnableCheckBox.setSelected(projectSetting.getEnableQuickInvoke());

        setPreview();
    }

    private void setPreview() {
        boolean selected = invokeEnableCheckBox.isSelected();
        previewPanel.setVisible(selected);

        if (!selected) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        String agentPath = JavaToolHelper.getBeanInvokerAgentPath();
        if (agentPath.isEmpty()) {
            sb.append("Agent not found! Try check your spring project and reopen it.");
        } else {
            sb.append("\"").append("-javaagent:").append(agentPath).append("\"\n");

            sb.append("\"").append("-D").append(PROPERTIES_KEY_INVOKE_PORT).append("=").append("0").append("\"");
        }
        previewTextArea.setText(sb.toString());
    }
}