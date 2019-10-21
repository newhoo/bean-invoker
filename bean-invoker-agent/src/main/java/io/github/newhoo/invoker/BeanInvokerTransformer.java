package io.github.newhoo.invoker;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * BeanInvokerTransformer
 *
 * @author huzunrong
 * @since 1.0
 */
public class BeanInvokerTransformer implements ClassFileTransformer {
    private static final String ABSTRACT_APPLICATION_CONTEXT = "org/springframework/context/support/AbstractApplicationContext";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!ABSTRACT_APPLICATION_CONTEXT.equals(className)) {
            return classfileBuffer;
        }

        System.out.println("Bean invoker agent starting...");

        CtClass cl = null;
        try {
            ClassPool pool = ClassPool.getDefault();
            cl = pool.makeClass(new ByteArrayInputStream(classfileBuffer));

            pool.importPackage("java.util");
            pool.importPackage("org.springframework.beans.factory.config");
            pool.importPackage("io.github.newhoo.invoker.server");

            CtMethod m = cl.getDeclaredMethod("finishRefresh");

            m.insertAfter(
                    "System.err.println(\"spring id: \" + this.getId());\n" +
                            "System.err.println(\"spring applicationName: \" + this.getApplicationName());\n" +
                            "System.err.println(\"spring context: \" + this);\n" +
                            "System.err.println(\"spring beanDefinitionNames: \" + this.getBeanDefinitionNames().length + \" \" + Arrays.asList(this.getBeanDefinitionNames()));" +
                            "\n" +
                            "ApplicationContextServer.startServer(this);"
            );

            return cl.toBytecode();
        } catch (Exception e) {
            System.err.println("Bean invoker agent error: " + e.toString());
            e.printStackTrace();
        } finally {
            if (cl != null) {
                cl.detach();// ClassPool默认不会回收，需要手动清理
            }
        }
        return classfileBuffer;
    }
}