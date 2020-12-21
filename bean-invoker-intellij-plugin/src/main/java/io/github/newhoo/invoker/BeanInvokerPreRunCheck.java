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

import java.net.URL;
import java.net.URLDecoder;

import static io.github.newhoo.invoker.common.Constant.APP_ID;

/**
 * BeanInvokerPreRunCheck
 *
 * @author huzunrong
 * @since 1.0.1
 */
public class BeanInvokerPreRunCheck extends JavaProgramPatcher {

    private static final Logger logger = Logger.getInstance(APP_ID);
//    private static final Set<String> SUPPORTED_RUN_CONFIGURATION = Stream.of(
//            "com.intellij.execution.application.ApplicationConfiguration",
//            "com.intellij.spring.boot.run.SpringBootApplicationRunConfiguration"
//    ).collect(toSet());

    @Override
    public void patchJavaParameters(Executor executor, RunProfile configuration, JavaParameters javaParameters) {
//        if (!SUPPORTED_RUN_CONFIGURATION.contains(configuration.getClass().getName())) {
//            return;
//        }

        if (configuration instanceof RunConfiguration) {
            RunConfiguration runConfiguration = (RunConfiguration) configuration;
            Project project = runConfiguration.getProject();
            PluginProjectSetting pluginProjectSetting = new PluginProjectSetting(project);

            logger.info(String.format("检查[%s]插件启用状态", APP_ID));

            if (pluginProjectSetting.getEnableQuickInvoke() && AppUtils.isSpringApp(project)) {
                ParametersList vmParametersList = javaParameters.getVMParametersList();

                String agentPath = getAgentPath();
                if (StringUtils.contains(agentPath, " ")) {
                    agentPath = "\"" + agentPath + "\"";
                }
                vmParametersList.addParametersString("-javaagent:" + agentPath);

                vmParametersList.addNotEmptyProperty(Constant.PROPERTIES_KEY_INVOKE_PORT,
                        String.valueOf(AppUtils.findAvailablePort(project)));
            }
        }
    }

    /**
     * -javaagent:/path/your/bean-invoker-agent.jar
     */
    private static String getAgentPath() {
        URL resource = io.github.newhoo.invoker.common.Constant.class.getResource("");
        if (resource != null && "jar".equals(resource.getProtocol())) {
            String path = resource.getPath();
            try {
                String decodePath = URLDecoder.decode(path.substring("file:/".length() - 1, path.indexOf("!/")), "UTF-8");
                if (decodePath.contains(":")) {
                    decodePath = decodePath.substring(1);
                }
                return decodePath;
            } catch (Exception e) {
                logger.error("URLDecoder Exception: " + resource.getPath(), e);
            }
        }
        return "";
    }
}