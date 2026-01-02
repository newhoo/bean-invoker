package io.github.newhoo.invoker;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class JavaToolHelper {

    public static boolean existsSpringJar(Project project) {
        return JavaToolHelper.findPsiClass("org.springframework.context.support.AbstractApplicationContext", project) != null;
    }

    @NotNull
    public static String getBeanInvokerAgentPath() {
        return getAgentPath("io.github.newhoo.bean-invoker", "bean-invoker-agent").orElse("");
    }

    /**
     * -javaagent:/path/your/mysql-explain-agent.jar
     */
    private static Optional<String> getAgentPath(String pluginId, String agentName) {
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId(pluginId));
        if (plugin != null) {
            Path pluginPath = plugin.getPluginPath();
            if (pluginPath == null) {
                return Optional.empty();
            }
            return Arrays.stream(Objects.requireNonNull(pluginPath.toFile().listFiles())).filter(File::isDirectory).flatMap(file -> Arrays.stream(Objects.requireNonNull(file.listFiles(f -> f.getName().endsWith(".jar"))))).filter(file -> file.getName().contains(agentName)).map(File::getAbsolutePath).findFirst();
        }
        return Optional.empty();
    }

    /**
     * 查找类
     *
     * @param typeCanonicalText 参数类型全限定名称
     * @param project           当前project
     * @return 查找到的类
     */
    private static PsiClass findPsiClass(String typeCanonicalText, Project project) {
        String className = typeCanonicalText;
        if (className.contains("[]")) {
            className = className.replaceAll("\\[]", "");
        }
        if (className.contains("<")) {
            className = className.substring(0, className.indexOf("<"));
        }
        if (className.lastIndexOf(".") > 0) {
            className = className.substring(className.lastIndexOf(".") + 1);
        }
        PsiClass[] classesByName = PsiShortNamesCache.getInstance(project).getClassesByName(className, GlobalSearchScope.allScope(project));
        for (PsiClass psiClass : classesByName) {
            if (typeCanonicalText.startsWith(psiClass.getQualifiedName())) {
                return psiClass;
            }
        }
        return null;
    }
}
