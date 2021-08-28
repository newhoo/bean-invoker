package io.github.newhoo.invoker;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.IncorrectOperationException;
import io.github.newhoo.invoker.setting.PluginProjectSetting;
import io.github.newhoo.invoker.util.AppUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.jvm.JvmClassKind.CLASS;

/**
 * PropertiesCreateAction
 *
 * @author huzunrong
 * @since 1.0.1
 */
public class InvokeMethodCreateIntention extends PsiElementBaseIntentionAction implements IntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiClassOwner psiClassOwner = (PsiClassOwner) element.getContainingFile();
        for (PsiClass psiClass : psiClassOwner.getClasses()) {
            for (PsiMethod psiMethod : psiClass.getMethods()) {
                if (editor != null && psiMethod.getReturnType() != null && psiMethod.getTextRange().containsOffset(editor.getCaretModel().getOffset())) {
                    AppUtils.generateTest(project, psiMethod, editor);
                }
            }
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (!new PluginProjectSetting(project).isSpringApp()) {
            return false;
        }
        if (element.getContainingFile().isWritable() && element.getContainingFile() instanceof PsiClassOwner) {
            PsiClassOwner psiClassOwner = (PsiClassOwner) element.getContainingFile();

            for (PsiClass psiClass : psiClassOwner.getClasses()) {
                if (psiClass.getClassKind() == CLASS) {
                    for (PsiMethod psiMethod : psiClass.getMethods()) {
                        if (editor != null
                                && psiMethod.getReturnType() != null
                                && psiMethod.getTextRange().containsOffset(editor.getCaretModel().getOffset())
                                && !psiMethod.getName().endsWith("TEST")) {
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
}