package io.github.newhoo.invoker;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.JavaProgramPatcher;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import io.github.newhoo.invoker.common.Constant;
import io.github.newhoo.invoker.i18n.InvokerBundle;
import io.github.newhoo.invoker.setting.PluginProjectSetting;
import io.github.newhoo.invoker.util.NotificationUtils;

import java.net.Socket;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static io.github.newhoo.invoker.ActionHelper.MY_KEY_BEAN_EXPORT_PORT;
import static io.github.newhoo.invoker.common.Constant.APP_ID;
import static io.github.newhoo.invoker.common.Constant.DEFAULT_INVOKE_PORT;
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
        if (!(configuration instanceof RunConfiguration)) {
            return;
        }
        System.out.println("bean-invoker-run-configuration-class" + configuration.getClass().getName() + " :: " + configuration.getClass());
        if (NOT_SUPPORTED_RUN_CONFIGURATION.contains(configuration.getClass().getName())) {
            return;
        }

        RunConfiguration runConfiguration = (RunConfiguration) configuration;
        Project project = runConfiguration.getProject();
        PluginProjectSetting setting = new PluginProjectSetting(project);

        if (!checkCondition(setting, project)) {
            return;
        }

        ParametersList vmParametersList = javaParameters.getVMParametersList();

        String agentPath = JavaToolHelper.getBeanInvokerAgentPath();
        if (!agentPath.isEmpty()) {
            if (agentPath.contains(" ")) {
                agentPath = "\"" + agentPath + "\"";
            }
            vmParametersList.addParametersString("-javaagent:" + agentPath);

            // find port
            Integer availablePort = findAvailablePort();
            if (availablePort == null) {
                NotificationUtils.warnBalloon(InvokerBundle.getMessage("no.available.port"), "", project);
                return;
            }
            vmParametersList.addNotEmptyProperty(Constant.PROPERTIES_KEY_INVOKE_PORT, String.valueOf(availablePort));

            // 设置到运行配置
            if (configuration instanceof UserDataHolder userDataHolder) {
                userDataHolder.putUserData(MY_KEY_BEAN_EXPORT_PORT, availablePort);
            }
        }
    }

    private boolean checkCondition(PluginProjectSetting setting, Project project) {
        logger.info(String.format("[bean-invoker] 检查[%s]插件启用状态", APP_ID));
        if (!setting.getEnableQuickInvoke()) {
            return false;
        }
        if (!JavaToolHelper.existsSpringJar(project)) {
            return false;
        }
        return true;
    }

    private static Integer findAvailablePort() {
        int initialPort = DEFAULT_INVOKE_PORT + new Random().nextInt(1000);
        for (int i = 0; i < 100; i++) {
            int availablePort = initialPort + i;
            if (!isPortUsing(availablePort)) {
                return availablePort;
            }
        }
        return null;
    }

    private static boolean isPortUsing(int port) {
        boolean flag = false;
        try (Socket s = new Socket("127.0.0.1", port)) {
            flag = true;
        } catch (Exception ignored) {
        }
        return flag;
    }
}