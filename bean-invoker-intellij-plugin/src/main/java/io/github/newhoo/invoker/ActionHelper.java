package io.github.newhoo.invoker;

import com.intellij.execution.ExecutionManager;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.impl.ExecutionManagerImpl;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameter;
import io.github.newhoo.invoker.i18n.InvokerBundle;
import io.github.newhoo.invoker.util.NotificationUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static io.github.newhoo.invoker.common.Constant.SERVICE_METHOD_SPLIT;

class ActionHelper {

    public static Key<Integer> MY_KEY_BEAN_EXPORT_PORT = Key.create("MY_KEY_RUN_PORT");

    @NotNull
    public static List<String> getBeanExposedPorts(Project project) {
        // 获取菜单
        ExecutionManager executionManager = ExecutionManager.getInstance(project);
        if (!(executionManager instanceof ExecutionManagerImpl executionManagerImpl)) {
            return Collections.emptyList();
        }
        List<String> retList = new ArrayList<>();
        RunContentManager runContentManager = RunContentManager.getInstance(project);
        for (RunContentDescriptor runContentDescriptor : runContentManager.getAllDescriptors()) {
            ProcessHandler processHandler = runContentDescriptor.getProcessHandler();
            if (processHandler == null || processHandler.isProcessTerminated()) {
                continue;
            }
            Set<ExecutionEnvironment> executionEnvironments = executionManagerImpl.getExecutionEnvironments(runContentDescriptor);
            if (executionEnvironments.isEmpty()) {
                continue;
            }
            ExecutionEnvironment executionEnvironment = executionEnvironments.toArray(new ExecutionEnvironment[0])[0];
            RunProfile runProfile = executionEnvironment.getRunProfile();
            if (runProfile instanceof UserDataHolder userDataHolder) {
                Integer invokePort = userDataHolder.getUserData(MY_KEY_BEAN_EXPORT_PORT);
                if (invokePort == null) {
                    continue;
                }
                retList.add(runProfile.getName() + ":" + invokePort);
            }
        }
        return retList;
    }

    public static PsiMethod getPositionMethod(@NotNull PsiElement psiElement, @NotNull Editor editor, @NotNull Project project) {
        PsiMethod positionMethod = getPsiMethod(psiElement);
        if (positionMethod == null) {
            return null;
        }
        if (!positionMethod.hasModifierProperty(PsiModifier.PUBLIC) || positionMethod.getParameterList().getParametersCount() > 0) {
            NotificationUtils.errorBalloon(InvokerBundle.message("positionMethod.call.error.title"), InvokerBundle.message("positionMethod.signature.error.message", positionMethod.getName()),
                    new NotificationAction(InvokerBundle.message("positionMethod.signature.error.messageBtn")) {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                            notification.expire();
                            generateTest(project, positionMethod, editor);
                        }
                    }, project);
            return null;
        }
        return positionMethod;
    }

    public static void sendRequest(String className, String methodName, int port, Project project) {
        String content = className + SERVICE_METHOD_SPLIT + methodName;
        new Thread(() -> {
            try (
                    Socket s = new Socket("127.0.0.1", port);
                    // 向服务器端发送消息
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            ) {
                bw.write(content + "\r\n");
                bw.flush();
            } catch (IOException e) {
                NotificationUtils.errorBalloon(InvokerBundle.message("positionMethod.call.error.title"), InvokerBundle.message("positionMethod.call.error.message", String.valueOf(port)), null, project);
            }
        }).start();
    }

    public static boolean showAction(PsiElement psiElement) {
        Language language = psiElement.getLanguage();
        if (language instanceof JavaLanguage) {
            return psiElement instanceof PsiMethod && !((PsiMethod) psiElement).isConstructor();
        }
        return "org.jetbrains.kotlin.idea.KotlinLanguage".equals(language.getClass().getCanonicalName())
                && "org.jetbrains.kotlin.psi.KtNamedFunction".equals(psiElement.getClass().getCanonicalName());
    }

    public static PsiMethod getPsiMethod(PsiElement psiElement) {
        Language language = psiElement.getLanguage();
        if (language instanceof JavaLanguage) {
            return (PsiMethod) psiElement;
        }
        if ("org.jetbrains.kotlin.idea.KotlinLanguage".equals(language.getClass().getCanonicalName())
                && "org.jetbrains.kotlin.psi.KtNamedFunction".equals(psiElement.getClass().getCanonicalName())) {
            List<PsiMethod> lightMethods = org.jetbrains.kotlin.asJava.LightClassUtilsKt.toLightMethods(psiElement);
            if (!lightMethods.isEmpty()) {
                return lightMethods.get(0);
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
}
