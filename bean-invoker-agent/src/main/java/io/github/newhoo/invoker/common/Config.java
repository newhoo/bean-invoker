package io.github.newhoo.invoker.common;

import static io.github.newhoo.invoker.common.Constant.DEFAULT_INVOKE_PORT;
import static io.github.newhoo.invoker.common.Constant.PROPERTIES_KEY_INVOKE_PORT;

/**
 * Config
 *
 * @author huzunrong
 * @since 1.0.1
 */
public final class Config {

    public static int beanInvokePort;

    public static void init() {
        String beanInvokePortStr = System.getProperty(PROPERTIES_KEY_INVOKE_PORT);
        if (beanInvokePortStr == null || beanInvokePortStr.length() == 0) {
            beanInvokePortStr = String.valueOf(DEFAULT_INVOKE_PORT);
        }
        beanInvokePort = Integer.parseInt(beanInvokePortStr);
    }
}