package io.github.newhoo.invoker;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
//import jdk.internal.org.objectweb.asm.ClassReader;
//import jdk.internal.org.objectweb.asm.ClassVisitor;
//import jdk.internal.org.objectweb.asm.ClassWriter;
//import jdk.internal.org.objectweb.asm.MethodVisitor;
//import jdk.internal.org.objectweb.asm.Opcodes;
//import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * BeanInvokerTransformer
 *
 * @author huzunrong
 * @since 1.0.1
 */
public class BeanInvokerTransformer implements ClassFileTransformer {
    private static final String ABSTRACT_APPLICATION_CONTEXT = "org/springframework/context/support/AbstractApplicationContext";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!ABSTRACT_APPLICATION_CONTEXT.equals(className)) {
            return classfileBuffer;
        }

        System.out.println("Bean invoker agent starting...");

//        return asmVisit(classfileBuffer);
        return assistVisit(classfileBuffer);
    }

    private byte[] assistVisit(byte[] classfileBuffer) {
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
            System.err.println("Bean invoker agent inject error: " + e.toString());
            e.printStackTrace();
        } finally {
            if (cl != null) {
                cl.detach();// ClassPool默认不会回收，需要手动清理
            }
        }
        return classfileBuffer;
    }

    /*private byte[] asmVisit(byte[] classfileBuffer) {
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!"finishRefresh".equals(name)) {
                    return mv;
                }
                System.out.println("visit method: " + name + "  ===  " + descriptor + "  ===  " + signature + "  ===  " + exceptions);

                return new AdviceAdapter(Opcodes.ASM5, mv, access, name, descriptor) {

                    @Override
                    protected void onMethodExit(int opcode) {
                        super.onMethodExit(opcode);
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "io/github/newhoo/invoker/server/ApplicationContextServer", "startServer", "(Ljava/lang/Object;)V", false);
                    }
                };
            }
        };

        cr.accept(cv, 0);
        return cw.toByteArray();
    }*/
}