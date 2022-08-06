package io.github.newhoo.invoker;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.IncorrectOperationException;
import io.github.newhoo.invoker.setting.PluginProjectSetting;
import io.github.newhoo.invoker.util.AppUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.NotNull;

/**
 * PropertiesCreateAction
 *
 * @author huzunrong
 * @since 1.0.1
 */
public class InvokeMethodCreateIntention extends PsiElementBaseIntentionAction implements IntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiElement parent = element.getParent();
        if (editor != null && parent instanceof PsiMethod) {
            AppUtils.generateTest(project, (PsiMethod) parent, editor);
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        PsiElement parent = element.getParent();
        return editor != null
                && new PluginProjectSetting(project).isSpringApp()
                && parent instanceof PsiMethod
                && !((PsiMethod) parent).isConstructor()
                && !((PsiMethod) parent).getName().endsWith("TEST");
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
        return "Generate invocation method";
    }
}