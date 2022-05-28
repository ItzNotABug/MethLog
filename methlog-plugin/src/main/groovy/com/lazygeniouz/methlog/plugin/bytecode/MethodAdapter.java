package com.lazygeniouz.methlog.plugin.bytecode;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

public final class MethodAdapter extends LocalVariablesSorter implements Opcodes {

    private final String className;
    private final String methodName;
    private int timingStartVarIndex;

    public MethodAdapter(String className, String name, int access, String desc, MethodVisitor mv) {
        super(ASM7, access, desc, mv);
        if (!className.endsWith("/")) {
            this.className = className.substring(className.lastIndexOf("/") + 1);
        } else {
            this.className = className;
        }
        this.methodName = name;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        timingStartVarIndex = newLocal(Type.LONG_TYPE);
        mv.visitMethodInsn(INVOKESTATIC, systemClass, timeInMillis, longDescriptor, false);
        mv.visitVarInsn(LSTORE, timingStartVarIndex);
    }

    @Override
    public void visitInsn(int opcode) {
        if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
            mv.visitMethodInsn(INVOKESTATIC, systemClass, timeInMillis, longDescriptor, false);
            mv.visitVarInsn(LLOAD, timingStartVarIndex);
            mv.visitInsn(LSUB);

            int index = newLocal(Type.LONG_TYPE);
            mv.visitVarInsn(LSTORE, index);
            mv.visitLdcInsn(className);
            mv.visitLdcInsn(methodName);
            mv.visitVarInsn(LLOAD, index);
            mv.visitMethodInsn(INVOKESTATIC, loggerClass, loggerMethodName, loggerMethodDescriptor, false);
        }
        super.visitInsn(opcode);
    }

    private static final String systemClass = "java/lang/System";
    private static final String timeInMillis = "currentTimeMillis";
    private static final String longDescriptor = "()J";

    // Logger
    private static final String loggerClass = "com/lazygeniouz/methlog/Logger";
    private static final String loggerMethodName = "logInfo";
    private static final String loggerMethodDescriptor = "(Ljava/lang/String;Ljava/lang/String;J)V";

}