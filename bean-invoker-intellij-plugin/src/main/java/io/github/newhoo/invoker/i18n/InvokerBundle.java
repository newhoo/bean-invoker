package io.github.newhoo.invoker.i18n;

import com.intellij.CommonBundle;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * i18n
 *
 * @author huzunrong
 * @date 2020/11/19 10:09 AM
 * @since 1.0.2
 */
public class InvokerBundle {

    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages.invoker", getLocale());

    private static Locale getLocale() {
        String lang = Locale.getDefault().getLanguage();
        if (lang.equals(Locale.ENGLISH.getLanguage()) || lang.equals(Locale.CHINESE.getLanguage())) {
            return Locale.getDefault();
        }
        return Locale.ENGLISH;
    }

    public static String getMessage(String key) {
        return resourceBundle.getString(key).trim();
    }

    public static String message(String key, Object... params) {
        return CommonBundle.message(resourceBundle, key, params).trim();
    }
}
