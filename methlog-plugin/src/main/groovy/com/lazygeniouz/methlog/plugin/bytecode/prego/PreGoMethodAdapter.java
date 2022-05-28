package com.lazygeniouz.methlog.plugin.bytecode.prego;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class PreGoMethodAdapter extends MethodVisitor implements Opcodes {

    private boolean needParameter;
    private final String methodName;
    private final PreGoClassAdapter.MethodCollector methodCollector;

    public PreGoMethodAdapter(
            String methodName, MethodVisitor mv, boolean needParameter,
            PreGoClassAdapter.MethodCollector methodCollector) {
        super(Opcodes.ASM7, mv);
        this.methodName = methodName;
        this.needParameter = needParameter;
        this.methodCollector = methodCollector;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationVisitor defaultAv = super.visitAnnotation(desc, visible);
        if (annotationClassDescriptor.equals(desc)) {
            needParameter = true;
        }
        return defaultAv;
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitEnd() {
        if (needParameter) methodCollector.onIncludeMethod(methodName);
        super.visitEnd();
    }

    private static final String annotationClassDescriptor = "Lcom/lazygeniouz/methlog/MethLog;";
}