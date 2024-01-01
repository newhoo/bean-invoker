package io.github.newhoo.invoker.util;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import io.github.newhoo.invoker.i18n.InvokerBundle;
import io.github.newhoo.invoker.setting.PluginProjectSetting;
import io.github.newhoo.invoker.setting.SettingConfigurable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static io.github.newhoo.invoker.common.Constant.DEFAULT_INVOKE_PORT;

/**
 * AppUtils
 *
 * @author huzunrong
 * @since 1.0
 */
public final class AppUtils {

    /**
     * 判断配置是否有效
     */
    public static boolean checkConfig(Project project) {
        PluginProjectSetting pluginProjectSetting = new PluginProjectSetting(project);
        if (!pluginProjectSetting.getEnableQuickInvoke()) {
            NotificationUtils.infoBalloon(InvokerBundle.getMessage("plugin.setting.complete.title"), InvokerBundle.getMessage("plugin.setting.complete.tip"),
                    new NotificationAction(InvokerBundle.getMessage("plugin.setting.complete.tipBtn")) {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                            ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingConfigurable.class);
                        }
                    }, project);
            return false;
        }
        return true;
    }

    /**
     * 判断是否为Spring应用
     */
    public static boolean isSpringApp(Project project) {
        PsiClass[] classesByName = PsiShortNamesCache.getInstance(project).getClassesByName("AbstractApplicationContext", GlobalSearchScope.allScope(project));
        for (PsiClass psiClass : classesByName) {
            if ("org.springframework.context.support.AbstractApplicationContext".equals(psiClass.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * -javaagent:/path/your/bean-invoker-agent.jar
     */
    public static Optional<String> getAgentPath(String pluginId, String agentName) {
        PluginId pluginId0 = PluginId.getId(pluginId);
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(pluginId0);
        if (plugin != null) {
            Path pluginPath = plugin.getPluginPath();
            if (pluginPath == null) {
                return Optional.empty();
            }
            return Arrays.stream(Objects.requireNonNull(pluginPath.toFile().listFiles()))
                    .filter(File::isDirectory)
                    .flatMap(file -> Arrays.stream(Objects.requireNonNull(file.listFiles(f -> f.getName().endsWith(".jar")))))
                    .filter(file -> file.getName().contains(agentName))
                    .map(File::getAbsolutePath)
                    .findFirst();
        }
        return Optional.empty();
    }

//    public static PsiMethod getPositionMethod(AnActionEvent anActionEvent) {
//        PsiFile psiFile = anActionEvent.getData(PSI_FILE);
//        if (!(psiFile instanceof PsiClassOwner)) {
//            return null;
//        }
//
//        Editor editor = anActionEvent.getData(EDITOR);
//        if (editor == null) {
//            return null;
//        }
//        int offset = editor.getCaretModel().getOffset();
//
//        PsiClassOwner psiClassOwner = (PsiClassOwner) psiFile;
//        for (PsiClass psiClass : psiClassOwner.getClasses()) {
//            for (PsiMethod psiMethod : psiClass.getMethods()) {
//                if (psiMethod.getTextRange() != null && psiMethod.getTextRange().containsOffset(offset)) {
//                    return psiMethod;
//                }
//            }
//        }
//        return null;
//    }

    /**
     * 查找类
     *
     * @param typeCanonicalText 参数类型全限定名称
     * @param project           当前project
     * @return 查找到的类
     *//*
    public static PsiClass findPsiClass(String typeCanonicalText, Project project) {
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
    }*/
    public static Integer findAvailablePort(Project project) {
        PluginProjectSetting pluginProjectSetting = new PluginProjectSetting(project);
        int port = pluginProjectSetting.getSettingInvokePort();
        if (port != 0) {
            return port;
        }

        int initialPort = DEFAULT_INVOKE_PORT + new Random().nextInt(1000);
        for (int i = 0; i < 100; i++) {
            int availablePort = initialPort + i;
            if (!isPortUsing(availablePort)) {
                return availablePort;
            }
        }
        return null;
    }

    public static void generateTest(Project project, PsiMethod positionMethod, Editor editor) {
        Document document = editor.getDocument();
        int startLineNum = document.getLineNumber(positionMethod.getTextRange().getStartOffset());
        int lineStartOffset = document.getLineStartOffset(startLineNum);
        String containingFileType = positionMethod.getContainingFile().getFileType().getName();

        WriteCommandAction.runWriteCommandAction(project, () -> {
//        ApplicationManager.getApplication().runWriteAction(() -> {
            CommandProcessor.getInstance().executeCommand(project, () -> {
                String generateKtMethod = null;
                if ("JAVA".equals(containingFileType)) {
                    generateKtMethod = getGenerateJavaMethod(positionMethod);
                } else if ("Kotlin".equals(containingFileType)) {
                    generateKtMethod = getGenerateKtMethod(positionMethod);
                }

                if (generateKtMethod == null || generateKtMethod.isEmpty()) {
                    return;
                }
                document.insertString(lineStartOffset, generateKtMethod);

                editor.getCaretModel().moveToOffset(document.getLineStartOffset(startLineNum + 2) + 8);
                editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
            }, "a", "b");
        });
    }

    private static String getGenerateKtMethod(PsiMethod positionMethod) {
        StringBuilder sb = new StringBuilder();
        sb.append("    // TODO clean: ").append(LocalDateTime.now()).append("\n")
                .append("    fun $name$() {\n");

        List<String> parameterNameList = new ArrayList<>(positionMethod.getParameterList().getParametersCount());
        for (PsiParameter parameter : positionMethod.getParameterList().getParameters()) {
            parameterNameList.add(parameter.getName());
            sb.append("        var ").append(parameter.getName()).append(" = ")
                    .append(parameter.getType().getPresentableText()).append("();\n");
        }

        sb.append("        ").append(positionMethod.getName()).append("(").append(String.join(", ", parameterNameList)).append(");\n");
        sb.append("        ").append("println()").append("\n");
        sb.append("    }\n\n");

        String generateName = positionMethod.getName() + "TEST";
        return sb.toString().replace("$name$", generateName);
    }

    private static String getGenerateJavaMethod(PsiMethod positionMethod) {
        StringBuilder sb = new StringBuilder();
        sb.append("    // TODO clean: ").append(LocalDateTime.now()).append("\n")
                .append("    public void $name$() {\n");

        List<String> parameterNameList = new ArrayList<>(positionMethod.getParameterList().getParametersCount());
        for (PsiParameter parameter : positionMethod.getParameterList().getParameters()) {
            parameterNameList.add(parameter.getName());
            sb.append("        ").append(parameter.getType().getPresentableText()).append(" ").append(parameter.getName()).append(" = new ")
                    .append(parameter.getType().getPresentableText()).append("();\n");
        }

        sb.append("        ").append(positionMethod.getName()).append("(").append(String.join(", ", parameterNameList)).append(");\n");
        sb.append("        ").append("System.out.println();").append("\n");
        sb.append("    }\n\n");

        String generateName = positionMethod.getName() + "TEST";
        return sb.toString().replace("$name$", generateName);
    }

    private static boolean isPortUsing(int port) {
        boolean flag = false;
        try {
            Socket socket = new Socket("127.0.0.1", port);
            flag = true;
        } catch (IOException ignored) {
        }
        return flag;
    }
}