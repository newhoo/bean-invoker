package io.github.newhoo.invoker;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import io.github.newhoo.invoker.setting.PluginProjectSetting;
import io.github.newhoo.invoker.util.AppUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;


/**
 * 启动后判断
 */
public class MyStartupActivity implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Bean Invoker Check", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                PluginProjectSetting setting = new PluginProjectSetting(project);

                ApplicationManager.getApplication().runReadAction(() -> {
                    setting.setIsSpringApp(AppUtils.isSpringApp(project));
                });

                if (StringUtils.isEmpty(setting.getAgentPath())) {
                    AppUtils.getAgentPath("io.github.newhoo.bean-invoker", "bean-invoker-agent")
                            .ifPresent(setting::setAgentPath);
                }
            }
        });
    }
}
