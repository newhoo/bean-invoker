package io.github.newhoo.invoker;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.JavaProgramPatcher;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.github.newhoo.invoker.common.Constant;
import io.github.newhoo.invoker.setting.PluginProjectSetting;
import io.github.newhoo.invoker.util.AppUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.stream.Stream;

import static io.github.newhoo.invoker.common.Constant.APP_ID;
import static java.util.stream.Collectors.toSet;

/**
 * BeanInvokerPreRunCheck
 *
 * @author huzunrong
 * @since 1.0.1
 */
public class BeanInvokerPreRunCheck extends JavaProgramPatcher {

    private static final Logger logger = Logger.getInstance(APP_ID);
    private static final Set<String> NOT_SUPPORTED_RUN_CONFIGURATION = Stream.of(
            "org.jetbrains.idea.maven.execution.MavenRunConfiguration"
    ).collect(toSet());

    @Override
    public void patchJavaParameters(Executor executor, RunProfile configuration, JavaParameters javaParameters) {
        if (configuration instanceof RunConfiguration) {
            System.out.println("bean-invoker-run-configuration-class" + configuration.getClass().getName() + " :: " + configuration.getClass());
            if (NOT_SUPPORTED_RUN_CONFIGURATION.contains(configuration.getClass().getName())) {
                return;
            }
            RunConfiguration runConfiguration = (RunConfiguration) configuration;
            Project project = runConfiguration.getProject();
            PluginProjectSetting pluginProjectSetting = new PluginProjectSetting(project);

            logger.info(String.format("检查[%s]插件启用状态", APP_ID));

            if (pluginProjectSetting.getEnableQuickInvoke() && pluginProjectSetting.isSpringApp()) {
                ParametersList vmParametersList = javaParameters.getVMParametersList();

                String agentPath = pluginProjectSetting.getAgentPath();
                if (StringUtils.isNotEmpty(agentPath)) {
                    if (StringUtils.contains(agentPath, " ")) {
                        agentPath = "\"" + agentPath + "\"";
                    }
                    vmParametersList.addParametersString("-javaagent:" + agentPath);

                    vmParametersList.addNotEmptyProperty(Constant.PROPERTIES_KEY_INVOKE_PORT, String.valueOf(AppUtils.findAvailablePort(project)));
                }
            }
        }
    }
}