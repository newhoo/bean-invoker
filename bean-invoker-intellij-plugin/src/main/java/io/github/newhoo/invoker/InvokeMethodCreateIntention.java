package io.github.newhoo.invoker;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.util.IncorrectOperationException;
import io.github.newhoo.invoker.setting.PluginProjectSetting;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.jvm.JvmClassKind.CLASS;

/**
 * PropertiesCreateAction
 *
 * @author huzunrong
 * @since 1.0
 */
public class InvokeMethodCreateIntention extends PsiElementBaseIntentionAction implements IntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiJavaFile psiJavaFile = (PsiJavaFile) element.getContainingFile();
        for (PsiClass psiClass : psiJavaFile.getClasses()) {
            for (PsiMethod psiMethod : psiClass.getMethods()) {
                if (editor != null && psiMethod.getTextRange().containsOffset(editor.getCaretModel().getOffset())) {
                    generateTest(project, psiMethod, editor);
                }
            }
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        PluginProjectSetting pluginProjectSetting = new PluginProjectSetting(project);
        if (!pluginProjectSetting.getEnableQuickInvoke()) {
            return false;
        }
        if (element.getContainingFile().isWritable() && element.getContainingFile() instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) element.getContainingFile();

            for (PsiClass psiClass : psiJavaFile.getClasses()) {
                if (psiClass.getClassKind() == CLASS) {
                    for (PsiMethod psiMethod : psiClass.getMethods()) {
                        if (editor != null
                                && psiMethod.getTextRange().containsOffset(editor.getCaretModel().getOffset())
                                && !psiMethod.getName().endsWith("$TEST")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Nls(capitalization = Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return "InvokeMethodCreateIntention";
    }

    @NotNull
    @Override
    public String getText() {
        return "Generate calling method";
    }

    private void generateTest(Project project, PsiMethod positionMethod, Editor editor) {
        Document document = editor.getDocument();
        int startLineNum = document.getLineNumber(positionMethod.getTextRange().getStartOffset());
        int lineStartOffset = document.getLineStartOffset(startLineNum);

        ApplicationManager.getApplication().runWriteAction(() -> {
            CommandProcessor.getInstance().executeCommand(project, () -> {
                document.insertString(lineStartOffset, getGenerateMethod(positionMethod));

                editor.getCaretModel().moveToOffset(document.getLineStartOffset(startLineNum + 2) + 8);
                editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
            }, "a", "b");
        });
    }

    private String getGenerateMethod(PsiMethod positionMethod) {
        StringBuilder sb = new StringBuilder();
        sb.append("    // TODO clean: ").append(LocalDateTime.now()).append("\n")
          .append("    public void $name$() {\n");

        List<String> parameterNameList = new ArrayList<>(positionMethod.getParameterList().getParametersCount());
        for (PsiParameter parameter : positionMethod.getParameterList().getParameters()) {
            parameterNameList.add(parameter.getName());
            sb.append("        ").append(parameter.getType().getPresentableText()).append(" ").append(parameter.getName()).append(" = new ")
              .append(parameter.getType().getPresentableText()).append("();\n");
        }

        sb.append("        ").append(positionMethod.getName()).append("(").append(String.join(", ", parameterNameList)).append(");\n")
          .append("    }\n\n");

        String generateName = positionMethod.getName() + "$TEST";
        return sb.toString().replace("$name$", generateName);
    }
}