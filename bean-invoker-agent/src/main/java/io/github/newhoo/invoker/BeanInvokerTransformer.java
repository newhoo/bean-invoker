package io.github.newhoo.invoker;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;

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

        return asmVisit(classfileBuffer);
    }

    private byte[] asmVisit(byte[] classfileBuffer) {
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
    }
}