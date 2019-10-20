package io.github.newhoo.invoker.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import io.github.newhoo.invoker.setting.PluginProjectSetting;

import java.io.IOException;
import java.net.Socket;

import static com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR;
import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE;
import static io.github.newhoo.invoker.common.Constant.DEFAULT_INVOKE_PORT;

/**
 * AppUtils
 *
 * @author huzunrong
 * @since 1.0
 */
public final class AppUtils {

    public static PsiMethod getPositionMethod(AnActionEvent anActionEvent) {
        PsiFile psiFile = anActionEvent.getData(PSI_FILE);
        if (!(psiFile instanceof PsiJavaFileImpl)) {
            return null;
        }

        Editor editor = anActionEvent.getData(EDITOR);

        PsiJavaFile psiJavaFile = (PsiJavaFileImpl) psiFile;
        for (PsiClass psiClass : psiJavaFile.getClasses()) {
            for (PsiMethod psiMethod : psiClass.getMethods()) {
                if (editor != null && psiMethod.getTextRange().containsOffset(editor.getCaretModel().getOffset())) {
                    return psiMethod;
                }
            }
        }
        return null;
    }

    /**
     * 查找类
     *
     * @param typeCanonicalText 参数类型全限定名称
     * @param project 当前project
     * @return 查找到的类
     */
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
    }

    /**
     * 判断是否为Spring应用
     */
    public static boolean isSpringApp(Project project) {
        return null != AppUtils.findPsiClass("org.springframework.context.support.AbstractApplicationContext", project);
    }

    public static Integer findAvailablePort(Project project) {
        PluginProjectSetting pluginProjectSetting = new PluginProjectSetting(project);

        for (int i = 0; i < 100; i++) {
            int availablePort = DEFAULT_INVOKE_PORT + i;
            if (!isPortUsing(availablePort)) {
                pluginProjectSetting.setSpringInvokePort(availablePort);
                return availablePort;
            }
        }
        NotificationUtils.warnBalloon(project, "没有找到可用端口，请手动设置", "");
        return null;
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